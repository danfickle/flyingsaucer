package com.github.neoflyingsaucer.pdfout;

import com.github.neoflyingsaucer.extend.output.FSFontMetrics;

public class PdfFontMetrics implements FSFontMetrics 
{
    private float _ascent;
    private float _descent;
    private float _strikethroughOffset;
    private float _strikethroughThickness;
    private float _underlineOffset;
    private float _underlineThickness;
	
	public PdfFontMetrics() 
	{
	}

	@Override
	public float getAscent() 
	{
		return _ascent;
	}

	@Override
	public float getDescent() 
	{
		return _descent;
	}

	@Override
	public float getStrikethroughOffset() {
		return _strikethroughOffset;
	}

	@Override
	public float getStrikethroughThickness() {
		return _strikethroughThickness;
	}

	@Override
	public float getUnderlineOffset() {
		return _underlineOffset;
	}

	@Override
	public float getUnderlineThickness() {
		return _underlineThickness;
	}

	public void setStrikethroughThickness(float f) {
		_strikethroughThickness = f;
	}

	public void setAscent(float f) 
	{
		_ascent = f;
	}

	public void setStrikethroughOffset(float f) {
		_strikethroughOffset = f;
	}

	public void setUnderlineOffset(float f) {
		_underlineOffset = f;
	}

	public void setUnderlineThickness(float f) {
		_underlineThickness = f;
	}

	public void setDescent(float f) {
		_descent = f;
	}
}
