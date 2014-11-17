package com.github.neoflyingsaucer.css.parser;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.xhtmlrenderer.css.parser.CSSErrorHandler;
import org.xhtmlrenderer.css.parser.CSSParser;
import org.xhtmlrenderer.css.sheet.StylesheetInfo.CSSOrigin;

import com.github.neoflyingsaucer.extend.useragent.LangId;

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
	
	private void assertDeclarationFailure(String declaration, LangId expectedMsgId)
	{
		expected.expect(RuntimeException.class);
		expected.expectMessage(expectedMsgId.toString());
		parseDeclaration(declaration);
	}
	
	@Test
	public void testWebkitGradientBackgroundImage()
	{
		assertDeclarationFailure(
				"background-image: -webkit-gradient(0);",
				LangId.FUNCTION_NOT_SUPPORTED);
	}
	
	@Test
	public void testRadialGradientBackgroundImage()
	{
		assertDeclarationFailure(
				"background-image: radial-gradient(closest-corner circle at 600px 600px, #001b24 0%, #000 100%);",
				LangId.FUNCTION_NOT_SUPPORTED);
	}

	@Test
	public void testRadialGradientBackground()
	{
		assertDeclarationFailure(
				"background: radial-gradient(closest-corner circle at 600px 600px, #001b24 0%, #000 100%);",
				LangId.FUNCTION_NOT_SUPPORTED);
	}
}
