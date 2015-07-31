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
import com.github.neoflyingsaucer.css.parser.PropertyValue;
import com.github.neoflyingsaucer.css.parser.PropertyValueImp;
import com.github.neoflyingsaucer.css.parser.PropertyValueImp.CSSValueType;
import com.github.neoflyingsaucer.css.sheet.PropertyDeclaration;
import com.github.neoflyingsaucer.css.sheet.StylesheetInfo.CSSOrigin;
import com.github.neoflyingsaucer.extend.controller.error.LangId;

import static com.github.neoflyingsaucer.css.parser.property.BuilderUtil.*;

public class SizePropertyBuilder implements PropertyBuilder {
    private static final CSSName[] ALL = { CSSName.FS_PAGE_ORIENTATION, CSSName.FS_PAGE_HEIGHT, CSSName.FS_PAGE_WIDTH };
    
    public List<PropertyDeclaration> buildDeclarations(
            final CSSName cssName, final List<PropertyValue> values, final CSSOrigin origin, final boolean important, final boolean inheritAllowed) {
        final List<PropertyDeclaration> result = new ArrayList<PropertyDeclaration>(3);
        checkValueCount(cssName, 1, 2, values.size());
        
        if (values.size() == 1) {
            final PropertyValue value = (PropertyValue)values.get(0);
            
            checkInheritAllowed(value, inheritAllowed);
            
            if (value.getCssValueTypeN() == CSSValueType.CSS_INHERIT) {
                return checkInheritAll(ALL, values, origin, important, inheritAllowed);
            } else if (value.getPrimitiveTypeN() == CSSPrimitiveUnit.CSS_IDENT) {
                final PageSize pageSize = PageSize.getPageSize(value.getStringValue());
                if (pageSize != null) {
                    result.add(new PropertyDeclaration(
                            CSSName.FS_PAGE_ORIENTATION, new PropertyValueImp(IdentValue.AUTO), important, origin));
                    result.add(new PropertyDeclaration(
                            CSSName.FS_PAGE_WIDTH, pageSize.getPageWidth(), important, origin));
                    result.add(new PropertyDeclaration(
                            CSSName.FS_PAGE_HEIGHT, pageSize.getPageHeight(), important, origin));
                    return result;
                }
                
                final IdentValue ident = checkIdent(cssName, value);
                if (ident == IdentValue.LANDSCAPE || ident == IdentValue.PORTRAIT) {
                    result.add(new PropertyDeclaration(
                            CSSName.FS_PAGE_ORIENTATION, value, important, origin));
                    result.add(new PropertyDeclaration(
                            CSSName.FS_PAGE_WIDTH, new PropertyValueImp(IdentValue.AUTO), important, origin));
                    result.add(new PropertyDeclaration(
                            CSSName.FS_PAGE_HEIGHT, new PropertyValueImp(IdentValue.AUTO), important, origin));
                    return result;
                } else if (ident == IdentValue.AUTO) {
                    result.add(new PropertyDeclaration(
                            CSSName.FS_PAGE_ORIENTATION, value, important, origin));
                    result.add(new PropertyDeclaration(
                            CSSName.FS_PAGE_WIDTH, value, important, origin));
                    result.add(new PropertyDeclaration(
                            CSSName.FS_PAGE_HEIGHT, value, important, origin));
                    return result;
                } else {
                    throw new CSSParseException(LangId.UNSUPPORTED_IDENTIFIER, -1, value.getCssText(), cssName);
                }
            } else if (isLength(value)) {
                if (value.getFloatValue() < 0.0f) {
                    throw new CSSParseException(LangId.NO_NEGATIVE, -1 , "page dimension");
                }
                
                result.add(new PropertyDeclaration(
                        CSSName.FS_PAGE_ORIENTATION, new PropertyValueImp(IdentValue.AUTO), important, origin));
                result.add(new PropertyDeclaration(
                        CSSName.FS_PAGE_WIDTH, value, important, origin));
                result.add(new PropertyDeclaration(
                        CSSName.FS_PAGE_HEIGHT, value, important, origin));
                
                return result;
            } else {
                throw new CSSParseException(LangId.MUST_BE_LENGTH_OR_IDENTIFIER, -1, cssName);
            }
        } else { /* values.size == 2 */
            PropertyValue value1 = (PropertyValue)values.get(0);
            PropertyValue value2 = (PropertyValue)values.get(1);
            
            checkInheritAllowed(value2, false);
            
            if (isLength(value1) && isLength(value2)) {
                if (value1.getFloatValue() < 0.0f) {
                    throw new CSSParseException(LangId.NO_NEGATIVE, -1, "page dimension");
                }
                
                if (value2.getFloatValue() < 0.0f) {
                    throw new CSSParseException(LangId.NO_NEGATIVE, -1, "page dimension");
                }
                
                result.add(new PropertyDeclaration(
                        CSSName.FS_PAGE_ORIENTATION, new PropertyValueImp(IdentValue.AUTO), important, origin));
                result.add(new PropertyDeclaration(
                        CSSName.FS_PAGE_WIDTH, value1, important, origin));
                result.add(new PropertyDeclaration(
                        CSSName.FS_PAGE_HEIGHT, value2, important, origin));
                
                return result;
            } else if (value1.getPrimitiveTypeN() == CSSPrimitiveUnit.CSS_IDENT &&
                       value2.getPrimitiveTypeN() == CSSPrimitiveUnit.CSS_IDENT) {
                if (value2.getStringValue().equals("landscape") || 
                        value2.getStringValue().equals("portrait")) {
                    final PropertyValue temp = value1;
                    value1 = value2;
                    value2 = temp;
                }
                
                if (! (value1.toString().equals("landscape") || value1.toString().equals("portrait"))) {
                    throw new CSSParseException(LangId.INVALID_PAGE_ORIENTATION, -1, value1);
                }
                
                result.add(new PropertyDeclaration(
                        CSSName.FS_PAGE_ORIENTATION, value1, important, origin));
                
                final PageSize pageSize = PageSize.getPageSize(value2.getStringValue());
                if (pageSize == null) {
                    throw new CSSParseException(LangId.INVALID_PAGE_SIZE, -1, value1);
                }
                
                result.add(new PropertyDeclaration(
                        CSSName.FS_PAGE_WIDTH, pageSize.getPageWidth(), important, origin));
                result.add(new PropertyDeclaration(
                        CSSName.FS_PAGE_HEIGHT, pageSize.getPageHeight(), important, origin));
                
                return result;
            } else {
                throw new CSSParseException(LangId.INVALID_SYNTAX, -1, "size");
            }
        }
    }
}
