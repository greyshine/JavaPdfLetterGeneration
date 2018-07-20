package de.greyshine.pdflettercreation.service;

import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.greyshine.utils.Email;
import de.greyshine.utils.IEmailService;
import de.greyshine.utils.IEmailService.SendException;
import de.greyshine.utils.ShowdownTransformer;
import de.greyshine.utils.SmtpEmailService;
import de.greyshine.utils.Utils;
import de.greyshine.utils.Wrapper;

@Service
public class EmailService {
	
	private static final Kvp[] EMPTY_KVPS = new Kvp[0];
	
	@Autowired
	private IEmailService emailService;
	
	public void sendEmailPlaintext(String inReceipient, String inSubject, String inPlainText) throws SendException {
		emailService.sendHtml(inReceipient, inSubject, null, inPlainText);
	}
	
	public void sendEmail( Email inEmail ) throws SendException {
		emailService.send(inEmail);
	}

	public void sendNewUserFirstEmail(String inEmail, String inConfirmationCode) {
		
		System.out.println( "send new user welcome: "+ inEmail );
		
	}
	
	public void sendEmailTemplated( String inEmail, String inTemplateName, Locale inLocale, Kvp... inKvps ) throws SendException {
		
		final Wrapper<String> theHtmlEmailTemplate = new Wrapper<>();  
		final Wrapper<String> theTextEmailTemplate = new Wrapper<>(); 
		
		theHtmlEmailTemplate.value = Utils.getResourceAsStringSafe( "/email-templates/"+ inTemplateName +"."+ inLocale.getLanguage().toLowerCase() +".md", Utils.CHARSET_UTF8, "" );
		theTextEmailTemplate.value = Utils.getResourceAsStringSafe( "/email-templates/"+ inTemplateName +"."+ inLocale.getLanguage().toLowerCase() +".txt", Utils.CHARSET_UTF8, "" );
		
		if ( theHtmlEmailTemplate.isBlankString() && theTextEmailTemplate.isBlankString() ) {
			throw new SendException("no template found: "+ inTemplateName);
		}
		
		theHtmlEmailTemplate.value = Utils.trimToEmpty( theHtmlEmailTemplate.value );
		theTextEmailTemplate.value = Utils.trimToEmpty( theTextEmailTemplate.value );
		
		String theSubject = null;
		int idxNewLine = theHtmlEmailTemplate.value.indexOf( '\n' );
		
		if ( idxNewLine > -1 ) {
			
			theSubject = theHtmlEmailTemplate.value.substring(0, idxNewLine).trim();
			theHtmlEmailTemplate.value = theHtmlEmailTemplate.value.substring( idxNewLine ).trim();
		
		} else if ( (idxNewLine=theTextEmailTemplate.value.indexOf( '\n' )) > -1 ) {
		
			theSubject = theTextEmailTemplate.value.substring(0, idxNewLine).trim();
			theTextEmailTemplate.value = theTextEmailTemplate.value.substring( idxNewLine ).trim();
		}
		
		theSubject = Utils.trimToEmpty( theSubject );
		
		Arrays.stream(inKvps == null ? EMPTY_KVPS : inKvps)//
			.filter( (inKvp)->{ return inKvp != null && inKvp.key != null; } )//
			.forEach( (inKvp)->{
			
				if (inKvp.value == null) {
					inKvp.value = "";
				}
				
				theHtmlEmailTemplate.value = theHtmlEmailTemplate.value.replace( "${"+ inKvp.key +"}" , inKvp.value);
				theTextEmailTemplate.value = theTextEmailTemplate.value.replace( "${"+ inKvp.key +"}" , inKvp.value);
			}
		);
		
		theHtmlEmailTemplate.value = ShowdownTransformer.toHtml( theHtmlEmailTemplate.value );
		
		sendEmail( new Email( inEmail ).charset( Utils.CHARSET_UTF8 ).subject( theSubject ).html( theHtmlEmailTemplate.value ).text( theTextEmailTemplate.value ) );
	}

	public static Kvp kvp(String inKey, String inValue) {
		return new Kvp(inKey, inValue);
	}
	
	public static class Kvp {
		
		String key, value;

		public Kvp(String key, String value) {
			
			this.key = key;
			this.value = value;
		}
	}
	
	public static void main(String[] args) throws IOException, SendException {
		
		EmailService es = new EmailService();
		es.emailService = new SmtpEmailService().init( Utils.loadProperties( "src/test/application-basedir-template/email.properties" ) );
		
		es.sendEmailTemplated("test@greyshine.de", "welcome-new-user", Locale.GERMANY, kvp("link-technical","http://localhost:8080/confirmationcode?email=kuemmel.dss@gmx.de&cc=xxx-yyy"), kvp("link-visible","www.pdferie.com/confirmationcode"), kvp("confirmationcode","XX-YY"), kvp("name", "Dirk Schumacher"));
	}
	
}
