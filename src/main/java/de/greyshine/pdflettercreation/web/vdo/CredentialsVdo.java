package de.greyshine.pdflettercreation.web.vdo;

import de.greyshine.pdflettercreation.web.utils.ViewDo;

public class CredentialsVdo extends ViewDo {

	private static final long serialVersionUID = 8253617181957554531L;

	@Validate( context="register", message = "no first and lastname", type = Validate.Type.PATTERN, value="\\s*\\w+([\\-\\s]\\w+)*\\s*,\\s*\\w+([\\-\\s]\\w+)*\\s*"  )
	@FormFieldName("name")
	public String lastFirstname;
	
	public String email;
	
	public String password;
	
	@Validate( context="register", message = "passwords do not match", type = Validate.Type.EQUAL_TO_OTHER_FIELD, value="password"  )
	public String passwordRepeat;
	
	
}
