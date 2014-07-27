package com.github.neoflyingsaucer.pdfout;

import java.awt.Rectangle;
import java.awt.RenderingHints.Key;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
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

import com.github.pdfstream.Page;
import com.github.pdfstream.Point;

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

    private Color _color = Color.RED;

    private Color _fillColor;
    private Color _strokeColor;

    private Stroke _stroke = STROKE_ONE;
    private Stroke _originalStroke = null;
    private Stroke _oldStroke = null;

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
    public void setColor(final FSColor color) {
        if (color instanceof FSRGBColor) {
            final FSRGBColor rgb = (FSRGBColor) color;
            _color = new Color(rgb.getRed(), rgb.getGreen(), rgb.getBlue(), (int) (rgb.getAlpha() * 255));
//        } else if (color instanceof FSCMYKColor) {
//            final FSCMYKColor cmyk = (FSCMYKColor) color;
//            _color = new CMYKColor(cmyk.getCyan(), cmyk.getMagenta(), cmyk.getYellow(), cmyk.getBlack());
        } else {
            throw new RuntimeException("internal error: unsupported color class " + color.getClass().getName());
        }
    }

	@Override
	public void setOpacity(float opacity) {
		// TODO Auto-generated method stub

	}

	@Override
	public void drawRect(int x, int y, int width, int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void drawOval(int x, int y, int width, int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void draw(final Shape s) 
	{
		try {
			followPath(s, STROKE);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
		try {
			followPath(bounds, STROKE);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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
	public void fill(Shape s) {
		try {
			followPath(s, FILL);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void fillRect(int x, int y, int width, int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void fillOval(int x, int y, int width, int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void clip(Shape s) {
		// TODO Auto-generated method stub

	}

	@Override
	public Shape getClip() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setClip(Shape s) {
		// TODO Auto-generated method stub

	}

	@Override
	public void translate(double tx, double ty) 
	{
		try {
			_currentPage.drawLine(0, 0, 100, 100);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

	@Override
	public void setStroke(Stroke s) {
		// TODO Auto-generated method stub

	}

	@Override
	public Stroke getStroke() {
		// TODO Auto-generated method stub
		return null;
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
	public boolean isSupportsSelection() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSupportsCMYKColors() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void drawLine(int x1, int y1, int x2, int y2) {
		Shape line = new Line2D.Double(x1, y1, x2, y2);
		try {
			followPath(line, STROKE);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setSharedContext(SharedContext _sharedContext) {
		// TODO Auto-generated method stub
		
	}

	public String getMetadataByName(String string) {
		// TODO Auto-generated method stub
		return null;
	}

	public void finishPage() {
		// TODO Auto-generated method stub
		
	}

	public void initializePage(Page pg, float h) {
		_currentPage = pg;
		
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

    private void followPath(Shape s, final int drawType) throws Exception {

    	System.err.println("!!!!");
    	
    	final Page cb = _currentPage;
        if (s == null)
            return;

        if (drawType == STROKE) {
            if (!(_stroke instanceof BasicStroke)) {
                s = _stroke.createStrokedShape(s);
                followPath(s, FILL);
                return;
            }
        }
        if (drawType == STROKE) {
            setStrokeDiff(_stroke, _oldStroke);
            _oldStroke = _stroke;
            ensureStrokeColor();
        } else if (drawType == FILL) {
            ensureFillColor();
        }

        List<Point> pdfPath = new ArrayList<>();
        PathIterator points;

        if (drawType == CLIP) {
            points = s.getPathIterator(IDENTITY);
        } else {
            points = s.getPathIterator(_transform);
        }
        final float[] coords = new float[6];
        int traces = 0;
        while (!points.isDone()) {
            ++traces;
            final int segtype = points.currentSegment(coords);

            for (int i = 0; i < coords.length; i++)
            {
            	coords[i] = Math.abs(coords[i]) / _dotsPerPoint;
            }
            //normalizeY(coords);
            switch (segtype) {
            case PathIterator.SEG_CLOSE:
            	LOGGER.info("Close segment");
            	break;

            case PathIterator.SEG_CUBICTO:
            	LOGGER.info("Curve to: {}, {}, {}, {}, {}, {}",
            			coords[0], coords[1],
            			coords[2], coords[3],
            			coords[4], coords[5]);
            	pdfPath.add(new Point(coords[0], coords[1], true));
            	pdfPath.add(new Point(coords[2], coords[3], true));
            	pdfPath.add(new Point(coords[4], coords[5]));
            	
            	//cb.curveTo(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);
                break;

            case PathIterator.SEG_LINETO:
                pdfPath.add(new Point(coords[0], coords[1]));
                LOGGER.info("Line to: {}, {}", coords[0], coords[1]);
                break;

            case PathIterator.SEG_MOVETO:
            	pdfPath.add(new Point(coords[0], coords[1]));
                LOGGER.info("Move to: {}, {}", coords[0], coords[1]);
            	//cb.moveTo(coords[0], coords[1]);
                break;

            case PathIterator.SEG_QUADTO:
            	pdfPath.add(new Point(coords[0], coords[1], true));
            	pdfPath.add(new Point(coords[2], coords[3], true));
            	break;
            }
            points.next();
        }

        switch (drawType) {
        case FILL:
            if (traces > 0) {
                if (points.getWindingRule() == PathIterator.WIND_EVEN_ODD)
                    cb.drawPath(pdfPath, 'f'); // TODO
                else
                	cb.drawPath(pdfPath, 'f'); // TODO
            }
            break;
        case STROKE:
            if (traces > 0)
                cb.drawPath(pdfPath, 'S');
            break;
        default: // drawType==CLIP
            if (traces == 0)
                ;//cb.rectangle(0, 0, 0, 0);
//            if (points.getWindingRule() == PathIterator.WIND_EVEN_ODD)
//            	cb.clipPath();
//            	cb.eoClip();
//            else
//                cb.clip();
//            cb.newPath();
        }
    }

	private void ensureFillColor() throws IOException {
		_currentPage.setBrushColor(new float[] { _color.getRed(), _color.getGreen(), _color.getBlue()} );
	}

	private void ensureStrokeColor() throws IOException {
		_currentPage.setPenColor(_color.getRed(), _color.getGreen(), _color.getBlue());
	}

	private void setStrokeDiff(Stroke _stroke2, Stroke _oldStroke2) {
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
