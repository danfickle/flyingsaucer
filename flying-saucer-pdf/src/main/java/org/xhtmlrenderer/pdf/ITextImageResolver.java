package org.xhtmlrenderer.pdf;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.xhtmlrenderer.resource.ImageResource;

import com.github.neoflyingsaucer.extend.output.FSImage;
import com.github.neoflyingsaucer.extend.output.ImageResolver;
import com.lowagie.text.Image;

public class ITextImageResolver implements ImageResolver {

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
			Image image = Image.getInstance(out.toByteArray());
			//scaleToOutputResolution(image);
			return new ITextFSImage(image);
		}
		catch(Exception e)
		{
			return null;
			// TODO
		}
	}

}
