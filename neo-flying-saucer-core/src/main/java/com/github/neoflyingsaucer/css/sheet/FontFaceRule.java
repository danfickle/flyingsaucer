/*
 * {{{ header & license
 * Copyright (c) 2007 Wisconsin Court System
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
package com.github.neoflyingsaucer.css.sheet;

import com.github.neoflyingsaucer.css.constants.CSSName;
import com.github.neoflyingsaucer.css.constants.IdentValue;
import com.github.neoflyingsaucer.css.newmatch.CascadedStyle;
import com.github.neoflyingsaucer.css.parser.property.Conversions;
import com.github.neoflyingsaucer.css.sheet.StylesheetInfo.CSSOrigin;
import com.github.neoflyingsaucer.css.style.CalculatedStyle;
import com.github.neoflyingsaucer.css.style.EmptyStyle;
import com.github.neoflyingsaucer.css.style.FSDerivedValue;
import com.github.neoflyingsaucer.extend.output.FSFontFaceItem;
import com.github.neoflyingsaucer.extend.output.FontSpecificationI;
import com.github.neoflyingsaucer.extend.useragent.Optional;
import com.github.neoflyingsaucer.layout.SharedContext;
import com.github.neoflyingsaucer.util.XRRuntimeException;

public class FontFaceRule implements RulesetContainer {
    private CSSOrigin _origin;
    private Ruleset _ruleset;
    private CalculatedStyle _calculatedStyle;

    public FontFaceRule(final CSSOrigin origin) {
        _origin = origin;
    }

    public void addContent(final Ruleset ruleset) {
        if (_ruleset != null) {
            throw new XRRuntimeException("Ruleset can only be set once");
        }
        _ruleset = ruleset;
    }

    public CSSOrigin getOrigin() {
        return _origin;
    }

    public void setOrigin(final CSSOrigin origin) {
        _origin = origin;
    }

    public CalculatedStyle getCalculatedStyle() {
        if (_calculatedStyle == null) {
            _calculatedStyle = new EmptyStyle().deriveStyle(
                    CascadedStyle.createLayoutStyle(_ruleset.getPropertyDeclarations()));
        }

        return _calculatedStyle;
    }

    public boolean hasFontFamily() {
        for (final PropertyDeclaration decl : _ruleset.getPropertyDeclarations()) {
            if (decl.getPropertyName().equals("font-family")) {
                return true;
            }
        }

        return false;
    }
    
    public static class FontFaceItem implements FSFontFaceItem
    {
    	private final byte[] bytes;
    	private final String fontFamily;
    	private final String encoding;
    	private final int weight;
    	private final FontSpecificationI spec;
    	
    	FontFaceItem(String fontFamily, byte[] bytes, String encoding, int weight, FontSpecificationI spec)
    	{
    		this.fontFamily = fontFamily;
    		this.bytes = bytes;
    		this.encoding = encoding;
    		this.weight = weight;
    		this.spec = spec;
    	}
    	
		@Override
		public String getFontFamily() 
		{
			return fontFamily;
		}

		@Override
		public byte[] getFontBytes() 
		{
			return bytes;
		}

		@Override
		public String getEncoding() 
		{
			return encoding;
		}

		@Override
		public int getWeight() 
		{
			return weight;
		}

		@Override
		public FontSpecificationI getSpecification() 
		{
			return spec;
		}
    }
    
    public Optional<FSFontFaceItem> getFontFaceItem(SharedContext ctx)
    {
    	CalculatedStyle style = getCalculatedStyle();
    	
    	String family;
    	String encoding;
    	int weight;
    	FontSpecificationI spec;
    	
    	if (style.isIdent(CSSName.SRC, IdentValue.NONE) ||
    		style.isIdent(CSSName.FONT_FAMILY, IdentValue.NONE))
    		return Optional.empty();
    	
    	FSDerivedValue src = style.valueByName(CSSName.SRC);
    	FSDerivedValue fontEncoding = style.valueByName(CSSName.FS_PDF_FONT_ENCODING);
    	FSDerivedValue fontFamily = style.valueByName(CSSName.FONT_FAMILY);
    	FSDerivedValue fontWeight = style.valueByName(CSSName.FONT_WEIGHT);
    	
    	Optional<byte[]> fontBytes = ctx.getUac().getBinaryResource(src.asString());
    	if (!fontBytes.isPresent())
    		return Optional.empty();
    	
    	family = fontFamily.asString();
    	spec = style.getFontSpecification();
    	weight = spec == null ? 400 : spec.getFontWeight(); 
     	encoding = fontEncoding.asString();
    	
    	return Optional.<FSFontFaceItem>of(new FontFaceItem(family, fontBytes.get(), encoding, weight, spec));
    }
}
