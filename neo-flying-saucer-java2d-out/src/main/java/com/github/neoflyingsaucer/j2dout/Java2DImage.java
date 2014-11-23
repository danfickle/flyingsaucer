package com.github.neoflyingsaucer.j2dout;

import java.awt.image.BufferedImage;

import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Mode;

import com.github.neoflyingsaucer.extend.output.FSImage;

public class Java2DImage implements FSImage
{
	private BufferedImage img;
	
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
		return img.getWidth();
	}

	@Override
	public int getHeight()
	{
		return img.getHeight();
	}

	@Override
	public void scale(int width, int height) 
	{
		if (width > 0 || height > 0)
		{
			BufferedImage newImg;
			
			if (width > 0 && height > 0)
				newImg = Scalr.resize(img, Mode.FIT_EXACT,  width, height);
			else if (width < 0)
				newImg = Scalr.resize(img, Mode.FIT_TO_HEIGHT, height); 
			else 
				newImg = Scalr.resize(img, Mode.FIT_TO_WIDTH, width);

			img = newImg;
		}
	}
}
