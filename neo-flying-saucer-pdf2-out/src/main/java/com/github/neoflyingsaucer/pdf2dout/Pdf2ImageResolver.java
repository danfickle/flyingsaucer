package com.github.neoflyingsaucer.pdf2dout;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.github.neoflyingsaucer.extend.controller.cancel.FSCancelController;
import com.github.neoflyingsaucer.extend.controller.error.FSError.FSErrorLevel;
import com.github.neoflyingsaucer.extend.controller.error.FSErrorController;
import com.github.neoflyingsaucer.extend.controller.error.LangId;
import com.github.neoflyingsaucer.extend.output.FSImage;
import com.github.neoflyingsaucer.extend.output.ImageResolver;

public class Pdf2ImageResolver implements ImageResolver
{
	private final float _dotsPerPoint;
	
	public Pdf2ImageResolver(float dotsPerPoint)
	{
		this._dotsPerPoint = dotsPerPoint;
	}
	
	
	@Override
	public FSImage resolveImage(String uri, InputStream strm)
	{
		byte[] bytes = new byte[32000];
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int l;
		
		if (strm == null)
			return null;
		
		try {
			while ((l = strm.read(bytes)) != -1)
			{
				out.write(bytes, 0, l);
				FSCancelController.cancelOpportunity(Pdf2ImageResolver.class);
			}
			
			Pdf2Image img = new Pdf2Image(out.toByteArray(), uri);
			img.scaleToOutputResolution(_dotsPerPoint);
			return img;
		}
		catch (IOException e) 
		{
			FSErrorController.log(Pdf2ImageResolver.class, FSErrorLevel.ERROR, LangId.COULDNT_LOAD_IMAGE, uri);
		}
		finally
		{
			try {
				strm.close();
			} catch (IOException e) { }
		}

		return null;
	}


	@Override
	public Class<?> getImageClass()
	{
		return Pdf2Image.class;
	}
}
