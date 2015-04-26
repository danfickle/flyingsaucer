package com.github.neoflyingsaucer.j2dout;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import com.github.neoflyingsaucer.extend.controller.error.FSErrorController;
import com.github.neoflyingsaucer.extend.controller.error.LangId;
import com.github.neoflyingsaucer.extend.controller.error.FSError.FSErrorLevel;
import com.github.neoflyingsaucer.extend.output.FSImage;
import com.github.neoflyingsaucer.extend.output.ImageResolver;

public class Java2DImageResolver implements ImageResolver
{
	private static final BufferedImage NULL_IMG = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
	
	@Override
	public FSImage resolveImage(String uri, InputStream strm)
	{
		BufferedImage img;
		
		if (strm == null)
			return new Java2DImage(NULL_IMG);
		
		try
		{
			img = ImageIO.read(strm);
		}
		catch (IOException e)
		{
			FSErrorController.log(Java2DImageResolver.class, FSErrorLevel.ERROR, LangId.COULDNT_LOAD_IMAGE, uri);
			img = NULL_IMG;
		}
		
		return new Java2DImage(img);
	}
}
