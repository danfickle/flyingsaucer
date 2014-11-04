package com.github.pdfstream;

import org.xhtmlrenderer.css.style.derived.FSLinearGradient;

public class LinearGradient 
{
	final FSLinearGradient gradient;
	int shadingObjNumber;
	final float dotsPerPoint;
	final float x, y;

	public LinearGradient(FSLinearGradient grad, float dotsPerPoint, float x, float y) 
	{
		gradient = grad;
		this.dotsPerPoint = dotsPerPoint;
		this.x = x;
		this.y = y;
	}
}
