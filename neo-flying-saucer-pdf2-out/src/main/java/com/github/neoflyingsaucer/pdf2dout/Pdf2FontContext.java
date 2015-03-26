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
package com.github.neoflyingsaucer.pdf2dout;

import java.awt.Rectangle;

import com.github.neoflyingsaucer.extend.output.FSFont;
import com.github.neoflyingsaucer.extend.output.FSFontMetrics;
import com.github.neoflyingsaucer.extend.output.FSGlyphVector;
import com.github.neoflyingsaucer.extend.output.FontContext;
import com.github.neoflyingsaucer.pdf2dout.Pdf2FontResolver.FontDescription;
import com.github.pdfstream.Font;

public class Pdf2FontContext implements FontContext 
{
	private static final float TEXT_MEASURING_DELTA = 0.01f;

	@Override
    public FSFontMetrics getFontMetrics(FSFont font, String string) 
	{
        final FontDescription descr = ((Pdf2Font )font).getFontDescription();
        final Font bf = descr.getFont();
        final float size = font.getSize2D() / 1000f;
        final Pdf2MetricsAdapter result = new Pdf2MetricsAdapter();

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
	public int getWidth(FSFont font, String s) 
	{
        final Font bf = ((Pdf2Font)font).getFontDescription().getFont();
        final float result = (bf.stringWidth(s)) * (font.getSize2D() / 1000f);
        
        if (result - Math.floor(result) < TEXT_MEASURING_DELTA) {
            return (int)result;
        } else {
            return (int)Math.ceil(result); 
        }
	}

	@Override
	public FSGlyphVector getGlyphVector(FSFont font, String s)
	{
		assert(false);
		return null;
	}

	@Override
	public float[] getGlyphPositions(FSFont font, FSGlyphVector fsGlyphVector) 
	{
		assert(false);
		return null;
	}

	@Override
	public Rectangle getGlyphBounds(FSFont font, FSGlyphVector fsGlyphVector,
			int index, float x, float y) 
	{
		assert(false);
		return null;
	}
}
