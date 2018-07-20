package de.greyshine.pdflettercreation.web.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import de.greyshine.pdflettercreation.Application;
import de.greyshine.pdflettercreation.Constants;
import de.greyshine.utils.deprecated.Utils;

public class WebContext {
	
	static final Log LOG = LogFactory.getLog( WebContext.class );

	public static final String REQUEST_ATTRIBUTE = WebContext.class.getSimpleName();

	private static final ThreadLocal<WebContext> TL_CONTEXT = new ThreadLocal<>();

	public HttpServletRequest request;
	public HttpServletResponse response;
	
	public static final HandlerMethodArgumentResolver HANDLERMETHODARGUMENTRESOLVER = new HandlerMethodArgumentResolver() {

		@Override
		public boolean supportsParameter(MethodParameter parameter) {
			return parameter.getParameterType() == WebContext.class;
		}

		@Override
		public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
				NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
			return webRequest.getNativeRequest( HttpServletRequest.class ).getAttribute( REQUEST_ATTRIBUTE );
		}
	};
	
	private Locale locale;

	private final List<String> globalMessages = new ArrayList<>(1);
	private final List<String> globalWarnings = new ArrayList<>(1);
	private final List<String> globalErrors = new ArrayList<>(1);

	private Map<String, List<String>> fieldMessages = new HashMap<>(1);
	private Map<String, List<String>> fieldWarnings = new HashMap<>(1);
	private Map<String, List<String>> fieldErrors = new HashMap<>(1);

	private Map<String, String> keyedMessages = new HashMap<>(1);
	private Map<String, String> keyedWarnings = new HashMap<>(1);
	private Map<String, String> keyedErrors = new HashMap<>(1);

	
	public static WebContext get() {
		return TL_CONTEXT.get();
	}
	
	private WebContext(){}
	
	public boolean isGlobalErrors() {
		return !globalErrors.isEmpty();
	}
	public boolean isFieldErrors() {
		return !fieldErrors.isEmpty();
	}
	public boolean isKeyedError() {
		return !keyedErrors.isEmpty();
	}
	public boolean isAnyError() {
		return isGlobalErrors() || isFieldErrors() || isKeyedError();
	}

	public void addMessage(String inMessage) {
		if (Utils.isNotBlank(inMessage)) {
			globalMessages.add(inMessage);
		}
	}

	public void addWarning(String inWarning) {
		if (Utils.isNotBlank(inWarning)) {
			globalWarnings.add(inWarning);
		}
	}

	public void addError(String inError) {
		if (Utils.isNotBlank(inError)) {
			globalErrors.add(inError);
		}
	}

	public void addKeyedError(String inKey, String inError) {
		if (Utils.isNotBlank(inError)) {
			globalErrors.add(inError);
		}
	}
	
	public String getKeyedError(String inKey) {
		return keyedErrors.get( inKey );
	}
	
	public boolean isFieldMessage(String inField) {
		try {
			return !fieldMessages.get( inField ).isEmpty();
		} catch (Exception e) {
			// swallow
			return false;
		}
	}
	public boolean isFieldWarning(String inField) {
		try {
			return !fieldWarnings.get( inField ).isEmpty();
		} catch (Exception e) {
			// swallow
			return false;
		}
	}
	public boolean isFieldError(String inField) {
		try {
			return !fieldErrors.get( inField ).isEmpty();
		} catch (Exception e) {
			// swallow
			return false;
		}
	}
	
	public List<String> getFieldErrors(String inField) {
		return Utils.defaultIfNull(fieldErrors.get( inField ), Collections.emptyList() );
	}
	public List<String> getFieldWarnings(String inField) {
		return Utils.defaultIfNull(fieldWarnings.get( inField ), Collections.emptyList() );
	}
	public List<String> getFieldMessages(String inField) {
		return Utils.defaultIfNull(fieldMessages.get( inField ), Collections.emptyList() );
	}
	
	public String getFieldError(String inField) {
		try {
			return fieldErrors.get( inField ).get(0);
		} catch (Exception e) {
			// swallow
			return null;
		}
	}
	
	public List<String> getGlobalErrors() {
		return globalErrors;
	}
	
	public List<String> getGlobalWarnings() {
		return globalWarnings;
	}
	
	public List<String> getGlobalMessages() {
		return globalMessages;
	}

	public void addFieldMessage(String inField, String inMessage) {

		List<String> l = fieldMessages.get( inField );
		
		if ( l == null ) {
			fieldMessages.put( inField , l=new ArrayList<>());
		}
		
		l.add( inMessage );
	}

	public void addFieldWarning(String inField, String inWarning) {

		List<String> l = fieldWarnings.get( inField );
		
		if ( l == null ) {
			fieldWarnings.put( inField , l=new ArrayList<>());
		}
		
		l.add( inWarning );
	}

	public void addFieldError(String inField, String inError) {
		
		List<String> l = fieldErrors.get( inField );
		
		if ( l == null ) {
			fieldErrors.put( inField , l=new ArrayList<>());
		}
		
		l.add( inError );
	}

	public String getRemoteIp() {
		
		if ( request == null ) { return null; }
		
		String the = request.getHeader( Constants.HEADER_x_forwarded_for );
		
		return Utils.defaultIfBlank( the , request.getRemoteAddr());
	}
	
	public Locale getLocale() {
		return locale;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() +" [anyError="+ isAnyError() +", globalErrors.size="+ globalErrors.size() +", fieldErrors="+ fieldErrors.keySet() +", keyedErrors="+ keyedErrors.keySet() +"]";
	}
	
	public static class WebContextHandlerInterceptor extends HandlerInterceptorAdapter {
		
		private Application application;
		
		public WebContextHandlerInterceptor(Application inApplication) {
			application = inApplication;
		}

		@Override
		public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
				throws Exception {
			
			final WebContext theContext = new WebContext();
			theContext.request = request;
			theContext.response = response;
			theContext.locale = evaluateLocale( request );
			response.setLocale( theContext.locale );

			TL_CONTEXT.set( theContext );
			
			request.setAttribute( REQUEST_ATTRIBUTE , theContext);
			
			return true;
		}

		private Locale evaluateLocale(HttpServletRequest inHsr) {
			
			Locale theLocale = null;
			
			final HttpSession theHs = inHsr.getSession(false);
			
			if ( theHs != null ) {
			
				theLocale = Utils.castSafe(Locale.class, theHs.getAttribute( Constants.SESSION_ATT_locale ) );
			} 
			
			if ( theHs != null && theLocale == null ) {
				
				theLocale = application.getLocale( inHsr.getLocale() );
				theHs.setAttribute( Constants.SESSION_ATT_locale , theLocale );
			}
			
			return theLocale != null ? theLocale : application.getLocale( inHsr.getLocale() );
		}

		@Override
		public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
				Exception ex) throws Exception {
			TL_CONTEXT.remove();
		}
	}

}
