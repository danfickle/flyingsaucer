package com.github.pdfstream;

public class PdfGreyScaleColor implements PdfColor
{
	private final float g;
	private final float a;
	
	public static final PdfColor BLACK = new PdfGreyScaleColor(0, 255);
	public static final PdfColor WHITE = new PdfGreyScaleColor(255, 255);	

	/**
	 * g must be a grey scale color in the range 0 to 255.
	 */
	public PdfGreyScaleColor(int g, int a) 
	{
		this.g = g / 255f;
		this.a = a / 255f;
	}
	
	
	@Override
	public void setStrokeColorOnPage(Page pg) 
	{
		pg.append(PDF.df.format(this.g));
		pg.append(" G\n");
	}

	@Override
	public void setNonStrokeColorOnPage(Page pg) 
	{
		pg.append(PDF.df.format(this.g));
		pg.append(" g\n");
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
		result = prime * result + Float.floatToIntBits(g);
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
		PdfGreyScaleColor other = (PdfGreyScaleColor) obj;
		if (Float.floatToIntBits(a) != Float.floatToIntBits(other.a))
			return false;
		if (Float.floatToIntBits(g) != Float.floatToIntBits(other.g))
			return false;
		return true;
	}
}
