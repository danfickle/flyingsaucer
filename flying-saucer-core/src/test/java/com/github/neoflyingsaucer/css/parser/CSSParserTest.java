package com.github.neoflyingsaucer.css.parser;

import org.junit.Test;

import static org.junit.Assert.*;

import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.parser.CSSErrorHandler;
import org.xhtmlrenderer.css.parser.CSSParser;
import org.xhtmlrenderer.css.parser.PropertyValue;
import org.xhtmlrenderer.css.sheet.Ruleset;
import org.xhtmlrenderer.css.sheet.StylesheetInfo.CSSOrigin;
import org.xhtmlrenderer.util.LangId;

/**
 * This class should be used to run expected successful 
 * operations of the css parser.
 */
public class CSSParserTest
{
	private Ruleset parseDeclaration(String declaration)
	{
		CSSParser parser = new CSSParser(new CSSErrorHandler() {
			@Override
			public void error(String uri, int line, LangId msgId, Object... args) {
				throw new RuntimeException(msgId.toString());
			}
		}, null);
		
		return parser.parseDeclaration("", CSSOrigin.AUTHOR, declaration); 
	}
	
	private static int declSize(Ruleset rs)
	{
		return rs.getPropertyDeclarations().size();
	}

	private static PropertyValue firstValue(Ruleset rs)
	{
		return rs.getPropertyDeclarations().get(0).getValue();
	}
	
	private static CSSName firstProperty(Ruleset rs)
	{
		return rs.getPropertyDeclarations().get(0).getCSSName();
	}
	
	@Test
	public void testRelativeUrlBackgroundImage()
	{
		Ruleset rs;
		
		rs = parseDeclaration("background-image:url(test.png)");
		assertEquals(1, declSize(rs));
		assertEquals(CSSName.BACKGROUND_IMAGE, firstProperty(rs));
		assertEquals("test.png", firstValue(rs).getStringValue());
	
		rs = parseDeclaration("background-image: url('test.png')");
		assertEquals(1, declSize(rs));
		assertEquals(CSSName.BACKGROUND_IMAGE, firstProperty(rs));
		assertEquals("test.png", firstValue(rs).getStringValue());

		rs = parseDeclaration("background-image :url(\"test.png\")");
		assertEquals(1, declSize(rs));
		assertEquals(CSSName.BACKGROUND_IMAGE, firstProperty(rs));
		assertEquals("test.png", firstValue(rs).getStringValue());
	}
	
	@Test
	public void testAbsoluteUrlBackgroundImage()
	{
		Ruleset rs;
		
		rs = parseDeclaration("background-image:url(http://example.com/test.png)");
		assertEquals(1, declSize(rs));
		assertEquals(CSSName.BACKGROUND_IMAGE, firstProperty(rs));
		assertEquals("http://example.com/test.png", firstValue(rs).getStringValue());
		
		rs = parseDeclaration("background-image: url('http://example.com/test.png')");
		assertEquals(1, declSize(rs));
		assertEquals(CSSName.BACKGROUND_IMAGE, firstProperty(rs));
		assertEquals("http://example.com/test.png", firstValue(rs).getStringValue());
		
		rs = parseDeclaration("background-image :url(\"http://example.com/test.png\")");
		assertEquals(1, declSize(rs));
		assertEquals(CSSName.BACKGROUND_IMAGE, firstProperty(rs));
		assertEquals("http://example.com/test.png", firstValue(rs).getStringValue());
	}
	
	@Test
	public void testLinearGradientBackgroundImage()
	{
		Ruleset rs = parseDeclaration("background-image: linear-gradient(to top, red, blue);");
		assertEquals(1, declSize(rs));
		assertEquals(CSSName.BACKGROUND_IMAGE, firstProperty(rs));
		assertEquals("linear-gradient", firstValue(rs).getFunction().getName());

		// NOTE: Currently each token in a function is reported as a parameter.
		assertEquals(4, firstValue(rs).getFunction().getParameters().size());
		assertEquals("[to, top, red, blue]", firstValue(rs).getFunction().getParameters().toString());
	}
	
	@Test
	public void testLinearGradientBackground()
	{
		parseDeclaration("background: linear-gradient(to top, red, blue);");
	}
}
