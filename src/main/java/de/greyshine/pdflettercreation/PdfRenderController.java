package de.greyshine.pdflettercreation;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

import de.greyshine.pdflettercreation.rendering.GermanV1RenderUtils;
import de.greyshine.pdflettercreation.rendering.RenderUtils.RenderResultDO;
import de.greyshine.pdflettercreation.web.PdfFormView;
import de.greyshine.pdflettercreation.web.utils.WebContext;
import de.greyshine.pdflettercreation.web.utils.WebUtils;
import de.greyshine.utils.Utils;

@Controller
public class PdfRenderController {
	
	@Autowired
	private LogService logService;

	@PostMapping( value={"/pdfForm"}, produces="text/html" )
	public String indexPost(HttpServletRequest inReq, PdfFormView inPdfFormView, WebContext inContext) throws Exception {
		
		if ( inContext.isAnyError() ) {
			
			inReq.setAttribute( Page.REQUEST_ATT, Page.MAIN );
			
			return "index.html";
		}
		
		RenderResultDO thePdfResult = GermanV1RenderUtils.getInstance().createPdf(inReq, inPdfFormView);
		
		inReq.getSession().setAttribute( Constants.REQUEST_ATT_pdfform_data_dev1, inPdfFormView);
		
		final String theUser = WebUtils.getSessionValue(inReq, Constants.SESSION_ATT_user_id, Constants.USER_NAME_ANONYMOUS, true );
		
		logService.logPdfCreation( theUser, thePdfResult.pdfBytes );
		
		final String base64Pdf= "data:application/pdf;base64,"+Utils.toBase64( thePdfResult.pdfBytes );
		
		inReq.setAttribute( "pdfdata" , base64Pdf );
		
		inReq.setAttribute( "navbarPdfformBack" , true );
		inReq.setAttribute( Page.REQUEST_ATT, Page.PDF );
		
		return "index.html";
	}

}
