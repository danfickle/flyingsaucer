package com.github.neoflyingsaucer.pdf2dout;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.neoflyingsaucer.extend.output.FSImage;
import com.github.neoflyingsaucer.extend.output.ImageResolver;

public class Pdf2ImageResolver implements ImageResolver
{
	private static final Logger LOGGER = LoggerFactory.getLogger(Pdf2ImageResolver.class);
	private final float _dotsPerPoint;
	
	public Pdf2ImageResolver(float dotsPerPoint)
	{
		this._dotsPerPoint = dotsPerPoint;
	}
	
	
	@Override
	public FSImage resolveImage(String uri, InputStream strm)
	{
		byte[] bytes = new byte[8096];
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int l;
		
		try {
			while ((l = strm.read(bytes)) != -1)
			{
				out.write(bytes, 0, l);
			}
			
			Pdf2Image img = new Pdf2Image(out.toByteArray(), uri);
			img.scaleToOutputResolution(_dotsPerPoint);
			return img;
		}
		catch (IOException e) 
		{
			// TODO
			System.err.println("Whoops!");
		}

		return null;
	}
}
