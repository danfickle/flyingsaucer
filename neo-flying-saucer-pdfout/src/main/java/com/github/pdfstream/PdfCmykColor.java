package com.github.pdfstream;

public class PdfCmykColor  implements PdfColor 
{
	private final float c, m, y, k, a;
	
	/**
	 * All values provided should be in the range 0 to 1.
	 */
	public PdfCmykColor(float c, float m, float y, float k, float a)
	{
		this.c = c;
		this.m = m;
		this.y = y;
		this.k = k;
		this.a = a;
	}
	
	@Override
	public void setStrokeColorOnPage(ContentStream pg) 
	{
		pg.append(PDF.df.format(this.c));
		pg.append(' ');
		pg.append(PDF.df.format(this.m));
		pg.append(' ');
		pg.append(PDF.df.format(this.y));
		pg.append(' ');
		pg.append(PDF.df.format(this.k));
		pg.append(" K\n");
	}

	@Override
	public void setNonStrokeColorOnPage(ContentStream pg) 
	{
		pg.append(PDF.df.format(this.c));
		pg.append(' ');
		pg.append(PDF.df.format(this.m));
		pg.append(' ');
		pg.append(PDF.df.format(this.y));
		pg.append(' ');
		pg.append(PDF.df.format(this.k));
		pg.append(" k\n");
	}

	@Override
	public void setAlphaColorOnPage(ContentStream pg) 
	{
		String extg = pg.pdf.getExtGStateForAlpha(this.a);
		
		pg.append('/');
		pg.append(extg);
		pg.append(" gs\n");
	}
}
