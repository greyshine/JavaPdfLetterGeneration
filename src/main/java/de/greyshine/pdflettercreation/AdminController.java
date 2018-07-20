package de.greyshine.pdflettercreation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import de.greyshine.pdflettercreation.service.UserService;

@Controller
public class AdminController {
	
	public static final String URI_buildPasswordHash = "/admin/passwordHash/{password}"; 
	
	@Autowired
	private UserService userService;
	
	@GetMapping( path= URI_buildPasswordHash, produces="text/plain")
	@ResponseBody
	// TODO: let only localhost call this URI
	public String buildPasswordHash(@PathVariable("password") String inWord ) {
		return userService.getPasswordHash( inWord );
	}

}
