package de.greyshine.pdflettercreation.web.utils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import de.greyshine.utils.deprecated.Utils;

public abstract class WebUtils {

	private WebUtils() {}
	
	public static <T> T getSessionValue(HttpServletRequest inReq, String inName, T inDefault, boolean inSetDefaultValue) {
		
		return getSessionValue( inReq.getSession( inSetDefaultValue ), inName, inDefault, inSetDefaultValue ); 
	}

	@SuppressWarnings("unchecked")
	public static <T> T getSessionValue(HttpSession inSession, String inName, T inDefault, boolean inSetDefaultValue) {
		
		if ( inSession == null ) { return inDefault; }
		
		T theValue = null;
		
		try {
			
			theValue = (T) inSession.getAttribute( inName );
			
		} catch (ClassCastException e) {
			// swallow
		}
		
		if ( theValue == null ) {
			theValue = inDefault;
		}
		
		if ( theValue == null && inSetDefaultValue  && inDefault != null ) {
			
			inSession.setAttribute( inName , inDefault);
		}
		
		return Utils.defaultIfNull(theValue, inDefault);
	}
	
}
