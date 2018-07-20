package de.greyshine.pdflettercreation.web;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import de.greyshine.pdflettercreation.Page;
import de.greyshine.pdflettercreation.service.UserService;
import de.greyshine.pdflettercreation.web.utils.ViewDo;
import de.greyshine.pdflettercreation.web.utils.ViewDo.Validate.Type;
import de.greyshine.pdflettercreation.web.utils.ViewDo.ValidationContext;
import de.greyshine.pdflettercreation.web.utils.WebContext;
import de.greyshine.pdflettercreation.web.vdo.AddressVdo;
import de.greyshine.pdflettercreation.web.vdo.BillingAddressVdo;
import de.greyshine.pdflettercreation.web.vdo.CredentialsVdo;
import de.greyshine.utils.Utils;

@Controller
public class RegisterController {

	@Autowired
	private UserService userService;

	@GetMapping( "/register" )
	public String show(HttpServletRequest inReq) {
		inReq.setAttribute( Page.REQUEST_ATT, Page.REGISTER);
		return "index.html";
	}
	
	@PostMapping( "register" )
	public String register(HttpServletRequest inReq, WebContext inWc, @ValidationContext("register") CredentialsVdo inCredentialsVdo, AddressVdo inAddressVdo, BillingAddressVdo inBillingAddressVdo) {
		
		if ( inWc.isAnyError() ) {
			
			inReq.setAttribute( Page.REQUEST_ATT , Page.REGISTER);
			return "index.html";
		}
		
		final String userId = userService.createUser( inCredentialsVdo, inAddressVdo, inBillingAddressVdo ).getObject();
		
		final String theEmail = userService.getEmail( userId );
		final String theCc = userService.getConfirmationCode(userId);
		
		WebContext.get().addMessage( "registered new user. You have received email with a conformation code. Follow the link or enter the conformation code." );
		
		return showConfirmationCode(inReq, theEmail, "");
	} 
	
	@GetMapping( value={"/confirmationcode"} )
	public String showConfirmationCode(HttpServletRequest inReq, @RequestParam( name="email", required=false ) String inEmail, @RequestParam( name="cc", required=false ) String inConfirmationCode) {
		
		inEmail  = Utils.trimToEmpty( inEmail );
		inConfirmationCode  = Utils.trimToEmpty( inConfirmationCode );
		
		inReq.setAttribute( "email" , inEmail);
		inReq.setAttribute( "cc" , inConfirmationCode);
		inReq.setAttribute( Page.REQUEST_ATT, Page.CONFIRMATIONCODE);
		
		return "index.html";
	}

	@PostMapping( value={"/confirmationcode"} )
	public String confirmationCode(HttpServletRequest inReq, ConfirmationCodeVdo inCcVdo) throws IOException {
		
		final String theUserId = userService.getUserIdForEmail( inCcVdo.email, true );
		
		userService.applyConfirmationCode(theUserId, inCcVdo.confirmationCode);
		
		return "index.html";
	}
	
	public static class ConfirmationCodeVdo extends ViewDo {
		
		private static final long serialVersionUID = 4802288687579492772L;

		@Validate(type = Type.REQUIRED)
		String email;
		
		@FormFieldName( "cc" )
		@Validate(type = Type.REQUIRED)
		String confirmationCode;
	}
	
}
