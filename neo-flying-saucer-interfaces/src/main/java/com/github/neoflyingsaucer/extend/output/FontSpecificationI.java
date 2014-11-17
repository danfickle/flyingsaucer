package com.github.neoflyingsaucer.extend.output;

public interface FontSpecificationI 
{
    public float getSize();
    public int getFontWeight();
    public String[] getFamilies();

    public static enum FontStyle
    {
    	NORMAL, ITALIC, OBLIQUE;
    }
    
    public FontStyle getStyle();

    public static enum FontVariant
    {
    	NORMAL, SMALL_CAPS;
    }
    
    public FontVariant getVariant();
}
