package org.xhtmlrenderer.renderers;

import java.awt.Rectangle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xhtmlrenderer.displaylist.DlOutputDevice;
import org.xhtmlrenderer.displaylist.DlTextRenderer;
import org.xhtmlrenderer.layout.BoxBuilder;
import org.xhtmlrenderer.layout.Layer;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.layout.SharedContext;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.render.Box;
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

public class ContinuousRenderer 
{
	private Box rootBox;
	private Document doc;
	private String uri;
	private FontContext fontContext;
	private Rectangle viewportSize;

	private final UserAgentCallback cb;
	private final SharedContext sharedContext;
	private final DisplayList displayList;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ContinuousRenderer.class);
	
	public ContinuousRenderer(UserAgentCallback cb)
	{
		this.cb = cb;
		this.sharedContext = newSharedContext(cb);
		this.displayList = new DisplayListImpl();
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
	
	public void setViewportSize(int width, int height)
	{
		viewportSize = new Rectangle(width, height);
	}
	
	public SharedContext getSharedContext()
	{
        return sharedContext;
    }
	
	public DisplayList renderToList()
	{
		HTMLResourceI res = ResourceLoadHelper.loadHtmlDocument(uri, cb);
		sharedContext.setDocumentURI(res.getURI());
		doc = res.getDocument();
		
		print();
		return displayList;
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

            sharedContext.set_TempCanvas(viewportSize);
            root.setContainingBlock(new ViewportBox(viewportSize));
            root.layout(c);

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

        DlOutputDevice dlOut = new DlOutputDevice(this.displayList, 72f);
        result.setOutputDevice(dlOut);

        getSharedContext().getTextRenderer().setup(result.getFontContext());

        Box rb = getRootBox();
        dlOut.setRoot(rb);
        dlOut.setSharedContext(sharedContext);
        
        if (rb != null) 
            result.setRootLayer(rb.getLayer());

        return result;
    }
	
    private void print() 
    {
            getSharedContext().setPrint(false);
            getSharedContext().setDPI(72f);
            getSharedContext().setUserAgentCallback(this.cb);
            getSharedContext().setNamespaceHandler(new HtmlNamespaceHandler());
            getSharedContext().getCss().setDocumentContext(getSharedContext(), getSharedContext().getNamespaceHandler(), doc);

            doDocumentLayout();
            
            RenderingContext c = newRenderingContext();
            
            getRootLayer().paint(c);
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

	public int getLayoutHeight() {
		return getRootBox().getHeight();
	}
}
