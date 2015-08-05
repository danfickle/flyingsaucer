package com.github.neoflyingsaucer.browser;

import java.awt.FileDialog;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;

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
import com.github.neoflyingsaucer.renderers.ContinuousRenderer;
import com.github.neoflyingsaucer.renderers.PagedRenderer;

public class ExportAction extends AbstractAction
{
	private static final long serialVersionUID = 1L;
	private final String _action;
	private final BrowserFrame _browser;
	
	public ExportAction(String action, BrowserFrame browser)
	{
		super(action);
		_action = action;
		_browser = browser;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) 
	{
		if (_action.equals("pdf"))
		{
			exportToPdf();
		}
		else if (_action.equals("paged-image"))
		{
			exportToPagedImage();
		}
		else if (_action.equals("continuous-image"))
		{
			exportToContinuousImage();
		}
	}

    private void exportToPdf()
    {
    	FileDialog fd = new FileDialog(_browser, "Save as PDF", FileDialog.SAVE);
    	fd.setVisible(true);
    	if (fd.getFile() != null)
    	{
    		File outTarget = new File(fd.getDirectory(), fd.getFile());

    		try {
				renderToPagedPdf(_browser.getCurrentDemo(), _browser.getUac(), outTarget.getAbsolutePath());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    }

    private void exportToPagedImage()
    {
    	FileDialog fd = new FileDialog(_browser, "Save as Paged Image", FileDialog.SAVE);
    	fd.setVisible(true);
    	if (fd.getFile() != null)
    	{
    		File outTarget = new File(fd.getDirectory(), fd.getFile());
    		String path = outTarget.getAbsolutePath();
    		
        	int ext = path.lastIndexOf('.');
        	String filename;
        	
        	if (ext != -1)
        		filename = path.substring(0, ext);
        	else
        		filename = path;

    		try {
				renderToPagedImage(_browser.getCurrentDemo(), _browser.getUac(), filename);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    }
    
    private void exportToContinuousImage()
    {
    	FileDialog fd = new FileDialog(_browser, "Save as Continuous Image", FileDialog.SAVE);
    	fd.setVisible(true);
    	if (fd.getFile() != null)
    	{
    		File outTarget = new File(fd.getDirectory(), fd.getFile());
   		
    		try {
				renderToContinuousImage(_browser.getCurrentDemo(), _browser.getUac(), outTarget.getAbsolutePath());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    }
    
    // These result in a DPI of 96DPI.
    private static final float PDF_DEFAULT_DOTS_PER_POINT = 20f * 4f / 3f;
    private static final int PDF_DEFAULT_DOTS_PER_PIXEL = 20;
    
    private static void renderToPagedPdf(String url, UserAgentCallback uac, String filename) throws IOException
    {
    	// Optional.
    	// FSErrorController.setThreadErrorHandler(handler);
    	// FSCancelController.setThreadCancelHandler(handler);
   	
    	PagedRenderer r3 = new PagedRenderer(uac, PDF_DEFAULT_DOTS_PER_POINT * 72f, PDF_DEFAULT_DOTS_PER_PIXEL);

    	Pdf2Out out = new Pdf2Out(PDF_DEFAULT_DOTS_PER_POINT, PdfOutMode.TEST_MODE);
    	BufferedOutputStream bs = new BufferedOutputStream(new FileOutputStream(filename));
    	try {
			out.initializePdf(bs);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	Pdf2FontResolver fontResolver = new Pdf2FontResolver(out.getDocument());
    	
    	r3.setDocumentUri(url);
    	r3.setImageResolver(new Pdf2ImageResolver(PDF_DEFAULT_DOTS_PER_PIXEL));
    	r3.setFontContext(new Pdf2FontContext());
    	r3.setFontResolver(fontResolver);
    	r3.setReplacedElementResolver(new Pdf2ReplacedElementResolver());
    	r3.prepare();

    	out.setDocumentInformationDictionary(r3.getHtmlMetadata());
    	
    	for (int i = 0; i < r3.getPageCount(); i++)
    	{
    		int height = r3.getPageHeight(i);
    		int width = r3.getPageWidth(i);
    		DisplayList dl = r3.renderToList(i);

    		out.initializePage(width, height);
    		out.render(dl);
    		out.finishPage();
    	}

    	out.finish();
    	bs.close();
    }
    
    private static void renderToPagedImage(String url, UserAgentCallback uac, String filename) throws IOException
    {
    	BufferedImage layoutGraphics = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
 
    	Graphics2D g2d2 = layoutGraphics.createGraphics();
        g2d2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    	
        // 72 DPI.
    	PagedRenderer r3 = new PagedRenderer(uac, 72, 1);
    	
    	r3.setDocumentUri(url);
    	r3.setImageResolver(new Java2DImageResolver());
    	r3.setFontContext(new Java2DFontContext(g2d2));
    	r3.setFontResolver(new Java2DFontResolver());
    	r3.setReplacedElementResolver(new Java2DReplacedElementResolver());
    	r3.prepare();
    	
    	for (int pageNo = 0; pageNo < r3.getPageCount(); pageNo++)
    	{
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
    	
    	   	ImageIO.write(img, "png", new File(filename + ".page-" + (pageNo + 1) + ".png"));
    	}

	   	g2d2.dispose();
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
