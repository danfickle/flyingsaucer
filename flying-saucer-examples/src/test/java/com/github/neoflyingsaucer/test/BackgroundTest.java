package com.github.neoflyingsaucer.test;

import org.junit.Ignore;
import org.junit.Test;

import com.github.neoflyingsaucer.test.support.BufferedImageTest;

public class BackgroundTest 
{
	// An image (4px x 4px) with one red(R) pixel at top left and the rest blue(B).
	private static final String PATTERN_IMAGE_DATA_URL = 
		"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAQAAAAECAIAAAAmkwkpAAAAE0lEQVR4nGP4zwAE/2EIwcLDAQCQsQ/xhr9I3AAAAABJRU5ErkJggg==";

	@Test
	@Ignore("Failing (not implemented)")
	public void testBackgroundImageMultiple()
	{
		String html =
			"<html><head><style>" +
			"@page { size: 8px 8px; margin: 0 }" +
			"body {  margin: 0; }" +
			"div { background-image: url(" + PATTERN_IMAGE_DATA_URL + "), url(" + PATTERN_IMAGE_DATA_URL + "); " +
			   "background-repeat: no-repeat; background-position: left top, right bottom; width: 8px; height: 6px; }" +
			"</style></head><body><div></div></body></html>";
		
		String expected = 
			"RBBB####" +
			"BBBB####" +
			"BBBB####" +
			"BBBB####" +
			"####RBBB" +
			"####BBBB" +
			"####BBBB" +
			"####BBBB";
		
		BufferedImageTest.assertImgEquals(html, expected, 8, 0, "BackgroundImageMultiple");
	}

	@Test
	public void testBackgroundImageWithStyleOutsideHead()
	{
		String html =
			"<html><head><style>" +
			"@page { size: 4px 4px; margin: 0 }" +
			"body {  margin: 0; }" +
			"</style></head><body><div><style>" + 
			"#1 { background-image: url(" + PATTERN_IMAGE_DATA_URL + "); width: 4px; height: 4px; }" +
			"</style></div><div id=1></div></body></html>";
		
		String expected = 
			"RBBB" +
			"BBBB" +
			"BBBB" +
			"BBBB";
		
		BufferedImageTest.assertImgEquals(html, expected, 4, 0, "BackgroundImageWithStyleOutsideHead");
	}
	
	@Test
	public void testBackgroundImageWithStyleOutsideHeadAndNoHead()
	{
		String html =
			"<html><body><div><style>" + 
			"@page { size: 4px 4px; margin: 0 }" +
			"body {  margin: 0; }" +
			"#1 { background-image: url(" + PATTERN_IMAGE_DATA_URL + "); width: 4px; height: 4px; }" +
			"</style></div><div id=1></div></body></html>";
		
		String expected = 
			"RBBB" +
			"BBBB" +
			"BBBB" +
			"BBBB";
		
		BufferedImageTest.assertImgEquals(html, expected, 4, 0, "BackgroundImageWithStyleOutsideHeadAndNoHead");
	}

	@Test
	public void testBackgroundRepeatOnMultiplePages()
	{
		String html =
			"<html><head><style>" +
			"@page { size: 4px 4px; margin: 0 }" +
			"body {  margin: 0; }" +
			"#1 { background-image: url(" + PATTERN_IMAGE_DATA_URL + "); width: 4px; height: 8px; }" +
			"</style></head><body><div id=1></div></body></html>";
		
		String expected = 
			"RBBB" +
			"BBBB" +
			"BBBB" +
			"BBBB";
		
		BufferedImageTest.assertImgEquals(html, expected, 4, 0, "BackgroundRepeatOnMultiplePages.1");

		expected = 
			"RBBB" +
			"BBBB" +
			"BBBB" +
			"BBBB";
		
		BufferedImageTest.assertImgEquals(html, expected, 4, 1, "BackgroundRepeatOnMultiplePages.2");
	}

	@Test
	public void testOverlappingBackgrounds()
	{
		String html =
			"<html><head><style>" +
			"@page { size: 8px 6px; margin: 0 }" +
			"body {  margin: 0; }" +
			"#1 { background-image: url(" + PATTERN_IMAGE_DATA_URL + "); background-repeat: no-repeat; width: 8px; height: 6px; z-index: 1; }" +
			"#2 { background-image: url(" + PATTERN_IMAGE_DATA_URL + "); background-repeat: no-repeat; width: 6px; height: 4px; z-index: 2; " +
			  "position: absolute; top: 2px; left: 1px; }" +
			"</style></head><body><div id=1></div><div id=2></div></body></html>";
		
		String expected = 
			"RBBB####" +
			"BBBB####" +
			"BRBBB###" +
			"BBBBB###" +
			"#BBBB###" +
			"#BBBB###";
		
		BufferedImageTest.assertImgEquals(html, expected, 8, 0, "OverlappingBackgrounds");
	}

	@Test
	public void testBackgroundColorClipping()
	{
		String html =
			"<html><head><style>" +
			"@page { size: 8px 6px; margin: 0 }" +
			"body {  margin: 0; }" +
			"div { background-color: #f00; width: 4px; height: 2px; margin: 0 auto; }" +
			"</style></head><body><div></div></body></html>";
		
		String expected = 
			"##RRRR##" +
			"##RRRR##" +
			"########" +
			"########" +
			"########" +
			"########";
		
		BufferedImageTest.assertImgEquals(html, expected, 8, 0, "BackgroundColorClipping");
	}

	@Test
	@Ignore("Failing (html background-image overrides body / off by one)")
	public void testBackgroundImageOnBodyAndHtml()
	{
		String html =
			"<html><head><style>" +
			"@page { size: 8px 6px; margin: 0; }" +
			"body {  margin: 2px; background-image: url(" + PATTERN_IMAGE_DATA_URL + "); }" +
			"html { background-image: url(" + PATTERN_IMAGE_DATA_URL + "); }" +
			"</style></head><body></body></html>";
		
		String expected = 
			"RBBBBRBBB" +
			"BBBBBBBBB" +
			"BBRBBBBBB" +
			"BBBBBBBBB" +
			"RBBBBRBBB" +
			"BBBBBBBBB";
		
		BufferedImageTest.assertImgEquals(html, expected, 8, 0, "BackgroundImageOnBodyAndHtml");
	}

	@Test
	@Ignore("Failing (html background-color overrides body)")
	public void testBackgroundColorOnBodyAndHtml()
	{
		String html =
			"<html><head><style>" +
			"@page { size: 8px 6px; margin: 0; }" +
			"body {  margin: 2px; background-color: #f00; }" +
			"html { background-color: #0f0; }" +
			"</style></head><body></body></html>";
		
		String expected = 
			"RRRRRRRR" +
			"RRRRRRRR" +
			"RRGGGGRR" +
			"RRGGGGRR" +
			"RRRRRRRR" +
			"RRRRRRRR";
		
		BufferedImageTest.assertImgEquals(html, expected, 8, 0, "BackgroundColorOnBodyAndHtml");
	}

	@Test
	public void testBackgroundImageClipping()
	{
		String html =
			"<html><head><style>" +
			"@page { size: 8px 6px; margin: 0 }" +
			"body {  margin: 0; }" +
			"div { background-image: url(" + PATTERN_IMAGE_DATA_URL + "); background-repeat: no-repeat; background-position: 50% 0; width: 8px; height: 2px; }" +
			"</style></head><body><div></div></body></html>";
		
		String expected = 
			"##RBBB##" +
			"##BBBB##" +
			"########" +
			"########" +
			"########" +
			"########";
		
		BufferedImageTest.assertImgEquals(html, expected, 8, 0, "BackgroundImageClipping");
	}

	@Test
	public void testBackgroundPositionPercentOffset()
	{
		String html =
			"<html><head><style>" +
			"@page { size: 8px 6px; margin: 0 }" +
			"body {  margin: 0; }" +
			"div { background-image: url(" + PATTERN_IMAGE_DATA_URL + "); background-repeat: no-repeat; background-position: 50% 25%; width: 8px; height: 6px; }" +
			"</style></head><body><div></div></body></html>";
		
		// Calculation for correct background-position in percentage terms is:
		// left = (container_width - image_width) * percentage
		String expected = 
			"########" +
			"##RBBB##" +
			"##BBBB##" +
			"##BBBB##" +
			"##BBBB##" +
			"########";
		
		BufferedImageTest.assertImgEquals(html, expected, 8, 0, "BackgroundPositionPercentOffset");
	}

	@Test
	public void testBackgroundPositionPixelOffset()
	{
		String html =
			"<html><head><style>" +
			"@page { size: 8px 6px; margin: 0 }" +
			"body {  margin: 0; }" +
			"div { background-image: url(" + PATTERN_IMAGE_DATA_URL + "); background-repeat: no-repeat; background-position: 1px 2px; width: 8px; height: 6px; }" +
			"</style></head><body><div></div></body></html>";
		
		String expected = 
			"########" +
			"########" +
			"#RBBB###" +
			"#BBBB###" +
			"#BBBB###" +
			"#BBBB###";
		
		BufferedImageTest.assertImgEquals(html, expected, 8, 0, "BackgroundPositionPixelOffset");
	}

	@Test
	public void testBackgroundPositionCenter()
	{
		String html =
			"<html><head><style>" +
			"@page { size: 8px 6px; margin: 0 }" +
			"body {  margin: 0; }" +
			"div { background-image: url(" + PATTERN_IMAGE_DATA_URL + "); background-repeat: no-repeat; background-position: center center; width: 8px; height: 6px; }" +
			"</style></head><body><div></div></body></html>";
		
		String expected = 
			"########" +
			"##RBBB##" +
			"##BBBB##" +
			"##BBBB##" +
			"##BBBB##" +
			"########";
		
		BufferedImageTest.assertImgEquals(html, expected, 8, 0, "BackgroundPositionCenter");
	}
	
	@Test
	public void testBackgroundPositionBottomRight()
	{
		String html =
			"<html><head><style>" +
			"@page { size: 8px 6px; margin: 0 }" +
			"body {  margin: 0; }" +
			"div { background-image: url(" + PATTERN_IMAGE_DATA_URL + "); background-repeat: no-repeat; background-position: right bottom; width: 8px; height: 6px; }" +
			"</style></head><body><div></div></body></html>";
		
		String expected = 
			"########" +
			"########" +
			"####RBBB" +
			"####BBBB" +
			"####BBBB" +
			"####BBBB";
		
		BufferedImageTest.assertImgEquals(html, expected, 8, 0, "BackgroundPositionBottomRight");
	}

	@Test
	public void testBackgroundPositionTopLeft()
	{
		String html =
			"<html><head><style>" +
			"@page { size: 8px 6px; margin: 0 }" +
			"body {  margin: 0; }" +
			"div { background-image: url(" + PATTERN_IMAGE_DATA_URL + "); background-repeat: no-repeat; background-position: left top; width: 8px; height: 6px; }" +
			"</style></head><body><div></div></body></html>";
		
		String expected = 
			"RBBB####" +
			"BBBB####" +
			"BBBB####" +
			"BBBB####" +
			"########" +
			"########";
		
		BufferedImageTest.assertImgEquals(html, expected, 8, 0, "BackgroundPositionTopLeft");
	}
	
	@Test
	public void testBackgroundShorthand()
	{
		String html =
			"<html><head><style>" +
			"@page { size: 8px 6px; margin: 0 }" +
			"body {  margin: 0; }" +
			"div { background: #0f0 url(" + PATTERN_IMAGE_DATA_URL + ") no-repeat left top; width: 8px; height: 6px; }" +
			"</style></head><body><div></div></body></html>";
		
		String expected = 
			"RBBBGGGG" +
			"BBBBGGGG" +
			"BBBBGGGG" +
			"BBBBGGGG" +
			"GGGGGGGG" +
			"GGGGGGGG";
		
		BufferedImageTest.assertImgEquals(html, expected, 8, 0, "BackgroundShorthand");
	}

	@Test
	public void testBackgroundImageAndColor()
	{
		String html =
			"<html><head><style>" +
			"@page { size: 8px 6px; margin: 0 }" +
			"body {  margin: 0; }" +
			"div { background-image: url(" + PATTERN_IMAGE_DATA_URL + "); width: 8px; height: 6px; background-repeat: repeat-y; background-color: #0f0; }" +
			"</style></head><body><div></div></body></html>";
		
		String expected = 
			"RBBBGGGG" +
			"BBBBGGGG" +
			"BBBBGGGG" +
			"BBBBGGGG" +
			"RBBBGGGG" +
			"BBBBGGGG";
		
		BufferedImageTest.assertImgEquals(html, expected, 8, 0, "BackgroundImageAndColor");
	}
	
	@Test
	public void testBackgroundImageRepeatYOnly()
	{
		String html =
			"<html><head><style>" +
			"@page { size: 8px 6px; margin: 0 }" +
			"body {  margin: 0; }" +
			"div { background-image: url(" + PATTERN_IMAGE_DATA_URL + "); width: 8px; height: 6px; background-repeat: repeat-y; }" +
			"</style></head><body><div></div></body></html>";
		
		String expected = 
			"RBBB####" +
			"BBBB####" +
			"BBBB####" +
			"BBBB####" +
			"RBBB####" +
			"BBBB####";
		
		BufferedImageTest.assertImgEquals(html, expected, 8, 0, "BackgroundImageRepeatXOnly");
	}
	
	@Test
	public void testBackgroundImageRepeatXOnly()
	{
		String html =
			"<html><head><style>" +
			"@page { size: 8px 6px; margin: 0 }" +
			"body {  margin: 0; }" +
			"div { background-image: url(" + PATTERN_IMAGE_DATA_URL + "); width: 8px; height: 6px; background-repeat: repeat-x; }" +
			"</style></head><body><div></div></body></html>";
		
		String expected = 
			"RBBBRBBB" +
			"BBBBBBBB" +
			"BBBBBBBB" +
			"BBBBBBBB" +
			"########" +
			"########";
		
		BufferedImageTest.assertImgEquals(html, expected, 8, 0, "BackgroundImageRepeatXOnly");
	}
	
	@Test
	public void testBackgroundImageRepeat()
	{
		String html =
			"<html><head><style>" +
			"@page { size: 8px 6px; margin: 0 }" +
			"body {  margin: 0; }" +
			"div { background-image: url(" + PATTERN_IMAGE_DATA_URL + "); width: 8px; height: 6px; }" +
			"</style></head><body><div></div></body></html>";
		
		String expected = 
			"RBBBRBBB" +
			"BBBBBBBB" +
			"BBBBBBBB" +
			"BBBBBBBB" +
			"RBBBRBBB" +
			"BBBBBBBB";
		
		BufferedImageTest.assertImgEquals(html, expected, 8, 0, "BackgroundImageRepeat");
	}
	
	@Test
	public void testBackgroundImageOnDiv()
	{
		String html =
			"<html><head><style>" +
			"@page { size: 4px 4px; margin: 0 }" +
			"body {  margin: 0; }" +
			"div { background-image: url(" + PATTERN_IMAGE_DATA_URL + "); width: 4px; height: 4px; }" +
			"</style></head><body><div></div></body></html>";
		
		String expected = 
			"RBBB" +
			"BBBB" +
			"BBBB" +
			"BBBB";
		
		BufferedImageTest.assertImgEquals(html, expected, 4, 0, "BackgroundImageOnDiv");
	}

	@Test
	@Ignore("Failing (off by one)")
	public void testBackgroundImageOnHtml()
	{
		String html =
			"<html><head><style>" +
			"@page { size: 4px 4px; margin: 0 }" +
			"html { background-image: url(" + PATTERN_IMAGE_DATA_URL + "); }" +
			"body { margin: 0; }" +
			"</style></head><body></body></html>";
		
		String expected = 
			"RBBB" +
			"BBBB" +
			"BBBB" +
			"BBBB";
		
		BufferedImageTest.assertImgEquals(html, expected, 8, 0, "BackgroundImageOnHtml");
	}

	@Test
	@Ignore("Failing (off by one)")
	public void testBackgroundImageOnBody()
	{
		String html =
			"<html><head><style>" +
			"@page { size: 4px 4px; margin: 0 }" +
			"body { background-image: url(" + PATTERN_IMAGE_DATA_URL + "); margin: 0; }" +
			"</style></head><body></body></html>";
		
		String expected = 
			"RBBB" +
			"BBBB" +
			"BBBB" +
			"BBBB";
		
		BufferedImageTest.assertImgEquals(html, expected, 8, 0, "BackgroundImageOnBody");
	}
	
	@Test
	public void testLinearGradientWithSameStartAndEnd()
	{
		String html = 
			"<html><head><style>" +
			"@page { size: 2px 1px; margin: 0; }" +
			"body { background-image: linear-gradient(to right, #f00, #f00); }" +
			"</style></head><body></body></html>";

		String expected = 
			"RR";
		
		BufferedImageTest.assertImgEquals(html, expected, 2, 0, "LinearGradientWithSameStartAndEnd");
	}
	
	@Test
	public void testLinearGradientWithHeightGreaterThanWidth()
	{
		String html = 
			"<html><head><style>" +
			"@page { size: 2px 4px; margin: 0; }" +
			"body { margin: 0; }" +
			"div { background-image: linear-gradient(to right, #f00, #f00); width: 2px; height: 4px; }" +
			"</style></head><body><div></div></body></html>";

		String expected = 
			"RR" +
			"RR" +
			"RR" +
			"RR";
		
		BufferedImageTest.assertImgEquals(html, expected, 2, 0, "LinearGradientWithHeightGreaterThanWidth");
	}
}
