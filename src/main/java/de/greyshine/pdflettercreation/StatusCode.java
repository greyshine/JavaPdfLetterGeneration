package de.greyshine.pdflettercreation;

import java.util.function.Function;

import de.greyshine.utils.beta.businessinfos.IStatusCode;
import de.greyshine.utils.beta.businessinfos.Result;

public enum StatusCode implements IStatusCode {
	
	LOGIN_OK(0),
	LOGIN_ERR_UNKNOWN_USER(2010),
	LOGIN_ERR_TOO_MANY_BAD_LOGINS(2020),
	LOGIN_ERR_NEEDS_CONFIRMATION(2030),
	LOGIN_ERR_ACCOUNT_LOCKED(2040),
	LOGIN_ERR_BAD_PASSWORD(2050),

	USERCREATION_OK(0),
	USERCREATION_ERR_ALREADY_EXISTS( 4010 ),
	
	TECHNICAL_ERR_UNKNWON( 5000 ),
	TECHNICAL_ERR_IOEXCEPTION( 5010 ),
	
	;
	public final int num;
	
	
	private StatusCode(int num) {
		this.num = num;
	}
	
	public int num() {
		return num;
	}

	@Override
	public boolean isError() {
		return name().toUpperCase().contains( "_ERR_" );
	}
	
	public static <S,T> Result<T> executeSafe( S inValue, Function<S,Result<T>> inFunction ) {
		
		try {
			
			return inFunction.apply( inValue );
			
		} catch (Exception e) {
			
			return new Result<>( StatusCode.TECHNICAL_ERR_UNKNWON, e );
		}
	}
}
