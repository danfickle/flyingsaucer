package com.github.neoflyingsaucer.pdf2dout;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.Raster;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.github.neoflyingsaucer.extend.output.DisplayListOuputDevice;
import com.github.neoflyingsaucer.extend.output.DlItem;
import com.github.neoflyingsaucer.extend.output.FSFont;
import com.github.neoflyingsaucer.extend.output.FSImage;
import com.github.neoflyingsaucer.extend.output.JustificationInfo;
import com.github.neoflyingsaucer.extend.output.ReplacedElement;
import com.github.neoflyingsaucer.pdf2dout.Pdf2FontResolver.FontDescription;
import com.github.neoflyingsaucer.pdf2dout.Pdf2ReplacedElementResolver.Pdf2ImageReplacedElement;
import com.github.pdfstream.Annotation;
import com.github.pdfstream.Bookmark;
import com.github.pdfstream.Destination;
import com.github.pdfstream.JPGImage;
import com.github.pdfstream.LinearGradient;
import com.github.pdfstream.PDF;
import com.github.pdfstream.PNGImage;
import com.github.pdfstream.Page;
import com.github.pdfstream.PdfCmykColor;
import com.github.pdfstream.PdfColor;
import com.github.pdfstream.PdfGreyScaleColor;
import com.github.pdfstream.PdfRgbaColor;

public class Pdf2Out implements DisplayListOuputDevice 
{
	private final float _dotsPerPoint;
    private Page _currentPage;
    private Stroke _stroke = STROKE_ONE;
    private Stroke _originalStroke = STROKE_ONE;
    private float _opacity = 1;
    private float _pageHeight;
    private AffineTransform _transform = new AffineTransform();
    private DlItem _color;
    private Area _clip;
    private PDF _pdfDoc;
    private Pdf2Font _font;
    
    private static final BasicStroke STROKE_ONE = new BasicStroke(1);
    private static final AffineTransform IDENTITY = new AffineTransform();
    private static final Logger LOGGER = LoggerFactory.getLogger(Pdf2Out.class);
    private static final int FILL = 1;
    private static final int STROKE = 2;
    private static final int CLIP = 3;
	
	public Pdf2Out(float dotsPerPoint)
	{
		this._dotsPerPoint = dotsPerPoint;
	}
	
	@Override
	public void render(DisplayList dl)
	{
		for (DlItem item : dl.getDisplayList())
		{
			switch (item.getType())
			{
			case LINE:
			{
				DlLine obj = (DlLine) item;
				drawLine(obj.x1, obj.y1, obj.x2, obj.y2);
				break;
			}
			case RGBCOLOR:
			{
				DlRGBColor obj = (DlRGBColor) item;
				setColor(obj);
				break;
			}
			case STROKE:
			{
				DlStroke stk = (DlStroke) item;
				setStroke(stk.stroke);
				break;
			}
			case OPACITY:
			{
				DlOpacity opac = (DlOpacity) item;
				setOpacity(opac.opacity);
				break;
			}
			case RECTANGLE:
			{
				DlRectangle rect = (DlRectangle) item;

				if (rect.op == Operation.STROKE)
					drawRect(rect.x, rect.y, rect.width, rect.height);
				else if (rect.op == Operation.FILL)
					fillRect(rect.x, rect.y, rect.width, rect.height);

				break;
			}
			case TRANSLATE:
			{
				DlTranslate trans = (DlTranslate) item;
				translate(trans.tx, trans.ty);
				break;
			}
			case CLIP:
			{
				DlClip clip = (DlClip) item;
				clip(clip.clip);
				break;
			}
			case SET_CLIP:
			{
				DlSetClip clip = (DlSetClip) item;
				setClip(clip.clip);
				break;				
			}
			case OVAL:
			{
				DlOval oval = (DlOval) item;
				
				if (oval.op == Operation.STROKE)
					drawOval(oval.x, oval.y, oval.width, oval.height);
				else if (oval.op == Operation.FILL)
					fillOval(oval.x, oval.y, oval.width, oval.height);
				
				break;
			}
			case DRAW_SHAPE:
			{
				DlDrawShape draw = (DlDrawShape) item;
				
				if (draw.op == Operation.STROKE)
					draw(draw.shape);
				else if (draw.op == Operation.FILL)
					fill(draw.shape);
				
				break;
			}
			case IMAGE:
			{
				DlImage img = (DlImage) item;
				drawImage(img.image, img.x, img.y);
				break;
			}
			case FONT:
			{
				DlFont font = (DlFont) item;
				setFont(font.font);
				break;
			}
			case STRING:
			{
				DlString s = (DlString) item;
				drawString(s.txt, s.x, s.y, null);
				break;
			}
			case STRING_EX:
			{
				DlStringEx s = (DlStringEx) item;
				drawString(s.txt, s.x, s.y, s.info);
				break;
			}
			case GLYPH_VECTOR:
			{
				DlGlyphVector g = (DlGlyphVector) item;
				//drawGlyphVector(g.vec, (int) g.x, (int) g.y);
				break;
			}
			case AA_OFF:
			{
				
				break;
			}
			case AA_DEFAULT:
			{
				
				break;
			}
			case REPLACED:
			{
				DlReplaced replaced = (DlReplaced) item;
				drawReplaced(replaced.replaced);
				break;
			}
			case LINEAR_GRADIENT:
			{
				DlLinearGradient linear = (DlLinearGradient) item;
				drawLinearGradient(linear);
				break;
			}
			case CMYKCOLOR:
			{
				DlCMYKColor obj = (DlCMYKColor) item;
				setColor(obj);
				break;
			}
			case BOOKMARK:
			{
				DlBookmark bm = (DlBookmark) item;
				createBookmark(bm);
				break;
			}
			case EXTERNAL_LINK:
			{
				DlExternalLink link = (DlExternalLink) item;
				createLink(link);
				break;
			}
			case INTERNAL_LINK:
			{
				DlInternalLink link = (DlInternalLink) item;
				createInternalLink(link);
				break;
			}
			}
		}
	}

	protected void createInternalLink(DlInternalLink link)
	{
		Point2D docCorner = new Point2D.Double(link.x1, link.y1);
		Point2D pdfCorner = new Point2D.Double();
		
		_transform.transform(docCorner, pdfCorner);

		pdfCorner.setLocation(pdfCorner.getX(), normalizeY((float) pdfCorner.getY()));

		Rectangle2D targetArea = new Rectangle2D.Float((float) pdfCorner.getX(), (float) pdfCorner.getY(),
	                (float) getDeviceLength(link.w), (float) getDeviceLength(link.h));
		
		float destY = _pageHeight - link.y / _dotsPerPoint;
		
		Destination destination = new Destination(0, destY, 0);
        destination.setPageObjNumber(link.pageNo);

        Annotation annot = new Annotation(null, destination,
        		(float) targetArea.getMinX(), (float) targetArea.getMinY(),
                (float) targetArea.getMaxX(), (float) targetArea.getMaxY());

        _currentPage.addAnnotation(annot);
	}
		
	protected void createLink(DlExternalLink link)
	{
		Point2D docCorner = new Point2D.Double(link.x1, link.y1);
		Point2D pdfCorner = new Point2D.Double();
		
		_transform.transform(docCorner, pdfCorner);

		pdfCorner.setLocation(pdfCorner.getX(), normalizeY((float) pdfCorner.getY()));

		Rectangle2D targetArea = new Rectangle2D.Float((float) pdfCorner.getX(), (float) pdfCorner.getY(),
	                (float) getDeviceLength(link.w), (float) getDeviceLength(link.h));
		
        Annotation annot = new Annotation(link.uri, (Destination) null,
        		(float) targetArea.getMinX(), (float) targetArea.getMinY(),
                (float) targetArea.getMaxX(), (float) targetArea.getMaxY());

        _currentPage.addAnnotation(annot);
	}
	
    private float getDeviceLength(float length) 
    {
        return length / _dotsPerPoint;
    }
	
	protected void createBookmark(DlBookmark bm)
	{
		float destY = _pageHeight - bm.y / _dotsPerPoint;
System.err.println(destY + "   " + bm.y + "  " + bm.content);
		Destination destination = new Destination(0, destY, 0);
        destination.setPageObjNumber(bm.pageNo);
		
    	Bookmark pdfbm = new Bookmark(destination, bm.content, bm.level);
    	_currentPage.getPdf().addBookmark(pdfbm);
	}

	public void finish()
	{
		try {
			_pdfDoc.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void finishPage() 
	{
		_currentPage.restore();
	}
	
	protected void drawLinearGradient(DlLinearGradient g) 
	{
        Rectangle2D.Float rect = new Rectangle2D.Float(g.x, g.y, g.width, g.height);
        Shape s = _transform.createTransformedShape(rect);
        Rectangle2D rect2 = s.getBounds2D();

        float y1 = (float) (_pageHeight - rect2.getMinY());
        float y2 = (float) (_pageHeight - rect2.getMaxY());
        float y3 = Math.min(y1,  y2);

		LinearGradient lg = _currentPage.getPdf().addLinearGradient(g, _dotsPerPoint, (float) rect2.getMinX(), y3, (float) rect2.getWidth(), (float) rect2.getHeight());
     
        _currentPage.drawLinearGradient(lg);
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

	protected void drawString(String s, float x, float y, JustificationInfo info)
	{
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

//     TODO
//	    final FontSpecificationI fontSpec = getFontSpecification();
//
//	    if (fontSpec != null) 
//	    {
//	        final int need = fontSpec.getFontWeight();
//	        final int have = desc.getWeight();
//
//	        if (need > have) {
//                cb.setTextRenderingMode(2); // TEXT_RENDER_MODE_FILL_STROKE
//                final float lineWidth = fontSize * 0.04f; // 4% of font size
//                cb.setPenWidth(lineWidth);
//                ensureStrokeColor();
//                resetMode = true;
//            }
//
//	        if ((fontSpec.getStyle() == FontStyle.ITALIC) && (desc.getStyle() != FontStyle.ITALIC)) 
//	        {
//	        	b = 0f;
//	            c = 0.21256f;
//	        }
//	    }

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
	

	public void setFont(FSFont font) 
	{
		_font = (Pdf2Font) font;
		
		_currentPage.getPdf().registerFont(_font.getFontDescription().getFont());
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

	public void drawImage(FSImage fsImage, int x, int y) 
	{
		final Pdf2Image image = ((Pdf2Image) fsImage);

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

	public void initializePdf(OutputStream os) throws Exception
	{
		_pdfDoc = new PDF(os);
	}
	
	public void initializePage(float w, float h)
	{
        float[] nextPageSize = new float[] { 
        		w / _dotsPerPoint,
        		h / _dotsPerPoint };
		
		_currentPage = new Page(_pdfDoc, nextPageSize);
		_pageHeight = nextPageSize[1];
		_transform = new AffineTransform();
		_transform.scale(1.0d / _dotsPerPoint, 1.0d / _dotsPerPoint);
		_currentPage.save();
        //_linkTargetAreas = new HashSet<Rectangle2D>();
	}
	
	protected void drawReplaced(ReplacedElement replaced)
	{
		if (replaced instanceof Pdf2ImageReplacedElement)
		{
            FSImage image = ((Pdf2ImageReplacedElement) replaced).getImage();
            Point location = replaced.getLocation();
            drawImage(image, location.x, location.y);
		}
	}
	
	protected void fill(Shape s) 
	{
		followPath(s, FILL);
	}
	
	protected void draw(Shape s) 
	{
		followPath(s, STROKE);
	}
	
	protected void drawOval(int x, int y, int width, int height) 
	{
		// TODO Auto-generated method stub
	}

	protected void fillOval(int x, int y, int width, int height) 
	{
		// TODO Auto-generated method stub
	}
	
	protected void setClip(Shape s) 
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
	
	protected void clip(Shape s) 
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

	protected void translate(double tx, double ty) 
	{
		_transform.translate(tx, ty);
	}
	
	protected void drawRect(int x, int y, int width, int height) 
	{
		Rectangle2D.Double d = new Rectangle2D.Double(x, y, width, height);
		followPath(d, STROKE);
	}
	
	protected void fillRect(int x, int y, int width, int height) 
	{
		Rectangle2D.Double d = new Rectangle2D.Double(x, y, width, height);
		followPath(d, FILL);
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
	
	protected void setOpacity(float opacity)
	{
		_opacity = opacity;
	}

	protected void setStroke(Stroke s)
	{
		_originalStroke = s;
		_stroke = transformStroke(s);
	}
	
	protected void setColor(DlItem color)
	{
		_color = color;
	}

	protected void drawLine(int x1, int y1, int x2, int y2) 
	{
		Shape line = new Line2D.Double(x1, y1, x2, y2);
		followPath(line, STROKE);
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
	
	private PdfColor getPdfColor(DlItem col, float opacity)
	{
			PdfColor pdfColor = PdfGreyScaleColor.BLACK;

			if (col instanceof DlRGBColor)
			{
				DlRGBColor rgba = (DlRGBColor) col;
				pdfColor = new PdfRgbaColor(
					rgba.r, rgba.g, rgba.b, (int) (rgba.a * opacity));			
			}
			else if (col instanceof DlCMYKColor)
			{
				DlCMYKColor cmyk = (DlCMYKColor) col;
				pdfColor = new PdfCmykColor(cmyk.c, cmyk.m, cmyk.y, cmyk.k, 1.0f);
			}
			else
			{
				assert(false);
			}
	    	
	    	return pdfColor;
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
	
	private void ensureFillColor() 
	{
		PdfColor pdfColor = getPdfColor(_color, _opacity);
		_currentPage.setBrushColor(pdfColor);
	}

	private void ensureStrokeColor()
	{
		PdfColor pdfColor = getPdfColor(_color, _opacity);
		_currentPage.setPenColor(pdfColor);
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

    /**
     * Sets the page count. MUST be called before outputting the first page.
     * @param pageCount
     */
    public void setPageCount(int pageCount)
	{
		_pdfDoc.setPageCount(pageCount);
	}
}
