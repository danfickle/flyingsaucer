package com.github.neoflyingsaucer.pdfout;

import com.github.neoflyingsaucer.extend.output.FSFont;
import com.github.neoflyingsaucer.pdfout.PdfFontResolver.FontDescription;

public class PdfFont implements FSFont
{
    private final FontDescription _font;
    private final float _size;
	
	public PdfFont(FontDescription result, float size) 
	{
		_font = result;
		_size = size;
	}

	@Override
    public float getSize2D() {
		return _size;
    }
    
    public FontDescription getFontDescription() {
        return _font;
    }
}
