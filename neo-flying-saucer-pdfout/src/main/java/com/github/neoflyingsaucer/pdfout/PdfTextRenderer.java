package com.github.neoflyingsaucer.pdfout;

import java.awt.Rectangle;

import org.xhtmlrenderer.extend.FSGlyphVector;
import org.xhtmlrenderer.extend.FontContext;
import org.xhtmlrenderer.extend.OutputDevice;
import org.xhtmlrenderer.extend.TextRenderer;
import org.xhtmlrenderer.render.FSFont;
import org.xhtmlrenderer.render.FSFontMetrics;
import org.xhtmlrenderer.render.JustificationInfo;

public class PdfTextRenderer implements TextRenderer{

	@Override
	public void setup(FontContext context) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void drawString(OutputDevice outputDevice, String string, float x,
			float y) {
		// TODO Auto-generated method stub
		
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
		return 100;
	}

	@Override
	public void setFontScale(float scale) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public float getFontScale() {
		// TODO Auto-generated method stub
		return 1;
	}

	@Override
	public void setSmoothingThreshold(float fontsize) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getSmoothingLevel() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setSmoothingLevel(int level) {
		// TODO Auto-generated method stub
		
	}

}
