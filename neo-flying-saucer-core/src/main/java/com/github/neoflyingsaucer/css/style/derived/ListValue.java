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
package com.github.neoflyingsaucer.css.style.derived;

import java.util.List;

import com.github.neoflyingsaucer.css.constants.CSSName;
import com.github.neoflyingsaucer.css.parser.PropertyValue;
import com.github.neoflyingsaucer.css.style.DerivedValue;

public class ListValue extends DerivedValue {
    private final List<?> _values;
    
    public ListValue(final CSSName name, final PropertyValue value) {
        super(name, value.getPrimitiveTypeN(), value.getCssText(), value.getCssText());
        
        _values = value.getValues();
    }
    
    public List<?> getValues() {
        return _values;
    }
    
    public String[] asStringArray() {
        if (_values == null || _values.isEmpty()) {
            return new String[0];
        }
        
        final String[] arr = new String[_values.size()];
        int i = 0;
        
        for (final Object value : _values) {
            arr[i++] = value.toString();
        }
        
        return arr;
    }
}
