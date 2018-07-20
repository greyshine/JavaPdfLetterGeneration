package de.greyshine.pdflettercreation.web.vdo;

import de.greyshine.pdflettercreation.web.utils.ViewDo;
import de.greyshine.pdflettercreation.web.utils.ViewDo.Validate.Type;

public class AddressVdo extends ViewDo {

	private static final long serialVersionUID = 6632492731878419450L;

	
	public String company;
	public String streethno;
	@Validate( type=Validate.Type.PATTERN, value="[0-9]{5}", required=true, message = "Invalide Postleitzahl" )
	public String zip;
	@Validate( type=Validate.Type.PATTERN, value="[^\\s]+", required=true, message = "Feld ist unbefüllt." )
	public String city;
	@Validate( type = Type.PATTERN, value="[^\\s]+" )
	public String country;
	
}
