package org.xhtmlrenderer.css.constants;

import java.util.Locale;


/**
 * Aims to enumerate all CSS unit types. From Webkit, not all units
 * are supported by Flyingsaucer.
 */
public enum CSSPrimitiveUnit
{
	CSS_ATTR,
	CSS_CM,
	CSS_COUNTER,
	CSS_DEG,
	CSS_DIMENSION,
	CSS_EMS,
	CSS_EXS,
	CSS_GRAD,
	CSS_HZ,
	CSS_IDENT,
	CSS_IN,
	CSS_KHZ,
	CSS_MM,
	CSS_MS,
	CSS_NUMBER,
	CSS_PC,
	CSS_PERCENTAGE,
	CSS_PT,
	CSS_PX,
	CSS_RAD,
	CSS_RECT,
	CSS_RGBCOLOR,
	CSS_S,
	CSS_STRING,
	CSS_UNKNOWN,
	CSS_URI,

	// From CSS Values and Units. Viewport-percentage Lengths (vw/vh/vmin/vmax).
    CSS_VW,
    CSS_VH,
    CSS_VMIN,
    CSS_VMAX,
    CSS_DPPX,
    CSS_DPI,
    CSS_DPCM,
    CSS_FR,
    CSS_PAIR, // We envision this being exposed as a means of getting computed style values for pairs (border-spacing/radius, background-position, etc.)
    CSS_UNICODE_RANGE,

    // These are from CSS3 Values and Units, but that isn't a finished standard yet
    CSS_TURN,
    CSS_REMS,
    CSS_CHS,

    // This is used by the CSS Shapes draft
    CSS_SHAPE,

    // Used by border images.
    CSS_QUAD,

    CSS_CALC,
    CSS_CALC_PERCENTAGE_WITH_NUMBER,
    CSS_CALC_PERCENTAGE_WITH_LENGTH,

    CSS_PROPERTY_ID,
    CSS_VALUE_ID;

	public static CSSPrimitiveUnit fromString(String unit) 
	{
		final String unitCompare = unit.toLowerCase(Locale.US);

		switch (unitCompare)
		{
		case "em":
			return CSSPrimitiveUnit.CSS_EMS;
		case "ex":
			return CSSPrimitiveUnit.CSS_EXS;
		case "rem":
			return CSSPrimitiveUnit.CSS_REMS;
		case "ch":
			return CSSPrimitiveUnit.CSS_CHS;
		case "vw":
			return CSSPrimitiveUnit.CSS_VW;
		case "vh":
			return CSSPrimitiveUnit.CSS_VH;
		case "vmin":
			return CSSPrimitiveUnit.CSS_VMIN;
		case "vmax":
			return CSSPrimitiveUnit.CSS_VMAX;
		case "cm":
			return CSSPrimitiveUnit.CSS_CM;
		case "mm":
			return CSSPrimitiveUnit.CSS_MM;
		case "in":
			return CSSPrimitiveUnit.CSS_IN;
		case "pt":
			return CSSPrimitiveUnit.CSS_PT;
		case "px":
			return CSSPrimitiveUnit.CSS_PX;
		case "pc":
			return CSSPrimitiveUnit.CSS_PC;
		case "deg":
			return CSSPrimitiveUnit.CSS_DEG;
		case "rad":
			return CSSPrimitiveUnit.CSS_RAD;
		case "turn":
			return CSSPrimitiveUnit.CSS_TURN;
		case "grad":
			return CSSPrimitiveUnit.CSS_GRAD;
		case "hz":
			return CSSPrimitiveUnit.CSS_HZ;
		case "khz":
			return CSSPrimitiveUnit.CSS_KHZ;
		case "dpi":
			return CSSPrimitiveUnit.CSS_DPI;
		case "dpcm":
			return CSSPrimitiveUnit.CSS_DPCM;
		case "dppx":
			return CSSPrimitiveUnit.CSS_DPPX;
		default:
			return CSSPrimitiveUnit.CSS_UNKNOWN;
		}
	}
}
