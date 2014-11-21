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

import java.awt.Graphics2D;

import com.github.neoflyingsaucer.extend.output.FSFont;
import com.github.neoflyingsaucer.extend.output.FSFontMetrics;
import com.github.neoflyingsaucer.extend.output.FontContext;

public class Java2DFontContext implements FontContext {
    private final Graphics2D _graphics;
    
    public Java2DFontContext(final Graphics2D graphics) {
        _graphics = graphics;
    }
    
    public Graphics2D getGraphics() {
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
}
