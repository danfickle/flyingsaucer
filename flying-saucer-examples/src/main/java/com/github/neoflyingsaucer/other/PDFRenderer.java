package com.github.neoflyingsaucer.other;

import com.github.neoflyingsaucer.extend.output.DisplayList;
import com.github.neoflyingsaucer.extend.useragent.UserAgentCallback;
import com.github.neoflyingsaucer.j2dout.Java2DFontContext;
import com.github.neoflyingsaucer.j2dout.Java2DFontResolver;
import com.github.neoflyingsaucer.j2dout.Java2DImageResolver;
import com.github.neoflyingsaucer.j2dout.Java2DOut;
import com.github.neoflyingsaucer.j2dout.Java2DReplacedElementResolver;
import com.github.neoflyingsaucer.pdf2dout.Pdf2FontContext;
import com.github.neoflyingsaucer.pdf2dout.Pdf2FontResolver;
import com.github.neoflyingsaucer.pdf2dout.Pdf2ImageResolver;
import com.github.neoflyingsaucer.pdf2dout.Pdf2Out;
import com.github.neoflyingsaucer.pdf2dout.Pdf2ReplacedElementResolver;
import com.github.neoflyingsaucer.pdf2dout.Pdf2Out.PdfOutMode;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.xhtmlrenderer.renderers.ContinuousRenderer;
import org.xhtmlrenderer.renderers.PagedRenderer;

/**
 * PDFRenderer supports headless rendering of XHTML documents, outputting
 * to PDF format.
 * <p>You can use this utility from the command line by passing in
 * the URL or file location as first parameter, and PDF path as second
 * parameter:</p>
 * <pre>
 * java -cp %classpath% org.xhtmlrenderer.simple.PDFRenderer url pdf
 * </pre>
 *
 * @author Pete Brant
 * @author Patrick Wright
 */
public class PDFRenderer {

    /**
     * Renders the XML file at the given URL as a PDF file
     * at the target location.
     *
     * @param url url for the XML file to render
     * @param pdf path to the PDF file to create
     * @param uac
     * @throws IOException       if the URL or PDF location is
     *                           invalid
     */
    public static void renderToPDF(final String url, final String pdf, final UserAgentCallback uac)
            throws IOException 
    {
    	renderToContinuousImage(url, uac, pdf + ".imgc.png");
    	renderToPagedImage(url, uac, 0, pdf + ".imgp.png");
    	renderToPagedPdf(url, uac, pdf);
    }
    
    
    private static final float PDF_DEFAULT_DOTS_PER_POINT = 20f * 4f / 3f;
    private static final int PDF_DEFAULT_DOTS_PER_PIXEL = 20;
    
    private static void renderToPagedPdf(String url, UserAgentCallback uac, String filename) throws IOException
    {
    	PagedRenderer r3 = new PagedRenderer(uac, PDF_DEFAULT_DOTS_PER_POINT * 72f, PDF_DEFAULT_DOTS_PER_PIXEL);
    	
    	r3.setDocumentUri(url);
    	r3.setImageResolver(new Pdf2ImageResolver(PDF_DEFAULT_DOTS_PER_PIXEL));
    	r3.setFontContext(new Pdf2FontContext());
    	r3.setFontResolver(new Pdf2FontResolver());
    	r3.setReplacedElementResolver(new Pdf2ReplacedElementResolver());
    	r3.prepare();
    	
    	Pdf2Out out = new Pdf2Out(PDF_DEFAULT_DOTS_PER_POINT, PdfOutMode.PRODUCTION_MODE);
    	BufferedOutputStream bs = new BufferedOutputStream(new FileOutputStream(filename));
    	try {
			out.initializePdf(bs);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	out.setPageCount(r3.getPageCount());
    	out.setDocumentInformationDictionary(r3.getHtmlMetadata());
    	
    	for (int i = 0; i < r3.getPageCount(); i++)
    	{
    		DisplayList dl = r3.renderToList(i);
    		int height = r3.getPageHeight(i);
    		int width = r3.getPageWidth(i);
    	
    		out.initializePage(width, height);
    		out.render(dl);
    		out.finishPage();
    	}

    	out.finish();
    	bs.close();
    }

    private static void renderToPagedImage(String url, UserAgentCallback uac, int pageNo, String filename) throws IOException
    {
    	BufferedImage layoutGraphics = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
 
    	Graphics2D g2d2 = layoutGraphics.createGraphics();
        g2d2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    	
    	PagedRenderer r3 = new PagedRenderer(uac, 72, 1);
    	
    	r3.setDocumentUri(url);
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
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
  	
    	Java2DOut out = new Java2DOut(g2d, RenderingHints.VALUE_ANTIALIAS_ON);
    	out.render(dl);
    	g2d.dispose();
    	g2d2.dispose();
    	
    	ImageIO.write(img, "png", new File(filename)); 
    }
    

    private static void renderToContinuousImage(String url, UserAgentCallback uac, String filename) throws IOException
    {
    	BufferedImage layoutGraphics = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
 
    	Graphics2D g2d2 = layoutGraphics.createGraphics();
        g2d2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    	
    	ContinuousRenderer r3 = new ContinuousRenderer(uac);
    	
    	r3.setDocumentUri(url);
    	r3.setImageResolver(new Java2DImageResolver());
    	r3.setFontContext(new Java2DFontContext(g2d2));
    	r3.setFontResolver(new Java2DFontResolver());
    	r3.setReplacedElementResolver(new Java2DReplacedElementResolver());
    	r3.setViewportSize(1000, 1000);
    	
    	DisplayList dl = r3.renderToList();
    	int height = r3.getLayoutHeight();
    	r3.setViewportSize(1000, height);
    	BufferedImage img = new BufferedImage(1000, height, BufferedImage.TYPE_INT_ARGB);
    	
    	Graphics2D g2d = img.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
  	
    	Java2DOut out = new Java2DOut(g2d, RenderingHints.VALUE_ANTIALIAS_ON);
    	out.render(dl);
    	g2d.dispose();
    	g2d2.dispose();
    	
    	ImageIO.write(img, "png", new File(filename)); 
    }
}
