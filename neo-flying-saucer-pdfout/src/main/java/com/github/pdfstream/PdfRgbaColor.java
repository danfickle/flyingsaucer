package com.github.pdfstream;

public class PdfRgbaColor implements PdfColor 
{
	private final float r, g, b, a;
	
	/**
	 * All values provided should be in the range 0 to 255.
	 */
	public PdfRgbaColor(int r, int g, int b, int a)
	{
		this.r = r / 255f;
		this.g = g / 255f;
		this.b = b / 255f;
		this.a = a / 255f;
	}
	
	@Override
	public void setStrokeColorOnPage(Page pg) 
	{
		pg.append(PDF.df.format(this.r));
		pg.append(' ');
		pg.append(PDF.df.format(this.g));
		pg.append(' ');
		pg.append(PDF.df.format(this.b));
		pg.append(" RG\n");
	}

	@Override
	public void setNonStrokeColorOnPage(Page pg) 
	{
		pg.append(PDF.df.format(this.r));
		pg.append(' ');
		pg.append(PDF.df.format(this.g));
		pg.append(' ');
		pg.append(PDF.df.format(this.b));
		pg.append(" rg\n");
	}

	@Override
	public void setAlphaColorOnPage(Page pg) 
	{
		String extg = pg.pdf.getExtGStateForAlpha(this.a);
		
		pg.append('/');
		pg.append(extg);
		pg.append(" gs\n");
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Float.floatToIntBits(a);
		result = prime * result + Float.floatToIntBits(b);
		result = prime * result + Float.floatToIntBits(g);
		result = prime * result + Float.floatToIntBits(r);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PdfRgbaColor other = (PdfRgbaColor) obj;
		if (Float.floatToIntBits(a) != Float.floatToIntBits(other.a))
			return false;
		if (Float.floatToIntBits(b) != Float.floatToIntBits(other.b))
			return false;
		if (Float.floatToIntBits(g) != Float.floatToIntBits(other.g))
			return false;
		if (Float.floatToIntBits(r) != Float.floatToIntBits(other.r))
			return false;
		return true;
	}
}
