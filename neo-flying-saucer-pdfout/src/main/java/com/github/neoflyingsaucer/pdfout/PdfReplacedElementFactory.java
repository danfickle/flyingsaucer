package com.github.neoflyingsaucer.pdfout;

import java.util.Optional;

import org.w3c.dom.Element;
import org.xhtmlrenderer.extend.ReplacedElement;
import org.xhtmlrenderer.extend.ReplacedElementFactory;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.resource.ImageResource;

public class PdfReplacedElementFactory implements ReplacedElementFactory {

	@Override
	public ReplacedElement createReplacedElement(LayoutContext c, BlockBox box,
			UserAgentCallback uac, int cssWidth, int cssHeight) 
	{
        final Element e = box.getElement();

        if (e == null) 
            return null;

        final String nodeName = e.getNodeName();

        if (nodeName.equalsIgnoreCase("img")) {
            final String srcAttr = e.getAttribute("src");

            if (srcAttr != null && !srcAttr.isEmpty()) 
            {
            	final Optional<String> resolved = uac.resolveURI(c.getSharedContext().getBaseURL(), srcAttr);

            	if (resolved.isPresent())
            	{
            		Optional<ImageResource> image = uac.getImageResource(resolved.get());
            		
            		if (image.isPresent())
            		{
            			PdfOutImage pdfImage = (PdfOutImage) image.get().getImage();
            			
            			if (cssWidth != -1 || cssHeight != -1)
            				pdfImage.scale(cssWidth, cssHeight);
            			
            			return new PdfImageElement(pdfImage);
            		}
            	}
            }
        }
        
        return null;
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub

	}

	@Override
	public void remove(Element e) {
		// TODO Auto-generated method stub

	}

}
