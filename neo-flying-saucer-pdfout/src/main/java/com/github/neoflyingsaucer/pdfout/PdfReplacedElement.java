package com.github.neoflyingsaucer.pdfout;

import org.xhtmlrenderer.extend.ReplacedElement;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.render.RenderingContext;

public interface PdfReplacedElement extends ReplacedElement
{
	public void paint(RenderingContext c, PdfOutputDevice outputDevice, BlockBox box);
}
