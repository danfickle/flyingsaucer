package com.github.neoflyingsaucer.pdfout;

import org.xhtmlrenderer.css.value.FontSpecification;
import org.xhtmlrenderer.extend.FontResolver;
import org.xhtmlrenderer.layout.SharedContext;
import org.xhtmlrenderer.render.FSFont;

public class PdfFontResolver implements FontResolver {

	@Override
	public FSFont resolveFont(SharedContext renderingContext,
			FontSpecification spec) {
		// TODO Auto-generated method stub
		return new FSFont() {
			
			@Override
			public float getSize2D() {
				// TODO Auto-generated method stub
				return 10;
			}
		};
	}

	@Override
	public void flushCache() {
		// TODO Auto-generated method stub

	}

}
