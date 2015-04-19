package com.github.neoflyingsaucer.extend.output;

public interface FSFontFaceItem
{
	public String getFontFamily();
	public byte[] getFontBytes();
	public String getEncoding();
	public int getWeight();
	public FontSpecificationI getSpecification();
}
