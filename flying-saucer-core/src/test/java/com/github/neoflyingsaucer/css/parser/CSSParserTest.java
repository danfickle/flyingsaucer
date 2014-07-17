package com.github.neoflyingsaucer.css.parser;

import org.junit.Test;
import org.xhtmlrenderer.css.parser.CSSErrorHandler;
import org.xhtmlrenderer.css.parser.CSSParser;
import org.xhtmlrenderer.css.sheet.StylesheetInfo.CSSOrigin;
import org.xhtmlrenderer.util.LangId;

/**
 * This class should be used to run expected successful 
 * operations of the css parser.
 */
public class CSSParserTest
{
	private void parseDeclaration(String declaration)
	{
		CSSParser parser = new CSSParser(new CSSErrorHandler() {
			@Override
			public void error(String uri, int line, LangId msgId, Object... args) {
				throw new RuntimeException(msgId.toString());
			}
		}, null);
		
		parser.parseDeclaration("", CSSOrigin.AUTHOR, declaration); 
	}

	@Test
	public void testRelativeUrlBackgroundImage()
	{
		parseDeclaration("background-image:url(test.png)");
		parseDeclaration("background-image: url('test.png')");
		parseDeclaration("background-image :url(\"test.png\")");
	}
	
	@Test
	public void testAbsoluteUrlBackgroundImage()
	{
		parseDeclaration("background-image:url(http://example.com/test.png)");
		parseDeclaration("background-image: url('http://example.com/test.png')");
		parseDeclaration("background-image :url(\"http://example.com/test.png\")");
	}
	
	@Test
	public void testLinearGradientBackgroundImage()
	{
		parseDeclaration("background-image: linear-gradient(to top, red, blue);");
	}
}
