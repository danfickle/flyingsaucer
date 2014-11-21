package org.xhtmlrenderer.displaylist;

import java.awt.Rectangle;

import org.xhtmlrenderer.extend.OutputDevice;
import org.xhtmlrenderer.extend.TextRenderer;

import com.github.neoflyingsaucer.extend.output.FSFont;
import com.github.neoflyingsaucer.extend.output.FSFontMetrics;
import com.github.neoflyingsaucer.extend.output.FSGlyphVector;
import com.github.neoflyingsaucer.extend.output.FontContext;
import com.github.neoflyingsaucer.extend.output.JustificationInfo;

public class DlTextRenderer implements TextRenderer {

	@Override
	public void setup(FontContext context) {
		// TODO Auto-generated method stub

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
	public FSFontMetrics getFSFontMetrics(FontContext context, FSFont font, String string)
	{
		return context.getFontMetrics(font, string);
	}

	@Override
	public int getWidth(FontContext context, FSFont font, String string) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setFontScale(float scale) {
		// TODO Auto-generated method stub

	}

	@Override
	public float getFontScale() {
		// TODO Auto-generated method stub
		return 1f;
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
