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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package com.github.neoflyingsaucer.pdf2dout;

import com.github.neoflyingsaucer.extend.output.FSFontMetrics;

public class Pdf2MetricsAdapter implements FSFontMetrics 
{
    private float _ascent;
    private float _descent;
    private float _strikethroughOffset;
    private float _strikethroughThickness;
    private float _underlineOffset;
    private float _underlineThickness;
	
	public Pdf2MetricsAdapter() 
	{
	}

	@Override
	public float getAscent() 
	{
		return _ascent;
	}

	@Override
	public float getDescent() 
	{
		return _descent;
	}

	@Override
	public float getStrikethroughOffset() {
		return _strikethroughOffset;
	}

	@Override
	public float getStrikethroughThickness() {
		return _strikethroughThickness;
	}

	@Override
	public float getUnderlineOffset() {
		return _underlineOffset;
	}

	@Override
	public float getUnderlineThickness() {
		return _underlineThickness;
	}

	public void setStrikethroughThickness(float f) {
		_strikethroughThickness = f;
	}

	public void setAscent(float f) 
	{
		_ascent = f;
	}

	public void setStrikethroughOffset(float f) {
		_strikethroughOffset = f;
	}

	public void setUnderlineOffset(float f) {
		_underlineOffset = f;
	}

	public void setUnderlineThickness(float f) {
		_underlineThickness = f;
	}

	public void setDescent(float f) {
		_descent = f;
	}
}
