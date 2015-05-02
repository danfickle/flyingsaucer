package org.xhtmlrenderer.swing;

import java.awt.image.BufferedImage;

import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Mode;

import com.github.neoflyingsaucer.extend.output.FSImage;

public class Java2DImage implements FSImage
{
	private final BufferedImage img;
	
	public Java2DImage(BufferedImage img)
	{
		this.img = img;
	}
	
	public BufferedImage getAWTImage()
	{
		return img;
	}
	
	@Override
	public int getWidth()
	{
		if (img == null)
			return 0;

		return img.getWidth();
	}

	@Override
	public int getHeight()
	{
		if (img == null)
			return 0;

		return img.getHeight();
	}

	@Override
	public FSImage scale(int width, int height) 
	{
		if (width > 0 || height > 0 && img != null)
		{
			BufferedImage newImg;
			
			if (width > 0 && height > 0)
				newImg = Scalr.resize(img, Mode.FIT_EXACT,  width, height);
			else if (width < 0)
				newImg = Scalr.resize(img, Mode.FIT_TO_HEIGHT, height); 
			else 
				newImg = Scalr.resize(img, Mode.FIT_TO_WIDTH, width);

			return new Java2DImage(newImg);
		}
		
		return this;
	}
}
