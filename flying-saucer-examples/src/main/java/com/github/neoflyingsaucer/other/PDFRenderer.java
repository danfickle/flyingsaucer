package com.github.neoflyingsaucer.other;

import com.github.neoflyingsaucer.defaultuseragent.DefaultUserAgent;
import com.github.neoflyingsaucer.extend.output.DisplayList;
import com.github.neoflyingsaucer.extend.useragent.UserAgentCallback;
import com.github.neoflyingsaucer.j2dout.Java2DOut;
import com.github.neoflyingsaucer.pdfout.PdfRenderer;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.HashMap;

import javax.imageio.ImageIO;

/**
 * <p/>
 * PDFRenderer supports headless rendering of XHTML documents, outputting
 * to PDF format. There are two static utility methods, one for rendering
 * a {@link java.net.URL}, {@link #renderToPDF(String, String)} and one
 * for rendering a {@link File}, {@link #renderToPDF(File, String)}</p>
 * <p>You can use this utility from the command line by passing in
 * the URL or file location as first parameter, and PDF path as second
 * parameter:
 * <pre>
 * java -cp %classpath% org.xhtmlrenderer.simple.PDFRenderer <url> <pdf>
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
     * @param pdfVersion version of PDF to output; null uses default version
     * @throws IOException       if the URL or PDF location is
     *                           invalid
     * @throws DocumentException if an error occurred
     *                           while building the Document.
     */
    public static void renderToPDF(final String url, final String pdf, final UserAgentCallback uac)
            throws IOException {
//
    	final PdfRenderer renderer = new PdfRenderer(uac);
    	renderer.setDocument(url);  
    	doRenderToPDF(renderer, pdf);
    	
		String html =
				"<html><head><style>" +
				"@page { size: 200px 400px; margin: 1px; }" +
				"body { background-color: #f00; margin: 2px; border: 3px solid blue; height: 100px; border-radius: 8px; color: white; padding: 10px; }" +
				"</style></head><body>TEST TEST LONG TEST LONG TEST LONG TEST LONG TEST LONG TEST LONG TEST LONG TEST</body></html>";
    	
    	
    	// Document doc = HTMLResourceHelper.load(html).getDocument();
    	DisplayList dl = DisplayListRenderer.renderToList(url, 1000, 1000, uac, 0);
    	System.err.println(dl.toString());
    	
    	BufferedImage img = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_ARGB);
    	Graphics2D g2d = img.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
  	
    	Java2DOut out = new Java2DOut(g2d);
    	out.render(dl);
    	g2d.dispose();
    	
    	ImageIO.write(img, "png", new File(pdf + ".img.png")); 
    }

    private static void doRenderToPDF(PdfRenderer renderer, String pdf) throws IOException {
        OutputStream os = null;
        try {
            os = new FileOutputStream(pdf);
            renderer.layout();
            renderer.createPDF(os);

            os.close();
            os = null;
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (final IOException e) {
                    // ignore
                }
            }
        }
		
	}

    /** prints out usage information, with optional error message
     * @param err
     */
    private static void usage(final String err) {
        if (err != null && err.length() > 0) {
            System.err.println("==>" + err);
        }
        System.err.println("Usage: ... url pdf [version]");
        System.err.println("   where version (optional) is between 1.2 and 1.7");
        System.exit(1);
    }
}
