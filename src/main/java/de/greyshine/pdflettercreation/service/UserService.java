package de.greyshine.pdflettercreation.service;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.greyshine.pdflettercreation.AdminController;
import de.greyshine.pdflettercreation.Application;
import de.greyshine.pdflettercreation.LogService;
import de.greyshine.pdflettercreation.StatusCode;
import de.greyshine.pdflettercreation.service.pdo.User;
import de.greyshine.pdflettercreation.web.utils.WebContext;
import de.greyshine.pdflettercreation.web.vdo.AddressVdo;
import de.greyshine.pdflettercreation.web.vdo.BillingAddressVdo;
import de.greyshine.pdflettercreation.web.vdo.CredentialsVdo;
import de.greyshine.utils.IEmailService.SendException;
import de.greyshine.utils.Utils;
import de.greyshine.utils.beta.JsonPersister;
import de.greyshine.utils.beta.businessinfos.Result;

@Service
public class UserService implements InitializingBean {

	private static final Log LOG = LogFactory.getLog( UserService.class );

	@Autowired
	private JsonPersister jp;
	
	@Autowired
	private Application application;
	
	@Autowired
	private LogService logService;
	
	@Autowired
	private EmailService emailService;
	
	// <email,userId>
	private static Map<String,String> emailUserIds = new HashMap<>();
	
	private final static AtomicBoolean IS_INITIALIZED_ADMIN_ACCOUNT = new AtomicBoolean(false);
	
	@Override
	public void afterPropertiesSet() throws Exception {
		
		synchronized ( IS_INITIALIZED_ADMIN_ACCOUNT ) {
			
			if ( !IS_INITIALIZED_ADMIN_ACCOUNT.get() ) {
				
				initAdminAccount();
				IS_INITIALIZED_ADMIN_ACCOUNT.set(true);
			}
			
			if ( isPassword("admin","admin") ) {
				LOG.fatal("Change the user admin password! Set it directly in the file. Generate it by calling web appplication URI "+ AdminController.URI_buildPasswordHash);
			}
		}
		
		if ( emailUserIds.isEmpty() ) {
			initLoginAccounts();
		}
	}
	
	private void initAdminAccount() throws IOException {
		
		final File theAdminUserDir = new File( application.getUsersDir(), "admin" );
		final File theAdminEmail = new File( theAdminUserDir, "email.txt" );
		if ( !Utils.isFile( theAdminEmail ) ) {
			Utils.writeFile(theAdminEmail , "admin", Utils.CHARSET_UTF8);
		}
		final File theAdminPassword = new File( theAdminUserDir, "password.txt" );
		if ( !Utils.isFile( theAdminPassword ) ) {
			Utils.writeFile(theAdminPassword , getPasswordHash( "admin" ) , Utils.CHARSET_UTF8);
		}
	}
	
	public File getEmailFile(String inUserId) {
		return new File( application.getUserDir(inUserId), "email.txt" );
	}
	public File getCofirmationcodeFile(String inUserId) {
		return new File( application.getUserDir(inUserId), "confirmationcode.txt" );
	}
	public File getBadLoginCountFile(String inUserId) {
		return new File( application.getUserDir(inUserId), "badlogincount.txt" );
	}

	public String getPasswordHash(String inPassword ) {
		
		if ( inPassword == null ) { return null; }
		
		return Utils.getSha256( Utils.getMd5( inPassword.trim()  ) ) ;
	}

	private void initLoginAccounts() {
		
		final Map<String,String> theLogins = new HashMap<>();
		
		synchronized ( emailUserIds ) {
			
			for( File aDir : Utils.listDirs( application.getUsersDir() ) ) {
				
				try {
					final String theEmail = Utils.readFileToString( new File( aDir, "email.txt" ) , Utils.CHARSET_UTF8);
					theLogins.put( theEmail, aDir.getName() );
				} catch (IOException e) {
					LOG.error( "failed to read login-accounts" );
				}
			}

			emailUserIds.clear();
			emailUserIds.putAll( theLogins );
		}
	}

	/**
	 * 
	 * @param inLogin
	 * @param inPassword
	 * @return userId on successs login
	 * @throws IOException 
	 */
	public Result<String> login(String inLogin, String inPassword) {
		
		final String theUserId = emailUserIds.get( inLogin ); 
		
		if ( theUserId == null ) { return new Result<>( StatusCode.LOGIN_ERR_UNKNOWN_USER ); }
		
		if ( !isPassword(inLogin, inPassword) ) {
			
			logService.log( "users/"+theUserId+"/events.log"  , "Login-FAILURE badPassword from "+ WebContext.get().getRemoteIp());
			
			try {
				increaseBadPasswordCount( theUserId );
			} catch (IOException e) {
				return new Result<>( StatusCode.TECHNICAL_ERR_IOEXCEPTION, e );
			}
			
			return new Result<>( StatusCode.LOGIN_ERR_BAD_PASSWORD );
		
		} else if ( isConfirmationCodeNeeded( inLogin ) ) {
			
			return new Result<>( StatusCode.LOGIN_ERR_NEEDS_CONFIRMATION );
		}
		
		try {
			setBadLoginCount(theUserId, 0);
		} catch (IOException e) {
			return new Result<>( StatusCode.TECHNICAL_ERR_IOEXCEPTION, e );
		}
		
		logService.log( "users/"+theUserId+"/events.log"  , "Login-SUCCESS from "+ WebContext.get().getRemoteIp());
		
		return new Result<>( StatusCode.LOGIN_OK,  theUserId );
	}

	private void increaseBadPasswordCount(String inUserId) throws IOException {
		
		int theCount = getBadLoginCount(inUserId);
		if ( theCount < 0 ) {
			theCount = 0;
		}
		theCount++;
		setBadLoginCount(inUserId, theCount);
	}

	public boolean isPassword(String inUserId, String inPassword) {
		
		inPassword = Utils.trimToNull( inPassword );
		
		try {
			
			return inPassword != null && getPasswordHash(inPassword).equals(getUserFileString( inUserId, "password.txt" ) );
			
		} catch (IOException e) {
			// 
			LOG.error( e );
		}
		
		return false;
	}

	private String getUserFileString(String inUserId, String inFileName) throws IOException {
		return Utils.readFileToString( getUserFile(inUserId, inFileName) , Utils.CHARSET_UTF8);
	}

	public File getUserFile(String inUserId, String inFileName) {
		return new File( application.getUserDir( inUserId ), inFileName );
	}
	
	
	public Result<String> createUser(CredentialsVdo inCredentialsVdo, AddressVdo inAddressVdo, BillingAddressVdo inBillingAddressVdo) {
		
		final User theUser = new User();
		theUser.id = UUID.randomUUID().toString();
		
		final File userFolder = application.getUserDir( theUser.id );
		
		if ( Utils.isDir( userFolder ) ) {
			return new Result<String>( StatusCode.USERCREATION_ERR_ALREADY_EXISTS, theUser.id);
		}
		
		mapCredentialsVdoToUser(inCredentialsVdo, theUser);
		mapAddressVdoToUser( inAddressVdo, theUser );
		mapBillingAddressVdoToUser( inBillingAddressVdo, theUser );
		
		final String theConfirmationCode = Utils.getSha256( theUser.id ).substring(0,15);
		
		final File theEmailFile = new File( userFolder, "email.txt" );
		final File thePasswordFile = new File( userFolder, "password.txt" );
		final File theConfirmationCodeFile = new File( userFolder, "confirmationcode.txt" );
		final File theUserFile = new File( userFolder, "user.json" );
		
		try {

			Utils.writeFile( theEmailFile, inCredentialsVdo.email );
			Utils.writeFile( thePasswordFile, getPasswordHash( inCredentialsVdo.password ) );
			Utils.writeFile( theConfirmationCodeFile, theConfirmationCode );
			Utils.writeFile( theUserFile, jp.getJsonString( theUser ) );

			emailUserIds.put( inCredentialsVdo.email, theUser.id );
			
			LOG.info( "created user: "+ theUser.id +" ("+ inCredentialsVdo.email +")" );
			
		} catch (IOException e) {
			
			return new Result<>( StatusCode.TECHNICAL_ERR_IOEXCEPTION, e );
		}
		
		emailService.sendNewUserFirstEmail( inCredentialsVdo.email, theConfirmationCode );
		
		try {
			logService.logNewUser( jp.getJsonString( theUser ) );
		} catch (SendException e) {
			LOG.error( e );
		}
		
		return new Result<String>( StatusCode.USERCREATION_OK, theUser.id );
	}

	private boolean isUserEmail(String inEmail) {
		return emailUserIds.containsKey( inEmail );
	}

	private void mapCredentialsVdoToUser(CredentialsVdo inCredentialsVdo, User inUser) {

		inUser.firstname = null;
		inUser.lastname = null;
		
		if ( Utils.isNotBlank( inCredentialsVdo.lastFirstname ) ) {
			
			final int theCommaIdx = inCredentialsVdo.lastFirstname.indexOf( ',' );
			inUser.firstname = Utils.trimToNull( inCredentialsVdo.lastFirstname.substring( theCommaIdx+1 ) );
			inUser.lastname = Utils.trimToNull( inCredentialsVdo.lastFirstname.substring( 0, theCommaIdx ) );
		}
		
	}
	
	private void mapAddressVdoToUser(AddressVdo inAddressVdo, User inUser) {
		
		inUser.company = Utils.trimToNull( inAddressVdo.company );
		inUser.streethno = Utils.trimToNull( inAddressVdo.streethno );
		inUser.zip = Utils.trimToNull( inAddressVdo.zip );
		inUser.city = Utils.trimToNull( inAddressVdo.city );
		inUser.country = Utils.trimToNull( inAddressVdo.country );

	}
	
	private void mapBillingAddressVdoToUser(BillingAddressVdo inBillingAddressVdo, User inUser) {
		
		inUser.billingCompany = Utils.trimToNull( inBillingAddressVdo.company );
		inUser.billingName = Utils.trimToNull( inBillingAddressVdo.name );
		inUser.billingZip = Utils.trimToNull( inBillingAddressVdo.zip );
		inUser.billingCity = Utils.trimToNull( inBillingAddressVdo.city );
		inUser.billingCountry = Utils.trimToNull( inBillingAddressVdo.country );
	
	}

	public String getEmail(String inUserId) {
		
		final File theEmailFile = new File( application.getUserDir( inUserId ), "email.txt" );
		
		try {
			
			return Utils.readToString( theEmailFile , Utils.CHARSET_UTF8);
		} 
		
		catch (IOException e) {
			throw Utils.toRuntimeException( e );
		}
	}

	public File getConfirmationCodeFile(String inUserId) {
		
		final File theCcf = new File( application.getUserDir( inUserId ), "confirmationcode.txt" );
		
		return Utils.isFile( theCcf ) ? theCcf : null;
	}
	
	public String getConfirmationCode(String inUserId) {
		
		final File theEmailFile = getConfirmationCodeFile(inUserId);
		
		try {
			return !Utils.isFile( theEmailFile ) ? null : Utils.readToString( theEmailFile , Utils.CHARSET_UTF8);
		} 
		
		catch (IOException e) {
			throw Utils.toRuntimeException( e );
		}
	}
	
	public boolean applyConfirmationCode(String inUserId, String inConfirmationCode) throws IOException {
		
		final String theCc = getConfirmationCode(inUserId);
		
		if ( theCc == null ) { return false; }
		else if ( !theCc.equals( inConfirmationCode ) ) { return false; }
		
		final File theCcFile = getConfirmationCodeFile( inUserId );
		
		final boolean isDeleted = Utils.delete( theCcFile );
		
		if ( !isDeleted ) {
			throw new IOException("confirmation code file not deleted.");
		}
		
		return true;
	}
	
	public boolean isConfirmationCodeNeeded(String inEmail) {
		
		String theUserId = emailUserIds.get( inEmail );
		
		if ( theUserId == null ) { return false; }
		
		return getConfirmationCode( inEmail ) != null;
	}

	public String getUserIdForEmail(String inEmail, boolean inReInitialize) {
		
		String theUserId = emailUserIds.get( inEmail );
		
		if ( Utils.isNotBlank( theUserId ) ) {
			
			return theUserId;
		}
		
		if ( inReInitialize ) {
			
			initLoginAccounts();
		
		} else {
		
			return null;
		}
		
		theUserId = emailUserIds.get( inEmail );
		
		return Utils.defaultIfBlank(theUserId, null);
	} 
	
	public int getBadLoginCount(String inUserId) throws IOException {
		final File theFile = getBadLoginCountFile(inUserId);
		return Utils.readFileToInteger( theFile, -1 );
	}

	public void setBadLoginCount(String inUserId, int inCount) throws IOException {
		
		final File theFile = getBadLoginCountFile(inUserId);
		
		if ( inCount < 1 ) {
			
			Utils.delete( theFile );
			
			if ( theFile.exists() ) { throw new IOException( "badlogincounts must not exist anymore: "+ theFile ); }
			
			return;
		
		} else {
		
			Utils.writeFile(theFile, String.valueOf( inCount ) );
			
			if ( Utils.isNoFile( theFile ) ) { throw new IOException( "badlogincounts must not exist anymore: "+ theFile ); }if ( theFile.exists() ) { throw new IOException( "badlogincounts must exist with value="+ inCount +": "+ theFile ); }
		}
		
	}
}
