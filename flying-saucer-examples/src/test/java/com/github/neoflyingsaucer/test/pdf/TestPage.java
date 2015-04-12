package com.github.neoflyingsaucer.test.pdf;

import org.junit.Ignore;
import org.junit.Test;

import com.github.neoflyingsaucer.test.support.PdfTest;

public class TestPage 
{
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
		pdf.assertContains("/MediaBox [0.0 0.0 612 1008]");
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
		pdf.assertContains("/MediaBox [0.0 0.0 216 288]");
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
			"q\n" +
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
			"2.775 41.588 m\n" +
			"3.541 41.588 4.163 40.966 4.163 40.2 c\n" +
			"4.163 39.434 3.541 38.812 2.775 38.812 c\n" +
			"2.009 38.812 1.388 39.434 1.388 40.2 c\n" +
			"1.388 40.966 2.009 41.588 2.775 41.588 c\n" +
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
	@Ignore("Failing (eating a space before the newline in the pre test)")
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
