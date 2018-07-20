package de.greyshine.pdflettercreation.service.pdo;

import java.time.LocalDateTime;

public class User {
	
	public String id;

	public LocalDateTime created = LocalDateTime.now();
	
	public String firstname;
	public String lastname;
	
	public String company;
	public String streethno;
	public String zip;
	public String city;
	public String country;
	
	public String billingName;
	public String billingCompany;
	public String billingStreetHno;
	public String billingZip;
	public String billingCity;
	public String billingCountry;
	
}
