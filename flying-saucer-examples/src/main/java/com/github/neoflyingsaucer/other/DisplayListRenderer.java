package com.github.neoflyingsaucer.other;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import org.w3c.dom.Document;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.displaylist.DlOutputDevice;
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
import org.xhtmlrenderer.swing.Java2DFontContext;
import org.xhtmlrenderer.swing.Java2DTextRenderer;
import org.xhtmlrenderer.swing.SwingReplacedElementFactory;

import com.github.neoflyingsaucer.displaylist.DisplayListImpl;
import com.github.neoflyingsaucer.extend.useragent.UserAgentCallback;
import com.github.neoflyingsaucer.j2dout.Java2DFontResolver;

public class DisplayListRenderer 
{
	private Box rootBox;
	private final SharedContext sharedContext;
	private boolean needRelayout;
	private Document doc;
	private final UserAgentCallback cb;
	private final DisplayListImpl displayList;
	
	public DisplayListRenderer(UserAgentCallback cb)
	{
		this.cb = cb;
		this.sharedContext = newSharedContext(cb);
		this.displayList = new DisplayListImpl();
	}
	
    public static DisplayListImpl renderToList(Document doc, int maxWidth, int maxHeight, UserAgentCallback cb, int pageNo)  
    {
        DisplayListRenderer g2r = new DisplayListRenderer(cb);
        g2r.setDocument(doc);
        g2r.print(pageNo);
        
        return g2r.displayList;
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
    
    public void doDocumentLayout() 
    {
            if (doc == null) 
            	return;

            final LayoutContext c = newLayoutContext();

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

        Java2DFontResolver fontResolver = new Java2DFontResolver();

        context.setFontResolver(fontResolver);
        context.setReplacedElementFactory(new SwingReplacedElementFactory());
        context.setTextRenderer(new Java2DTextRenderer());

        return context;
    }

    private LayoutContext newLayoutContext()
    {
        LayoutContext result = getSharedContext().newLayoutContextInstance();

        BufferedImage bi = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        
        Graphics2D layoutGraphics =
            bi.createGraphics();

        result.setFontContext(new Java2DFontContext(layoutGraphics));

        getSharedContext().getTextRenderer().setup(result.getFontContext());

        return result;
    }
    
    public void assignPagePrintPositions() {
        final RenderingContext c = newRenderingContext();
        getRootLayer().assignPagePaintingPositions(c, Layer.PAGED_MODE_PRINT);
    }
    
    public RenderingContext newRenderingContext() 
    {
        RenderingContext result = getSharedContext().newRenderingContextInstance();

        BufferedImage bi = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        
        Graphics2D layoutGraphics =
            bi.createGraphics();

        result.setFontContext(new Java2DFontContext(layoutGraphics));
        result.setOutputDevice(new DlOutputDevice(this.displayList));

        getSharedContext().getTextRenderer().setup(result.getFontContext());

        final Box rb = getRootBox();

        if (rb != null) 
            result.setRootLayer(rb.getLayer());

        return result;
    }
	
    public Dimension print(final int page) 
    {
            getSharedContext().setPrint(true);
            getSharedContext().setDPI(72f);
            getSharedContext().getTextRenderer().setSmoothingThreshold(0);
            getSharedContext().setUserAgentCallback(this.cb);
            getSharedContext().setReplacedElementFactory(new SwingReplacedElementFactory());
            getSharedContext().setNamespaceHandler(new HtmlNamespaceHandler());
            getSharedContext().getCss().setDocumentContext(getSharedContext(), getSharedContext().getNamespaceHandler(), doc);

            doDocumentLayout();
            assignPagePrintPositions();
            
            if (page >= getRootLayer().getPages().size()) {
                return null;
            }
            
            // render the document
            Dimension intrinsicSize = paintPage(page);
            
            return intrinsicSize;
    }
    
	public Dimension paintPage(final int pageNo)
	{
		  	final Layer root = getRootLayer();

	        if (root == null) {
	            throw new RuntimeException("Document needs layout");
	        }

	        if (pageNo < 0 || pageNo >= root.getPages().size()) {
	            throw new IllegalArgumentException("Page " + pageNo + " is not between 0 " +
	                    "and " + root.getPages().size());
	        }

	        final RenderingContext c = newRenderingContext();

	        final PageBox page = (PageBox) root.getPages().get(pageNo);
	        c.setPageCount(root.getPages().size());
	        c.setPage(pageNo, page);

	        page.paintBackground(c, 0, Layer.PAGED_MODE_PRINT);
	        page.paintMarginAreas(c, 0, Layer.PAGED_MODE_PRINT);
	        page.paintBorder(c, 0, Layer.PAGED_MODE_PRINT);

	        //final Shape working = g.getClip();

	        final Rectangle content = page.getPrintClippingBounds(c);
	        //g.clip(content);

	        final int top = -page.getPaintingTop() +
	            page.getMarginBorderPadding(c, CalculatedStyle.TOP);

	        final int left = page.getMarginBorderPadding(c, CalculatedStyle.LEFT);

	        //g.translate(left, top);
	          root.paint(c);
	       // g.translate(-left, -top);

	        //g.setClip(working);
	        
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
