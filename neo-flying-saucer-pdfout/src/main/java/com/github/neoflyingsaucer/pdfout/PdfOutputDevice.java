package com.github.neoflyingsaucer.pdfout;

import java.awt.Rectangle;
import java.awt.RenderingHints.Key;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.Raster;
import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Shape;
import java.awt.Stroke;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.css.parser.FSCMYKColor;
import org.xhtmlrenderer.css.parser.FSColor;
import org.xhtmlrenderer.css.parser.FSRGBColor;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.css.style.derived.FSLinearGradient;
import org.xhtmlrenderer.css.value.FontSpecification;
import org.xhtmlrenderer.extend.FSImage;
import org.xhtmlrenderer.extend.NamespaceHandler;
import org.xhtmlrenderer.extend.OutputDevice;
import org.xhtmlrenderer.layout.SharedContext;
import org.xhtmlrenderer.render.AbstractOutputDevice;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.render.BorderPainter;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.render.FSFont;
import org.xhtmlrenderer.render.InlineLayoutBox;
import org.xhtmlrenderer.render.InlineText;
import org.xhtmlrenderer.render.JustificationInfo;
import org.xhtmlrenderer.render.PageBox;
import org.xhtmlrenderer.render.RenderingContext;
import org.xhtmlrenderer.util.Configuration;
import org.xhtmlrenderer.util.Optional;

import com.github.neoflyingsaucer.pdfout.PdfFontResolver.FontDescription;
import com.github.pdfstream.Annotation;
import com.github.pdfstream.Destination;
import com.github.pdfstream.JPGImage;
import com.github.pdfstream.PDF;
import com.github.pdfstream.PNGImage;
import com.github.pdfstream.Page;
import com.github.pdfstream.PdfCmykColor;
import com.github.pdfstream.PdfColor;
import com.github.pdfstream.PdfGreyScaleColor;
import com.github.pdfstream.PdfRgbaColor;

public class PdfOutputDevice extends AbstractOutputDevice implements OutputDevice 
{
    private static final Logger LOGGER = LoggerFactory.getLogger(PdfOutputDevice.class);
    private static final int FILL = 1;
    private static final int STROKE = 2;
    private static final int CLIP = 3;

    private static AffineTransform IDENTITY = new AffineTransform();

    private static final BasicStroke STROKE_ONE = new BasicStroke(1);

    private static final boolean ROUND_RECT_DIMENSIONS_DOWN = Configuration.isTrue("xr.pdf.round.rect.dimensions.down", false);

    private Page _currentPage;
    private float _pageHeight;

    private PdfFont _font;

    private AffineTransform _transform = new AffineTransform();

    private FSColor _color = null;
    private float _opacity = 1;

    private Stroke _stroke = STROKE_ONE;
    private Stroke _originalStroke = STROKE_ONE;

    private Area _clip;

    private SharedContext _sharedContext;
    private final float _dotsPerPoint;
    
    private PdfHelper _pdfHelper;
    
//    private final Map<URI, PdfReader> _readerCache = new HashMap<URI, PdfReader>();
//
//    private PdfDestination _defaultDestination;
//
//    private final List<Bookmark> _bookmarks = new ArrayList<Bookmark>();
//
//    private final List<Metadata> _metadata = new ArrayList<Metadata>();

    private Box _root;

    private int _startPageNo;

    private int _nextFormFieldIndex;

    private Set<Rectangle2D> _linkTargetAreas;
    
	public PdfOutputDevice(float dotsPerPoint) 
	{
		_dotsPerPoint = dotsPerPoint;
	}

	@Override
	public void drawSelection(RenderingContext c, InlineText inlineText) 
	{
		// Unimplemented.
	}

	@Override
	public void paintReplacedElement(RenderingContext c, BlockBox box) {
        final PdfReplacedElement element = (PdfReplacedElement) box.getReplacedElement();
        element.paint(c, this, box);
	}

	@Override
	public void setFont(FSFont font) 
	{
		_font = (PdfFont) font;
		
		_currentPage.getPdf().registerFont(_font.getFontDescription().getFont());
	}

	@Override
    public void setColor(final FSColor color)
	{
		_color = color;
    }

	@Override
	public void setOpacity(float opacity)
	{
		_opacity = opacity;
	}

	@Override
	public void drawRect(int x, int y, int width, int height) 
	{
		Rectangle2D.Double d = new Rectangle2D.Double(x, y, width, height);
		followPath(d, STROKE);
	}

	@Override
	public void drawOval(int x, int y, int width, int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void draw(final Shape s) 
	{
		followPath(s, STROKE);
	}

	@Override
    public void drawBorderLine(final Rectangle bounds, final int side, final int lineWidth, final boolean solid) 
	{
        final float x = bounds.x;
        final float y = bounds.y;
        final float w = bounds.width;
        final float h = bounds.height;

        final float adj = solid ? (float) lineWidth / 2 : 0;
        final float adj2 = lineWidth % 2 != 0 ? 0.5f : 0f;

        Line2D.Float line = null;

        // FIXME: findbugs reports possible loss of precision, compare with
        // width / (float)2
        if (side == BorderPainter.TOP) {
            line = new Line2D.Float(x + adj, y + lineWidth / 2 + adj2, x + w - adj, y + lineWidth / 2 + adj2);
        } else if (side == BorderPainter.LEFT) {
            line = new Line2D.Float(x + lineWidth / 2 + adj2, y + adj, x + lineWidth / 2 + adj2, y + h - adj);
        } else if (side == BorderPainter.RIGHT) {
            float offset = lineWidth / 2;
            if (lineWidth % 2 != 0) {
                offset += 1;
            }
            line = new Line2D.Float(x + w - offset + adj2, y + adj, x + w - offset + adj2, y + h - adj);
        } else if (side == BorderPainter.BOTTOM) {
            float offset = lineWidth / 2;
            if (lineWidth % 2 != 0) {
                offset += 1;
            }
            line = new Line2D.Float(x + adj, y + h - offset + adj2, x + w - adj, y + h - offset + adj2);
        }

        draw(line);
    }

	@Override
	public void drawBorderLine(Shape bounds, int side, int width, boolean solid) 
	{
		followPath(bounds, STROKE);
	}

	@Override
	public void drawImage(FSImage fsImage, int x, int y) {
		final PdfOutImage image = ((PdfOutImage) fsImage);

        if (fsImage.getHeight() <= 0 || fsImage.getWidth() <= 0) {
            return;
        }

        final AffineTransform at = AffineTransform.getTranslateInstance(x, y);
        at.translate(0, fsImage.getHeight());
        at.scale(fsImage.getWidth(), fsImage.getHeight());

        final AffineTransform inverse = normalizeMatrix(_transform);
        final AffineTransform flipper = AffineTransform.getScaleInstance(1, -1);
        inverse.concatenate(at);
        inverse.concatenate(flipper);

        final double[] mx = new double[6];
        inverse.getMatrix(mx);
        
        _currentPage.setOpacity(_opacity);
        
        if (image.isJpeg())
        {
        	JPGImage img = new JPGImage(image.getUri(), image.getBytes(), image.getIntrinsicWidth(), image.getIntrinsicHeight(), image.getNumberComponents());
        	_currentPage.addImage(img, (float) mx[0], (float) mx[1], (float) mx[2], (float) mx[3], (float) mx[4], (float) mx[5]);
        }
        else
        {
        	BufferedImage img;
			try {
				img = ImageIO.read(new ByteArrayInputStream(image.getBytes()));
			} catch (IOException e) {
				LOGGER.warn("Unable to read image at uri({})", image.getUri(), e);
				return;
			}

			Raster raster;
			
        	if (img.getType() != BufferedImage.TYPE_INT_ARGB)
        	{
        		BufferedImage buf = new BufferedImage(image.getOriginalWidth(), image.getOriginalHeight(), BufferedImage.TYPE_INT_ARGB);

        		Graphics g = buf.createGraphics();
        		g.drawImage(img, 0, 0, null);
        		g.dispose();

        		raster = buf.getData();
        	}
        	else
        	{
        		raster = img.getData();
        	}
        	
        	DataBufferInt buf = (DataBufferInt) raster.getDataBuffer();
        	int[] arr = buf.getData();

        	PNGImage png = new PNGImage(image.getUri(), arr, image.getOriginalWidth(), image.getOriginalHeight(), 3);
        	_currentPage.addImage(png, (float) mx[0], (float) mx[1], (float) mx[2], (float) mx[3], (float) mx[4], (float) mx[5]);
        }
    }
	
	@Override
	public void drawLinearGradient(FSLinearGradient gradient, int x, int y,
			int width, int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void fill(Shape s) 
	{
		followPath(s, FILL);
	}

	@Override
	public void fillRect(int x, int y, int width, int height) 
	{
		Rectangle2D.Double d = new Rectangle2D.Double(x, y, width, height);
		followPath(d, FILL);
	}

	@Override
	public void fillOval(int x, int y, int width, int height) {
		// TODO Auto-generated method stub

	}

	@Override
    public void paintBackground(final RenderingContext c, final Box box) 
	{
        super.paintBackground(c, box);
        processLink(c, box);
    }
	
    private void processLink(final RenderingContext c, final Box box)
    {
        final Element elem = box.getElement();

        if (elem != null) 
        {
            final NamespaceHandler handler = _sharedContext.getNamespaceHandler();
            final Optional<String> ouri = handler.getLinkUri(elem);

            if (ouri.isPresent()) 
            {
            	String uri = ouri.get();

            	if (uri.length() > 1 && uri.startsWith("#")) 
            	{
                    final String anchor = uri.substring(1);
                    final Box target = _sharedContext.getBoxById(anchor);
                    
                    if (target != null) 
                    {
                        final Destination dest = createDestination(c, target);

                        if (dest != null) 
                        {
                            final Rectangle2D targetArea = checkLinkArea(c, box);
                            if (targetArea == null) {
                                return;
                            }

                            final Annotation annot = new Annotation(null, dest,
                            		(float) targetArea.getMinX(), (float) targetArea.getMinY(),
                                    (float) targetArea.getMaxX(), (float) targetArea.getMaxY());

                            _currentPage.addAnnotation(annot);
                        }
                    }
                }
            	else if (uri.indexOf("://") != -1) 
            	{
            		Rectangle2D pdfPageRect = checkLinkArea(c, box);

            		if (pdfPageRect == null)
            			return;
            		
            		final Annotation annot = new Annotation(uri, (Destination) null,
            				(float) pdfPageRect.getMinX(), (float) pdfPageRect.getMinY(),
            				(float) pdfPageRect.getMaxX(), (float) pdfPageRect.getMaxY());

            		_currentPage.addAnnotation(annot);
                }
            }
        }
    }

    private Destination createDestination(final RenderingContext c, final Box box) 
    {
        Destination result = null;

        final PageBox page = _root.getLayer().getPage(c, getPageRefY(box));
        
        if (page != null)
        {
            int distanceFromTop = page.getMarginBorderPadding(c, CalculatedStyle.TOP);
            distanceFromTop += box.getAbsY() + box.getMargin(c).top() - page.getTop();
            
            result = new Destination(0, page.getHeight(c) / _dotsPerPoint - distanceFromTop / _dotsPerPoint, 0);
            result.setPageObjNumber(_startPageNo + page.getPageNo());
        }

        return result;
    }
    
    private int getPageRefY(final Box box) 
    {
        if (box instanceof InlineLayoutBox) 
        {
            final InlineLayoutBox iB = (InlineLayoutBox) box;
            return iB.getAbsY() + iB.getBaseline();
        }
        else 
        {
            return box.getAbsY();
        }
    }
    
    private Rectangle2D checkLinkArea(final RenderingContext c, final Box box) 
    {
        final Rectangle2D targetArea = calcTotalLinkArea(c, box);

        if (_linkTargetAreas.contains(targetArea)) 
        {
            return null;
        }
        
        _linkTargetAreas.add(targetArea);

        return targetArea;
    }
	
    private Rectangle2D calcTotalLinkArea(final RenderingContext c, final Box box) 
    {
        Box current = box;

        while (true)
        {
            final Box prev = current.getPreviousSibling();

            if (prev == null || prev.getElement() != box.getElement()) {
                break;
            }

            current = prev;
        }

        Rectangle2D result = createLocalTargetArea(c, current, true);

        current = current.getNextSibling();

        while (current != null && current.getElement() == box.getElement()) 
        {
            result = add(result, createLocalTargetArea(c, current, true));

            current = current.getNextSibling();
        }

        return result;
    }
    
    private Rectangle2D add(final Rectangle2D r1, final Rectangle2D r2) 
    {
        final float llx = (float) Math.min(r1.getMinX(), r2.getMinX());
        final float urx = (float) Math.max(r1.getMaxX(), r2.getMaxX());
        final float lly = (float) Math.min(r1.getMinY(), r2.getMinY());
        final float ury = (float) Math.max(r1.getMaxY(), r2.getMaxY());

        return new Rectangle2D.Float(llx, lly, urx, ury);
    }
    
    public Rectangle2D createLocalTargetArea(final RenderingContext c, final Box box, final boolean useAggregateBounds) 
    {
        Rectangle bounds;

        if (useAggregateBounds && box.getPaintingInfo() != null) {
            bounds = box.getPaintingInfo().getAggregateBounds();
        } else {
            bounds = box.getContentAreaEdge(box.getAbsX(), box.getAbsY(), c);
        }

        final Point2D docCorner = new Point2D.Double(bounds.x, bounds.y + bounds.height);
        final Point2D pdfCorner = new Point.Double();
        
        _transform.transform(docCorner, pdfCorner);

        pdfCorner.setLocation(pdfCorner.getX(), normalizeY((float) pdfCorner.getY()));

        final Rectangle2D result = new Rectangle2D.Float((float) pdfCorner.getX(), (float) pdfCorner.getY(),
                (float) getDeviceLength(bounds.width), (float) getDeviceLength(bounds.height));

        return result;
    }
    
    public float getDeviceLength(final float length) 
    {
        return length / _dotsPerPoint;
    }
    
	@Override
	public void clip(Shape s) 
	{
        if (s != null) 
        {
            Shape s2 = _transform.createTransformedShape(s);
            
            if (_clip == null)
                _clip = new Area(s2);
            else
                _clip.intersect(new Area(s2));
            
            followPath(s2, CLIP);
        }
	}

	@Override
	public Shape getClip() {
        try {
            return _transform.createInverse().createTransformedShape(_clip);
        } catch (final NoninvertibleTransformException e) {
            return null;
        }
	}

	@Override
	public void setClip(Shape s) 
	{
		_currentPage.restore();
		_currentPage.save();

		if (s != null)
            s = _transform.createTransformedShape(s);
        if (s == null) {
            _clip = null;
        } else {
            _clip = new Area(s);
            followPath(s, CLIP);
        }
	}

	@Override
	public void translate(double tx, double ty) 
	{
		_transform.translate(tx, ty);
	}

	@Override
	public void setStroke(Stroke s)
	{
		_originalStroke = s;
		_stroke = transformStroke(s);
	}

    private Stroke transformStroke(final Stroke stroke)
    {
        if (!(stroke instanceof BasicStroke))
            return stroke;

        final BasicStroke st = (BasicStroke) stroke;
        final float scale = (float) Math.sqrt(Math.abs(_transform.getDeterminant()));
        final float dash[] = st.getDashArray();

        if (dash != null) {
            for (int k = 0; k < dash.length; ++k) {
              dash[k] *= scale;
            }
        }

        return new BasicStroke(st.getLineWidth() * scale, st.getEndCap(), st.getLineJoin(), st.getMiterLimit(), dash, st.getDashPhase()
                * scale);
    }
	
	@Override
	public Stroke getStroke() {
		return _originalStroke;
	}

	@Override
	public Object getRenderingHint(Key key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setRenderingHint(Key key, Object value) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isSupportsSelection() 
	{
		return false;
	}

	@Override
	public boolean isSupportsCMYKColors() 
	{
		return true;
	}

	@Override
	protected void drawLine(int x1, int y1, int x2, int y2) {
		Shape line = new Line2D.Double(x1, y1, x2, y2);
		followPath(line, STROKE);
	}

	public void setSharedContext(SharedContext sharedContext) 
	{
		this._sharedContext = sharedContext;
	}

	public String getMetadataByName(String string) {
		// TODO Auto-generated method stub
		return null;
	}

	public void finishPage() 
	{
		_currentPage.restore();
	}

	public void initializePage(Page pg, float h) {
		_currentPage = pg;
		_pageHeight = h;
		_transform = new AffineTransform();
		_transform.scale(1.0d / _dotsPerPoint, 1.0d / _dotsPerPoint);
		_currentPage.save();
        _linkTargetAreas = new HashSet<Rectangle2D>();
	}

	public void finish(RenderingContext c, BlockBox _root) {
		// TODO
	}

	public void setRoot(BlockBox root) {
		_root = root;
	}

	public void start(Document doc) 
	{
		_pdfHelper = new PdfHelper();
		_pdfHelper.loadMetadata(doc);
	}

    private void followPath(Shape s, final int drawType) 
    {
        if (s == null)
            return;

    	final Page cb = _currentPage;
        
        if (drawType == STROKE) 
        {
            if (!(_stroke instanceof BasicStroke)) 
            {
                s = _stroke.createStrokedShape(s);
                followPath(s, FILL);
                return;
            }
        }
        
        if (drawType == STROKE) 
        {
            setStrokeDiff(_stroke);
            ensureStrokeColor();
        }
        else if (drawType == FILL) 
        {
            ensureFillColor();
        }

        PathIterator points;

        if (drawType == CLIP) {
            points = s.getPathIterator(IDENTITY);
        } else {
            points = s.getPathIterator(_transform);
        }
        
        final float[] coords = new float[6];
        int traces = 0;

        cb.pathOpen();
        
        while (!points.isDone())
        {
            ++traces;
            final int segtype = points.currentSegment(coords);
            normalizeY(coords);
            
            switch (segtype)
            {
            case PathIterator.SEG_CLOSE:
            	cb.pathCloseSubpath();
            	break;

            case PathIterator.SEG_CUBICTO:
            	cb.pathCurveTo(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);
                break;

            case PathIterator.SEG_LINETO:
            	cb.pathLineTo(coords[0], coords[1]);
                break;

            case PathIterator.SEG_MOVETO:
            	cb.pathOpen();
            	cb.pathMoveTo(coords[0], coords[1]);
                break;

            case PathIterator.SEG_QUADTO:
            	cb.pathCurveTo(coords[0], coords[1], coords[2], coords[3]);
            	break;
            }

            points.next();
        }

        switch (drawType) {
        case FILL:
            if (traces > 0) {
                if (points.getWindingRule() == PathIterator.WIND_EVEN_ODD)
                	cb.pathFillEvenOdd();
                else
                	cb.pathFillNonZero();
            }
            break;
        case STROKE:
            if (traces > 0)
                cb.pathStroke();
            break;
        default: // drawType==CLIP
            if (traces == 0)
                ;//cb.rectangle(0, 0, 0, 0);
            if (points.getWindingRule() == PathIterator.WIND_EVEN_ODD)
            	cb.pathClipEvenOdd();
            else
                cb.pathClipNonZero();
        }
    }

    private PdfColor getPdfColor(FSColor col, float opacity)
    {
		PdfColor pdfColor = PdfGreyScaleColor.BLACK;

		if (col instanceof FSRGBColor)
		{
			FSRGBColor rgba = (FSRGBColor) col;
			pdfColor = new PdfRgbaColor(
				rgba.getRed(), rgba.getGreen(), rgba.getBlue(), (int) (rgba.getAlpha() * 255 * opacity));			
		}
		else if (col instanceof FSCMYKColor)
		{
			FSCMYKColor cmyk = (FSCMYKColor) col;
			pdfColor = new PdfCmykColor(cmyk.getCyan(), cmyk.getMagenta(), cmyk.getYellow(), cmyk.getBlack(), 1.0f);
		}
		else
		{
			assert(false);
		}
    	
    	return pdfColor;
    }
    
	private void ensureFillColor() 
	{
		PdfColor pdfColor = getPdfColor(_color, _opacity);
		_currentPage.setBrushColor(pdfColor);
	}

	private void ensureStrokeColor() {
		PdfColor pdfColor = getPdfColor(_color, _opacity);
		_currentPage.setPenColor(pdfColor);
	}

	private void setStrokeDiff(Stroke newStroke) 
	{
		final Page cb = _currentPage;
		if (!(newStroke instanceof BasicStroke))
			return;

		final BasicStroke nStroke = (BasicStroke) newStroke;

		cb.setPenWidth(nStroke.getLineWidth());

		switch (nStroke.getEndCap()) {
        case BasicStroke.CAP_BUTT:
            cb.setLineCapStyle(0);
            break;
        case BasicStroke.CAP_SQUARE:
            cb.setLineCapStyle(2);
            break;
        default:
            cb.setLineCapStyle(1);
            break;
        }
		
        switch (nStroke.getLineJoin()) {
        case BasicStroke.JOIN_MITER:
            cb.setLineJoinStyle(0);
            break;
        case BasicStroke.JOIN_BEVEL:
            cb.setLineJoinStyle(2);
            break;
        default:
            cb.setLineJoinStyle(1);
            break;
        }

        cb.setMiterLimit(nStroke.getMiterLimit());
        final float dash[] = nStroke.getDashArray();

        if (dash == null)
        {
        	cb.setLinePattern("[] 0");
        }
        else 
        {
        	StringBuilder sb = new StringBuilder(15);
        	sb.append('[');
        	
            for (int k = 0; k < dash.length; ++k) 
            {
            	sb.append(PDF.formatFloat(dash[k]));

            	if (k != dash.length - 1)
            		sb.append(' ');
            }
            sb.append(']');
            sb.append(PDF.formatFloat(nStroke.getDashPhase()));
            cb.setLinePattern(sb.toString());
        }
    }

	private float normalizeY(final float y) 
	{
        return _pageHeight - y;
    }

    private void normalizeY(final float[] coords) 
    {
        coords[1] = normalizeY(coords[1]);
        coords[3] = normalizeY(coords[3]);
        coords[5] = normalizeY(coords[5]);
    }

	public void drawString(String s, float x, float y, JustificationInfo info) 
	{
		if (Configuration.isTrue("xr.renderer.replace-missing-characters", false)) 
			s = s; // TODO replaceMissingCharacters(s);

		if (s.isEmpty())
			return;

		final Page cb = _currentPage;

		// The fill color is also used for text.
		ensureFillColor();

		final AffineTransform at = (AffineTransform) _transform.clone();
	    at.translate(x, y);

	    final AffineTransform inverse = normalizeMatrix(at);
	    final AffineTransform flipper = AffineTransform.getScaleInstance(1, -1);
	    inverse.concatenate(flipper);
	    inverse.scale(_dotsPerPoint, _dotsPerPoint);

	    final double[] mx = new double[6];
	    inverse.getMatrix(mx);

	    cb.beginText();
	    
	    // Check if bold or italic need to be emulated
	    boolean resetMode = false;

	    final FontDescription desc = _font.getFontDescription();
	    final float fontSize = _font.getSize2D() / _dotsPerPoint;
	    cb.setTextFont(desc.getFont(), fontSize);

	    float b = (float) mx[1];
	    float c = (float) mx[2];

	    final FontSpecification fontSpec = getFontSpecification();

	    if (fontSpec != null) 
	    {
	        final int need = PdfFontResolver.convertWeightToInt(fontSpec.fontWeight);
	        final int have = desc.getWeight();

	        if (need > have) {
                cb.setTextRenderingMode(2); // TEXT_RENDER_MODE_FILL_STROKE
                final float lineWidth = fontSize * 0.04f; // 4% of font size
                cb.setPenWidth(lineWidth);
                ensureStrokeColor();
                resetMode = true;
            }

	        if ((fontSpec.fontStyle == IdentValue.ITALIC) && (desc.getStyle() != IdentValue.ITALIC)) 
	        {
	        	b = 0f;
	            c = 0.21256f;
	        }
	    }

	    cb.setTextMatrix((float) mx[0], b, c, (float) mx[3], (float) mx[4], (float) mx[5]);

	    if (info == null) 
	    {
	    	cb.showText(s);
	    }
	    else
	    {
	    	final char[] cc = s.toCharArray();
	    	final float[] justification = makeJustificationArray(cc, info);
	    	cb.showText(cc, justification);
	    }
	    
	    if (resetMode)
	    {
	    	cb.setTextRenderingMode(0 /* TEXT_RENDER_MODE_FILL */);
	        cb.setPenWidth(1);
	    }

	    cb.endText();
	}
	
    private float[] makeJustificationArray(final char[] cc, final JustificationInfo info) 
    {
    	final float[] res = new float[cc.length];
    	
    	for (int i = 0; i < cc.length; i++) 
    	{
            if (i != cc.length - 1) 
            {
            	char c = cc[i];

            	float offset;
                if (c == ' ' || c == '\u00a0' || c == '\u3000') {
                    offset = info.getSpaceAdjust();
                } else {
                    offset = info.getNonSpaceAdjust();
                }

                res[i] = (-offset / _dotsPerPoint) / (_font.getSize2D() / _dotsPerPoint);
            }
        }

    	return res;
    }

    private AffineTransform normalizeMatrix(final AffineTransform current) {
        final double[] mx = new double[6];
        AffineTransform result = new AffineTransform();
        result.getMatrix(mx);
        mx[3] = -1;
        mx[5] = _pageHeight;
        result = new AffineTransform(mx);
        result.concatenate(current);
        return result;
    }

	public int getNextFormFieldIndex() 
	{
		return ++_nextFormFieldIndex;
	}

	public Page getCurrentPage() {
		return _currentPage;
	}

	public void setDidValues(PDF doc) 
	{
		_pdfHelper.setDidValues(doc);
	}
    

	
	
//    private String replaceMissingCharacters(final String string)
//    {
//        final char[] charArr = string.toCharArray();
//        final char replacementCharacter = Configuration.valueAsChar("xr.renderer.missing-character-replacement", '#');
//
//        // first check to see if the replacement character even exists in the
//        // given font. If not, then do nothing.
//        if (!_font.getFontDescription().getFont().charExists(replacementCharacter)) {
//            LOGGER.info( "Missing replacement character [" + replacementCharacter + ":" + (int) replacementCharacter
//                    + "]. No replacement will occur.");
//            return string;
//        }
//
//        // iterate through each character in the string and make an appropriate
//        // replacement
//        for (int i = 0; i < charArr.length; i++) 
//        {
//            if (!(charArr[i] == ' ' || charArr[i] == '\u00a0' || charArr[i] == '\u3000' || _font.getFontDescription().getFont()
//                    .charExists(charArr[i]))) {
//                LOGGER.info( "Missing character [" + charArr[i] + ":" + (int) charArr[i] + "] in string [" + string
//                        + "]. Replacing with '" + replacementCharacter + "'");
//                charArr[i] = replacementCharacter;
//            }
//        }
//
//        return String.valueOf(charArr);
//    }
}
