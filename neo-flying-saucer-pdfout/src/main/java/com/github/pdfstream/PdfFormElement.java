package com.github.pdfstream;

import java.util.ArrayList;
import java.util.List;

public class PdfFormElement 
{
	float x1, y1, x2, y2;
	String partial;
	String export;
	String clzz;
	String defaultState;
	String value;
	boolean readOnly = false;
	List<PdfAppearanceStream> strms = new ArrayList<PdfAppearanceStream>(2);
	
	public void setRectangle(float x, float y, float x2, float y2)
	{
		this.x1 = x;
		this.y1 = y;
		this.x2 = x2;
		this.y2 = y2;
	}

	public void setPartialName(String partial)
	{
		this.partial = partial;
	}

	public void setExportName(String fieldName) 
	{
		this.export = fieldName;
	}

	public void setExportValue(String onValue) 
	{
		this.value = onValue;
	}

	public void setDefaultState(String state) 
	{
		this.defaultState = state;
	}

	public void setClass(String clss) 
	{
		this.clzz = clss;
	}

	public void setReadOnly(boolean b) 
	{
		this.readOnly = b;
	}
	
	public void addApearanceStream(PdfAppearanceStream strm)
	{
		this.strms.add(strm);
	}

	public void publishAppearanceStreams(PDF pdf) 
	{
		for (PdfAppearanceStream strm : strms)
		{
			pdf.addAppearanceStream(strm);
		}
	}
}
