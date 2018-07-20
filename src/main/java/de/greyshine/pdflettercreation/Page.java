package de.greyshine.pdflettercreation;

import de.greyshine.utils.Utils;

public final class Page {

	public final static String REQUEST_ATT = "page";
	public final String id;
	public final String include;
	public final boolean isFoward;
	
	public static final Page MAIN = new Page("main");
	public static final Page LOGIN = new Page("login");
	public static final Page USER = new Page("user");
	public static final Page PDF = new Page("pdf");
	public static final Page REGISTER = new Page("register");
	public static final Page CONFIRMATIONCODE = new Page("confirmationcode");
	public static final Page DASHBOARD_FWD = new Page( "dashboard", true ); 
	public static final Page DASHBOARD = new Page( "dashboard" ); 
	
	public Page( String inName ) {
		this( inName, inName, false );
	}
	
	public Page( String inName, boolean isForward ) {
		this(inName, inName, isForward);
	}
	
	public Page( String id, String include ) {
		this(id, include, false);
	}
	
	public Page( String id, String include, boolean isForward ) {
		this.id = "div-"+Utils.trimToDefault(id, "404");
		this.include = isForward ? include : "pages/div."+Utils.trimToDefault(include,"404")+".html";
		isFoward = isForward;
	}

	public String getId() {
		return id;
	}

	public String getInclude() {
		return include;
	}

	@Override
	public String toString() {
		return "Page [id=" + id + ", include=" + include + "]";
	}

}
