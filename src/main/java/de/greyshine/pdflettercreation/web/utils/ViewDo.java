package de.greyshine.pdflettercreation.web.utils;

import java.io.Serializable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import de.greyshine.utils.ReflectionUtils;
import de.greyshine.utils.deprecated.Utils;

/**
 * DataObject working as the backend part of a html/presentation page/form
 */
public abstract class ViewDo implements Serializable {

	private static final long serialVersionUID = -2553757903122988675L;
	
	private final static Log LOG = LogFactory.getLog( ViewDo.class );
	
	public static final String VALIDATIONCONTEXT_DEFAULT = "default";

	// <class,<html-field-name,field>
	private static final Map<Class<?>, Map<String,Field>> VIEWDOFIELDS = new HashMap<>();
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface Validate {

		enum Type {
			PATTERN, MIN, MIN_EQUAL, MAX, MAX_EQUAL, CUSTOM, ENUM, REQUIRED, EQUAL_TO_OTHER_FIELD;
		}

		Type type();

		String value() default "";

		String message() default "";

		boolean required() default false;
		
		String[] context() default {};
	}

	/**
	 * 
	 * Mapping information of which html form field name is mapped to the java object attribute
	 *
	 */
	@Retention( RetentionPolicy.RUNTIME )
	@Target( ElementType.FIELD )
	public @interface FormFieldName {
		/**
		 * @return name-attribute of the field in the html form
		 */
		String value();
	}
	
	/**
	 * Context which is passed as a named validation context in the validation method.<br/>
	 * It will be applied by the Comtroller method's where it is declared with the parameter. 
	 */
	@Retention( RetentionPolicy.RUNTIME )
	@Target( ElementType.PARAMETER )
	public @interface ValidationContext {
		String value() default VALIDATIONCONTEXT_DEFAULT;
	}
	
	/**
	 * 
	 * Skip the validation and use as plain DO
	 *
	 */
	@Retention( RetentionPolicy.RUNTIME )
	@Target( ElementType.PARAMETER )
	public @interface SkipValidation {}
	

	public interface CustomValidator<T extends ViewDo> {
		boolean validate(String inField, String inValue, T viewDo, WebContext inContext);
	}

	/**
	 * Automatic method parameter injection of {@link ViewDo}s.<br/>
	 * http://geekabyte.blogspot.de/2014/08/how-to-inject-objects-into-spring-mvc.html
	 */
	public static class SpringMvcViewDoResolver implements HandlerMethodArgumentResolver {

		final Log LOG = LogFactory.getLog(SpringMvcViewDoResolver.class);

		@Override
		public boolean supportsParameter(MethodParameter parameter) {

			return ViewDo.class.isAssignableFrom(parameter.getParameterType());
		}

		@Override
		public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
				NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {

			final ViewDo theView = (ViewDo) parameter.getParameterType().newInstance();
			final HttpServletRequest theHsr = webRequest.getNativeRequest(HttpServletRequest.class);

			theView.map( theHsr.getParameterMap() );
			
			final ValidationContext theValidationContextAnnotation = parameter.getParameterAnnotation( ValidationContext.class );
			final SkipValidation theSkipValidationAnnontation = parameter.getParameterAnnotation( SkipValidation.class );
			final String theValidationContext = theValidationContextAnnotation == null || Utils.isBlank( theValidationContextAnnotation.value() ) ? VALIDATIONCONTEXT_DEFAULT : theValidationContextAnnotation.value().trim();
			
			if ( theSkipValidationAnnontation == null ) {
				
				theView.validate( theValidationContext, WebContext.get() );
				LOG.debug( "validated  ViewDo of "+ theView.getClass().getTypeName() +": "+ theView +"; context="+ theValidationContext +", isValid="+ theView.isValid );
				
			} else {
			
				LOG.debug( "skipping validation of "+ theView );
			}

			return theView;
		}
	}
	
	/**
	 * 
	 */
	private Boolean isValid = null;
	
	public boolean isValidated() {
		return isValid != null;
	}
	
	public Boolean getValidationResult() {
		return isValid;
	}
	
	/**
	 * 
	 * @param inParameters
	 */
	private void map(Map<String,String[]> inParameters ){
		
		for( Entry<String,Field> aFieldEntry : getMapableFields().entrySet() ) {
			
			final String[] theValues = inParameters.get( aFieldEntry.getKey() );
			
			if ( theValues == null ) { continue; }

			
			final Field theField = aFieldEntry.getValue();
			
			try {
				
			
			if ( theField.getType() == String.class && theValues.length > 0 ) {
				
				ReflectionUtils.setFieldValue(theField, this, Utils.trimToEmpty( theValues[0] ));
			
			} else if ( theField.getType() == String[].class ) {
				
				Utils.trimAllToEmpty( theValues );
				ReflectionUtils.setFieldValue(theField, this, theValues);
				
			}
			
			} catch (Exception e) {
				
				LOG.debug("failed to map " + aFieldEntry.getKey() + " to " + getClass().getTypeName() +"."+ aFieldEntry.getValue().getName(), e);
			}
		}
	}
	
	private Map<String,Field> getMapableFields() {
		
		Map<String,Field> theFieldsMap = VIEWDOFIELDS.get( getClass() );
		
		if ( theFieldsMap != null ) { return theFieldsMap; }
		
		theFieldsMap = new HashMap<>();
		
		for( Field aField : getClass().getDeclaredFields() ){
			
			final boolean isPublic = ReflectionUtils.isPublic( aField );
			final boolean isTransient = ReflectionUtils.isTransient( aField );
			final boolean isString = String.class == aField.getType() || String[].class == aField.getType(); 
			
			if ( !isPublic || isTransient || !isString ) { continue; }
			
			aField.setAccessible(true);
			
			final FormFieldName theFormFieldName = aField.getDeclaredAnnotation( FormFieldName.class );
			final String theHtmlFormFieldName = theFormFieldName == null || theFormFieldName.value().trim().isEmpty() ? aField.getName() : theFormFieldName.value().trim();

			if ( theFieldsMap.containsKey( theHtmlFormFieldName ) ) {
				LOG.fatal( "double field naming for html field name: "+ theHtmlFormFieldName +", field1="+ theFieldsMap.get( theHtmlFormFieldName ) +", field2="+ aField );
			} else {
				theFieldsMap.put( theHtmlFormFieldName , aField);
			}
		}
		
		VIEWDOFIELDS.put( getClass() , theFieldsMap);
		return theFieldsMap;
		
	}

	protected void validate(String inValidationContext, WebContext inRequestContext) {

		inValidationContext = Utils.defaultIfBlank(inValidationContext, VALIDATIONCONTEXT_DEFAULT);

		final List<Field> theFields = ReflectionUtils.getFields(getClass(), String.class, false, false, Validate.class);

		boolean isValid = true;
		
		try {

			Fields:
			for (Field aField : theFields) {

				Validators:
				for( Validate aValidator: aField.getAnnotationsByType( Validate.class ) ) {
		
					// check if validation context matches
					// it will always match if length is 0; no special context given, so it defaults to validate always
					if ( aValidator.context().length > 0 ) {
					
						boolean doValidate = false;
						
						for (String aValidContext : aValidator.context()) {
							
							if ( inValidationContext.equals( aValidContext ) ) {
								
								doValidate = true;
								break;
							}
						}
						
						if ( doValidate == false ) {
							continue Validators;
						}
					}
					
					
					final String theValue = Utils.trimToEmpty( ReflectionUtils.getFieldValue(aField, this) );
					
					// when blank and not required continue
					if ( !aValidator.required() && Validate.Type.REQUIRED != aValidator.type() && Utils.isBlank( theValue ) ) {
						continue;
					}
					
					String theMessage = "invalid";
					
					switch (aValidator.type()) {
					case PATTERN:
						
						final String thePattern =  aValidator.value();
						
						if ( Utils.isNoMatch(theValue, thePattern ) ) {
							isValid = false;
							theMessage = Utils.trim(aValidator.message(), "invalid pattern ("+ aValidator.value() +")");
							WebContext.get().addFieldError(aField.getName(), theMessage);
							LOG.debug( "fieldError [viewDo="+this+", field="+ aField.getName() +", message="+ theMessage +", validator="+ aValidator.type() +"]" );
						}
						break;
						
					case REQUIRED:
						if ( Utils.isBlank( theValue ) ) {
							isValid = false;
							theMessage = Utils.trim(aValidator.message(), "field is blank");
							WebContext.get().addFieldError(aField.getName(), theMessage);
							LOG.debug( "fieldError [viewDo="+this+", field="+ aField.getName() +", message="+ theMessage +", validator="+ aValidator.type() +"]" );
						}
						break;
					case MIN:
						//CHECK if value is comparable
						if ( true ) { throw new UnsupportedOperationException("TODO: implement"); }
						LOG.debug( "fieldError [viewDo="+this+", field="+ aField.getName() +", message="+ theMessage +", validator="+ aValidator.type() +"]" );
						break;
					case MAX:
						//CHECK if value is comparable
						if ( true ) { throw new UnsupportedOperationException("TODO: implement"); }
						LOG.debug( "fieldError [viewDo="+this+", field="+ aField.getName() +", message="+ theMessage +", validator="+ aValidator.type() +"]" );
						break;
					case MIN_EQUAL:
						//CHECK if value is comparable
						if ( true ) { throw new UnsupportedOperationException("TODO: implement"); }
						LOG.debug( "fieldError [viewDo="+this+", field="+ aField.getName() +", message="+ theMessage +", validator="+ aValidator.type() +"]" );
						break;
					case MAX_EQUAL:
						//CHECK if value is comparable
						if ( true ) { throw new UnsupportedOperationException("TODO: implement"); }
						LOG.debug( "fieldError [viewDo="+this+", field="+ aField.getName() +", message="+ theMessage +", validator="+ aValidator.type() +"]" );
						break;

					case EQUAL_TO_OTHER_FIELD:
						
						String theOtherValue = ReflectionUtils.getFieldValueSafe( aValidator.value(), this );
						
						if ( theOtherValue == null || !theOtherValue.equals( theValue ) ) {
							
							isValid = false;
							theMessage = Utils.trim(aValidator.message(), "field must be equal to '"+ aValidator.value() +"'");
							WebContext.get().addFieldError(aField.getName(), theMessage);
							LOG.debug( "fieldError [viewDo="+this+", field="+ aField.getName() +", message="+ theMessage +", validator="+ aValidator.type() +"]" );
						}
						
						break;
						
					case CUSTOM:
						
						@SuppressWarnings("rawtypes")
						CustomValidator c = ReflectionUtils.newInstance( aValidator.value() , false);
						
						if ( !c.validate(aField.getName(), theValue, this, WebContext.get()) ) {
							
							isValid = false;
							theMessage = Utils.trim(aValidator.message(), theMessage);
							WebContext.get().addFieldError(aField.getName(), theMessage);
							LOG.debug( "fieldError [viewDo="+this+", field="+ aField.getName() +", message="+ theMessage +", validator="+ aValidator.type() +"]" );
						}
						
					default:
						break;
					}
				}
			}
			
		} catch (Exception e) {

			isValid = false;
			throw Utils.toRuntimeException(e);
		}
		
		this.isValid = isValid;
	}
	
	@Override
	public String toString() {
		
		return getClass().getTypeName()+" [isValid="+ isValid +"]";
	}
}
