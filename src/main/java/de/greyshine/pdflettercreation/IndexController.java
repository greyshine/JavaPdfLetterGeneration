package de.greyshine.pdflettercreation;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import de.greyshine.pdflettercreation.service.UserService;
import de.greyshine.pdflettercreation.web.utils.ViewDo.SkipValidation;
import de.greyshine.pdflettercreation.web.utils.WebContext;
import de.greyshine.pdflettercreation.web.vdo.CredentialsVdo;
import de.greyshine.utils.Utils;
import de.greyshine.utils.beta.businessinfos.Result;

@Controller
public class IndexController {
	
	@Autowired
	private LogService logService;

	@Autowired
	private UserService userService;

	@GetMapping( value={"/index.html","/"} )
	public String index(HttpServletRequest inReq, HttpSession inSession) {
		
		logService.log(inReq);
		
		inReq.setAttribute( Page.REQUEST_ATT , new Page("main") );
		
		return "index.html";
	}
	
	@GetMapping("/pdfForm")
	public String index(HttpServletRequest inReq) {

		inReq.setAttribute( "pageInclude" , "pdf-form" );
		return "index.html";
	}
	
	@GetMapping("/logout")
	public String logout(HttpServletRequest inReq, HttpSession inSession) {
		
		inSession.invalidate();
		
		WebContext.get().addMessage("You have been logged out.");
		
		inReq.setAttribute( "pageInclude" , "" );
		
		return "index.html";
	}
	
	@PostMapping("/login")
	public String login(HttpServletRequest inReq, HttpSession inSession, @SkipValidation CredentialsVdo inCredentials ) {
		
		inSession.invalidate();
		
		if ( userService.isConfirmationCodeNeeded( inCredentials.email ) ) {
			
			WebContext.get().addError( "enter confirmation code before proceeding." );
			inReq.setAttribute( Page.REQUEST_ATT , Page.CONFIRMATIONCODE );
			return "index.html";
		}
		
		final Result<String> theUserId = userService.login( inCredentials.email, inCredentials.password );
		
		if ( theUserId.getStatusCode() == StatusCode.LOGIN_OK ) {
		
			inReq.getSession(true).setAttribute( Constants.SESSION_ATT_user_id , Utils.trimToNull( theUserId.getObject() ) );
			
			WebContext.get().addMessage( "You have been successfully logged in.");
			inReq.setAttribute( Page.REQUEST_ATT , Page.DASHBOARD_FWD );
			
		} else {
		
			WebContext.get().addWarning( "bad login or password" );
			inReq.setAttribute( Page.REQUEST_ATT , Page.LOGIN );
			
			return "index.html";
				
		}
		
		return "index.html";
	}
	
	@GetMapping( value={"/page/{name}"} )
	public String showPage(HttpServletRequest inReq, @PathVariable("name") String inPage) {

		inReq.setAttribute( Page.REQUEST_ATT, new Page(inPage));
		return "index.html";
	}
	
	@PutMapping( value="/ajax/cookiedisclaimer", produces="text/plain" )
	@ResponseBody()
	public String checkCookiedisclaimer( HttpSession inSession ) {
		inSession.setAttribute("cookiedisclaimer", Boolean.TRUE);
		return "OK";
	}

	@GetMapping( value={"/dashboard"} )
	public String showDashboard(HttpServletRequest inReq) {
		
		inReq.setAttribute( Page.REQUEST_ATT, Page.DASHBOARD);
		
		return "index.html";
	}
}