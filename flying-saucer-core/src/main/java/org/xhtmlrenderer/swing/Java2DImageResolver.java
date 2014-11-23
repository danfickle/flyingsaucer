package org.xhtmlrenderer.swing;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.neoflyingsaucer.extend.output.FSImage;
import com.github.neoflyingsaucer.extend.output.ImageResolver;

public class Java2DImageResolver implements ImageResolver
{
	private static final BufferedImage NULL_IMG = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
	private static final Logger LOGGER = LoggerFactory.getLogger(Java2DImageResolver.class);
	
	@Override
	public FSImage resolveImage(String uri, InputStream strm)
	{
		BufferedImage img;
		
		try
		{
			img = ImageIO.read(strm);
		}
		catch (IOException e)
		{
			LOGGER.warn("Could not read image", e);
			img = NULL_IMG;
		}
		
		return new Java2DImage(img);
	}
}
