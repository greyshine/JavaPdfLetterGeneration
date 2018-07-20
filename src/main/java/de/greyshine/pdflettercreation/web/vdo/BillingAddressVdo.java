package de.greyshine.pdflettercreation.web.vdo;

import de.greyshine.pdflettercreation.web.utils.ViewDo;
import de.greyshine.pdflettercreation.web.utils.ViewDo.Validate.Type;

public class BillingAddressVdo extends ViewDo {

	private static final long serialVersionUID = 6632492731878419450L;

	@FormFieldName("billingName")
	@Validate( context="register", required=false, message = "no first and lastname (billing)", type = Validate.Type.PATTERN, value="\\s*\\w+([\\-\\s]\\w+)*\\s*,\\s*\\w+([\\-\\s]\\w+)*\\s*"  )
	public String name;
	
	@FormFieldName("billingCompany")
	public String company;
	
	@FormFieldName("billingStreethno")
	public String streethno;
	
	@FormFieldName("billingZip")
	@Validate( type=Validate.Type.PATTERN, value="[0-9]{5}", required=false, message = "Invalide Postleitzahl" )
	public String zip;
	
	@FormFieldName("billingCity")
	@Validate( type=Validate.Type.PATTERN, value="[^\\s]+", required=false, message = "Feld ist unbefüllt." )
	public String city;
	
	@FormFieldName("billingCountry")
	@Validate( type = Type.PATTERN, value="[^\\s]+" )
	public String country;
	
}
