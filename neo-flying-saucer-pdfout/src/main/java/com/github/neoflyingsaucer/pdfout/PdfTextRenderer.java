package com.github.neoflyingsaucer.pdfout;

import java.awt.Rectangle;
import java.io.IOException;

import org.xhtmlrenderer.extend.FSGlyphVector;
import org.xhtmlrenderer.extend.FontContext;
import org.xhtmlrenderer.extend.OutputDevice;
import org.xhtmlrenderer.extend.TextRenderer;
import org.xhtmlrenderer.render.FSFont;
import org.xhtmlrenderer.render.FSFontMetrics;
import org.xhtmlrenderer.render.JustificationInfo;

import com.github.pdfstream.CoreFont;
import com.github.pdfstream.Font;
import com.github.pdfstream.PDF;
import com.github.pdfstream.Page;

public class PdfTextRenderer implements TextRenderer{

	private  Page _page;
	private  PDF _pdf;
	private  Font _f;
	
	public PdfTextRenderer() 
	{

	}

	public void setInfo(Page page, PDF pdf)
	{
		_page = page;
		_pdf = pdf;
		Font f = null;
		
		try {
			f = new Font(_pdf, CoreFont.HELVETICA);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		_f = f;
		

	}
	
	@Override
	public void setup(FontContext context) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void drawString(OutputDevice outputDevice, String string, float x,
			float y) {

		try {

			_page.drawString(_f, string, x, y);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void drawString(OutputDevice outputDevice, String string, float x,
			float y, JustificationInfo info) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void drawGlyphVector(OutputDevice outputDevice,
			FSGlyphVector vector, float x, float y) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public FSGlyphVector getGlyphVector(OutputDevice outputDevice, FSFont font,
			String string) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public float[] getGlyphPositions(OutputDevice outputDevice, FSFont font,
			FSGlyphVector fsGlyphVector) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Rectangle getGlyphBounds(OutputDevice outputDevice, FSFont font,
			FSGlyphVector fsGlyphVector, int index, float x, float y) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FSFontMetrics getFSFontMetrics(FontContext context, FSFont font,
			String string) {
		// TODO Auto-generated method stub
		return new PdfFontMetrics();
	}

	@Override
	public int getWidth(FontContext context, FSFont font, String string) {
		// TODO Auto-generated method stub
		return (int) 100;
	}

	@Override
	public void setFontScale(float scale) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public float getFontScale() {
		// TODO Auto-generated method stub
		return 100;
	}

	@Override
	public void setSmoothingThreshold(float fontsize) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getSmoothingLevel() {
		// TODO Auto-generated method stub
		return 10;
	}

	@Override
	public void setSmoothingLevel(int level) {
		// TODO Auto-generated method stub
		
	}

}
