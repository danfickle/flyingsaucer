/*
 * {{{ header & license
 * Copyright (c) 2006 Wisconsin Court System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package com.github.neoflyingsaucer.j2dout;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.font.GlyphVector;

import com.github.neoflyingsaucer.extend.output.FSFont;
import com.github.neoflyingsaucer.extend.output.FSFontMetrics;
import com.github.neoflyingsaucer.extend.output.FSGlyphVector;
import com.github.neoflyingsaucer.extend.output.FontContext;

public class Java2DFontContext implements FontContext 
{
    private final Graphics2D _graphics;
    
    public Java2DFontContext(final Graphics2D graphics)
    {
        _graphics = graphics;
    }
    
    public Graphics2D getGraphics()
    {
        return _graphics;
    }
    
    @Override
    public FSFontMetrics getFontMetrics(FSFont font, String s)
    {
        // TODO: Antialias and fractional metrics.
    	Java2DMetricsAdapter adapter = new Java2DMetricsAdapter(
                ((Java2DFont) font).getAWTFont().getLineMetrics(
                        s, _graphics.getFontRenderContext()));
        
        return adapter;
    }

	@Override
	public int getWidth(FSFont font, String s) 
	{
        // TODO: Antialias and fractional metrics.
		Font awtFont = ((Java2DFont) font).getAWTFont();
        return (int) Math.round(_graphics.getFontMetrics(awtFont).getStringBounds(s, _graphics).getWidth());            
	}

	@Override
	public FSGlyphVector getGlyphVector(FSFont font, String s)
	{
		// TODO: Antialias and fractional metrics.
		Font awtFont = ((Java2DFont) font).getAWTFont();
		GlyphVector vector = awtFont.createGlyphVector(_graphics.getFontRenderContext(), s);
		return new Java2DGlyphVector(vector);
	}

	@Override
	public float[] getGlyphPositions(FSFont font, FSGlyphVector fsGlyphVector) 
	{
		// TODO: Antialias and fractional metrics.
		GlyphVector vector = ((Java2DGlyphVector) fsGlyphVector).getGlyphVector();
		float[] result = vector.getGlyphPositions(0, vector.getNumGlyphs() + 1, null);
        return result;
	}

	@Override
	public Rectangle getGlyphBounds(FSFont font, FSGlyphVector fsGlyphVector,
			int index, float x, float y)
	{
		// TODO: Antialias and fractional metrics.
        GlyphVector vector = ((Java2DGlyphVector) fsGlyphVector).getGlyphVector();
        Rectangle result = vector.getGlyphPixelBounds(index, _graphics.getFontRenderContext(), x, y);
        return result;
	}
}
