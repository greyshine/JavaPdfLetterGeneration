package de.greyshine.pdflettercreation.web;

import de.greyshine.pdflettercreation.web.utils.ViewDo;
import de.greyshine.pdflettercreation.web.utils.ViewDo.Validate.Type;
import de.greyshine.pdflettercreation.web.utils.WebContext;
import de.greyshine.utils.Utils;

public class PdfFormView extends ViewDo {

	private static final long serialVersionUID = -1865198106886659944L;
	
	public String topimage;

	public String senderline1;
	public String senderline2;
	
	public String addressline1;
	public String addressline2;
	public String addressline3;
	public String addressline4;
	public String addressline5;
	public String addressline6;
	
	public String extraline1left;
	public String extraline1right;
	public String extraline2left;
	public String extraline2right;
	public String extraline3left;
	public String extraline3right;
	public String extraline4left;
	public String extraline4right;
	public String extraline5left;
	public String extraline5right;
	public String extraline6left;
	public String extraline6right;
	public String extraline7left;
	public String extraline7right;
	public String extraline8left;
	public String extraline8right;
	public String extraline9left;
	public String extraline9right;
	
	public String location;
	public String date;

	public String subjectline1;
	public String subjectline2;

	@Validate( type=Type.REQUIRED, message="Kein Brieftext" )
	public String lettertext;
	
	public String footerleft;
	public String footerright;
	
	@Override
	public void validate(String inValidationContext, WebContext inWebContext) {
		
		super.validate( inValidationContext, inWebContext );
		
		int theBlankAddressCount = Utils.isBlank( addressline1 ) ? 1 : 0;
		theBlankAddressCount += Utils.isBlank( addressline2 ) ? 1 : 0;
		theBlankAddressCount += Utils.isBlank( addressline3 ) ? 1 : 0;
		theBlankAddressCount += Utils.isBlank( addressline4 ) ? 1 : 0;
		theBlankAddressCount += Utils.isBlank( addressline5 ) ? 1 : 0;
		theBlankAddressCount += Utils.isBlank( addressline6 ) ? 1 : 0;
		
		if ( theBlankAddressCount > 4 ) {
			
			inWebContext.addFieldError( "addressline1" , "At least two address fields must be filled");
		}
	}	
}
