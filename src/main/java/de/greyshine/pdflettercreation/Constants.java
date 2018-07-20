package de.greyshine.pdflettercreation;

import java.util.Locale;

public abstract class Constants {

	public static final String PDFIMAGE_LEFTBORDER = "data:image/*;base64,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";
	public static final String USER_NAME_ANONYMOUS = "anonymous";

	public static final String HEADER_x_forwarded_for = "x-forwarded-for";
	
	public static final String REQUEST_ATT_pdfform_data_dev1 = "pdfform_data_dev1";
	
	public static final String SESSION_ATT_user_id = "userId";
	public static final String SESSION_ATT_locale = Locale.class.getSimpleName();
	
}