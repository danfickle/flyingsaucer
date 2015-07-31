package com.github.neoflyingsaucer.browser;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import com.github.neoflyingsaucer.extend.output.DisplayList;
import com.github.neoflyingsaucer.extend.useragent.UserAgentCallback;
import com.github.neoflyingsaucer.j2dout.Java2DFontContext;
import com.github.neoflyingsaucer.j2dout.Java2DFontResolver;
import com.github.neoflyingsaucer.j2dout.Java2DImageResolver;
import com.github.neoflyingsaucer.j2dout.Java2DOut;
import com.github.neoflyingsaucer.j2dout.Java2DReplacedElementResolver;
import com.github.neoflyingsaucer.renderers.PagedRenderer;

public class HtmlPagedPanel extends JPanel 
{
	private static final long serialVersionUID = 1L;
	private static final int PAGE_Y_PADDING_PIXELS = 10;
	private static final int PAGE_OFFSET_PIXELS = 4;
	
	private PagedRenderer _renderer;
	private Graphics2D _layoutGraphics;
	
	private void newLayoutGraphics()
	{
		BufferedImage layoutGraphics = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);

		Graphics2D g2d2 = layoutGraphics.createGraphics();
        g2d2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

        _layoutGraphics = g2d2;
	}
	
	public void prepare(UserAgentCallback userAgent, String uri)
	{
		if (_layoutGraphics != null)
			_layoutGraphics.dispose();
		
		newLayoutGraphics();
		
		_renderer = new PagedRenderer(userAgent, 72f, 1);
		_renderer.setDocumentUri(uri);
		_renderer.setImageResolver(new Java2DImageResolver());
    	_renderer.setFontContext(new Java2DFontContext(_layoutGraphics));
    	_renderer.setFontResolver(new Java2DFontResolver());
    	_renderer.setReplacedElementResolver(new Java2DReplacedElementResolver());
    	_renderer.prepare();
    	
    	int height = PAGE_OFFSET_PIXELS;
    	int width = PAGE_OFFSET_PIXELS;
    	
	   	for (int pageNo = 0; pageNo < _renderer.getPageCount(); pageNo++)
    	{
	   		height += _renderer.getPageHeight(pageNo);
	   		height += PAGE_Y_PADDING_PIXELS;
	   		
	   		width = Math.max(width, _renderer.getPageWidth(pageNo) + 6);
    	}

	   	setPreferredSize(new Dimension(width, height));
	}
	
	public void destroy()
	{
		_layoutGraphics.dispose();
		_layoutGraphics = null;
	}
	
	@Override
	public void paint(Graphics g) 
	{
		super.paint(g);
		
		if (_renderer == null)
    		return;
		
    	Graphics2D g2 = (Graphics2D) g;
	   	g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	   	g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

	   	int pageYPosition = PAGE_OFFSET_PIXELS;
	   	
	   	for (int pageNo = 0; pageNo < _renderer.getPageCount(); pageNo++)
    	{
    	   	int height = _renderer.getPageHeight(pageNo);
    	   	int width = _renderer.getPageWidth(pageNo);
	   		
	   		Rectangle pageRect = new Rectangle(PAGE_OFFSET_PIXELS, pageYPosition, width, height);

	   		if (g2.getClip() == null || g2.getClip().intersects(pageRect))
	   		{
	   			g2.translate(PAGE_OFFSET_PIXELS, pageYPosition);
	   			
	   			DisplayList dl = _renderer.renderToList(pageNo);
  	
	   			Java2DOut out = new Java2DOut(g2, RenderingHints.VALUE_ANTIALIAS_ON);
	   			out.render(dl);
    	   	
	   			g2.setStroke(new BasicStroke());
	   			g2.setColor(Color.BLACK);
	   			g2.drawRect(0, 0, width + 1, height + 1);
    	   	
	   			g2.translate(-PAGE_OFFSET_PIXELS, -pageYPosition);
	   		}
    	   	
	   		pageYPosition += height;
    	   	pageYPosition += PAGE_Y_PADDING_PIXELS;
    	}
	}
}
