package de.greyshine.pdflettercreation.rendering;

import javax.servlet.http.HttpServletRequest;

import de.greyshine.pdflettercreation.web.PdfFormView;
import de.greyshine.utils.pdfletter.PdfLetterRenderer.DataObject;

public abstract class RenderUtils {

	public abstract RenderResultDO createPdf(HttpServletRequest inReq, PdfFormView inPdfFormView) throws Exception;

	public static class RenderResultDO {
		
		public byte[] pdfBytes;
		public DataObject dataObject;
		public long datasize;
		public long time = System.currentTimeMillis();
		public long durance;
	}
	
}
