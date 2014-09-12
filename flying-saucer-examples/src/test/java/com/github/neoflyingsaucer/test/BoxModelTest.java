package com.github.neoflyingsaucer.test;

import org.junit.Test;

import com.github.neoflyingsaucer.test.support.BufferedImageTest;

public class BoxModelTest 
{
	@Test
	public void testPageMarginSize()
	{
		String html =
			"<html><head><style>@page { size: 4px 4px; margin: 1px; } " +
			"body { background-color: #00f; }</style></head>" +
		    "<body></body></html>";
		
		String expected = 
			"####" +
			"#BB#" +
			"#BB#" +
			"####";

		BufferedImageTest.assertImgEquals(html, expected, 4, 100, 100);
	}
}
