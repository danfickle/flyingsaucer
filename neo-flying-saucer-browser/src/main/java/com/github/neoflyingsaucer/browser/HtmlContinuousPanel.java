package com.github.neoflyingsaucer.browser;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
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
import com.github.neoflyingsaucer.renderers.ContinuousRenderer;

public class HtmlContinuousPanel extends JPanel
{
	private static final long serialVersionUID = 1L;
	private ContinuousRenderer _renderer;
	private Graphics2D _layoutGraphics;
	private DisplayList _dl;
	
	private void newLayoutGraphics()
	{
		BufferedImage layoutGraphics = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);

		Graphics2D g2d2 = layoutGraphics.createGraphics();
        g2d2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

        _layoutGraphics = g2d2;
	}
	
	public void prepare(UserAgentCallback userAgent, String uri, int width)
	{
		if (_layoutGraphics != null)
			_layoutGraphics.dispose();
		
		newLayoutGraphics();
		
		_renderer = new ContinuousRenderer(userAgent);
		_renderer.setDocumentUri(uri);
		_renderer.setImageResolver(new Java2DImageResolver());
    	_renderer.setFontContext(new Java2DFontContext(_layoutGraphics));
    	_renderer.setFontResolver(new Java2DFontResolver());
    	_renderer.setReplacedElementResolver(new Java2DReplacedElementResolver());
    	_renderer.setViewportSize(width, 0);
    	
    	_dl = _renderer.renderToList();
    	
    	setPreferredSize(new Dimension(width, _renderer.getLayoutHeight()));
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
  	
    	Java2DOut out = new Java2DOut(g2, RenderingHints.VALUE_ANTIALIAS_ON);
    	out.render(_dl);
	}
}
