package de.greyshine.pdflettercreation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import de.greyshine.pdflettercreation.service.EmailService;
import de.greyshine.pdflettercreation.web.utils.WebContext;
import de.greyshine.utils.IEmailService.SendException;
import de.greyshine.utils.ToString;
import de.greyshine.utils.Utils;

@Service
public class LogService {
	
	static {
		Utils.registerToString( JsonElement.class , ToString.TOSTRINGER_JSON);
	}
	
	final static Log LOG = LogFactory.getLog( LogFactory.class );
	
	private final static AtomicLong IDS = new AtomicLong(0);
	
	@Autowired
	private Application application;
	
	@Autowired
	private EmailService emailService;
	
	final static Map<String,OutputStream> logStreams = new HashMap<>();
	
	static final Object SYNC_LOGS = new Object();
	
	public void log(HttpServletRequest inReq) {
		log( "access.log", toJson( inReq ) );
	}
	
	public void log(String inFile, Object inMsg) {
		
		inFile = Utils.defaultIfBlank( inFile , "log.log");
		
		synchronized ( SYNC_LOGS ) {
			
			OutputStream logStream = logStreams.get( inFile );
			
			if ( logStream == null ) {
				
				try {
					
					final File logFile = new File( application.getDirLogs(), inFile );
					
					Utils.mkParentDirs( logFile );
					
					logStream = new FileOutputStream( logFile , true );
			
					logStreams.put( inFile , logStream);
					
				} catch (Exception e) {
					System.err.println( "Failed to create log output: "+ application.getDirLogs() );
				}
			}
			
			final String theLogText = "["+Utils.formatDate("dd.MM.yyyy HH:mm:ss.SSS", LocalDateTime.now()) +"]["+ Thread.currentThread().getName() +"] "+Utils.toString( inMsg );

			try {
				
				logStream.write( theLogText.getBytes( Utils.CHARSET_UTF8 ) );
			
			} catch (Exception e) {
				logStreams.put(inFile, null);
				Utils.close( logStream );
				System.out.println( theLogText );
			}
		}
	}

	private JsonObject toJson(HttpServletRequest inReq) {
		
		final JsonObject theJo = new JsonObject();
		
		theJo.addProperty( "uri" , inReq.getRequestURI() );
		theJo.addProperty( "host" , inReq.getRemoteHost() );
		theJo.addProperty( "locale" , String.valueOf( inReq.getLocale() ) );
		
		final JsonObject theHeaders = new JsonObject();
		theJo.add( "headers" , theHeaders);
		for( String aHeader : Utils.toIterable( inReq.getHeaderNames() ) ) {
			
			final JsonArray theValues = new JsonArray();
			theHeaders.add( aHeader , theValues);
			
			theValues.add( inReq.getHeader( aHeader ) );
		}

		final JsonObject theParameters = new JsonObject();
		theJo.add( "parameters" , theParameters);
		
		for( String aParameter : Utils.toIterable( inReq.getParameterNames() ) ) {
			
			final JsonArray theValues = new JsonArray();
			theParameters.add( aParameter , theValues);
			
			for( String aValue : Utils.defaultIfNull(inReq.getParameterValues( aParameter ), Utils.EMPTY_STRINGS ) ) {
				theValues.add( aValue );
			}
		}
		
		return theJo;
	}

	public void logPdfCreation(String inUser, byte[] inPdfBytes) {
		
		final String fileName = Utils.formatDate( "yyyyMMdd_HHmmss" , LocalDateTime.now())+"-"+ WebContext.get().getRemoteIp() +"-"+ IDS.incrementAndGet() +".pdf";
		
		try {
			
			Utils.write( new File( application.getUserDir( inUser ), fileName ), inPdfBytes);
		
		} catch (IOException e) {
			
			LOG.error( e );
		}
	}

	public void logNewUser( String inUserJson ) throws SendException {
	
		emailService.sendEmailPlaintext( "kuemmel.dss@gmx.de", "info: new user", inUserJson );
	}
}
