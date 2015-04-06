package com.github.neoflyingsaucer.test;

import org.junit.Test;

import com.github.neoflyingsaucer.test.support.BufferedImageTest;

public class FloatTest
{
	@Test
	public void testClearLeft()
	{
		String html =
			"<html><head><style>" +
			"@page { size: 4px 4px; margin: 0; }" +
			"body { background-color: #0f0; margin: 0; }" +
			"div#1 { float:right; width: 1px; background-color: #f00; height: 3px; }" +
			"div#2 { float:left; width: 1px; background-color: #00f; height: 2px; }" +
			"div#3 { clear: left; width: 2px; background-color: #00f; height: 1px; }" +
			"</style></head><body><div id=1></div><div id=2></div><div id=3></div></body></html>";
		
		String expected = 
			"BGGR" +
			"BGGR" +
			"BBGR" +
			"GGGG";

		BufferedImageTest.assertImgEquals(html, expected, 4, 0, "ClearLeft");
	}

	@Test
	public void testClearBoth()
	{
		String html =
			"<html><head><style>" +
			"@page { size: 4px 4px; margin: 0; }" +
			"body { background-color: #0f0; margin: 0; }" +
			"div#1 { float:right; width: 1px; background-color: #f00; height: 3px; }" +
			"div#2 { float:left; width: 1px; background-color: #00f; height: 3px; }" +
			"div#3 { clear: both; width: 3px; background-color: #00f; height: 1px; }" +
			"</style></head><body><div id=1></div><div id=2></div><div id=3></div></body></html>";
		
		String expected = 
			"BGGR" +
			"BGGR" +
			"BGGR" +
			"BBBG";

		BufferedImageTest.assertImgEquals(html, expected, 4, 0, "ClearBoth");
	}

	@Test
	public void testFloatLeftAndRight()
	{
		String html =
			"<html><head><style>" +
			"@page { size: 4px 4px; margin: 0; }" +
			"body { background-color: #0f0; margin: 0; }" +
			"div#1 { float:right; width: 1px; background-color: #f00; height: 3px; }" +
			"div#2 { float:left; width: 1px; background-color: #00f; height: 3px; }" +
			"</style></head><body><div id=1></div><div id=2></div></body></html>";
		
		String expected = 
			"BGGR" +
			"BGGR" +
			"BGGR" +
			"GGGG";

		BufferedImageTest.assertImgEquals(html, expected, 4, 0, "FloatLeftAndRight");
	}

	@Test
	public void testFloatLeftWrap()
	{
		String html =
			"<html><head><style>" +
			"@page { size: 4px 6px; margin: 0; }" +
			"body { background-color: #0f0; margin: 0; }" +
			"#1 { float:left; width: 1px; background-color: #f00; height: 3px; }" +
			"#2 { float:left; width: 1px; background-color: #00f; height: 3px; }" +
			"#3 { float:left; width: 1px; background-color: #f00; height: 3px; }" +
			"#4 { float:left; width: 1px; background-color: #00f; height: 3px; }" +
			"#5 { float:left; width: 1px; background-color: #f00; height: 3px; }" +
			"#6 { float:left; width: 1px; background-color: #00f; height: 3px; }" +
			"</style></head><body><div id=\"1\"></div><div id=\"2\"></div><div id=\"3\"></div>" +
			"<div id=\"4\"></div><div id=\"5\"></div><div id=\"6\"></div></body></html>";
		
		String expected = 
			"RBRB" +
			"RBRB" +
			"RBRB" +
			"RBGG" +
			"RBGG" +
			"RBGG";

		BufferedImageTest.assertImgEquals(html, expected, 4, 0, "FloatLeftWrap");
	}

	@Test
	public void testFloatRightWrap()
	{
		String html =
			"<html><head><style>" +
			"@page { size: 4px 6px; margin: 0; }" +
			"body { background-color: #0f0; margin: 0; }" +
			"#1 { float:right; width: 1px; background-color: #f00; height: 3px; }" +
			"#2 { float:right; width: 1px; background-color: #00f; height: 3px; }" +
			"#3 { float:right; width: 1px; background-color: #f00; height: 3px; }" +
			"#4 { float:right; width: 1px; background-color: #00f; height: 3px; }" +
			"#5 { float:right; width: 1px; background-color: #f00; height: 3px; }" +
			"#6 { float:right; width: 1px; background-color: #00f; height: 3px; }" +
			"</style></head><body><div id=\"1\"></div><div id=\"2\"></div><div id=\"3\"></div>" +
			"<div id=\"4\"></div><div id=\"5\"></div><div id=\"6\"></div></body></html>";
		
		String expected = 
			"BRBR" +
			"BRBR" +
			"BRBR" +
			"GGBR" +
			"GGBR" +
			"GGBR";

		BufferedImageTest.assertImgEquals(html, expected, 4, 0, "FloatRightWrap");
	}

	@Test
	public void testFloatRight()
	{
		String html =
			"<html><head><style>" +
			"@page { size: 4px 4px; margin: 0; }" +
			"body { background-color: #0f0; margin: 0; }" +
			"div { float:right; width: 1px; background-color: #f00; height: 3px; }" +
			"</style></head><body><div></div></body></html>";
		
		String expected = 
			"GGGR" +
			"GGGR" +
			"GGGR" +
			"GGGG";

		BufferedImageTest.assertImgEquals(html, expected, 4, 0, "FloatRight");
	}
	
	@Test
	public void testFloatLeftAcrossPages()
	{
		String html =
			"<html><head><style>" +
			"@page { size: 4px 2px; margin: 0; }" +
			"body { background-color: #0f0; margin: 0; }" +
			"div { float: left; width: 1px; height: 3px; background-color: #f00; }" +
			"</style></head><body><div></div></body></html>";
		
		String expected = 
			"RGGG" +
			"RGGG";

		BufferedImageTest.assertImgEquals(html, expected, 4, 0, "FloatLeftAcrossPages.1");

		expected = 
			"RGGG" +
			"GGGG";

		BufferedImageTest.assertImgEquals(html, expected, 4, 1, "FloatLeftAcrossPages.2");
	}
}
