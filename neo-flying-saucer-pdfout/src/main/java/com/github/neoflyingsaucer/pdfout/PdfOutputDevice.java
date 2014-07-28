package com.github.neoflyingsaucer.pdfout;

import java.awt.Rectangle;
import java.awt.RenderingHints.Key;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Shape;
import java.awt.Stroke;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xhtmlrenderer.css.parser.FSCMYKColor;
import org.xhtmlrenderer.css.parser.FSColor;
import org.xhtmlrenderer.css.parser.FSRGBColor;
import org.xhtmlrenderer.css.style.derived.FSLinearGradient;
import org.xhtmlrenderer.extend.FSImage;
import org.xhtmlrenderer.extend.OutputDevice;
import org.xhtmlrenderer.layout.SharedContext;
import org.xhtmlrenderer.render.AbstractOutputDevice;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.render.BorderPainter;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.render.FSFont;
import org.xhtmlrenderer.render.InlineText;
import org.xhtmlrenderer.render.RenderingContext;
import org.xhtmlrenderer.util.Configuration;

import com.github.pdfstream.PDF;
import com.github.pdfstream.Page;
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

    //private ITextFSFont _font;

    private AffineTransform _transform = new AffineTransform();

    private FSColor _color = null;
    private float _opacity = 1;

    private Stroke _stroke = STROKE_ONE;
    private Stroke _originalStroke = STROKE_ONE;

    private Area _clip;

    private SharedContext _sharedContext;
    private final float _dotsPerPoint;

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

    private Set<String> _linkTargetAreas;
    
    private boolean haveOpacity = false;
	
	
	public PdfOutputDevice(float dotsPerPoint) 
	{
		_dotsPerPoint = dotsPerPoint;

	}

	@Override
	public void drawSelection(RenderingContext c, InlineText inlineText) {
		// TODO Auto-generated method stub

	}

	@Override
	public void paintReplacedElement(RenderingContext c, BlockBox box) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setFont(FSFont font) {
		// TODO Auto-generated method stub

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
	public void drawImage(FSImage image, int x, int y) {
		// TODO Auto-generated method stub

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
	public void setStroke(Stroke s) {
		_originalStroke = s;
		_stroke = transformStroke(s);
	}

    private Stroke transformStroke(final Stroke stroke) {
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

	public void setSharedContext(SharedContext _sharedContext) {
		// TODO Auto-generated method stub
		
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
	}

	public void finish(RenderingContext c, BlockBox _root) {
		System.err.println("finish");
		
	}

	public void setRoot(BlockBox _root) {
		System.err.println("setRoot" + _root.toString());
		
	}

	public void start(Document _doc) {
		System.err.println("start");
		
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
			// TODO
			
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

        // TODO cb.setMiterLimit(nStroke.getMiterLimit());
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
}
