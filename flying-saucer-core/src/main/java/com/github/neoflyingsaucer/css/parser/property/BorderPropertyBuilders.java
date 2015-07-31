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
package com.github.neoflyingsaucer.css.parser.property;

import java.util.ArrayList;
import java.util.List;

import com.github.neoflyingsaucer.css.constants.CSSName;
import com.github.neoflyingsaucer.css.constants.CSSPrimitiveUnit;
import com.github.neoflyingsaucer.css.constants.IdentValue;
import com.github.neoflyingsaucer.css.parser.CSSParseException;
import com.github.neoflyingsaucer.css.parser.FSRGBColor;
import com.github.neoflyingsaucer.css.parser.PropertyValue;
import com.github.neoflyingsaucer.css.parser.PropertyValueImp;
import com.github.neoflyingsaucer.css.parser.PropertyValueImp.CSSValueType;
import com.github.neoflyingsaucer.css.sheet.PropertyDeclaration;
import com.github.neoflyingsaucer.css.sheet.StylesheetInfo.CSSOrigin;
import com.github.neoflyingsaucer.extend.controller.error.LangId;

import static com.github.neoflyingsaucer.css.parser.property.BuilderUtil.*;

public class BorderPropertyBuilders {
    private static abstract class BorderSidePropertyBuilder implements PropertyBuilder {
        protected abstract CSSName[][] getProperties();
        
        private void addAll(final List<PropertyDeclaration> result, final CSSName[] properties, final PropertyValue value, final CSSOrigin origin, final boolean important) {
            for (final CSSName property : properties) {
                result.add(new PropertyDeclaration(
                        property, value, important, origin));
            }
        }
        
        public List<PropertyDeclaration> buildDeclarations(
                final CSSName cssName, final List<PropertyValue> values, final CSSOrigin origin, final boolean important, final boolean inheritAllowed) {
            final CSSName[][] props = getProperties();
            
            final List<PropertyDeclaration> result = new ArrayList<PropertyDeclaration>(3);
            
            if (values.size() == 1 && 
                (values.get(0)).getCssValueTypeN() == CSSValueType.CSS_INHERIT) {
                final PropertyValue value = values.get(0);
                addAll(result, props[0], value, origin, important);
                addAll(result, props[1], value, origin, important);
                addAll(result, props[2], value, origin, important);
                
                return result;
            } else {
                checkValueCount(cssName, 1, 3, values.size());
                boolean haveBorderStyle = false;
                boolean haveBorderColor = false;
                boolean haveBorderWidth = false;
                
                for (final PropertyValue value : values) {
                    checkInheritAllowed(value, false);
                    boolean matched = false;
                    final PropertyValue borderWidth = convertToBorderWidth(value);
                    if (borderWidth != null) {
                        if (haveBorderWidth) {
                            throw new CSSParseException(LangId.NO_TWICE, -1, "border width");
                        }
                        haveBorderWidth = true;
                        matched = true;
                        addAll(result, props[0], borderWidth, origin, important);
                    }
                    
                    if (isBorderStyle(value)) {
                        if (haveBorderStyle) {
                            throw new CSSParseException(LangId.NO_TWICE, -1, "border style");
                        }
                        haveBorderStyle = true;
                        matched = true;
                        addAll(result, props[1], value, origin, important);
                    }
                    
                    final PropertyValue borderColor = convertToBorderColor(value);
                    if (borderColor != null) {
                        if (haveBorderColor) {
                            throw new CSSParseException(LangId.NO_TWICE, -1, "border color");
                        }
                        haveBorderColor = true;
                        matched = true;
                        addAll(result, props[2], borderColor, origin, important);
                    }
                    
                    if (! matched) {
                        throw new CSSParseException(LangId.BORDER_VALUE_INVALID, -1, value.getCssText());
                    }
                }
                
                if (! haveBorderWidth) {
                    addAll(result, props[0], new PropertyValueImp(IdentValue.FS_INITIAL_VALUE), origin, important);
                }
                
                if (! haveBorderStyle) {
                    addAll(result, props[1], new PropertyValueImp(IdentValue.FS_INITIAL_VALUE), origin, important);
                }
                
                if (! haveBorderColor) {
                    addAll(result, props[2], new PropertyValueImp(IdentValue.FS_INITIAL_VALUE), origin, important);
                }
                
                return result;
            }
        }
        
        private boolean isBorderStyle(final PropertyValue value) {
            if (value.getPrimitiveTypeN() != CSSPrimitiveUnit.CSS_IDENT) {
                return false;
            }
            
            final IdentValue ident = IdentValue.fsValueOf(value.getCssText());
            if (ident == null) {
                return false;
            }
            
            return IdentSet.BORDER_STYLES.contains(ident);
        }
        
        private PropertyValue convertToBorderWidth(final PropertyValue value) {
        	final CSSPrimitiveUnit type = value.getPrimitiveTypeN();
            if (type != CSSPrimitiveUnit.CSS_IDENT && ! isLength(value)) {
                return null;
            }
            
            if (isLength(value)) {
                return value;
            } else {
                final IdentValue ident = IdentValue.fsValueOf(value.getStringValue());
                if (ident == null) {
                    return null;
                }
                
                if (IdentSet.BORDER_WIDTHS.contains(ident)) {
                    return Conversions.getBorderWidth(ident.toString());
                } else {
                    return null;
                }
            }
        } 
        
        private PropertyValue convertToBorderColor(final PropertyValue value) {
        	final CSSPrimitiveUnit type = value.getPrimitiveTypeN();
            if (type != CSSPrimitiveUnit.CSS_IDENT && type != CSSPrimitiveUnit.CSS_RGBCOLOR) {
                return null;
            }
            
            if (type == CSSPrimitiveUnit.CSS_RGBCOLOR) {
                return value;
            } else {
                final FSRGBColor color = Conversions.getColor(value.getStringValue());
                if (color != null) {
                    return new PropertyValueImp(color);
                }
                
                final IdentValue ident = IdentValue.fsValueOf(value.getCssText());
                if (ident == null || ident != IdentValue.TRANSPARENT) {
                    return null;
                }
                
                return value;
            }
        }
    }
    
    public static class BorderTop extends BorderSidePropertyBuilder {
        protected CSSName[][] getProperties() {
            return new CSSName[][] { 
                    new CSSName[] { CSSName.BORDER_TOP_WIDTH }, 
                    new CSSName[] { CSSName.BORDER_TOP_STYLE }, 
                    new CSSName[] { CSSName.BORDER_TOP_COLOR } };
        }
    }
    
    public static class BorderRight extends BorderSidePropertyBuilder {
        protected CSSName[][] getProperties() {
            return new CSSName[][] { 
                    new CSSName[] { CSSName.BORDER_RIGHT_WIDTH }, 
                    new CSSName[] { CSSName.BORDER_RIGHT_STYLE }, 
                    new CSSName[] { CSSName.BORDER_RIGHT_COLOR } };
        }
    }
    
    public static class BorderBottom extends BorderSidePropertyBuilder {
        protected CSSName[][] getProperties() {
            return new CSSName[][] { 
                    new CSSName[] { CSSName.BORDER_BOTTOM_WIDTH }, 
                    new CSSName[] { CSSName.BORDER_BOTTOM_STYLE }, 
                    new CSSName[] { CSSName.BORDER_BOTTOM_COLOR } };
        }
    }
    
    public static class BorderLeft extends BorderSidePropertyBuilder {
        protected CSSName[][] getProperties() {
            return new CSSName[][] { 
                    new CSSName[] { CSSName.BORDER_LEFT_WIDTH }, 
                    new CSSName[] { CSSName.BORDER_LEFT_STYLE }, 
                    new CSSName[] { CSSName.BORDER_LEFT_COLOR } };
        }
    }
    
    public static class Border extends BorderSidePropertyBuilder {
        protected CSSName[][] getProperties() {
            return new CSSName[][] { 
                    new CSSName[] { 
                            CSSName.BORDER_TOP_WIDTH, CSSName.BORDER_RIGHT_WIDTH,
                            CSSName.BORDER_BOTTOM_WIDTH, CSSName.BORDER_LEFT_WIDTH },
                    new CSSName[] { 
                            CSSName.BORDER_TOP_STYLE, CSSName.BORDER_RIGHT_STYLE,
                            CSSName.BORDER_BOTTOM_STYLE, CSSName.BORDER_LEFT_STYLE },                            
                    new CSSName[] { 
                            CSSName.BORDER_TOP_COLOR, CSSName.BORDER_RIGHT_COLOR,
                            CSSName.BORDER_BOTTOM_COLOR, CSSName.BORDER_LEFT_COLOR } };                            
        }
    } 
}
