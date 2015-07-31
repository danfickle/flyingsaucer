package com.github.neoflyingsaucer.css.parser.property;

import java.util.EnumSet;

import com.github.neoflyingsaucer.css.constants.IdentValue;

import static com.github.neoflyingsaucer.css.constants.IdentValue.*;

public class IdentSet 
{
    public static final EnumSet<IdentValue> BACKGROUND_SIZES = EnumSet.of(
            IdentValue.AUTO, IdentValue.CONTAIN, IdentValue.COVER);
	
	// none | hidden | dotted | dashed | solid | double | groove | ridge | inset | outset
    public static final EnumSet<IdentValue> BORDER_STYLES = EnumSet.of(
    		NONE, HIDDEN, DOTTED, DASHED, SOLID, DOUBLE, GROOVE, RIDGE,
    		INSET, OUTSET);
	
	// thin | medium | thick
    public static final EnumSet<IdentValue> BORDER_WIDTHS = EnumSet.of(
    		THIN, MEDIUM, THICK);

    // normal | small-caps | inherit
    public static final EnumSet<IdentValue> FONT_VARIANTS = EnumSet.of(
    		NORMAL, SMALL_CAPS);

    // normal | italic | oblique | inherit
    public static final EnumSet<IdentValue> FONT_STYLES = EnumSet.of(
    		NORMAL, ITALIC, OBLIQUE);

    public static final EnumSet<IdentValue> FONT_WEIGHTS = EnumSet.of(
    		NORMAL, BOLD, BOLDER, LIGHTER);

    public static final EnumSet<IdentValue> PAGE_ORIENTATIONS = EnumSet.of(
    		AUTO, PORTRAIT, LANDSCAPE);

    // inside | outside | inherit
    public static final EnumSet<IdentValue> LIST_STYLE_POSITIONS = EnumSet.of(
            INSIDE, OUTSIDE);

    // disc | circle | square | decimal
    // | decimal-leading-zero | lower-roman | upper-roman
    // | lower-greek | lower-latin | upper-latin | armenian
    // | georgian | lower-alpha | upper-alpha | none | inherit
    public static final EnumSet<IdentValue> LIST_STYLE_TYPES = EnumSet.of(
            DISC, CIRCLE, SQUARE, DECIMAL, DECIMAL_LEADING_ZERO,
            LOWER_ROMAN, UPPER_ROMAN, LOWER_GREEK, LOWER_LATIN,
            UPPER_LATIN, ARMENIAN, GEORGIAN, LOWER_ALPHA,
            UPPER_ALPHA, NONE);

    // repeat | repeat-x | repeat-y | no-repeat | inherit
    public static final EnumSet<IdentValue> BACKGROUND_REPEATS = EnumSet.of(
                    REPEAT, REPEAT_X, REPEAT_Y, NO_REPEAT);

    // scroll | fixed | inherit
    public static final EnumSet<IdentValue> BACKGROUND_ATTACHMENTS = EnumSet.of(
            SCROLL, FIXED);

    // left | right | top | bottom | center
    public static final EnumSet<IdentValue> BACKGROUND_POSITIONS = EnumSet.of(
                    LEFT, RIGHT, TOP, BOTTOM, CENTER);

    public static final EnumSet<IdentValue> ABSOLUTE_FONT_SIZES = EnumSet.of(
                    XX_SMALL, X_SMALL, SMALL, MEDIUM, LARGE, X_LARGE, XX_LARGE);

    public static final EnumSet<IdentValue> RELATIVE_FONT_SIZES = EnumSet.of(
                    SMALLER, LARGER);
}
