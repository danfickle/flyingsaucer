package com.github.neoflyingsaucer.css.parser;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.xhtmlrenderer.css.parser.CSSErrorHandler;
import org.xhtmlrenderer.css.parser.CSSParser;
import org.xhtmlrenderer.css.sheet.StylesheetInfo.CSSOrigin;
import org.xhtmlrenderer.util.LangId;

/**
 * This class should be used to run JUnit tests on expected
 * errors in the css parser.
 */
public class CSSParserFailureTest 
{
	@Rule
	public ExpectedException expected = ExpectedException.none();
	
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
	public void testWebkitGradientBackgroundImage()
	{
		expected.expect(RuntimeException.class);
		expected.expectMessage(LangId.FUNCTION_NOT_SUPPORTED.toString());
		parseDeclaration("background-image: -webkit-gradient(0);");
	}
	
	@Test
	public void testRadialGradientBackgroundImage()
	{
		expected.expect(RuntimeException.class);
		expected.expectMessage(LangId.FUNCTION_NOT_SUPPORTED.toString());
		parseDeclaration("background-image: radial-gradient(0);");
	}
}
