package de.greyshine.pdflettercreation.web;

import javax.servlet.http.HttpSessionActivationListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.springframework.beans.factory.annotation.Autowired;

import de.greyshine.pdflettercreation.LogService;

public class SessionListener implements HttpSessionListener, HttpSessionActivationListener {

	private static volatile long counts = 0;
	
	@Autowired	
	private LogService logService;
	
	
	
	@Override
	public void sessionCreated(HttpSessionEvent se) {
		logService.log( "session-count.log", ++counts );
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent se) {
		
	}

	@Override
	public void sessionWillPassivate(HttpSessionEvent se) {
	}

	@Override
	public void sessionDidActivate(HttpSessionEvent se) {
		
	}

}
