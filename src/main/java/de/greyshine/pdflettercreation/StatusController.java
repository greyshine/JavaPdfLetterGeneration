package de.greyshine.pdflettercreation;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class StatusController {

	@GetMapping(value="/status",produces="text/plain")
	@ResponseBody
	public String status(HttpServletRequest inReq) {
		
		final StringBuilder s = new StringBuilder();
		
		s//
		.append( "URL: "+ inReq.getRequestURL() ).append( '\n' )//
		.append( "URI: "+ inReq.getRequestURI() ).append( '\n' )//
		.append( inReq.getProtocol() ).append( '\n' )
		.append( "RequestedSessionId: "+ inReq.getRequestedSessionId() ).append( '\n' )
		.append( "ContextPath: "+ inReq.getContextPath() ).append( '\n' )//
		.append( "PathTranslated: "+ inReq.getPathTranslated() ).append( '\n' )
		.append( "RemoteHost: "+ inReq.getRemoteHost()).append( '\n' )//
		.append( "RemoteAddress: "+ inReq.getRemoteAddr()+":"+ inReq.getRemotePort()).append( '\n' )//
		.append( "\n\n" );
		
		final Enumeration<String> theHeaderNames = inReq.getHeaderNames();
		
		while( theHeaderNames.hasMoreElements() ) {
			
			final String theHeaderName = theHeaderNames.nextElement();
			final Enumeration<String> theHeaderValues = inReq.getHeaders( theHeaderName );
			
			while( theHeaderValues.hasMoreElements() ) {
				s.append( theHeaderName ).append(" = ").append( theHeaderValues.nextElement() ).append('\n');
			}
		}
		
		return s.toString();
	}
	
}
