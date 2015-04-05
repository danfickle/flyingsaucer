package org.xhtmlrenderer.displaylist;

import java.awt.Rectangle;
import java.awt.RenderingHints.Key;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.BasicStroke;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.css.parser.FSCMYKColor;
import org.xhtmlrenderer.css.parser.FSColor;
import org.xhtmlrenderer.css.parser.FSRGBColor;
import org.xhtmlrenderer.css.parser.property.PrimitivePropertyBuilders.BookmarkLevel;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.css.style.derived.FSLinearGradient;
import org.xhtmlrenderer.css.style.derived.FSLinearGradient.StopValue;
import org.xhtmlrenderer.extend.NamespaceHandler;
import org.xhtmlrenderer.extend.OutputDevice;
import org.xhtmlrenderer.layout.SharedContext;
import org.xhtmlrenderer.render.AbstractOutputDevice;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.render.BorderPainter;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.render.InlineLayoutBox;
import org.xhtmlrenderer.render.InlineText;
import org.xhtmlrenderer.render.PageBox;
import org.xhtmlrenderer.render.RenderingContext;

import com.github.neoflyingsaucer.displaylist.DlInstruction;
import com.github.neoflyingsaucer.displaylist.DlInstruction.DlAntiAliasDefault;
import com.github.neoflyingsaucer.displaylist.DlInstruction.DlAntiAliasOff;
import com.github.neoflyingsaucer.displaylist.DlInstruction.DlBookmark;
import com.github.neoflyingsaucer.displaylist.DlInstruction.DlCMYKColor;
import com.github.neoflyingsaucer.displaylist.DlInstruction.DlClip;
import com.github.neoflyingsaucer.displaylist.DlInstruction.DlDrawShape;
import com.github.neoflyingsaucer.displaylist.DlInstruction.DlExternalLink;
import com.github.neoflyingsaucer.displaylist.DlInstruction.DlFont;
import com.github.neoflyingsaucer.displaylist.DlInstruction.DlGlyphVector;
import com.github.neoflyingsaucer.displaylist.DlInstruction.DlImage;
import com.github.neoflyingsaucer.displaylist.DlInstruction.DlInternalLink;
import com.github.neoflyingsaucer.displaylist.DlInstruction.DlLine;
import com.github.neoflyingsaucer.displaylist.DlInstruction.DlLinearGradient;
import com.github.neoflyingsaucer.displaylist.DlInstruction.DlOpacity;
import com.github.neoflyingsaucer.displaylist.DlInstruction.DlOval;
import com.github.neoflyingsaucer.displaylist.DlInstruction.DlRGBColor;
import com.github.neoflyingsaucer.displaylist.DlInstruction.DlRectangle;
import com.github.neoflyingsaucer.displaylist.DlInstruction.DlReplaced;
import com.github.neoflyingsaucer.displaylist.DlInstruction.DlSetClip;
import com.github.neoflyingsaucer.displaylist.DlInstruction.DlString;
import com.github.neoflyingsaucer.displaylist.DlInstruction.DlStringEx;
import com.github.neoflyingsaucer.displaylist.DlInstruction.DlStroke;
import com.github.neoflyingsaucer.displaylist.DlInstruction.DlTranslate;
import com.github.neoflyingsaucer.displaylist.DlInstruction.Operation;
import com.github.neoflyingsaucer.extend.output.DisplayList;
import com.github.neoflyingsaucer.extend.output.FSFont;
import com.github.neoflyingsaucer.extend.output.FSGlyphVector;
import com.github.neoflyingsaucer.extend.output.FSImage;
import com.github.neoflyingsaucer.extend.output.JustificationInfo;
import com.github.neoflyingsaucer.extend.output.ReplacedElement;
import com.github.neoflyingsaucer.extend.useragent.Optional;

/**
 * This class outputs the result of rendering to an in-memory data structure that can then be
 * read by the Java2D out and PDF2 out modules.
 * 
 * This provides a level of abstraction and loosens coupling between the ultimate output modules and the core.
 * In fact the ultimate Java2D and PDF2 output modules do not even depend directly on the core module.
 * 
 * See {@link DlInstruction}, {@link OutputDevice}
 * @author daniel
 */
public class DlOutputDevice extends AbstractOutputDevice implements OutputDevice 
{
	private final DisplayList dl;
    private final SharedContext sharedContext;
    private final Box root;
    private static final Logger LOGGER = LoggerFactory.getLogger(DlOutputDevice.class);
    
    private Area clip;
    private Stroke stroke;
    private Object renderingHint = RenderingHints.VALUE_ANTIALIAS_DEFAULT;
	
	public DlOutputDevice(DisplayList displayList, SharedContext ctx, Box root) 
	{
		this.dl = displayList;
		this.sharedContext = ctx;
		this.root = root;
	}

	/**
	 * See {@link DlString}
	 * @param s
	 * @param x
	 * @param y
	 */
	public void drawString(String s, float x, float y)
	{
		dl.add(new DlInstruction.DlString(s, x, y));
	}
	
	/**
	 * See {@link DlStringEx}
	 * @param s
	 * @param x
	 * @param y
	 * @param info
	 */
	public void drawString(String s, float x, float y, JustificationInfo info)
	{
		dl.add(new DlInstruction.DlStringEx(s, x, y, info));
	}
	
	/**
	 * See {@link DlGlyphVector}
	 * @param vec
	 * @param x
	 * @param y
	 */
	public void drawGlyphVector(FSGlyphVector vec, float x, float y)
	{
		dl.add(new DlInstruction.DlGlyphVector(vec, x, y));
	}
	
	/**
	 * See {@link DlOpacity}
	 */
	@Override
	public void setOpacity(float opacity) 
	{
		dl.add(new DlInstruction.DlOpacity(opacity));
	}

	/**
	 * See {@link DlLine}
	 */
	@Override
    protected void drawLine(int x1, int y1, int x2, int y2) 
    {
    	dl.add(new DlInstruction.DlLine(x1, y1, x2, y2));
    }

	/**
	 * See {@link DlTranslate}
	 */
	@Override
	public void translate(double tx, double ty) 
	{
		dl.add(new DlInstruction.DlTranslate(tx, ty));
	}

	/**
	 * See {@link DlStroke}
	 */
	@Override
	public void setStroke(Stroke s) 
	{
		assert(s instanceof BasicStroke);
		
		if (!(s instanceof BasicStroke))
			return;
		
		BasicStroke basic = (BasicStroke) s;
		
		stroke = basic;
		
		dl.add(new DlInstruction.DlStroke(basic));
	}

	/**
	 * Set color. We support CMYK, RGB and RGBA colors.
	 * See {@link DlRGBColor}, {@link DlCMYKColor}
	 */
    @Override
    public void setColor(final FSColor color) 
    {
    	assert(color instanceof FSRGBColor || 
    	       color instanceof FSCMYKColor);
    	
    	if (color instanceof FSRGBColor) 
        {
            FSRGBColor rgb = (FSRGBColor) color;
            dl.add(new DlInstruction.DlRGBColor(rgb.getRed(), rgb.getGreen(), rgb.getBlue(), (int) (rgb.getAlpha() * 255)));
        }
        else if (color instanceof FSCMYKColor)
        {
        	FSCMYKColor cmyk = (FSCMYKColor) color;
        	dl.add(new DlInstruction.DlCMYKColor(cmyk.getCyan(), cmyk.getMagenta(), cmyk.getYellow(), cmyk.getBlack()));
        }
    }
	
    /**
     * See {@link DlRectangle}
     */
	@Override
	public void fillRect(int x, int y, int width, int height) 
	{
		dl.add(new DlInstruction.DlRectangle(x, y, width, height, Operation.FILL));
	}
    
	/**
	 * See {@link DlRectangle}
	 */
	@Override
	public void drawRect(int x, int y, int width, int height) 
	{
		dl.add(new DlInstruction.DlRectangle(x, y, width, height, Operation.STROKE));
	}
	
	/**
	 * See {@link DlSetClip}
	 */
	@Override
	public void setClip(Shape s) 
	{
        if (s == null) 
            clip = null;
        else
            clip = new Area(s);
		
		dl.add(new DlInstruction.DlSetClip(s));
	}
	
	/**
	 * See {@link DlClip}
	 */
	@Override
	public void clip(Shape s2) 
	{
        if (clip == null)
            clip = new Area(s2);
        else
            clip.intersect(new Area(s2));
		
		dl.add(new DlInstruction.DlClip(s2));
	}
	
	/**
	 * See {@link DlOval}
	 */
	@Override
	public void drawOval(int x, int y, int width, int height) 
	{
		dl.add(new DlInstruction.DlOval(x, y, width, height, Operation.STROKE));
	}
	
	/**
	 * See {@link DlOval}
	 */
	@Override
	public void fillOval(int x, int y, int width, int height) 
	{
		dl.add(new DlInstruction.DlOval(x, y, width, height, Operation.FILL));
	}
	
	/**
	 * See {@link DlDrawShape}
	 */
	@Override
	public void draw(Shape s) 
	{
		dl.add(new DlInstruction.DlDrawShape(s, Operation.STROKE));
	}
	
	/**
	 * See {@link DlDrawShape}
	 */
	@Override
	public void fill(Shape s) 
	{
		dl.add(new DlInstruction.DlDrawShape(s, Operation.FILL));
	}
	
	@Override
	public boolean isSupportsCMYKColors() 
	{
		return true;
	}
	
	@Override
	public Stroke getStroke()
	{
		return stroke;
	}

	@Override
	public Object getRenderingHint(Key key)
	{
		assert(key == RenderingHints.KEY_ANTIALIASING);
		return renderingHint;
	}

	/**
	 * See {@link DlAntiAliasDefault}, {@link DlAntiAliasOff}
	 */
	@Override
	public void setRenderingHint(Key key, Object value) 
	{
		assert(key == RenderingHints.KEY_ANTIALIASING);
		assert(value == RenderingHints.VALUE_ANTIALIAS_DEFAULT || value == RenderingHints.VALUE_ANTIALIAS_OFF);
		
		if (value == RenderingHints.VALUE_ANTIALIAS_DEFAULT)
		{
			dl.add(new DlInstruction.DlAntiAliasDefault());
		}
		else
		{
			dl.add(new DlInstruction.DlAntiAliasOff());
		}
	}

	@Override
	@Deprecated
	public boolean isSupportsSelection()
	{
		return false;
	}

	@Override
	@Deprecated
	public void drawSelection(RenderingContext c, InlineText inlineText)
	{
		// NOT IMPLEMENTED: We no longer support a selection.
	}

	/**
	 * See {@link DlReplaced}
	 */
	@Override
	public void paintReplacedElement(RenderingContext c, BlockBox box)
	{
		ReplacedElement replaced = box.getReplacedElement();
		dl.add(new DlInstruction.DlReplaced(replaced));
	}

	/**
	 * See {@link DlFont}
	 */
	@Override
	public void setFont(FSFont font)
	{
		dl.add(new DlInstruction.DlFont(font));
	}

    @Override
    public void drawBorderLine(Rectangle bounds, int side, int lineWidth, boolean solid) 
    {
    	final int x = bounds.x;
        final int y = bounds.y;
        final int w = bounds.width;
        final int h = bounds.height;
        
        final int adj = solid ? 1 : 0;
        
        if (side == BorderPainter.TOP)
        {
            drawLine(x, y + (lineWidth / 2), x + w - adj, y + (lineWidth / 2));
        }
        else if (side == BorderPainter.LEFT) 
        {
            drawLine(x + (lineWidth / 2), y, x + (lineWidth / 2), y + h - adj);
        }
        else if (side == BorderPainter.RIGHT) 
        {
            int offset = (lineWidth / 2);

            if (lineWidth % 2 != 0)
            {
                offset += 1;
            }

            drawLine(x + w - offset, y, x + w - offset, y + h - adj);
        }
        else if (side == BorderPainter.BOTTOM)
        {
            int offset = (lineWidth / 2);

            if (lineWidth % 2 != 0)
            {
                offset += 1;
            }

            drawLine(x, y + h - offset, x + w - adj, y + h - offset);
        }
    }

	@Override
	public void drawBorderLine(Shape bounds, int side, int width, boolean solid)
	{
		draw(bounds);
	}

	/**
	 * See {@link DlImage}
	 */
	@Override
	public void drawImage(FSImage image, int x, int y) 
	{
		dl.add(new DlInstruction.DlImage(image, x, y));
	}

	/**
	 * Draw a linear gradient.
	 * See {@link DlLinearGradient}
	 */
	@Override
	public void drawLinearGradient(FSLinearGradient gradient, int x, int y, int width, int height)
	{
		DlInstruction.DlLinearGradient linear = new DlInstruction.DlLinearGradient(
				gradient.getStartX(), gradient.getStartY(), gradient.getEndX(), gradient.getEndY(), x, y, width, height);
		
		for (StopValue sv : gradient.getStopPoints())
		{
			if (sv.getColor() instanceof FSRGBColor)
			{
				FSRGBColor rgb = (FSRGBColor) sv.getColor();
				DlRGBColor rgba = new DlRGBColor(rgb.getRed(), rgb.getGreen(), rgb.getBlue(), (int) (rgb.getAlpha() * 255));
				DlInstruction.DlStopPoint stopPoint = new DlInstruction.DlStopPoint(sv.getLength(), rgba);
				linear.stopPoints.add(stopPoint);
			}
			else
			{
				LOGGER.warn("We only support RGBA stop pointsw for linear gradients currently.");
				// TODO: Convert to RGB or support CMYK gradients.
			}
		}
		
		dl.add(linear);
	}

	/**
	 * @return The clip in display list units.
	 */
	@Override
	public Shape getClip() 
	{
		return clip;
	}
	
	/**
	 * Paints the background. We also use this to process any bookmarks on the element
	 * for the box and links.
	 * @param c
	 * @param box
	 */
	@Override
    public void paintBackground(RenderingContext c, Box box) 
	{
        super.paintBackground(c, box);

        processLink(c, box);
        
        if (!box.getStyle().isIdent(CSSName.FS_BOOKMARK_LEVEL, IdentValue.NONE))
        {
        	processBookmark(c, box);
        }
    }
	
	
	/**
	 * @param c
	 * @param box
	 * @return A Rectange2D representing the entire link area of an <b>element</b>. This may be made up of
	 * multiple <b>layout boxes</b>.
	 * See {@link #createTargetArea(RenderingContext, Box)}
	 */
    private Rectangle2D calcTotalLinkArea(RenderingContext c, Box box) 
    {
        Box current = box;

        while (true)
        {
            final Box prev = current.getPreviousSibling();

            if (prev == null || prev.getElement() != box.getElement())
            {
                break;
            }

            current = prev;
        }

        Rectangle2D result = createTargetArea(c, current);

        current = current.getNextSibling();

        while (current != null && current.getElement() == box.getElement()) 
        {
            result = addRectangle(result, createTargetArea(c, current));

            current = current.getNextSibling();
        }

        return result;
    }
	
    /**
     * @param c
     * @param box
     * @return A Rectangle2D representing the target area of a <b>layout box</b> in display list units.
     * These will need to be adjusted for a specific device such as PDF.
     */
    public Rectangle2D createTargetArea(RenderingContext c, Box box) 
    {
        Rectangle bounds;

        if (box.getPaintingInfo() != null) {
            bounds = box.getPaintingInfo().getAggregateBounds();
        } else {
            bounds = box.getContentAreaEdge(box.getAbsX(), box.getAbsY(), c);
        }

        return new Rectangle2D.Float(bounds.x, bounds.y + bounds.height, bounds.width, bounds.height);
    }
    
    /**
     * @param r1
     * @param r2
     * @return A Rectangle2D representing the combined area of r1 and r2.
     */
    private Rectangle2D addRectangle(Rectangle2D r1, Rectangle2D r2) 
    {
        float llx = (float) Math.min(r1.getMinX(), r2.getMinX());
        float urx = (float) Math.max(r1.getMaxX(), r2.getMaxX());
        float lly = (float) Math.min(r1.getMinY(), r2.getMinY());
        float ury = (float) Math.max(r1.getMaxY(), r2.getMaxY());

        return new Rectangle2D.Float(llx, lly, urx, ury);
    }
    
    /**
     * Processes a internal hash link or external uri and adds a link instruction to the display list.
     * Currently does not process internal links in continuous (non-paged) documents.
     * See {@link DlInternalLink}, {@link DlExternalLink}.
     * @param c
     * @param box
     */
    private void processLink(RenderingContext c, Box box)
    {
        final Element elem = box.getElement();

        if (elem != null) 
        {
            NamespaceHandler handler = sharedContext.getNamespaceHandler();
            Optional<String> ouri = handler.getLinkUri(elem);

            if (ouri.isPresent()) 
            {
            	String uri = ouri.get();

            	if (uri.length() > 1 && uri.startsWith("#")) 
            	{
            		// Internal hash link.
            		
            		String anchor = uri.substring(1);
                   
                    Box target = sharedContext.getBoxById(anchor);
                    if (target == null)
                    {
                    	LOGGER.warn("Unable to find hash link target: {}", uri);
                    	return;
                    }
                    
               		// Continuous renderer does not support internal links currently.
               		if (root.getLayer().getPages().isEmpty())
               		{
               			LOGGER.info("Continuous renderer does not currently support internal links.");
               			return;
               		}

                   	PageBox page = root.getLayer().getPage(c, getPageRefY(target));
                   	assert(page != null);
                   	if (page == null)
                   		return;

                   	int distanceFromTop = page.getMarginBorderPadding(c, CalculatedStyle.TOP);
                    distanceFromTop += target.getAbsY() + target.getMargin(c).top() - page.getTop();
 
                    int pageNo = page.getPageNo();
                            
                    Rectangle2D linkArea = calcTotalLinkArea(c, box);
                    assert(linkArea != null);
                    if (linkArea == null) 
                      	return;
                    
                    DlInstruction.DlInternalLink link = new DlInstruction.DlInternalLink(pageNo, distanceFromTop,
                    	(float) linkArea.getMinX(), (float) linkArea.getMinY(), 
                    	(float) linkArea.getWidth(), (float) linkArea.getHeight());
                        
                    dl.add(link);
                }
            	else if (uri.indexOf("://") != -1) 
            	{
            		// External URI link
            		
            		Rectangle2D pdfPageRect = calcTotalLinkArea(c, box);

            		assert(pdfPageRect != null);
            		
            		if (pdfPageRect == null)
            			return;
            		
            		DlInstruction.DlExternalLink link = new DlInstruction.DlExternalLink(uri,
            				(float) pdfPageRect.getMinX(), (float) pdfPageRect.getMinY(),
            				(float) pdfPageRect.getWidth(), (float) pdfPageRect.getHeight());
            				
            		dl.add(link);
                }
            }
        }
    }
	
    /**
     * Processes a box which acts as a bookmark(<code>fs-bookmark-level</code> is not <code>none</code>).
	 * See {@link DlBookmark}, {@link BookmarkLevel}
     * @param c
     * @param box
     */
	private void processBookmark(RenderingContext c, Box box)
	{
		assert(!box.getStyle().isIdent(CSSName.FS_BOOKMARK_LEVEL, IdentValue.NONE));
		
		if (root.getLayer().getPages().isEmpty())
		{
			LOGGER.info("Continuous renderer does not support bookmarks currently.");
			return;
		}
		
		int bookmarkLevel = (int) box.getStyle().asFloat(CSSName.FS_BOOKMARK_LEVEL);

    	String bookmarkContent = box.getElement().getTextContent();
 
    	PageBox page = root.getLayer().getPage(c, getPageRefY(box));
    	
    	assert(page != null);
    	
        if (page != null)
        {
            int distanceFromTop = page.getMarginBorderPadding(c, CalculatedStyle.TOP);
            distanceFromTop += box.getAbsY() + box.getMargin(c).top() - page.getTop();
            
            DlInstruction.DlBookmark dlBookmark = new DlInstruction.DlBookmark(bookmarkLevel,
            		distanceFromTop, bookmarkContent, page.getPageNo());
            
            dl.add(dlBookmark);
        }
	}
	
	/**
	 * @param box
	 * @return the y coordinate of box in display list units.
	 */
    private int getPageRefY(Box box)
    {
        if (box instanceof InlineLayoutBox) 
        {
            InlineLayoutBox iB = (InlineLayoutBox) box;
            return iB.getAbsY() + iB.getBaseline();
        }
        else 
        {
            return box.getAbsY();
        }
    }

	@Override
	public void clip(Shape s, boolean store)
	{
		if (store)
			clip(s);
		else
			dl.add(new DlInstruction.DlClip(s));
	}
}
