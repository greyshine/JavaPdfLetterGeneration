package de.greyshine.pdflettercreation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ThymeleafController {

	@GetMapping("/thymeleaf-example")
	public String helloworld(HttpServletRequest inReq) {
		
		inReq.setAttribute( "attribute1" , "John Doe" );
		inReq.setAttribute( "attribute2" , new Something("Hello World") );
		
		final List<Object> theList = new ArrayList<>(); 
		theList.add( "One" );
		theList.add( new Something( "Two" ) );
		inReq.setAttribute( "list" , theList );
		
		final Map<String,Object> theMap = new HashMap<>();
		theMap.put( "one" , "One");
		theMap.put( "two" , new Something( "Two" ));
		inReq.setAttribute( "map" , theMap );
		
		return "index.html";
	}
	
	class Something {
		
		String value;
		
		Something(String value) {
			this.value = value;
		}
		
		public String getValue() {
			return value;
		};
		
		public String toString() {
			return value;
		}
		
	}
	
	
	
}
