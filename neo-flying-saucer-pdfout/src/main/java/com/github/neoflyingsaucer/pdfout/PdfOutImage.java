package com.github.neoflyingsaucer.pdfout;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.stream.ImageInputStream;

import org.xhtmlrenderer.extend.FSImage;
import org.xhtmlrenderer.layout.SharedContext;

public class PdfOutImage implements FSImage 
{
	private final byte[] _bytes;
	private final String _uri;

	private int _intrinsicWidth;
	private int _intrinsicHeight;

	private final int _originalWidth;
	private final int _originalHeight;
	
	private int _setWidth = -1;
	private int _setHeight = -1;
	
	private boolean _isJpeg;
	private int _colorComponents;
	
	public PdfOutImage(byte[] image, String uri) throws IOException
	{
		_bytes = image;
		_uri = uri;
		
		ImageInputStream in = ImageIO.createImageInputStream(new ByteArrayInputStream(_bytes));

		try {
			final Iterator<ImageReader> readers = ImageIO.getImageReaders(in);
		    if (readers.hasNext()) {
		        ImageReader reader = readers.next();
	            reader.setInput(in);
	            _intrinsicWidth = reader.getWidth(0);
	            _intrinsicHeight = reader.getHeight(0);
	            
	            _originalWidth = _intrinsicWidth;
	            _originalHeight = _intrinsicHeight;
	            
	            String type = reader.getFormatName();
	            
	           _isJpeg = 
	        	   (type.equalsIgnoreCase("jpeg") ||
	            	type.equalsIgnoreCase("jpg") ||
	            	type.equalsIgnoreCase("jfif"));

	           ImageTypeSpecifier spec = reader.getRawImageType(0);
	           
	           if (spec != null)
	           {
	        	   _colorComponents = spec.getNumComponents();
	           }
	           else
	           {
	        	   // Due to a bug in the JRE, getRawImageType
	        	   // can return null.
	        	   Iterator<ImageTypeSpecifier> iter = reader.getImageTypes(0);
	        	   ImageTypeSpecifier speci = iter.next();
	        	   _colorComponents = speci.getNumComponents();
	           }
		    }
		    else
		    {
		    	throw new IOException("Unrecognized Image format");
		    }
		} finally {
		    if (in != null)
				in.close();
		}
	}
	
    public void scaleToOutputResolution(SharedContext _sharedContext) {
        final float factor = _sharedContext.getDotsPerPixel();
        if (factor != 1.0f) {
            _intrinsicWidth *= factor;
            _intrinsicHeight *= factor;
        }
    }

	public int getOriginalWidth()
	{
		return _originalWidth;
	}

	public int getOriginalHeight()
	{
		return _originalHeight;
	}
    
	public int getIntrinsicWidth()
	{
		return _intrinsicWidth;
	}

	public int getIntrinsicHeight()
	{
		return _intrinsicHeight;
	}
	
	@Override
	public int getWidth() 
	{
		if (_setWidth != -1)
			return _setWidth;
			
		return _intrinsicWidth;
	}
	
	@Override
	public int getHeight() 
	{
		if (_setHeight != -1)
			return _setHeight;

		return _intrinsicHeight;
	}

	@Override
	public void scale(int width, int height)
	{
		if (width != -1)
		{
			_setWidth = width;

			if (height == -1 && _intrinsicWidth != 0)
			{
				// Use the width ratio to set the height.
				_setHeight = (int) (((float) _setWidth / (float) _intrinsicWidth) * _intrinsicHeight); 
			}
			else
			{
				_setHeight = height;
			}
		}
		else if (height != -1)
		{
			_setHeight = height;
			
			if (_intrinsicHeight != 0)
			{
				// Use the height ratio to set the width.
				_setWidth = (int) (((float) _setHeight / (float) _intrinsicHeight) * _intrinsicWidth); 
			}
		}
	}

	public byte[] getBytes()
	{
		return _bytes;
	}
	
	public String getUri()
	{
		return _uri;
	}
	
	public boolean isJpeg()
	{
		return _isJpeg;
	}
	
	public int getNumberComponents()
	{
		return _colorComponents;
	}
}
