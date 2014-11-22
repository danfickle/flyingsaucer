package org.xhtmlrenderer.displaylist;

import java.awt.Rectangle;

import org.xhtmlrenderer.extend.OutputDevice;
import org.xhtmlrenderer.extend.TextRenderer;

import com.github.neoflyingsaucer.extend.output.FSFont;
import com.github.neoflyingsaucer.extend.output.FSFontMetrics;
import com.github.neoflyingsaucer.extend.output.FSGlyphVector;
import com.github.neoflyingsaucer.extend.output.FontContext;
import com.github.neoflyingsaucer.extend.output.JustificationInfo;

public class DlTextRenderer implements TextRenderer
{
	private FontContext ctx;
	
	@Override
	public void setup(FontContext context)
	{
		this.ctx = context;
	}

	@Override
	public void drawString(OutputDevice outputDevice, String string, float x, float y) 
	{
		((DlOutputDevice) outputDevice).drawString(string, x, y);
	}

	@Override
	public void drawString(OutputDevice outputDevice, String string, float x, float y, JustificationInfo info)
	{
		((DlOutputDevice) outputDevice).drawString(string, x, y, info);
	}

	@Override
	public void drawGlyphVector(OutputDevice outputDevice, FSGlyphVector vector, float x, float y)
	{
		((DlOutputDevice) outputDevice).drawGlyphVector(vector, x, y);
	}

	@Override
	public FSGlyphVector getGlyphVector(OutputDevice outputDevice, FSFont font, String s)
	{
		return ctx.getGlyphVector(font, s);
	}

	@Override
	public float[] getGlyphPositions(OutputDevice outputDevice, FSFont font,
			FSGlyphVector fsGlyphVector)
	{
		return ctx.getGlyphPositions(font, fsGlyphVector);
	}

	@Override
	public Rectangle getGlyphBounds(OutputDevice outputDevice, FSFont font,
			FSGlyphVector fsGlyphVector, int index, float x, float y) 
	{
		return ctx.getGlyphBounds(font, fsGlyphVector, index, x, y);
	}

	@Override
	public FSFontMetrics getFSFontMetrics(FontContext context, FSFont font, String string)
	{
		return context.getFontMetrics(font, string);
	}

	@Override
	public int getWidth(FontContext context, FSFont font, String string)
	{
		return context.getWidth(font, string);
	}
}
