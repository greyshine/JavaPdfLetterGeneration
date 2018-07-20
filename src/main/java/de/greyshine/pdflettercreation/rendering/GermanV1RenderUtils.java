package de.greyshine.pdflettercreation.rendering;

import javax.servlet.http.HttpServletRequest;

import de.greyshine.pdflettercreation.Constants;
import de.greyshine.pdflettercreation.web.PdfFormView;
import de.greyshine.utils.Utils;
import de.greyshine.utils.pdfletter.GermanLetterVersion1;
import de.greyshine.utils.pdfletter.GermanLetterVersion1.Data;
import de.greyshine.utils.pdfletter.PdfLetterRenderer;

public final class GermanV1RenderUtils extends RenderUtils {

	private static final GermanV1RenderUtils INSTANCE = new GermanV1RenderUtils();

	private static final PdfLetterRenderer<Data> pdfLetterRenderer = GermanLetterVersion1.newPdfLetterRenderer();
	
	private GermanV1RenderUtils(){}
	
	public RenderResultDO createPdf(HttpServletRequest inReq, PdfFormView inPdfFormView) throws Exception {
		
		final Data theData = createPdfDataFromViewDo(inPdfFormView);
		inReq.setAttribute( Constants.REQUEST_ATT_pdfform_data_dev1, inPdfFormView);
		
		final RenderResultDO renderResult = new RenderResultDO();
		renderResult.dataObject = theData;
		
		theData.meta.title = "Generated PDF";
		theData.escapeHtml = true;
		theData.leftBorderImageSrc = Constants.PDFIMAGE_LEFTBORDER;

		renderResult.pdfBytes = pdfLetterRenderer.renderAsBytes( theData );
		
		renderResult.durance = System.currentTimeMillis()-renderResult.time;
		
		return renderResult;
	}

	public Data createPdfDataFromViewDo(PdfFormView inPdfFormView) {
		
		final Data theData = pdfLetterRenderer.newDataObject();
		
		theData.leftBorderImageSrc = inPdfFormView.topimage;
		theData.senderline1 = inPdfFormView.senderline1;
		theData.senderline2 = inPdfFormView.senderline2;
		theData.addressline1 = inPdfFormView.addressline1;
		theData.addressline2 = inPdfFormView.addressline2;
		theData.addressline3 = inPdfFormView.addressline3;
		theData.addressline4 = inPdfFormView.addressline4;
		theData.addressline5 = inPdfFormView.addressline5;
		theData.addressline6 = inPdfFormView.addressline6;
		theData.subjectline1 = inPdfFormView.subjectline1;
		theData.subjectline2 = inPdfFormView.subjectline2;
		theData.extraline1left = inPdfFormView.extraline1left;
		theData.extraline1right = inPdfFormView.extraline1right;
		theData.extraline2left = inPdfFormView.extraline2left;
		theData.extraline2right = inPdfFormView.extraline2right;
		theData.extraline3left = inPdfFormView.extraline3left;
		theData.extraline3right = inPdfFormView.extraline3right;
		theData.extraline4left = inPdfFormView.extraline4left;
		theData.extraline4right = inPdfFormView.extraline4right;
		theData.extraline5left = inPdfFormView.extraline5left;
		theData.extraline5right = inPdfFormView.extraline5right;
		theData.extraline6left = inPdfFormView.extraline6left;
		theData.extraline6right = inPdfFormView.extraline6right;
		theData.extraline7left = inPdfFormView.extraline7left;
		theData.extraline7right = inPdfFormView.extraline7right;
		theData.extraline8left = inPdfFormView.extraline8left;
		theData.extraline8right = inPdfFormView.extraline8right;
		theData.extraline9left = inPdfFormView.extraline9left;
		theData.extraline9right = inPdfFormView.extraline9right;

		theData.lettertext = inPdfFormView.lettertext;
		theData.footerleft = inPdfFormView.footerleft;
		theData.footerright = inPdfFormView.footerright;
		
		final String theImage = Utils.trimToNull( inPdfFormView.topimage );
		
		if ( Utils.isNotBlank( theImage ) ) {
			theData.topImageBase64 = "data:image/*;base64,"+theImage; 
		}
		
		return theData;
	}

	public static GermanV1RenderUtils getInstance() {
		return INSTANCE;
	}
}
