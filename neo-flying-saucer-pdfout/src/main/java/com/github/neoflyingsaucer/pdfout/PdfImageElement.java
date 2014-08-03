package com.github.neoflyingsaucer.pdfout;

import java.awt.Point;
import java.awt.Rectangle;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.render.RenderingContext;

public class PdfImageElement implements PdfReplacedElement
{
	private Point _location = new Point(0, 0);
	private final PdfOutImage _image;
	
	public PdfImageElement(PdfOutImage image)
	{
		_image = image;
	}
	
	@Override
	public int getIntrinsicWidth() {
		return _image.getWidth();
	}

	@Override
	public int getIntrinsicHeight() {
		return _image.getHeight();
	}

	@Override
	public Point getLocation() {
		return _location;
	}

	@Override
	public void setLocation(int x, int y) {
		_location = new Point(x, y);
	}

	@Override
	public void detach(LayoutContext c) { }

	@Override
	public boolean hasBaseline() {
		return false;
	}

	@Override
	public int getBaseline() {
		return 0;
	}

	@Override
    public void paint(final RenderingContext c, final PdfOutputDevice outputDevice, final BlockBox box)
    {
        final Rectangle contentBounds = box.getContentAreaEdge(box.getAbsX(), box.getAbsY(), c);
        //final ReplacedElement element = box.getReplacedElement();
        outputDevice.drawImage(_image, contentBounds.x, contentBounds.y);
    }
}
