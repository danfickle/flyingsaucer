package com.github.pdfstream;

import java.io.ByteArrayOutputStream;

public class PdfAppearanceStream 
{
	final float width, height;
	final String state;
	int objNumber;
	ByteArrayOutputStream os = new ByteArrayOutputStream();
	
    protected void append(String str){
        int len = str.length();
        for (int i = 0; i < len; i++) {
            os.write((byte) str.charAt(i));
        }
    }
	
	public PdfAppearanceStream(String state, float width, float height) 
	{
		this.width = width;
		this.height = height;
		this.state = state;
	}

	public void setRGBColorStroke(int red, int green, int blue) 
	{
		float r = red / 255f;
		float g = green / 255f;
		float b = blue / 255f;

		append(PDF.df.format(r));
		append(" ");
		append(PDF.df.format(b));
		append(" ");
		append(PDF.df.format(g));
		append(" RG\n");
	}

	public void setCMYKColorStroke(int c, int m, int y, int k) 
	{
		append(PDF.formatFloat(c / 255f));
		append(" ");
		
		append(PDF.formatFloat(m / 255f));
		append(" ");
		
		append(PDF.formatFloat(y / 255f));
		append(" ");

		append(PDF.formatFloat(k / 255f));
		append(" K\n");
	}

	public void setRGBColorFill(int red, int green, int blue) 
	{
		float r = red / 255f;
		float g = green / 255f;
		float b = blue / 255f;

		append(PDF.df.format(r));
		append(" ");
		append(PDF.df.format(b));
		append(" ");
		append(PDF.df.format(g));
		append(" rg\n");
	}

	public void setCMYKColorFill(int c, int m, int y, int k)
	{
		append(PDF.formatFloat(c / 255f));
		append(" ");
		
		append(PDF.formatFloat(m / 255f));
		append(" ");
		
		append(PDF.formatFloat(y / 255f));
		append(" ");

		append(PDF.formatFloat(k / 255f));
		append(" K\n");

	}

	public void setLineWidth(float strokeWidth) 
	{
        append(PDF.formatFloat(strokeWidth));
        append(" w\n");
	}

	public void moveTo(float x, float y) 
	{
        append(PDF.formatFloat(x));
        append(" ");
        append(PDF.formatFloat(y));
        append(" m\n");
	}

	public void lineTo(float x, float y) 
	{
	   	append(PDF.formatFloat(x));
    	append(" ");
    	append(PDF.formatFloat(y));
    	append(" l\n");
	}

	public void closePath() 
	{
    	append("h\n");
	}

	public void fill() 
	{
		append("f\n");
	}

	public void stroke() 
	{
		append("S\n");
	}
}
