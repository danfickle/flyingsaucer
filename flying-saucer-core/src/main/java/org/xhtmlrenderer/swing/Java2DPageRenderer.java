package org.xhtmlrenderer.swing;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.BufferedImage;

import org.w3c.dom.Document;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.layout.BoxBuilder;
import org.xhtmlrenderer.layout.Layer;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.layout.SharedContext;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.render.PageBox;
import org.xhtmlrenderer.render.RenderingContext;
import org.xhtmlrenderer.render.ViewportBox;
import org.xhtmlrenderer.simple.HtmlNamespaceHandler;

import com.github.neoflyingsaucer.extend.useragent.UserAgentCallback;

public class Java2DPageRenderer 
{
	private Box rootBox;
	private final SharedContext sharedContext;
	private boolean needRelayout;
	private Document doc;
	private final UserAgentCallback cb;
	
	public Java2DPageRenderer(UserAgentCallback cb)
	{
		this.cb = cb;
		this.sharedContext = newSharedContext(cb);
	}
	
    public static BufferedImage renderToImage(Document doc, int maxWidth, int maxHeight, UserAgentCallback cb, int pageNo)  
    {
        Java2DPageRenderer g2r = new Java2DPageRenderer(cb);
        g2r.setDocument(doc);

        Dimension dim = new Dimension(maxWidth, maxHeight);
        BufferedImage buff = new BufferedImage((int) dim.getWidth(), (int) dim.getHeight(), BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = (Graphics2D) buff.getGraphics();
          Dimension size = g2r.print(g, pageNo);
        g.dispose();

        BufferedImage resize = new BufferedImage((int) size.getWidth(), (int) size.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D gr = (Graphics2D) resize.getGraphics();
          gr.drawImage(buff, 0, 0, null);
        gr.dispose();
        
        return resize;
    }
	
    private void setDocument(Document doc2) 
    {
		this.doc = doc2;
	}

	public SharedContext getSharedContext()
	{
        return sharedContext;
    }
    
    private Rectangle getInitialExtents(final LayoutContext c)
    {
        final PageBox first = Layer.createPageBox(c, "first");

        return new Rectangle(0, 0,
               first.getContentWidth(c), first.getContentHeight(c));
    }
    
    public void doDocumentLayout(final Graphics g) 
    {
            if (g == null || doc == null) 
            	return;

            final LayoutContext c = newLayoutContext((Graphics2D) g);

            // final long start = System.currentTimeMillis();

            BlockBox root = (BlockBox) getRootBox();

            if (root != null && isNeedRelayout()) {
                root.reset(c);
            } else {
                root = BoxBuilder.createRootBox(c, doc);
                setRootBox(root);
            }

            root.setContainingBlock(new ViewportBox(getInitialExtents(c)));
            root.layout(c);

            // final long end = System.currentTimeMillis();

            Dimension intrinsicSize = root.getLayer().getPaintingDimension(c);

            if (c.isPrint()) {
                root.getLayer().trimEmptyPages(c, intrinsicSize.height);
                root.getLayer().layoutPages(c);
            }
    }

    protected boolean isNeedRelayout() {
        return needRelayout;
    }

    protected void setNeedRelayout(final boolean needRelayout) {
        this.needRelayout = needRelayout;
    }
    
    private SharedContext newSharedContext(final UserAgentCallback userAgent) 
    {
    	SharedContext context = new SharedContext(userAgent);

        AWTFontResolver fontResolver = new AWTFontResolver();

        context.setFontResolver(fontResolver);
        context.setReplacedElementFactory(new SwingReplacedElementFactory());
        context.setTextRenderer(new Java2DTextRenderer());

        return context;
    }

    private LayoutContext newLayoutContext(final Graphics2D g)
    {
        LayoutContext result = getSharedContext().newLayoutContextInstance();

        Graphics2D layoutGraphics =
            g.getDeviceConfiguration().createCompatibleImage(1, 1).createGraphics();

        result.setFontContext(new Java2DFontContext(layoutGraphics));

        getSharedContext().getTextRenderer().setup(result.getFontContext());

        return result;
    }
    
    public void assignPagePrintPositions(final Graphics2D g) {
        final RenderingContext c = newRenderingContext(g);
        getRootLayer().assignPagePaintingPositions(c, Layer.PAGED_MODE_PRINT);
    }
    
    public RenderingContext newRenderingContext(final Graphics2D g) 
    {
        RenderingContext result = getSharedContext().newRenderingContextInstance();

        result.setFontContext(new Java2DFontContext(g));
        result.setOutputDevice(new Java2DOutputDevice(g));

        getSharedContext().getTextRenderer().setup(result.getFontContext());

        final Box rb = getRootBox();

        if (rb != null) 
            result.setRootLayer(rb.getLayer());

        return result;
    }
	
    public Dimension print(final Graphics g, final int page) 
    {
            final Graphics2D g2 = (Graphics2D) g;
            
            getSharedContext().setPrint(true);
            getSharedContext().setDPI(72f);
            // getSharedContext().getTextRenderer().setSmoothingThreshold(0);
            getSharedContext().setUserAgentCallback(this.cb);
            getSharedContext().setReplacedElementFactory(new SwingReplacedElementFactory());
            getSharedContext().setNamespaceHandler(new HtmlNamespaceHandler());
            getSharedContext().getCss().setDocumentContext(getSharedContext(), getSharedContext().getNamespaceHandler(), doc);

            doDocumentLayout(g2);
            assignPagePrintPositions(g2);
            
            if (page >= getRootLayer().getPages().size()) {
                return null;
            }
            
            // render the document
            Dimension intrinsicSize = paintPage(g2, page);
            
            return intrinsicSize;
    }
    
	public Dimension paintPage(final Graphics2D g, final int pageNo)
	{
		  	final Layer root = getRootLayer();

	        if (root == null) {
	            throw new RuntimeException("Document needs layout");
	        }

	        if (pageNo < 0 || pageNo >= root.getPages().size()) {
	            throw new IllegalArgumentException("Page " + pageNo + " is not between 0 " +
	                    "and " + root.getPages().size());
	        }

	        final RenderingContext c = newRenderingContext(g);

	        final PageBox page = (PageBox) root.getPages().get(pageNo);
	        c.setPageCount(root.getPages().size());
	        c.setPage(pageNo, page);

	        page.paintBackground(c, 0, Layer.PAGED_MODE_PRINT);
	        page.paintMarginAreas(c, 0, Layer.PAGED_MODE_PRINT);
	        page.paintBorder(c, 0, Layer.PAGED_MODE_PRINT);

	        final Shape working = g.getClip();

	        final Rectangle content = page.getPrintClippingBounds(c);
	        g.clip(content);

	        final int top = -page.getPaintingTop() +
	            page.getMarginBorderPadding(c, CalculatedStyle.TOP);

	        final int left = page.getMarginBorderPadding(c, CalculatedStyle.LEFT);

	        g.translate(left, top);
	          root.paint(c);
	        g.translate(-left, -top);

	        g.setClip(working);
	        
	        return new Dimension(page.getWidth(c), page.getHeight(c));
	}
	  
	    public Layer getRootLayer() {
	        return getRootBox() == null ? null : getRootBox().getLayer();
	    }
	    
	    public Box getRootBox() {
	        return rootBox;
	    }

	    public void setRootBox(final Box rootBox) {
	        this.rootBox = rootBox;
	    }
}
