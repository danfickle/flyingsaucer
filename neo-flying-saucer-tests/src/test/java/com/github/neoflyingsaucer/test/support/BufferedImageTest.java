package com.github.neoflyingsaucer.test.support;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;

import com.github.neoflyingsaucer.defaultuseragent.DefaultUserAgent;
import com.github.neoflyingsaucer.defaultuseragent.HTMLResourceHelper;
import com.github.neoflyingsaucer.extend.output.DisplayList;
import com.github.neoflyingsaucer.j2dout.Java2DFontContext;
import com.github.neoflyingsaucer.j2dout.Java2DFontResolver;
import com.github.neoflyingsaucer.j2dout.Java2DImageResolver;
import com.github.neoflyingsaucer.j2dout.Java2DOut;
import com.github.neoflyingsaucer.j2dout.Java2DReplacedElementResolver;
import com.github.neoflyingsaucer.renderers.PagedRenderer;

public class BufferedImageTest
{
	private static BufferedImage renderImage(String html, int pageNo)
	{
		return renderToPagedImage(html, pageNo);
	}

    private static BufferedImage renderToPagedImage(String doc, int pageNo)
    {
    	BufferedImage layoutGraphics = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
 
    	Graphics2D g2d2 = layoutGraphics.createGraphics();
        g2d2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g2d2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
    	
    	PagedRenderer r3 = new PagedRenderer(new DefaultUserAgent(), 72, 1);
    	
    	r3.setDocumentHtml(doc);
    	r3.setImageResolver(new Java2DImageResolver());
    	r3.setFontContext(new Java2DFontContext(g2d2));
    	r3.setFontResolver(new Java2DFontResolver());
    	r3.setReplacedElementResolver(new Java2DReplacedElementResolver());
    	r3.prepare();
    	
    	DisplayList dl = r3.renderToList(pageNo);
    	int height = r3.getPageHeight(pageNo);
    	int width = r3.getPageWidth(pageNo);
    	BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    	
    	Graphics2D g2d = img.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
  	
    	Java2DOut out = new Java2DOut(g2d, RenderingHints.VALUE_ANTIALIAS_OFF);
    	out.render(dl);
    	g2d.dispose();
    	g2d2.dispose();
    	
    	return img;
    }
	
	private static BufferedImage pixelMapToImage(String image, int width)
	{
		int height = image.length() / width;
		BufferedImage target = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		
		for (int i = 0; i < height; i++)
		{
			for (int j = 0; j < width; j++)
			{
				char px = image.charAt((i * width) + j);
				int rgb;
				
				switch (px)
				{
				case 'R':
					rgb = 0xFFFF0000;
					break;
				case 'G':
					rgb = 0xFF00FF00;
					break;
				case 'B':
					rgb = 0xFF0000FF;
					break;
				case 'W':
					rgb = 0xFFFFFFFF;
					break;
				case '#':
					rgb = 0x00000000;
					break;
				default:
					throw new RuntimeException("Unknown pixel char: " + px);
				}
				
				target.setRGB(j, i, rgb); 
			}
		}
		
		return target;
	}
	
	private static boolean bufferedImagesEqual(BufferedImage img1, BufferedImage img2)
	{
	    if (img1.getWidth() != img2.getWidth() || img1.getHeight() != img2.getHeight())
	    	return false;
	    	
	    for (int x = 0; x < img1.getWidth(); x++) 
	    {
	        for (int y = 0; y < img1.getHeight(); y++) 
	        {
	           if (img1.getRGB(x, y) != img2.getRGB(x, y))
	                return false;
	        }
		}
	
		return true;
	}
	
	public static List<String> imageToPixelMap(BufferedImage img)
	{
		int w = img.getWidth();
		int h = img.getHeight();
		List<String> res = new ArrayList<String>(h);

		for (int y = 0; y < h; y++)
		{
			StringBuilder sb = new StringBuilder(w);
			
			for (int x = 0; x < w; x++)
			{
				int rgb = img.getRGB(x, y);
				char ch;
				
				switch (rgb)
				{
				case 0xFFFF0000:
					ch = 'R';
					break;
				case 0xFF00FF00:
					ch = 'G'; 
					break;
				case 0xFF0000FF:
					ch = 'B'; 
					break;
				case 0xFFFFFFFF:
					ch = 'W';
					break;
				case 0x00000000:
					ch = '#';
					break;
				default:
					ch = '_';
				}

				sb.append(ch);
			}
			
			res.add(sb.toString());
		}
		
		return res;
	}
	
	public static void assertImgEquals(String html, String image, int imageWidth, int pageNo, String name)
	{
		BufferedImage original = renderImage(html, pageNo);

		if (!image.isEmpty())
		{
			BufferedImage target = pixelMapToImage(image, imageWidth);
		
			if (bufferedImagesEqual(original, target))
				return;
		}
		
		List<String> pixelMapOriginal = imageToPixelMap(original);
		
		System.err.println("TEST FAILED(" + name + ")");
		System.err.println(" GOT:");

		for (int i = 0; i < pixelMapOriginal.size(); i++)
		{
			System.err.print("\t\"" + pixelMapOriginal.get(i) + "\"");
			
			if (i != pixelMapOriginal.size() - 1)
				System.err.println(" +");
			else
				System.err.println(";");
		}
		
		throw new RuntimeException("Images didn't match");
	}
}
