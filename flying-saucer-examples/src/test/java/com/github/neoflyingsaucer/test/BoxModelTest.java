package com.github.neoflyingsaucer.test;

import org.junit.Test;

import com.github.neoflyingsaucer.test.support.BufferedImageTest;

public class BoxModelTest 
{
	@Test
	public void testPageMarginSize()
	{
		String html =
			"<html><head><style>" +
			"@page { size: 4px 4px; margin: 1px; }" +
			"body { background-color: #00f; margin: 0; }" +
			"</style></head><body></body></html>";
		
		String expected = 
			"####" +
			"#BB#" +
			"#BB#" +
			"####";

		BufferedImageTest.assertImgEquals(html, expected, 4, 100, 100, 0);
	}
	
	@Test
	public void testDifferingPageMarginSizes()
	{
		String html =
			"<html><head><style>" +
			"@page { size: 7px 3px; margin: 1px 2px 0 3px; }" +
			"body { background-color: #0f0; margin: 0; }" +
			"</style></head><body></body></html>";
		
		String expected = 
			"#######" +
			"###GG##" +
			"###GG##";

		BufferedImageTest.assertImgEquals(html, expected, 7, 100, 100, 0);
	}
	
	@Test
	public void testMultiplePages()
	{
		String html =
			"<html><head><style>" +
			"@page { size: 4px 4px; margin: 1px; }" +
			"body { background-color: #0f0; margin: 0; }" +
			"div { background-color: #f00; height: 3px; margin: 0; }" +
			"</style></head><body><div></div></body></html>";
		
		String expected = 
			"####" +
			"#RR#" +
			"#RR#" +
			"####";

		BufferedImageTest.assertImgEquals(html, expected, 4, 100, 100, 0);

		expected = 
			"####" +
			"#RR#" +
			"#GG#" +
			"####";
		
		BufferedImageTest.assertImgEquals(html, expected, 4, 100, 100, 1);
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

		BufferedImageTest.assertImgEquals(html, expected, 4, 100, 100, 0);
	}
	
	@Test
	public void testAbsolutePositioning()
	{
		String html =
			"<html><head><style>" +
			"@page { size: 4px 3px; margin: 0; }" +
			"body { background-color: #0f0; margin: 0; }" +
			"div { position:absolute; width: 1px; height: 2px; left: 2px; top: 1px; background-color: #f00; }" +
			"</style></head><body><div></div></body></html>";
		
		String expected = 
			"GGGG" +
			"GGRG" +
			"GGRG";

		BufferedImageTest.assertImgEquals(html, expected, 4, 100, 100, 0);
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

		BufferedImageTest.assertImgEquals(html, expected, 4, 100, 100, 0);

		expected = 
			"RGGG" +
			"GGGG";

		BufferedImageTest.assertImgEquals(html, expected, 4, 100, 100, 1);
	}
	
	@Test
	public void testDivMargins()
	{
		String html =
			"<html><head><style>" +
			"@page { size: 6px 6px; margin: 0; }" +
			"body { background-color: #0f0; margin: 0; }" +
			".div1 { background-color: #f00; margin: 1px 2px 3px 0; height: 1px; }" +
			".div2 { background-color: #00f; margin: 0; height: 1px; }" +
			"</style></head><body><div class=\"div1\"></div><div class=\"div2\"></div></body></html>";
		
		String expected = 
			"GGGGGG" +
			"RRRRGG" +
			"GGGGGG" +
			"GGGGGG" +
			"GGGGGG" +
			"BBBBBB";

		BufferedImageTest.assertImgEquals(html, expected, 6, 100, 100, 0);
	}
	
	@Test
	public void testDivPadding()
	{
		String html =
			"<html><head><style>" +
			"@page { size: 8px 8px; margin: 0; }" +
			"body { background-color: #0f0; margin: 0; }" +
			".div1 { background-color: #f00; padding: 1px 2px 3px 0; margin: 0; height: 1px; width: 1px; }" +
			".div2 { background-color: #00f; margin: 0; padding: 0; height: 1px; }" +
			"</style></head><body><div class=\"div1\"></div><div class=\"div2\"></div></body></html>";
		
		String expected = 
			"RRRGGGGG" +
			"RRRGGGGG" +
			"RRRGGGGG" +
			"RRRGGGGG" +
			"RRRGGGGG" +
			"BBBBBBBB" +
			"GGGGGGGG" +
			"GGGGGGGG";

		BufferedImageTest.assertImgEquals(html, expected, 8, 100, 100, 0);
	}
	
	@Test
	public void testDivBorder()
	{
		String html =
			"<html><head><style>" +
			"@page { size: 6px 6px; margin: 0; }" +
			"body { background-color: #0f0; margin: 0; }" +
			".div1 { background-color: #f00; border-width: 1px 2px 3px 0; margin: 0; padding: 0; height: 1px; width: 1px; border-color: #00f; border-style: solid; }" +
			"</style></head><body><div class=\"div1\"></div></body></html>";
		
		String expected = 
			"BBBGGG" +
			"RBBGGG" +
			"BBBGGG" +
			"BBBGGG" +
			"BBBGGG" +
			"GGGGGG";

		BufferedImageTest.assertImgEquals(html, expected, 6, 100, 100, 0);
	}
	
	@Test
	public void testDivPaddingBorderMargin()
	{
		String html =
			"<html><head><style>" +
			"@page { size: 8px 8px; margin: 0; }" +
			"body { background-color: #0f0; margin: 0; }" +
			".div1 { background-color: #f00; border: 1px solid #00f; margin: 1px; padding: 1px; height: 1px; width: 1px; }" +
			"</style></head><body><div class=\"div1\"></div></body></html>";
		
		// Something about using a 1px uniform border makes this sample produce
		// too many red pixels (4x4 instead of 3x3).
		// To be investigated...
		String expected = 
				"";

		BufferedImageTest.assertImgEquals(html, expected, 8, 100, 100, 0);
	}	
	
}
