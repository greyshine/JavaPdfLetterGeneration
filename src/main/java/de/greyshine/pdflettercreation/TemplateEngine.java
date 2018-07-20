package de.greyshine.pdflettercreation;

import java.net.URL;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.cache.AlwaysValidCacheEntryValidity;
import org.thymeleaf.cache.ICacheEntryValidity;
import org.thymeleaf.cache.NonCacheableCacheEntryValidity;
import org.thymeleaf.extras.java8time.dialect.Java8TimeDialect;
import org.thymeleaf.extras.springsecurity4.dialect.SpringSecurityDialect;
import org.thymeleaf.spring4.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ITemplateResolver;
import org.thymeleaf.templateresolver.TemplateResolution;
import org.thymeleaf.templateresource.ITemplateResource;
import org.thymeleaf.templateresource.UrlTemplateResource;

public class TemplateEngine extends SpringTemplateEngine {

	static final Log LOG = LogFactory.getLog( TemplateEngine.class );
	
	public final String template_path_thymeleaf = "/public/WEB-INF/templates-thymeleaf/"; 
	private boolean cacheThymeleafTemplates = false;
	
	public TemplateEngine() {
		
		setTemplateResolver( new TemplateResolver() );
	    addDialect(new SpringSecurityDialect());
	    addDialect(new Java8TimeDialect());
	}
	
	private TemplateMode evalTemplateMode(String template) {
		
		if ( template == null ) { return TemplateMode.TEXT; }
		
		template = template.toLowerCase();
		
		if ( template.endsWith( ".html" ) ) {
			return TemplateMode.HTML; 
		}
		if ( template.endsWith( ".htm" ) ) {
			return TemplateMode.HTML; 
		}
		if ( template.endsWith( ".xml" ) ) {
			return TemplateMode.XML; 
		}
		if ( template.endsWith( ".xhtml" ) ) {
			return TemplateMode.XML; 
		}
		if ( template.endsWith( ".css" ) ) {
			return TemplateMode.CSS; 
		}
		if ( template.endsWith( ".js" ) ) {
			return TemplateMode.JAVASCRIPT; 
		}
		
		return TemplateMode.TEXT;
	}

	
	private class TemplateResolver implements ITemplateResolver {

		@Override
		public String getName() {
			return getClass().getName();
		}

		@Override
		public Integer getOrder() {
			return 0;
		}

		@Override
		public TemplateResolution resolveTemplate(IEngineConfiguration configuration, String ownerTemplate,
				String template, Map<String, Object> templateResolutionAttributes) {
			
			String theTemplateResourceUrl = template_path_thymeleaf + template; 
    		
    		URL theUrl = getClass().getResource( theTemplateResourceUrl );
			theUrl = theUrl != null ? theUrl : Thread.currentThread().getContextClassLoader().getResource( theTemplateResourceUrl );
			
			if ( theUrl == null ) {
			
				LOG.fatal( "url could not be resolved:  "+ template +" ("+theTemplateResourceUrl+")");
				
				return null;
			}
			
			final ITemplateResource theTemplateResource = new UrlTemplateResource( theUrl , "UTF-8");
			
			final TemplateMode theTemplateMode = evalTemplateMode(template);
			
			final ICacheEntryValidity theCacheEntryValidity = cacheThymeleafTemplates ? AlwaysValidCacheEntryValidity.INSTANCE : NonCacheableCacheEntryValidity.INSTANCE;
			
			LOG.debug( theUrl );
			
			return new TemplateResolution( theTemplateResource, theTemplateMode, theCacheEntryValidity );
		}

	}
	
}
