package com.github.neoflyingsaucer.css.parser;

import java.awt.Rectangle;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.xhtmlrenderer.css.mediaquery.MediaQueryList;
import org.xhtmlrenderer.css.parser.CSSErrorHandler;
import org.xhtmlrenderer.css.parser.CSSParser;
import org.xhtmlrenderer.layout.SharedContext;

import com.github.neoflyingsaucer.extend.useragent.LangId;

import static org.junit.Assert.*;

public class CSSMediaQueryTest
{
	@Rule
	public ExpectedException expected = ExpectedException.none();
	
	private MediaQueryList parseMediaQuery(String query)
	{
		CSSParser parser = new CSSParser(new CSSErrorHandler() {
			@Override
			public void error(String uri, int line, LangId msgId, Object... args) {
				throw new RuntimeException(msgId.toString());
			}
		}, null);
		
		return parser.parseMediaQueryListInternal(query); 
	}

	// Used for testing the evaluation of media queries.
	private static class Context extends SharedContext
	{
		final float _width;
		final float _height;
		final String _media;
		
		@Override
		public float getMmPerPx() {
			return 3;
		}
		
		@Override
		public int getDotsPerPixel() {
			return 1;
		}
		
		@Override
		public Rectangle getFixedRectangle() {
			return new Rectangle(0, 0, (int) _width, (int) _height);
		}
		
		@Override
		public String getMedia() {
			return _media;
		}
		
		private Context(float w, float h, String m) {
			_width = w;
			_height = h;
			_media = m;
		}
	}
	
	private SharedContext ctx1 = new Context(100, 100, "print");
	private SharedContext ctx2 = new Context(100, 100, "screen");
	private SharedContext ctx3 = new Context(99, 990, "screen");
	private SharedContext ctx4 = new Context(101, 100, "print");
	
	@Test
	public void testCss3MediaQueries()
	{
		MediaQueryList mq;
		
		mq = parseMediaQuery("not print");
		assertFalse(mq.eval(ctx1)); // print
		assertTrue(mq.eval(ctx2));  // screen
		
		mq = parseMediaQuery("(min-width: 100px)");
		assertTrue(mq.eval(ctx1));  // 100px wide
		assertFalse(mq.eval(ctx3)); // 99px wide

		mq = parseMediaQuery("(max-width: 100px)");
		assertFalse(mq.eval(ctx4)); // 101px wide
		assertTrue(mq.eval(ctx1));  // 100px wide
		assertTrue(mq.eval(ctx3));  // 99px wide
		
		mq = parseMediaQuery("screen and (min-height: 600px)");
		assertFalse(mq.eval(ctx1)); // print, 100px wide
		assertTrue(mq.eval(ctx3));  // screen, 990px high
		
		mq = parseMediaQuery("(aspect-ratio: 1 / 1) and (color)");
		assertTrue(mq.eval(ctx1));  // 1 / 1 aspect ratio
		assertFalse(mq.eval(ctx3)); // 1 / 10 aspect ratio
		
		mq = parseMediaQuery("(min-aspect-ratio: 1 / 1) and (min-device-height: 1.2cm)");
		assertTrue(mq.eval(ctx1));  // 1 / 1 aspect ratio
		assertFalse(mq.eval(ctx3)); // 1 / 10 aspect ratio
		
		mq = parseMediaQuery("(max-aspect-ratio: 2 / 1)");
		assertTrue(mq.eval(ctx1)); // 1 / 1 aspect ratio
		assertTrue(mq.eval(ctx3)); // 1 / 10 aspect ratio
		
		mq = parseMediaQuery("(color) , (monochrome)");
		assertTrue(mq.eval(ctx1)); // color
		
		mq = parseMediaQuery("(color:8)");
		assertTrue(mq.eval(ctx1)); // 8bit color
		
		mq = parseMediaQuery("(orientation: portrait)");
		assertTrue(mq.eval(ctx3));  // higher than wide.
		assertFalse(mq.eval(ctx1)); // same width and height

		mq = parseMediaQuery("(orientation: landscape)");
		assertFalse(mq.eval(ctx3)); // higher than wide.
		assertTrue(mq.eval(ctx1));  // same width and height

		mq = parseMediaQuery("(resolution: 1dppx)");
		assertTrue(mq.eval(ctx1));  // 1 dot per pixel
		
		mq = parseMediaQuery("(resolution: 30dpcm)");
		assertTrue(mq.eval(ctx1));  // 3 dots per mm

		mq = parseMediaQuery("(min-resolution: 75dpi)");
		assertTrue(mq.eval(ctx1));  // 76 dpi
	}
	
	@Test
	public void testCss3MediaQueriesa()
	{
		expected.expect(RuntimeException.class);
		parseMediaQuery("(min-width: {100px})");
	}
	
	@Test
	public void testCss3MediaQueriesb()
	{
		expected.expect(RuntimeException.class);
		parseMediaQuery("(min-width: 100px: 200px)");
	}
	
	@Test
	public void testCss3MediaQueriesc()
	{
		expected.expect(RuntimeException.class);
		parseMediaQuery("screen garbage (min-width: 600px)");
	}
	
	@Test
	public void testCss3MediaQueriesd()
	{
		expected.expect(RuntimeException.class);
		parseMediaQuery("(min-width: 1000px) and (garbage)");
	}
	
	@Test
	public void testCss3MediaQueriese()
	{
		expected.expect(RuntimeException.class);
		parseMediaQuery("(garbage: 1 / 1) and (min-device-height: 1.2cm)");
	}
	
	@Test
	public void testCss3MediaQueriesf()
	{
		expected.expect(RuntimeException.class);
		parseMediaQuery("(color) separator (monochrome)");
	}
}
