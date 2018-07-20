package de.greyshine.pdflettercreation.web.vdo;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import de.greyshine.pdflettercreation.web.utils.ViewDo.CustomValidator;
import de.greyshine.pdflettercreation.web.utils.WebContext;

public class PasswordValidator implements CustomValidator<CredentialsVdo> {

	static final Set<String> secialChars = Collections.unmodifiableSet( new HashSet<>( Arrays.asList( "-","_",".",":",",","+","*", "!", "\"", "§", "$", "%", "&", "/", "\\", "(", ")" ) ) );
	
	@Override
	public boolean validate(String inField, String inValue, CredentialsVdo viewDo, WebContext inContext) {
		
		return isValid( inValue );
	}

	public static boolean isValid(String inValue) {
		
		if ( inValue == null || inValue.trim().isEmpty() ) { return false; }
		
		final int len1 = inValue.length();
		
		if ( len1 < 6 ) { return false; }
		
		inValue = inValue.trim();
		if ( len1 != inValue.length() ) { return false; }
		
		int count_AZ = 0;
		int count_az = 0;
		int count_09 = 0;
		boolean containsSpecial = false;
		
		for( char c : inValue.toCharArray() ){
			
			String theChar = String.valueOf(c);
			
			if ( theChar.matches( "[A-Z]" ) ) {
			
				count_AZ++;
			
			} else if ( theChar.matches( "[a-z]" ) ) {
				
				count_az++;
				
			} else if ( theChar.matches( "[0-9]" ) ) {
				
				count_09++;
				
			} else if ( secialChars.contains( theChar ) ) {
				
				containsSpecial = true;
			
			} else {
				
				return false;
			}
			
		}
		
		
		return containsSpecial && count_AZ > 0 && count_az > 0 && count_09 > 0 ;
	}

}
