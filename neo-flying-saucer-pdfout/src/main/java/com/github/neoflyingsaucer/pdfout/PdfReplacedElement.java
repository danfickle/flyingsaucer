package com.github.neoflyingsaucer.pdfout;

import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.render.RenderingContext;

import com.github.neoflyingsaucer.extend.output.ReplacedElement;

public interface PdfReplacedElement extends ReplacedElement
{
	public void paint(RenderingContext c, PdfOutputDevice outputDevice, BlockBox box);
}
