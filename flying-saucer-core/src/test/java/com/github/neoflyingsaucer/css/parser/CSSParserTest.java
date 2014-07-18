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
	
	private void parseMediaQuery(String query)
	{
		CSSParser parser = new CSSParser(new CSSErrorHandler() {
			@Override
			public void error(String uri, int line, LangId msgId, Object... args) {
				throw new RuntimeException(msgId.toString());
			}
		}, null);
		
		parser.parseMediaQueryListInternal(query); 
	}
	
	
	@Test
	public void testCss3MediaQueries()
	{
		parseMediaQuery("not print");
		parseMediaQuery("(min-width: 100px)");
		parseMediaQuery("screen and (min-width: 600px)");
		parseMediaQuery("(min-width: 1000px) and (color)");
		parseMediaQuery("(min-aspect-ratio: 1 / 1) and (min-device-height: 1.2cm)");
		parseMediaQuery("(color) , (monochrome)");
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
