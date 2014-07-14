package com.github.neoflyingsaucer.css.parser;

import java.util.Locale;
import java.util.ResourceBundle;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.xhtmlrenderer.css.parser.CSSErrorHandler;
import org.xhtmlrenderer.css.parser.CSSParser;
import org.xhtmlrenderer.css.sheet.StylesheetInfo.CSSOrigin;
import org.xhtmlrenderer.layout.SharedContext;

public class CSSParserTest
{
	@Rule
	public ExpectedException expected = ExpectedException.none();
	
	private void parseDeclaration(String declaration)
	{
		SharedContext.ERRS.set(ResourceBundle.getBundle("languages.ErrorMessages", Locale.US));

		CSSParser parser = new CSSParser(new CSSErrorHandler() {
			@Override
			public void error(String uri, String message) {
				throw new RuntimeException(message);
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
	
	@Test
	public void testWebkitGradientBackgroundImage()
	{
		expected.expect(RuntimeException.class);
		expected.expectMessage("Function (-webkit-gradient) not supported");
		parseDeclaration("background-image: -webkit-gradient(0);");
	}
	
	@Test
	public void testRadialGradientBackgroundImage()
	{
		expected.expect(RuntimeException.class);
		expected.expectMessage("Function (radial-gradient) not supported");
		parseDeclaration("background-image: radial-gradient(0);");
	}
}
