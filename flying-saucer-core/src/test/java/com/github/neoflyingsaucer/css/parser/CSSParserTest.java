package com.github.neoflyingsaucer.css.parser;

import org.junit.Test;
import static org.junit.Assert.*;
import org.xhtmlrenderer.css.parser.CSSErrorHandler;
import org.xhtmlrenderer.css.parser.CSSParser;
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
	
	@Test
	public void testRelativeUrlBackgroundImage()
	{
		Ruleset rs;
		
		rs = parseDeclaration("background-image:url(test.png)");
		assertEquals(1, rs.getPropertyDeclarations().size());
		assertEquals("background-image", rs.getPropertyDeclarations().get(0).getPropertyName());
		assertEquals("test.png", rs.getPropertyDeclarations().get(0).getValue().getStringValue());
	
		rs = parseDeclaration("background-image: url('test.png')");
		assertEquals(1, rs.getPropertyDeclarations().size());
		assertEquals("test.png", rs.getPropertyDeclarations().get(0).getValue().getStringValue());

		rs = parseDeclaration("background-image :url(\"test.png\")");
		assertEquals(1, rs.getPropertyDeclarations().size());
		assertEquals("test.png", rs.getPropertyDeclarations().get(0).getValue().getStringValue());
	}
	
	@Test
	public void testAbsoluteUrlBackgroundImage()
	{
		Ruleset rs;
		
		rs = parseDeclaration("background-image:url(http://example.com/test.png)");
		assertEquals(1, rs.getPropertyDeclarations().size());
		assertEquals("http://example.com/test.png", rs.getPropertyDeclarations().get(0).getValue().getStringValue());
		
		rs = parseDeclaration("background-image: url('http://example.com/test.png')");
		assertEquals(1, rs.getPropertyDeclarations().size());
		assertEquals("http://example.com/test.png", rs.getPropertyDeclarations().get(0).getValue().getStringValue());
		
		rs = parseDeclaration("background-image :url(\"http://example.com/test.png\")");
		assertEquals(1, rs.getPropertyDeclarations().size());
		assertEquals("http://example.com/test.png", rs.getPropertyDeclarations().get(0).getValue().getStringValue());	}
	
	@Test
	public void testLinearGradientBackgroundImage()
	{
		Ruleset rs = parseDeclaration("background-image: linear-gradient(to top, red, blue);");
		assertEquals(1, rs.getPropertyDeclarations().size());
		assertEquals("linear-gradient", rs.getPropertyDeclarations().get(0).getValue().getFunction().getName());
		// NOTE: Curretly each token in a function is reported as a parameter.
		assertEquals(4, rs.getPropertyDeclarations().get(0).getValue().getFunction().getParameters().size());
		assertEquals("[to, top, red, blue]", rs.getPropertyDeclarations().get(0).getValue().getFunction().getParameters().toString());
	}
}
