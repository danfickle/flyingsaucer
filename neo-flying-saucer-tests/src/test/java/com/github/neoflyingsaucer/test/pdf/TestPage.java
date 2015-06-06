package com.github.neoflyingsaucer.test.pdf;

import org.junit.Ignore;
import org.junit.Test;

import com.github.neoflyingsaucer.test.support.PdfTest;

public class TestPage 
{
	// An image (4px x 4px) with one red(R) pixel at top left and the rest blue(B).
	private static final String PATTERN_IMAGE_DATA_URL = 
			"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAQAAAAECAIAAAAmkwkpAAAAE0lEQVR4nGP4zwAE/2EIwcLDAQCQsQ/xhr9I3AAAAABJRU5ErkJggg==";

	@Test
	public void testPdfBookmarks()
	{
		PdfTest pdf = new PdfTest("PdfBookmarks");
		
		String html =
			"<html><head><style>h1 { fs-bookmark-level: 1; } h2 { fs-bookmark-level: 2; }</style></head>" +
			"<body><h1>My bookmark test</h1><h2>Second test</h2></body></html>";
		
		pdf.prepare(html);
		
		String bookmark =
			"/Dest [0 /XYZ null 799 null]\n" +
			"/Title (My bookmark test)\n";
		
		pdf.assertContains(bookmark);
		pdf.assertContains("/Type /Outlines");
	}
	
	@Test
	public void testLinearGradientWithOpacity()
	{
		PdfTest pdf = new PdfTest("LinearGradientWithOpacity");
		
		String html = 
			"<html><head><style>" +
			"@page { size: 100px 20px; margin: 0 }" +
			"body { margin: 0; }" +
			"#1 { background-color: white; opacity: 0.3; width: 100px; height: 20px; }" +
			"#2 { background-image: linear-gradient(to bottom, red, green); width: 100%; height: 100%; }" +
			"</style></head><body><div id=1><div id=2></div></div></body></html>";

		pdf.prepare(html);
		
		String transparencyShading = 
			"/ColorSpace /DeviceGray\n" +
			"/ShadingType 2\n" +
			"/Extend [true true]\n";
		
		pdf.assertContains(transparencyShading);
		
		String terminalFunction =
			"/FunctionType 2\n" +
			"/N 1.0\n" +
			"/Domain [0.0 1.0]\n" +
			"/C0 [0.3]\n" +
			"/C1 [0.3]\n";
		
		pdf.assertContains(terminalFunction);
	}

	@Test
	public void testLinearGradientWithAlpha()
	{
		PdfTest pdf = new PdfTest("LinearGradientWithAlpha");
		
		String html = 
			"<html><head><style>" +
			"@page { size: 100px 20px; margin: 0 }" +
			"body { margin: 0; }" +
			"div { background-color: white; background-image: linear-gradient(to right, rgba(255, 0, 0, 0.5), rgba(0, 255, 0, 0.2)); width: 100px; height: 20px; }" +
			"</style></head><body><div></div></body></html>";

		pdf.prepare(html);
		
		String xObjectDict = 
			"/BBox [0.0 0.0 75.0 15.0]\n" +
			"/FormType 1\n" +
			"/Subtype /Form\n" +
			"/Type /XObject\n";
		
		pdf.assertContains(xObjectDict);
	}
	
	@Test
	public void testLinearGradient()
	{
		PdfTest pdf = new PdfTest("LinearGradient");
		
		String html = 
			"<html><head><style>" +
			"@page { size: 100px 20px; margin: 0 }" +
			"body { margin: 0; }" +
			"div { background-color: white; background-image: linear-gradient(to right, #f00, #00f); width: 100px; height: 20px; }" +
			"</style></head><body><div></div></body></html>";

		pdf.prepare(html);
		
		String patternDict = 
			"/Type /Pattern\n" +
			"/PatternType 2\n";
		
		String shadingDict = 
			"/ShadingType 2\n" +
			"/ColorSpace /DeviceRGB\n" +
			"/Extend [true true]\n";

		String stitcherDict = 
			"/FunctionType 3\n" +
			"/Domain [0.0 1.0]\n" +
			"/Bounds []\n" +
			"/Encode [0.0 1.0]\n";
		
		String terminalDict = 
			"/FunctionType 2\n" +
			"/N 1.0\n" +
			"/Domain [0.0 1.0]\n" +
			"/C0 [1.0 0.0 0.0]\n" +
			"/C1 [0.0 0.0 1.0]\n";
		
		pdf.assertContains(patternDict);
		pdf.assertContains(shadingDict);
		pdf.assertContains(stitcherDict);
		pdf.assertContains(terminalDict);
	}

	@Test
	public void testPNGImage()
	{
		PdfTest pdf = new PdfTest("PNGImage");
		
		String html =
			"<html><head><style>" +
			"@page { size: 4px 4px; margin: 0 }" +
			"body {  margin: 0; }" +
			"div { background-image: url(" + PATTERN_IMAGE_DATA_URL + "); width: 4px; height: 4px; }" +
			"</style></head><body><div></div></body></html>";
		
		String imgObject =
			"/Type /XObject\n" +
			"/Subtype /Image\n" +
			"/Filter /FlateDecode\n" +
			"/ColorSpace /DeviceRGB\n" +
			"/BitsPerComponent 8\n" +
			"/Height 4\n" +
			"/Width 4\n";
		
		pdf.prepare(html);
		pdf.assertContains(imgObject);
	}

	@Test
	public void testBasicFonts()
	{
		PdfTest pdf = new PdfTest("BasicFonts");
		
		String html =
			"<html><head><style>" +
		     "#1 { font-family: Times; font-weight: bold; font-size: 12pt; }" +
			 "#2 { font-family: Courier; font-weight: normal; font-size: 36px; }" +
		     "#3 { font-family: Helvetica; font-weight: 800; font-size: 10em; }" +
			 "</style></head><body><div id=1>TEST1</div><div id=2>TEST2</div><div id=3>TEST3</div></body></html>";
		
		pdf.prepare(html);
		pdf.assertContains("/BaseFont /Times-Bold");
		pdf.assertContains("/BaseFont /Courier");
		pdf.assertContains("/BaseFont /Helvetica-Bold");
	}

	@Test
	public void testBasicColors()
	{
		PdfTest pdf = new PdfTest("BasicColors");
		
		String html =
			"<html><head><style>body { color: #f00; background-color: #0f0; border: 1px solid #00f; }</style></head><body>TESTING</body></html>";
		
		pdf.prepare(html);
		pdf.assertContains("1 0 0 rg");
		pdf.assertContains("0 0 1 rg");
		pdf.assertContains("0 1 0 rg");
	}

	@Test
	public void testPageSizeA4()
	{
		PdfTest pdf = new PdfTest("PageSizeA4");

		String html =
			"<html><head><style>@page { size: A4; }</style></head><body></body></html>";
		
		pdf.prepare(html);
		pdf.assertContains("/MediaBox [0.0 0.0 595.275 841.875]");
	}
	
	@Test
	public void testPageSizeLegal()
	{
		PdfTest pdf = new PdfTest("PageSizeLegal");

		String html =
			"<html><head><style>@page { size: legal; }</style></head><body></body></html>";
		
		pdf.prepare(html);
		pdf.assertContains("/MediaBox [0.0 0.0 612.0 1008.0]");
	}
	
	@Test
	public void testPageSizePixels()
	{
		PdfTest pdf = new PdfTest("PageSizePixels");

		String html =
			"<html><head><style>@page { size: 10px; margin: 0; }</style></head><body></body></html>";
		
		pdf.prepare(html);
		pdf.assertContains("/MediaBox [0.0 0.0 7.5 7.5]");
	}
	
	@Test
	public void testPageSizeInches()
	{
		PdfTest pdf = new PdfTest("PageSizeInches");

		String html =
			"<html><head><style>@page { size: 3in 4in; margin: 0; }</style></head><body></body></html>";
		
		pdf.prepare(html);
		pdf.assertContains("/MediaBox [0.0 0.0 216.0 288.0]");
	}
	
	@Test
	public void testPageMargin()
	{
		PdfTest pdf = new PdfTest("PageMargin");

		String html =
			"<html><head><style>@page { size: 30px; margin: 1px 2px 3px 4px; }</style></head><body></body></html>";
		
		pdf.prepare(html);
		pdf.assertContains("/MediaBox [0.0 0.0 22.5 22.5]");

		String pageClipOperation = 
			"3 21.75 m\n" +
			"21 21.75 l\n" +
			"21 2.25 l\n" +
			"3 2.25 l\n" +
			"3 21.75 l\n" +
			"h\n" +
			"W\n" +
			"n\n";
		
		pdf.assertContains(pageClipOperation);
	}

	@Test
	@Ignore("Failing (off by -0.038 on the left)")
	public void testBodyBackgroundColor()
	{
		PdfTest pdf = new PdfTest("BodyBackgroundColor");

		String html =
			"<html><head><style>@page { size: 30px; margin: 0; } body { background-color: #f00; }</style></head><body></body></html>";
		
		pdf.prepare(html);
		pdf.assertContains("/MediaBox [0.0 0.0 22.5 22.5]");

		String bodyPaintOperation = 
			"1 0 0 rg\n" +
			"/GS0 gs\n" +
			"0 22.5 m\n" +
			"22.5 22.5 l\n" +
			"22.5 0 l\n" +
			"0 0 l\n" +
			"0 22.5 l\n" +
			"h\n" +
			"f\n";
		
		pdf.assertContains(bodyPaintOperation);
	}

	@Test
	public void testListStyleTypeDisc()
	{
		PdfTest pdf = new PdfTest("ListStyleTypeDisc");

		String html =
			"<html><head><style>" +
		    "@page { size: 60px; margin: 0; }" +
		    "body { margin: 0; font-size: 10px; }" +
		    "ul { list-style-position: inside; margin: 0; padding: 0; list-style-type: disc; }</style></head><body>" +
		    "<ul><li>TEST</li></ul></body></html>";
		
		pdf.prepare(html);
		
		// Correct based on visual inspection of resulting PDF.
		String drawEllipseOperation = 
			"2.7750000954 41.5875015259 m\n" +
			"3.5406002998 41.5875015259 4.1625003815 40.9655990601 4.1625003815 40.2000007629 c\n" +
			"4.1625003815 39.4343986511 3.5406002998 38.8125 2.7750000954 38.8125 c\n" +
			"2.0093998909 38.8125 1.3875000477 39.4343986511 1.3875000477 40.2000007629 c\n" +
			"1.3875000477 40.9655990601 2.0093998909 41.5875015259 2.7750000954 41.5875015259 c\n" +
			"h\n" +
			"f*\n";
		
		pdf.assertContains(drawEllipseOperation);
	}

	@Test
	@Ignore("Failing (infinite loop somewhere in code)")
	public void testInfiniteLoopBugOnTooWideContent()
	{
		PdfTest pdf = new PdfTest("InfiniteLoopBugOnTooWideContent");

		String html =
			"<html><head><style>" +
		    "@page { size: 30px; margin: 0; }" +
		    "</style></head><body>" +
		    "<ul><li>TEST</li></ul></body></html>";
		
		pdf.prepare(html);
		pdf.assertContains("/MediaBox");
	}
	
	@Test
	public void testWhitespaceHandling()
	{
		PdfTest pdf = new PdfTest("WhitespaceHandling");

		String html =
			"<html><head><style>" +
		    "@page { size: A4; margin: 0; }" +
		    "</style></head><body>" +
		    "<span>TEST    WS-COLLAPSE</span>" +
		    "<pre>TEST     WS-PRE</pre></body></html>";
		
		pdf.prepare(html);
		pdf.assertContains("(TEST WS-COLLAPSE)");
		pdf.assertContains("(TEST     WS-PRE)");
	}

	@Test
	public void testWhitespaceHandlingWithNewlines()
	{
		PdfTest pdf = new PdfTest("WhitespaceHandlingWithNewlines");

		String html =
			"<html><head><style>" +
		    "@page { size: A4; margin: 0; }" +
		    "</style></head><body>" +
		    "<span>TEST  \n  WS-COLLAPSE</span>" +
		    "<pre>TEST   \n  WS-PRE</pre></body></html>";
		
		pdf.prepare(html);
		pdf.assertContains("(TEST WS-COLLAPSE)");
		pdf.assertContains("(TEST   )");
		pdf.assertContains("(  WS-PRE)");
	}
}
