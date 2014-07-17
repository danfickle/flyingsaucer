/*
 * {{{ header & license
 * Copyright (c) 2011 Wisconsin Court System
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
package org.xhtmlrenderer.css.parser.property;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.constants.CSSPrimitiveUnit;
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.css.parser.CSSParseException;
import org.xhtmlrenderer.css.parser.PropertyValue;
import org.xhtmlrenderer.css.parser.PropertyValueImp;
import org.xhtmlrenderer.css.parser.PropertyValueImp.CSSValueType;
import org.xhtmlrenderer.css.sheet.PropertyDeclaration;
import org.xhtmlrenderer.css.sheet.StylesheetInfo.CSSOrigin;
import org.xhtmlrenderer.util.LangId;

import static org.xhtmlrenderer.css.parser.property.BuilderUtil.*;

public class QuotesPropertyBuilder implements PropertyBuilder {

    public List<PropertyDeclaration> buildDeclarations(final CSSName cssName, final List<PropertyValue> values, final CSSOrigin origin, final boolean important, final boolean inheritAllowed) {
        if (values.size() == 1) {
            final PropertyValue value = (PropertyValue)values.get(0);
            if (value.getCssValueTypeN() == CSSValueType.CSS_INHERIT) {
                return Collections.emptyList();
            } else if (value.getPrimitiveTypeN() == CSSPrimitiveUnit.CSS_IDENT) {
                final IdentValue ident = checkIdent(CSSName.QUOTES, value);
                if (ident == IdentValue.NONE) {
                    return Collections.singletonList(
                            new PropertyDeclaration(CSSName.QUOTES, value, important, origin));
                }
            }
        }
        
        if (values.size() % 2 == 1) {
            throw new CSSParseException(LangId.MISMATCHED_QUOTES, -1);
        }
        
        final List<String> resultValues = new ArrayList<String>();
        for (final PropertyValue value : values) {
            
            if (value.getOperator() != null) {
                throw new CSSParseException(LangId.UNEXPECTED_OPERATOR, -1, value.getOperator().getExternalName()); 
            }
            
            final CSSPrimitiveUnit type = value.getPrimitiveTypeN();
            if (type == CSSPrimitiveUnit.CSS_STRING) {
                resultValues.add(value.getStringValue());
            } else if (type == CSSPrimitiveUnit.CSS_URI) {
                throw new CSSParseException(LangId.INVALID_VALUE, -1, "uri", "quotes");
            } else if (value.getPropertyValueType() == PropertyValueImp.VALUE_TYPE_FUNCTION) {
                throw new CSSParseException(LangId.FUNCTION_NOT_SUPPORTED, -1, value.getFunction().getName());
            } else if (type == CSSPrimitiveUnit.CSS_IDENT) {
                throw new CSSParseException(LangId.INVALID_VALUE, -1, "identifier", "quotes");
            } else {
                throw new CSSParseException(LangId.INVALID_VALUE, -1, value.getCssText(), "quotes");
            }
        }
        
        if (resultValues.size() > 0) {
            return Collections.singletonList(
                    new PropertyDeclaration(CSSName.QUOTES, new PropertyValueImp(resultValues), important, origin));
        } else {
            return Collections.emptyList();
        }
    }
}
