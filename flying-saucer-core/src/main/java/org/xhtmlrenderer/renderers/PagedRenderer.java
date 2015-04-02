package org.xhtmlrenderer.renderers;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Shape;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.displaylist.DlOutputDevice;
import org.xhtmlrenderer.displaylist.DlTextRenderer;
import org.xhtmlrenderer.layout.BoxBuilder;
import org.xhtmlrenderer.layout.Layer;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.layout.SharedContext;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.render.PageBox;
import org.xhtmlrenderer.render.RenderingContext;
import org.xhtmlrenderer.render.ViewportBox;
import org.xhtmlrenderer.resource.ResourceLoadHelper;
import org.xhtmlrenderer.simple.HtmlNamespaceHandler;

import com.github.neoflyingsaucer.displaylist.DisplayListImpl;
import com.github.neoflyingsaucer.extend.output.DisplayList;
import com.github.neoflyingsaucer.extend.output.FontContext;
import com.github.neoflyingsaucer.extend.output.FontResolver;
import com.github.neoflyingsaucer.extend.output.ImageResolver;
import com.github.neoflyingsaucer.extend.output.ReplacedElementResolver;
import com.github.neoflyingsaucer.extend.useragent.HTMLResourceI;
import com.github.neoflyingsaucer.extend.useragent.UserAgentCallback;

public class PagedRenderer 
{
	private Box rootBox;
	private Document doc;
	private String uri;
	private FontContext fontContext;
	private DisplayList displayList;
	
	private final UserAgentCallback cb;
	private final SharedContext sharedContext;
	private final float dpi;
	private final int dpp;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(PagedRenderer.class);
	
	public PagedRenderer(UserAgentCallback cb, float dpi, int dpp)
	{
		this.cb = cb;
		this.sharedContext = newSharedContext(cb);
		this.dpi = dpi;
		this.dpp = dpp;
	}
	
	public void setDocumentUri(String uri)
	{
		this.uri = uri;
	}

	public void setImageResolver(ImageResolver imgResolver)
	{
		sharedContext.setImageResolver(imgResolver);
	}

	public void setFontResolver(FontResolver fontResolver)
	{
		sharedContext.setFontResolver(fontResolver);
	}
	
	public void setReplacedElementResolver(ReplacedElementResolver resolver)
	{
		sharedContext.setReplacedElementResolver(resolver);
		sharedContext.setReplacedElementFactory(null);
	}
	
	public void setFontContext(FontContext ctx)
	{
		fontContext = ctx;
	}
	
	public SharedContext getSharedContext()
	{
        return sharedContext;
    }
	
	public DisplayList renderToList(int pageNo)
	{
		paintPage(pageNo);
		return displayList;
	}
	
    private Rectangle getInitialExtents(LayoutContext c)
    {
        PageBox first = Layer.createPageBox(c, "first");

        return new Rectangle(0, 0, first.getContentWidth(c), first.getContentHeight(c));
    }
	
    private void doDocumentLayout() 
    {
            if (doc == null) 
            	return;

            LayoutContext c = newLayoutContext();

            long start = System.currentTimeMillis();

            BlockBox root = (BlockBox) getRootBox();

            if (root != null) {
                root.reset(c);
            } else {
                root = BoxBuilder.createRootBox(c, doc);
                setRootBox(root);
            }

            root.setContainingBlock(new ViewportBox(getInitialExtents(c)));
            root.layout(c);

            Dimension intrinsicSize = root.getLayer().getPaintingDimension(c);
            
            root.getLayer().trimEmptyPages(c, intrinsicSize.height);
            root.getLayer().layoutPages(c);
            
            long end = System.currentTimeMillis();
            
            LOGGER.info("Layout took " + (end - start) + " milliseconds");
    }

    private SharedContext newSharedContext(final UserAgentCallback userAgent) 
    {
    	SharedContext context = new SharedContext(userAgent);
        context.setTextRenderer(new DlTextRenderer());
        return context;
    }

    private LayoutContext newLayoutContext()
    {
        LayoutContext result = getSharedContext().newLayoutContextInstance();
        result.setFontContext(fontContext);
        getSharedContext().getTextRenderer().setup(result.getFontContext());
        return result;
    }
    
    private RenderingContext newRenderingContext() 
    {
        RenderingContext result = getSharedContext().newRenderingContextInstance();
        result.setFontContext(fontContext);
        
        DlOutputDevice dlOut = new DlOutputDevice(this.displayList, dpi);
        result.setOutputDevice(dlOut);

        getSharedContext().getTextRenderer().setup(result.getFontContext());
        
        dlOut.setRoot(getRootBox());
        dlOut.setSharedContext(sharedContext);
        final Box rb = getRootBox();

        if (rb != null) 
            result.setRootLayer(rb.getLayer());

        return result;
    }
    
    private void assignPagePrintPositions() 
    {
        RenderingContext c = newRenderingContext();
        getRootLayer().assignPagePaintingPositions(c, Layer.PAGED_MODE_PRINT);
    }
	
    public void prepare() 
    {
		HTMLResourceI res = ResourceLoadHelper.loadHtmlDocument(uri, cb);
		sharedContext.setDocumentURI(res.getURI());
		doc = res.getDocument();
    	
    	getSharedContext().setPrint(true);
    	// TODO: Make configurable.
        getSharedContext().setDPI(dpi);
        getSharedContext().setDotsPerPixel(dpp);
        getSharedContext().setUserAgentCallback(this.cb);
        getSharedContext().setNamespaceHandler(new HtmlNamespaceHandler());
        getSharedContext().getCss().setDocumentContext(getSharedContext(), getSharedContext().getNamespaceHandler(), doc);

        doDocumentLayout();
        assignPagePrintPositions();
    }
    
	private Dimension paintPage(int pageNo)
	{
		  	Layer root = getRootLayer();
		  	displayList = new DisplayListImpl();

	        if (pageNo < 0 || pageNo >= root.getPages().size()) {
	            throw new IllegalArgumentException("Page " + pageNo + " is not between 0 " + "and " + root.getPages().size());
	        }

	        RenderingContext c = newRenderingContext();

	        PageBox page = root.getPages().get(pageNo);
	        c.setPageCount(root.getPages().size());
	        c.setPage(pageNo, page);

	        page.paintBackground(c, 0, Layer.PAGED_MODE_PRINT);
	        page.paintMarginAreas(c, 0, Layer.PAGED_MODE_PRINT);
	        page.paintBorder(c, 0, Layer.PAGED_MODE_PRINT);

	        // TODO: Clipping not working!
	        //Shape working = c.getOutputDevice().getClip();
	        //Rectangle content = page.getPrintClippingBounds(c);
	        //c.getOutputDevice().clip(content);

	        int top = -page.getPaintingTop() + page.getMarginBorderPadding(c, CalculatedStyle.TOP);
	        int left = page.getMarginBorderPadding(c, CalculatedStyle.LEFT);

	        c.getOutputDevice().translate(left, top);
	          root.paint(c);
	        c.getOutputDevice().translate(-left, -top);

	        //c.getOutputDevice().setClip(working);
	        
	        return new Dimension(page.getWidth(c), page.getHeight(c));
	}

	public Layer getRootLayer() {
        return getRootBox() == null ? null : getRootBox().getLayer();
	}
	    
	public Box getRootBox() {
	    return rootBox;
	}

	private void setRootBox(Box rootBox) {
	    this.rootBox = rootBox;
	}
	
	public int getPageCount()
	{
		return getRootLayer().getPages().size();
	}

	public int getPageHeight(int pageNo) 
	{
		RenderingContext c = newRenderingContext();
		return getRootLayer().getPages().get(pageNo).getHeight(c);
	}
	
	public int getPageWidth(int pageNo) 
	{
		RenderingContext c = newRenderingContext();
		return getRootLayer().getPages().get(pageNo).getWidth(c);
	}	
}
