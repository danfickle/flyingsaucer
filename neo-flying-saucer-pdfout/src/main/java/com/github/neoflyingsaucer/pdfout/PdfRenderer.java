package com.github.neoflyingsaucer.pdfout;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Shape;
import java.io.OutputStream;
import java.util.List;

import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xhtmlrenderer.context.StyleReference;
import org.xhtmlrenderer.css.parser.property.PageSize;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.extend.FontContext;
import org.xhtmlrenderer.extend.FontResolver;
import org.xhtmlrenderer.extend.NamespaceHandler;
import org.xhtmlrenderer.extend.ReplacedElementFactory;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.layout.BoxBuilder;
import org.xhtmlrenderer.layout.Layer;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.layout.SharedContext;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.render.PageBox;
import org.xhtmlrenderer.render.RenderingContext;
import org.xhtmlrenderer.render.ViewportBox;
import org.xhtmlrenderer.resource.HTMLResource;
import org.xhtmlrenderer.resource.ResourceLoadHelper;
import org.xhtmlrenderer.simple.HtmlNamespaceHandler;

import com.github.pdfstream.PDF;
import com.github.pdfstream.Page;

public class PdfRenderer
{
	private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(PdfRenderer.class);
	
	// These two defaults combine to produce an effective resolution of 96 px to
    // the inch
    private static final float DEFAULT_DOTS_PER_POINT = 20f * 4f / 3f;
    private static final int DEFAULT_DOTS_PER_PIXEL = 20;

    private final SharedContext _sharedContext;
    private final PdfOutputDevice _outputDevice;
    private final float _dotsPerPoint;
    private final PdfTextRenderer _textRenderer;
    
    private BlockBox _root;
	private Document _doc;
	private PDF _pdfDoc;

    public PdfRenderer(UserAgentCallback uac)
    {
        this(DEFAULT_DOTS_PER_POINT, DEFAULT_DOTS_PER_PIXEL, uac);
    }
	
    public PdfRenderer(final float dotsPerPoint, final int dotsPerPixel, final UserAgentCallback uac) 
    {
        _dotsPerPoint = dotsPerPoint;

        _outputDevice = new PdfOutputDevice(dotsPerPoint);

        final UserAgentCallback old = uac;
        final UserAgentCallback newish = new PdfOutUserAgent(old);

        _sharedContext = new SharedContext();
        _sharedContext.setUserAgentCallback(newish);
        _sharedContext.setCss(new StyleReference(newish));
        //userAgent.setSharedContext(_sharedContext);
        _outputDevice.setSharedContext(_sharedContext);
  
        final FontResolver fr = new PdfFontResolver();
        _sharedContext.setFontResolver(fr);
        _textRenderer = new PdfTextRenderer();

        final ReplacedElementFactory re = new PdfReplacedElementFactory();        
		_sharedContext.setReplacedElementFactory(re);
        _sharedContext.setTextRenderer(_textRenderer);
        _sharedContext.setDPI(72 * _dotsPerPoint);
        _sharedContext.setDotsPerPixel(dotsPerPixel);
        _sharedContext.setPrint(true);
    }
    
    private Document loadDocument(final String uri) {
    	HTMLResource rs = ResourceLoadHelper.loadHtmlDocument(uri, _sharedContext.getUac());
    	_sharedContext.setDocumentURI(rs.getURI());
    	return rs.getDocument();
    }
    
    public void setDocument(final String uri) {
        setDocument(loadDocument(uri), uri);
    }
    
    public void setDocument(final Document doc, final String url) {
        setDocument(doc, url, new HtmlNamespaceHandler());
    }
    
    public void setDocument(final Document doc, final String url, final NamespaceHandler nsh) 
    {
        _doc = doc;

        //getFontResolver().flushFontFaceFonts();
		final Rectangle rect = new Rectangle(0, 0, (int) PageSize.A4.getPageWidth().getFloatValue(), (int) PageSize.A4.getPageHeight().getFloatValue());
        _sharedContext.reset();
		_sharedContext.set_TempCanvas(rect);
        _sharedContext.setBaseURL(url);
        _sharedContext.setNamespaceHandler(nsh);
        _sharedContext.getCss().setDocumentContext(_sharedContext, _sharedContext.getNamespaceHandler(), doc);
        //getFontResolver().importFontFaces(_sharedContext.getCss().getFontFaceRules());
    }

    public void layout() 
    {
        final LayoutContext c = newLayoutContext();
        final BlockBox root = BoxBuilder.createRootBox(c, _doc);
        
        root.setContainingBlock(new ViewportBox(getInitialExtents(c)));
        root.layout(c);
        
        final Dimension dim = root.getLayer().getPaintingDimension(c);
        root.getLayer().trimEmptyPages(c, dim.height);
        root.getLayer().layoutPages(c);
        
        _root = root;
    }
    
    private Rectangle getInitialExtents(final LayoutContext c)
    {
        final PageBox first = Layer.createPageBox(c, "first");
        return new Rectangle(0, 0, first.getContentWidth(c), first.getContentHeight(c));
    }

    private LayoutContext newLayoutContext() 
    {
        final LayoutContext result = _sharedContext.newLayoutContextInstance();
        result.setFontContext(new FontContext() {
		});

        _sharedContext.getTextRenderer().setup(result.getFontContext());

        return result;
    }
    
    private RenderingContext newRenderingContext() 
    {
        final RenderingContext result = _sharedContext.newRenderingContextInstance();
        result.setFontContext(new FontContext() {
		});

        result.setOutputDevice(_outputDevice);

        _sharedContext.getTextRenderer().setup(result.getFontContext());

        result.setRootLayer(_root.getLayer());

        return result;
    }
    
    private void paintPage(final RenderingContext c, final PDF writer, final PageBox page) 
    {
        //provideMetadataToPage(writer, page);

        page.paintBackground(c, 0, Layer.PAGED_MODE_PRINT);
        page.paintMarginAreas(c, 0, Layer.PAGED_MODE_PRINT);
        page.paintBorder(c, 0, Layer.PAGED_MODE_PRINT);

        final Shape working = _outputDevice.getClip();

        final Rectangle content = page.getPrintClippingBounds(c);
        _outputDevice.clip(content);

        final int top = -page.getPaintingTop() + page.getMarginBorderPadding(c, CalculatedStyle.TOP);

        final int left = page.getMarginBorderPadding(c, CalculatedStyle.LEFT);

        _outputDevice.translate(left, top);
        _root.getLayer().paint(c);
        _outputDevice.translate(-left, -top);

        _outputDevice.setClip(working);
    }
    
    private void writePDF(final List<PageBox> pages, final RenderingContext c, final float[] firstPageSize, final PDF doc) throws Exception {
        _outputDevice.setRoot(_root);

        _outputDevice.start(_doc);
        //

        _root.getLayer().assignPagePaintingPositions(c, Layer.PAGED_MODE_PRINT);

        final int pageCount = _root.getLayer().getPages().size();

        doc.setPageCount(pageCount);
        
        c.setPageCount(pageCount);
        c.setOutputDevice(_outputDevice);
        setDidValues(doc);

        for (int i = 0; i < pageCount; i++) 
        {
        	LOGGER.info("Painting page {} of {}", (i + 1), pageCount);
        	
        	final PageBox currentPage = pages.get(i);
            c.setPage(i, currentPage);

            float[] nextPageSize = new float[] { 
            		currentPage.getWidth(c) / _dotsPerPoint,
            		currentPage.getHeight(c) / _dotsPerPoint };
            Page page;
            try {
					page = new Page(_pdfDoc, nextPageSize);
				} catch (Exception e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				}
            
            _outputDevice.initializePage(page, nextPageSize[1]);
            paintPage(c, _pdfDoc, currentPage);
            _outputDevice.finishPage();
        }

        _outputDevice.finish(c, _root);
    }

    /**
     * Sets the document information dictionary values from html metadata
     */
    private void setDidValues(final PDF doc) 
    {
    	_outputDevice.setDidValues(doc);
    }
    
    /**
     * NOTE: Caller is responsible for cleaning up the OutputStream if
     * something goes wrong.
     */
    public void createPDF(final OutputStream os) 
    {
        final List<PageBox> pages = _root.getLayer().getPages();

        //UserAgentCallback old = _sharedContext.getUac();
        
        final RenderingContext c = newRenderingContext();
        
        c.setInitialPageNo(1);
        final PageBox firstPage = (PageBox) pages.get(0);
        float[] firstPageSize = new float[] { 
        		firstPage.getWidth(c) / _dotsPerPoint,
        		firstPage.getHeight(c) / _dotsPerPoint };
        
        try {
			_pdfDoc = new PDF(os);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        try {
        	writePDF(pages, c, firstPageSize, _pdfDoc);
        	_pdfDoc.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
