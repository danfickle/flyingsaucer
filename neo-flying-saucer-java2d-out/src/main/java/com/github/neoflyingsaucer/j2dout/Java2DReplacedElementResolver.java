package com.github.neoflyingsaucer.j2dout;

import java.awt.Point;

import org.w3c.dom.Element;

import com.github.neoflyingsaucer.extend.controller.error.FSErrorController;
import com.github.neoflyingsaucer.extend.controller.error.FSError.FSErrorLevel;
import com.github.neoflyingsaucer.extend.controller.error.LangId;
import com.github.neoflyingsaucer.extend.output.FSImage;
import com.github.neoflyingsaucer.extend.output.ImageResolver;
import com.github.neoflyingsaucer.extend.output.ReplacedElement;
import com.github.neoflyingsaucer.extend.output.ReplacedElementResolver;
import com.github.neoflyingsaucer.extend.useragent.ImageResourceI;
import com.github.neoflyingsaucer.extend.useragent.Optional;
import com.github.neoflyingsaucer.extend.useragent.UserAgentCallback;

public class Java2DReplacedElementResolver implements ReplacedElementResolver
{
	@Override
	public ReplacedElement createReplacedElement(Element e, String baseUri, UserAgentCallback uac, ImageResolver imgResolver, float cssWidth, float cssHeight)
	{
		if ("img".equals(e.getTagName()))
		{
			return replaceImage(uac, baseUri, e, imgResolver, (int) cssWidth, (int) cssHeight);
		}
		
		return null;
	}
	
    protected ReplacedElement replaceImage(UserAgentCallback uac, String baseUri, Element elem, ImageResolver imgResolver, int cssWidth, int cssHeight) 
    {
    	ReplacedElement re = null;
        String imageSrc = elem.getAttribute("src");

        if (imageSrc.isEmpty())
        {
        	FSErrorController.log(Java2DReplacedElementResolver.class, FSErrorLevel.ERROR, LangId.NO_IMAGE_SRC_PROVIDED);
        	return new Java2DImageReplacedElement(null, cssWidth, cssHeight);
        }

        // lookup in cache, or instantiate
      	// TODO: Make sure we have the correct base uri.
        Optional<String> ruri = uac.resolveURI(baseUri, imageSrc);

        if (ruri.isPresent())
        {
        	re = null;// lookupImageReplacedElement(elem, ruri.get(), cssWidth, cssHeight);
           
        	if (re == null) {
            	//ImageResourceI imageResource = uac.getImageResourceCache().get(ruri.get(), cssWidth, cssHeight);
            	// TODO: ImageResource may be null.
            		
        		Optional<ImageResourceI> img = uac.getImageResource(ruri.get());
        		
        		if (img.isPresent())
        		{
        			FSImage image = imgResolver.resolveImage(ruri.get(), img.get().getImage());
        			re = new Java2DImageReplacedElement(image, cssWidth, cssHeight);
        		}
            }
            else
            {
            	return new Java2DImageReplacedElement(null, cssWidth, cssHeight);
            }
        }
        return re;
    }

	@Override
	public void reset()
	{
	}

	public static class Java2DImageReplacedElement implements ReplacedElement
	{
		private final FSImage img;
		private Point location = new Point(0, 0);

		public Java2DImageReplacedElement(FSImage img, int cssWidth, int cssHeight)
		{
			this.img = img;
		}

		@Override
		public int getIntrinsicWidth()
		{
			if (img == null)
				return 0;
			
			return img.getWidth();
		}

		@Override
		public int getIntrinsicHeight()
		{
			if (img == null)
				return 0;
			
			return img.getHeight();
		}

		@Override
		public Point getLocation() 
		{
			return location;
		}

		@Override
		public void setLocation(int x, int y)
		{
			location = new Point(x, y);
		}

		@Override
		public void detach()
		{
		}

		@Override
		public boolean hasBaseline()
		{
			return false;
		}

		@Override
		public int getBaseline()
		{
			return 0;
		}
		
	    public FSImage getImage() 
	    {
	        return img;
	    }
	}
}
