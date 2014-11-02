package com.github.neoflyingsaucer.pdfout;

import java.awt.Rectangle;
import org.xhtmlrenderer.extend.FSGlyphVector;
import org.xhtmlrenderer.extend.FontContext;
import org.xhtmlrenderer.extend.OutputDevice;
import org.xhtmlrenderer.extend.TextRenderer;
import org.xhtmlrenderer.render.FSFont;
import org.xhtmlrenderer.render.FSFontMetrics;
import org.xhtmlrenderer.render.JustificationInfo;

import com.github.neoflyingsaucer.pdfout.PdfFontResolver.FontDescription;
import com.github.pdfstream.Font;

public class PdfTextRenderer implements TextRenderer
{
    private static float TEXT_MEASURING_DELTA = 0.01f;
	
	public PdfTextRenderer() 
	{

	}

	@Override
	public void setup(FontContext context) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void drawString(OutputDevice outputDevice, String string, float x, float y) 
	{
		((PdfOutputDevice) outputDevice).drawString(string, x, y, null);
	}

	@Override
	public void drawString(OutputDevice outputDevice, String string, float x,
			float y, JustificationInfo info) 
	{
		((PdfOutputDevice) outputDevice).drawString(string, x, y, info);
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
    public FSFontMetrics getFSFontMetrics(final FontContext context, final FSFont font, final String string) 
	{
        final FontDescription descr = ((PdfFont )font).getFontDescription();
        final Font bf = descr.getFont();
        final float size = font.getSize2D();
        final PdfFontMetrics result = new PdfFontMetrics();

        result.setAscent(bf.getBBoxURy() * size);
        result.setDescent(-bf.getBBoxLLy() * size);
        
        result.setStrikethroughOffset(-descr.getYStrikeoutPosition() / 1000f * size);
        if (descr.getYStrikeoutSize() != 0) {
            result.setStrikethroughThickness(descr.getYStrikeoutSize() / 1000f * size);
        } else {
            result.setStrikethroughThickness(size / 12.0f);
        }
        
        result.setUnderlineOffset(-descr.getUnderlinePosition() / 1000f * size);
        result.setUnderlineThickness(descr.getUnderlineThickness() / 1000f * size);

        return result;
    }

	@Override
	public int getWidth(FontContext context, FSFont font, String string) 
	{
        final Font bf = ((PdfFont)font).getFontDescription().getFont();
        final float result = bf.stringWidth(string) * font.getSize2D();
        if (result - Math.floor(result) < TEXT_MEASURING_DELTA) {
            return (int)result;
        } else {
            return (int)Math.ceil(result); 
        }
	}

	@Override
	public void setFontScale(float scale) {
		// TODO Auto-generated method stub
	}

	@Override
	public float getFontScale() 
	{
		return 1.0f;
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
