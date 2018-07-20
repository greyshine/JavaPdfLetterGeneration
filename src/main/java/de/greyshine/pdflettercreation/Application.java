package de.greyshine.pdflettercreation;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import de.greyshine.pdflettercreation.web.SessionListener;
import de.greyshine.pdflettercreation.web.XyzInterceptor;
import de.greyshine.pdflettercreation.web.utils.ViewDo;
import de.greyshine.pdflettercreation.web.utils.WebContext;
import de.greyshine.utils.IEmailService;
import de.greyshine.utils.SmtpEmailService;
import de.greyshine.utils.Utils;
import de.greyshine.utils.beta.JsonPersister;

@SpringBootApplication
public class Application extends WebMvcConfigurerAdapter implements InitializingBean {

	static final Log LOG = LogFactory.getLog( Application.class );
	
	@Autowired
	private ApplicationArguments applicationArguments;
	
	private File basepath;

	private final List<Locale> allowedLocales = Arrays.asList( new Locale("de"), new Locale("en") );  
			
	@Bean
	public JsonPersister getJsonPersister() {
		
		final JsonPersister theJp = new JsonPersister();
		
		return theJp;
	}
	
	@Bean
	public TemplateEngine templateResolver() {
		return new TemplateEngine();
	}
	
	@Bean
	public XyzInterceptor getXyzInterceptor() {
		
		return new XyzInterceptor();
	}
	
	@Bean
	public IEmailService getEmailService() throws IOException {
		
		final Properties theProperties = Utils.loadProperties( new File( basepath, "email.properties" ) );
		
		final SmtpEmailService theEs = new SmtpEmailService( theProperties );
		
		return theEs;
	}
	
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		super.addResourceHandlers(registry);
	}

	/**
	 * ! This method must have the name <tt>localeResolver</tt>
	 * @return 
	 */
	@Bean
	public LocaleResolver localeResolver() {
		
		return new LocaleResolver() {

			@Override
			public Locale resolveLocale(HttpServletRequest request) {
				return WebContext.get().getLocale();
			}

			@Override
			public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
				System.out.println( "AHA" );
				System.exit(1);
			}};
	}

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
		argumentResolvers.add( WebContext.HANDLERMETHODARGUMENTRESOLVER );
		argumentResolvers.add( new ViewDo.SpringMvcViewDoResolver() );
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor( new WebContext.WebContextHandlerInterceptor( this ) );
		registry.addInterceptor( getXyzInterceptor() );
	}
	
	@Bean
	public SessionListener createSessionListener(){
		return new SessionListener();
	}
	
	public File getBasepath() {
		return basepath;
	}
	
	public File getDirLogs() {
		
		final File l = new File(getBasepath(), "logs" );
		
		Utils.mkParentDirs( l );
		
		return l;
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		
		final String[] args = applicationArguments.getSourceArgs(); 
		
		String basedir = null;
		
		for( String anArg : args ) {
			if ( anArg.toLowerCase().startsWith( "-basedir=" ) ) {
				basedir = anArg.substring( "-basedir=".length());
			}
		}
		
		basedir = Utils.trimToDefault( basedir , Utils.getCanonicalFile( new File(".") ).getAbsolutePath() );
		
		basepath = Utils.getCanonicalFile( new File( basedir ) );
		basepath.mkdirs();
		
		LOG.info( "basepath: "+ basepath.getAbsolutePath() );
	}

	public File getUsersDir() {
		return new File( getBasepath(), "users" );
	}

	public File getUserDir(String inUser) {
		inUser = Utils.defaultIfBlank( inUser , Constants.USER_NAME_ANONYMOUS);
		return new File( getUsersDir(), inUser );
	}

	public static void startSpringBootApplication(String[] args) {
		SpringApplication.run( Application.class, args )  ;
	}

	public Locale getLocale(Locale inPreferredLocale) {
		
		if ( inPreferredLocale != null ) { 
		
			for( Locale aLocale : allowedLocales ) {
				
				if ( aLocale.getLanguage().equalsIgnoreCase( inPreferredLocale.getLanguage() ) ) {
					return aLocale;
				}
			}
		}
		
		return allowedLocales.get(0);
	}
}
