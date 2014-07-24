package com.github.neoflyingsaucer.css.parser;

import org.junit.Test;
import org.xhtmlrenderer.css.parser.CSSErrorHandler;
import org.xhtmlrenderer.css.parser.CSSParser;
import org.xhtmlrenderer.css.sheet.Ruleset;
import org.xhtmlrenderer.css.sheet.StylesheetInfo.CSSOrigin;
import org.xhtmlrenderer.util.LangId;

public class SimpleDeclarationTest
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
	public void testColor()
	{
		parseDeclaration("color: #fff;");
		parseDeclaration("color: #ffffff;");
		parseDeclaration("color: #000000;");
		
		parseDeclaration("color: rgb(1, 2, 3);");
		parseDeclaration("color: rgb(50%, 50%, 50%);");
		parseDeclaration("color: rgb(255, 255, 255);");

		parseDeclaration("color: rgba(1, 2, 3, 0.5);");
		parseDeclaration("color: rgba(50%, 50%, 50%, 50%);");
		parseDeclaration("color: rgba(255, 255, 255, 0.2);");
		
		parseDeclaration("color: red;");
		parseDeclaration("color: blue;");
		parseDeclaration("color: green;");
		parseDeclaration("color: transparent;");
		parseDeclaration("color: inherit;");
		
//		parseDeclaration("color: initial;");
//		parseDeclaration("color: hsla(50, 33%, 25%, 0.75);");
//		parseDeclaration("color: hsl(50, 33%, 25%);");
//		parseDeclaration("color: currentcolor;");
	}
	
	@Test
	public void testColorUsage()
	{
		parseDeclaration("background-color: transparent;");
		parseDeclaration("border-color: transparent;");
		parseDeclaration("border-left-color: transparent;");
		parseDeclaration("border-right-color: transparent;");
		parseDeclaration("border-top-color: transparent;");
		parseDeclaration("border-bottom-color: transparent;");
	}
	
	@Test
	public void testBackgroundRepeat()
	{
		parseDeclaration("background-repeat: no-repeat;");
		parseDeclaration("background-repeat: repeat;");
		parseDeclaration("background-repeat: repeat-x;");
		parseDeclaration("background-repeat: repeat-y;");
		parseDeclaration("background-repeat: inherit;");
		
//		parseDeclaration("background-repeat: space;");
//		parseDeclaration("background-repeat: round;");
	}

	public void testBackgroundPosition()
	{
		parseDeclaration("background-position: 50% 50%;");
		parseDeclaration("background-position: 50%;");
		parseDeclaration("background-position: 50px 50px;");
		parseDeclaration("background-position: top right;");
		parseDeclaration("background-position: bottom left;");
		parseDeclaration("background-position: top;");
		parseDeclaration("background-position: inherit;");
	}
}
