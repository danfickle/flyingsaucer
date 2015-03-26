package com.github.pdfstream;

public interface PdfColor
{
	public void setStrokeColorOnPage(ContentStream pg);
	public void setNonStrokeColorOnPage(ContentStream pg);
	public void setAlphaColorOnPage(ContentStream pg);
}
