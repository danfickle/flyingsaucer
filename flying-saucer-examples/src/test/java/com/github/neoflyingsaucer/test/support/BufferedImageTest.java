package com.github.neoflyingsaucer.test.support;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.xhtmlrenderer.swing.Java2DPageRenderer;
import com.github.neoflyingsaucer.defaultuseragent.DefaultUserAgent;
import com.github.neoflyingsaucer.defaultuseragent.HTMLResourceHelper;

public class BufferedImageTest
{
	private static BufferedImage renderImage(String html, int width, int height)
	{
		Document doc = HTMLResourceHelper.load(html).getDocument();
		BufferedImage buff = Java2DPageRenderer.renderToImage(doc, width, height, new DefaultUserAgent(), 0);
		return buff;
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
	
	
	public static void assertImgEquals(String html, String image, int imageWidth, int maxWidth, int maxHheight)
	{
		BufferedImage original = renderImage(html, maxWidth, maxHheight);

		if (!image.isEmpty())
		{
			BufferedImage target = pixelMapToImage(image, imageWidth);
		
			if (bufferedImagesEqual(original, target))
				return;
		}
		
		List<String> pixelMapOriginal = imageToPixelMap(original);
		
		System.out.println("TEST FAILED");
		System.out.println(" GOT:");

		for (int i = 0; i < pixelMapOriginal.size(); i++)
		{
			System.out.print("\t\"" + pixelMapOriginal.get(i) + "\"");
			
			if (i != pixelMapOriginal.size() - 1)
				System.out.println(" +");
			else
				System.out.println(";");
		}
		
		throw new RuntimeException("Images didn't match");
	}
}
