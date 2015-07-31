package com.github.neoflyingsaucer.renderers;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;

import com.github.neoflyingsaucer.css.sheet.FontFaceRule;
import com.github.neoflyingsaucer.displaylist.DisplayListImpl;
import com.github.neoflyingsaucer.displaylist.DlOutputDevice;
import com.github.neoflyingsaucer.displaylist.DlTextRenderer;
import com.github.neoflyingsaucer.extend.output.DisplayList;
import com.github.neoflyingsaucer.extend.output.FSFontFaceItem;
import com.github.neoflyingsaucer.extend.output.FontContext;
import com.github.neoflyingsaucer.extend.output.FontResolver;
import com.github.neoflyingsaucer.extend.output.ImageResolver;
import com.github.neoflyingsaucer.extend.output.ReplacedElementResolver;
import com.github.neoflyingsaucer.extend.useragent.HTMLResourceI;
import com.github.neoflyingsaucer.extend.useragent.Optional;
import com.github.neoflyingsaucer.extend.useragent.UserAgentCallback;
import com.github.neoflyingsaucer.layout.BoxBuilder;
import com.github.neoflyingsaucer.layout.Layer;
import com.github.neoflyingsaucer.layout.LayoutContext;
import com.github.neoflyingsaucer.layout.SharedContext;
import com.github.neoflyingsaucer.render.BlockBox;
import com.github.neoflyingsaucer.render.Box;
import com.github.neoflyingsaucer.render.RenderingContext;
import com.github.neoflyingsaucer.render.ViewportBox;
import com.github.neoflyingsaucer.resource.ResourceLoadHelper;
import com.github.neoflyingsaucer.simple.HtmlNamespaceHandler;

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

            BlockBox root = (BlockBox) getRootBox();

            if (root != null) {
                root.reset(c);
            } else {
                root = BoxBuilder.createRootBox(c, doc);
                setRootBox(root);
            }

            sharedContext.getFontResolver().importFontFaceItems(getFontFaceItems());
            
            sharedContext.set_TempCanvas(viewportSize);
            root.setContainingBlock(new ViewportBox(viewportSize));
            root.layout(c);
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

        DlOutputDevice dlOut = new DlOutputDevice(this.displayList, getSharedContext(), getRootBox());
        result.setOutputDevice(dlOut);

        getSharedContext().getTextRenderer().setup(result.getFontContext());

        Box rb = getRootBox();
        
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
	
	public List<FSFontFaceItem> getFontFaceItems()
	{
		List<FSFontFaceItem> faces = new ArrayList<FSFontFaceItem>();
		
		for (FontFaceRule rule : sharedContext.getCss().getFontFaceRules())
		{
			Optional<FSFontFaceItem> item = rule.getFontFaceItem(sharedContext);
			
			if (item.isPresent())
				faces.add(item.get());
		}

		return faces;
	}
}
