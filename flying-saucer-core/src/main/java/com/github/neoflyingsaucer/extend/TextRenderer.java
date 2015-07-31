/*
 * {{{ header & license
 * Copyright (c) 2004, 2005 Joshua Marinacci
 * Copyright (c) 2006 Wisconsin Court System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package com.github.neoflyingsaucer.extend;

import java.awt.Rectangle;

import com.github.neoflyingsaucer.extend.OutputDevice;
import com.github.neoflyingsaucer.extend.output.FSFont;
import com.github.neoflyingsaucer.extend.output.FSFontMetrics;
import com.github.neoflyingsaucer.extend.output.FSGlyphVector;
import com.github.neoflyingsaucer.extend.output.FontContext;
import com.github.neoflyingsaucer.extend.output.JustificationInfo;

public interface TextRenderer
{
    public void setup(FontContext context);

    public void drawString(OutputDevice outputDevice, String string, float x, float y);
    
    public void drawString(OutputDevice outputDevice, String string, float x, float y, JustificationInfo info);
    
    public void drawGlyphVector(OutputDevice outputDevice, FSGlyphVector vector, float x, float y);
    
    public FSGlyphVector getGlyphVector(OutputDevice outputDevice, FSFont font, String string);
    
    public float[] getGlyphPositions(OutputDevice outputDevice, FSFont font, FSGlyphVector fsGlyphVector);
    
    public Rectangle getGlyphBounds(OutputDevice outputDevice, FSFont font, FSGlyphVector fsGlyphVector, int index, float x, float y);

    public FSFontMetrics getFSFontMetrics(FontContext context, FSFont font, String string );

    public int getWidth(FontContext context, FSFont font, String string);
}
