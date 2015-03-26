package com.github.neoflyingsaucer.extend.output;

public interface DlItem 
{
	public enum DlType
	{
		LINE,
		OPACITY,
		STROKE,
		RGBCOLOR,
		TRANSLATE,
		RECTANGLE,
		CLIP,
		SET_CLIP,
		OVAL,
		DRAW_SHAPE,
		CMYKCOLOR,
		IMAGE,
		FONT,
		STRING,
		STRING_EX,
		GLYPH_VECTOR,
		AA_OFF,
		AA_DEFAULT,
		REPLACED,
		LINEAR_GRADIENT,
		BOOKMARK;
	}
	
	public DlType getType();
}
