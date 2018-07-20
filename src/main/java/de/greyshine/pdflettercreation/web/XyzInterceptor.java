package de.greyshine.pdflettercreation.web;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import de.greyshine.pdflettercreation.Constants;
import de.greyshine.pdflettercreation.IndexController;
import de.greyshine.pdflettercreation.LogService;
import de.greyshine.pdflettercreation.Page;
import de.greyshine.utils.Utils;

public class XyzInterceptor extends HandlerInterceptorAdapter {

	private static final Log LOG = LogFactory.getLog( XyzInterceptor.class );

	public enum Target {
		NOOP
	}
	
	@Retention( RetentionPolicy.RUNTIME )
	@java.lang.annotation.Target( ElementType.METHOD ) 
	public @interface TriggerInterceptor {
		Target value();
	}

	@Autowired
	private LogService logService;
	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		
		request.setAttribute( Page.REQUEST_ATT , Page.MAIN);
		
		logService.log(request);
		
		handleCookieDisclaimer( request );
		preparePdfFormView(request);
		
		if ( handler instanceof HandlerMethod ) {
			
			Thread.currentThread().setName( request.getRequestURI()+" > "+ ((HandlerMethod)handler).getMethod().getDeclaringClass().getTypeName() +"."+((HandlerMethod)handler).getMethod().getName() );
			
			try {

				switch ( ((HandlerMethod)handler).getMethodAnnotation( TriggerInterceptor.class ).value() ) {
				
				case NOOP:
				default:
					break;
				}
				
			} catch (Exception e) {
				// swallow
			}
			
		} else {
			
			Thread.currentThread().setName( request.getRequestURI() );
		}
		
		
		return true;
	}

	
	
	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
		
		super.postHandle(request, response, handler, modelAndView);
		
		final Page thePage = Utils.cast( request.getAttribute( Page.REQUEST_ATT ) );
		
		if ( thePage != null && thePage.isFoward ) {
			
			//response.setStatus(HttpServletResponse.SC_OK);
//			response.setHeader("Location", "http://google.de");
			response.sendRedirect( request.getContextPath()+ "/"+thePage.include );
		}
	}



	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
		
	}



	private void preparePdfFormView(HttpServletRequest inReq) {
		
		try {
			
			PdfFormView thePdfFormView = (PdfFormView) inReq.getSession().getAttribute( Constants.REQUEST_ATT_pdfform_data_dev1 );
			thePdfFormView = thePdfFormView != null ? thePdfFormView : new PdfFormView();
			inReq.setAttribute( Constants.REQUEST_ATT_pdfform_data_dev1 , thePdfFormView);
			
		} catch (Exception e) {
			// swallow
		}
	}

	private void handleCookieDisclaimer(HttpServletRequest inReq) {
		
		try {
			inReq.setAttribute( "cookiedisclaimer" , inReq.getSession().getAttribute( "cookiedisclaimer" ));
		} catch (Exception e) {
			
			LOG.debug( e );
			// swallow
		}
	}
	
	
}
