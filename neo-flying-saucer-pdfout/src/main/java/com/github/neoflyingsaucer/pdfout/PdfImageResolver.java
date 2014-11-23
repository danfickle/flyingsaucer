package com.github.neoflyingsaucer.pdfout;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.xhtmlrenderer.layout.SharedContext;
import org.xhtmlrenderer.resource.ImageResource;

import com.github.neoflyingsaucer.extend.output.FSImage;
import com.github.neoflyingsaucer.extend.output.ImageResolver;
import com.github.neoflyingsaucer.extend.useragent.ImageResourceI;
import com.github.neoflyingsaucer.extend.useragent.Optional;

public class PdfImageResolver implements ImageResolver 
{
	private final SharedContext _sharedContext;
	
	public PdfImageResolver(SharedContext ctx)
	{
		_sharedContext = ctx;
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
			
			PdfOutImage img = new PdfOutImage(out.toByteArray(), uri);
			img.scaleToOutputResolution(_sharedContext);
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
