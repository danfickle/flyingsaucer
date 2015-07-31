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
import java.util.Collections;
import java.util.List;

import com.github.neoflyingsaucer.css.constants.CSSName;
import com.github.neoflyingsaucer.css.constants.CSSPrimitiveUnit;
import com.github.neoflyingsaucer.css.parser.CSSParseException;
import com.github.neoflyingsaucer.css.parser.CounterData;
import com.github.neoflyingsaucer.css.parser.PropertyValue;
import com.github.neoflyingsaucer.css.parser.PropertyValueImp;
import com.github.neoflyingsaucer.css.parser.PropertyValueImp.CSSValueType;
import com.github.neoflyingsaucer.css.sheet.PropertyDeclaration;
import com.github.neoflyingsaucer.css.sheet.StylesheetInfo.CSSOrigin;
import com.github.neoflyingsaucer.extend.controller.error.LangId;

import static com.github.neoflyingsaucer.css.parser.property.BuilderUtil.*;

public abstract class CounterPropertyBuilder implements PropertyBuilder {
    // [ <identifier> <integer>? ]+ | none | inherit 
    
    protected abstract int getDefaultValue();
    
    // XXX returns a PropertyValue of type VALUE_TYPE_LIST, but the List contains
    // CounterData objects and not PropertyValue objects
    public List<PropertyDeclaration> buildDeclarations(final CSSName cssName, final List<PropertyValue> values, final CSSOrigin origin, final boolean important, final boolean inheritAllowed) {
        if (values.size() == 1) {
            final PropertyValue value = (PropertyValue)values.get(0);
            
            checkInheritAllowed(value, inheritAllowed);
            
            if (value.getCssValueTypeN() == CSSValueType.CSS_INHERIT) {
                return Collections.singletonList(new PropertyDeclaration(cssName, value, important, origin));
            } else if (value.getPrimitiveTypeN() == CSSPrimitiveUnit.CSS_IDENT) {
                if (value.getCssText().equals("none")) {
                    return Collections.singletonList(new PropertyDeclaration(cssName, value, important, origin));
                } else {
                    final CounterData data = new CounterData(
                            value.getStringValue(),
                            getDefaultValue());
                    
                    return Collections.singletonList(
                            new PropertyDeclaration(cssName, new PropertyValueImp(
                                    Collections.singletonList(data)), important, origin));
                }
            }
            
            throw new CSSParseException(LangId.INVALID_SYNTAX, -1, cssName);
        } else {
            final List<CounterData> result = new ArrayList<CounterData>();
            for (int i = 0; i < values.size(); i++) {
                final PropertyValue value = (PropertyValue)values.get(i);
                
                if (value.getPrimitiveTypeN() == CSSPrimitiveUnit.CSS_IDENT) {
                    final String name = value.getStringValue();
                    int cValue = getDefaultValue();
                    
                    if (i < values.size() - 1) {
                        final PropertyValue next = (PropertyValue)values.get(i+1);
                        if (next.getPrimitiveTypeN() == CSSPrimitiveUnit.CSS_NUMBER) {
                            checkNumberIsInteger(cssName, next);
                            
                            cValue = (int)next.getFloatValue();
                        } 
                        
                        i++;
                    }
                    result.add(new CounterData(name, cValue));
                } else {
                    throw new CSSParseException(LangId.INVALID_SYNTAX, -1, cssName);
                }
            }
            
            return Collections.singletonList(
                    new PropertyDeclaration(cssName, new PropertyValueImp(result), important, origin));
        }
    }
    
    private void checkNumberIsInteger(final CSSName cssName, final PropertyValue value) {
        if ((int)value.getFloatValue() !=
                    Math.round(value.getFloatValue())) {
            throw new CSSParseException(LangId.MUST_BE_INT, -1, cssName);
        }
    }
    
    public static class CounterReset extends CounterPropertyBuilder {
        protected int getDefaultValue() {
            return 0;
        }
    }

    public static class CounterIncrement extends CounterPropertyBuilder {
        protected int getDefaultValue() {
            return 1;
        }
    }
}
