package com.github.neoflyingsaucer.css.constants;

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
		String u = unit.toLowerCase(Locale.US);

		if (u.equals("px"))
			return CSSPrimitiveUnit.CSS_PX;

		if (u.equals("%"))
			return CSSPrimitiveUnit.CSS_PERCENTAGE;
		
		if (u.equals("pt"))
			return CSSPrimitiveUnit.CSS_PT;
		
		if (u.equals("em"))
			return CSSPrimitiveUnit.CSS_EMS;
		
		if (u.equals("ex"))
			return CSSPrimitiveUnit.CSS_EXS;
		
		if (u.equals("rem"))
			return CSSPrimitiveUnit.CSS_REMS;
		
		if (u.equals("ch"))
			return CSSPrimitiveUnit.CSS_CHS;
		
		if (u.equals("vw"))
			return CSSPrimitiveUnit.CSS_VW;
		
		if (u.equals("vh"))
			return CSSPrimitiveUnit.CSS_VH;
		
		if (u.equals("vmin"))
			return CSSPrimitiveUnit.CSS_VMIN;
		
		if (u.equals("vmax"))
			return CSSPrimitiveUnit.CSS_VMAX;

		if (u.equals("cm"))
			return CSSPrimitiveUnit.CSS_CM;
		
		if (u.equals("mm"))
			return CSSPrimitiveUnit.CSS_MM;
		
		if (u.equals("in"))
			return CSSPrimitiveUnit.CSS_IN;
		
		if (u.equals("pc"))
			return CSSPrimitiveUnit.CSS_PC;

		if (u.equals("deg"))
			return CSSPrimitiveUnit.CSS_DEG;
		
		if (u.equals("rad"))
			return CSSPrimitiveUnit.CSS_RAD;
		
		if (u.equals("turn"))
			return CSSPrimitiveUnit.CSS_TURN;
		
		if (u.equals("grad"))
			return CSSPrimitiveUnit.CSS_GRAD;
	
		if (u.equals("hz"))
			return CSSPrimitiveUnit.CSS_HZ;
		
		if (u.equals("khz"))
			return CSSPrimitiveUnit.CSS_KHZ;
		
		if (u.equals("dpi"))
			return CSSPrimitiveUnit.CSS_DPI;
		
		if (u.equals("dpcm"))
			return CSSPrimitiveUnit.CSS_DPCM;
		
		if (u.equals("dppx"))
			return CSSPrimitiveUnit.CSS_DPPX;
		
		return CSSPrimitiveUnit.CSS_UNKNOWN;
	}
}
