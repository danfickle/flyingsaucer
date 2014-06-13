/*
 * {{{ header & license
 * ValueConstants.java
 * Copyright (c) 2004, 2005 Patrick Wright
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
package org.xhtmlrenderer.css.constants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.css.CSSValue;
import org.xhtmlrenderer.css.parser.PropertyValue;
import org.xhtmlrenderer.util.GeneralUtil;

// TODO: Fix up below and move to CSSPrimitiveUnit enum.

/**
 * Utility class for working with <code>CSSValue</code> instances.
 *
 * @author empty
 */
public final class ValueConstants {

    private static final Logger LOGGER = LoggerFactory.getLogger(ValueConstants.class);

    public static String stringForSACPrimitiveType(final CSSPrimitiveUnit type) {
        return type.toString();
    }

    /**
     * Returns true if the specified type absolute (even if we have a computed
     * value for it), meaning that either the value can be used directly (e.g.
     * pixels) or there is a fixed context-independent conversion for it (e.g.
     * inches). Proportional types (e.g. %) return false.
     *
     * @param type The CSSValue type to check.
     * @return See desc.
     */
    //TODO: method may be unnecessary (tobe)
    public static boolean isAbsoluteUnit(final CSSPrimitiveUnit type) {
        // TODO: check this list...

        // note, all types are included here to make sure none are missed
        switch (type) {
            // proportional length or size
            case CSS_PERCENTAGE:
                return false;
                // refer to values known to the DerivedValue instance (tobe)
            case CSS_EMS:
            case CSS_EXS:
                // length
            case CSS_IN:
            case CSS_CM:
            case CSS_MM:
            case CSS_PT:
            case CSS_PC:
            case CSS_PX:

                // color
            case CSS_RGBCOLOR:

                // ?
            case CSS_ATTR:
            case CSS_DIMENSION:
            case CSS_NUMBER:
            case CSS_RECT:

                // counters
            case CSS_COUNTER:

                // angles
            case CSS_DEG:
            case CSS_GRAD:
            case CSS_RAD:

                // aural - freq
            case CSS_HZ:
            case CSS_KHZ:

                // time
            case CSS_S:
            case CSS_MS:

                // URI
            case CSS_URI:

            case CSS_IDENT:
            case CSS_STRING:
                return true;
            case CSS_UNKNOWN:
                LOGGER.warn("Asked whether type was absolute, given CSS_UNKNOWN as the type. " +
                        "Might be one of those funny values like background-position.");
                GeneralUtil.dumpShortException(new Exception());
                // fall-through
            default:
                return false;
        }
    }

    /**
     * Gets the cssValueTypeDesc attribute of the {@link CSSValue} object
     *
     * @param cssValue PARAM
     * @return The cssValueTypeDesc value
     */
    public static String getCssValueTypeDesc(final PropertyValue cssValue) {
        switch (cssValue.getCssValueTypeN()) {
            case CSS_CUSTOM:
                return "CSS_CUSTOM";
            case CSS_INHERIT:
                return "CSS_INHERIT";
            case CSS_PRIMITIVE_VALUE:
                return "CSS_PRIMITIVE_VALUE";
            case CSS_VALUE_LIST:
                return "CSS_VALUE_LIST";
            default:
                return "UNKNOWN";
        }
    }

    /**
     * Returns true if the SAC primitive value type is a number unit--a unit
     * that can only contain a numeric value. This is a shorthand way of saying,
     * did the user declare this as a number unit (like px)?
     *
     * @param cssPrimitiveType PARAM
     * @return See desc.
     */
    public static boolean isNumber(final CSSPrimitiveUnit cssPrimitiveType) {
        switch (cssPrimitiveType) {
            // fall thru on all these
            // relative length or size
            case CSS_EMS:
            case CSS_EXS:
            case CSS_PERCENTAGE:
                // relatives will be treated separately from lengths;
                return false;
                // length
            case CSS_PX:
            case CSS_IN:
            case CSS_CM:
            case CSS_MM:
            case CSS_PT:
            case CSS_PC:
                return true;
            default:
                return false;
        }
    }
}
