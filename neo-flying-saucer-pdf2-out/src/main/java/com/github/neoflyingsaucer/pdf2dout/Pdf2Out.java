package com.github.neoflyingsaucer.pdf2dout;

import java.awt.BasicStroke;
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
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.imageio.ImageIO;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionURI;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDBorderStyleDictionary;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageXYZDestination;

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
import com.github.neoflyingsaucer.extend.controller.cancel.FSCancelController;
import com.github.neoflyingsaucer.extend.controller.error.FSErrorController;
import com.github.neoflyingsaucer.extend.controller.error.FSError.FSErrorLevel;
import com.github.neoflyingsaucer.extend.controller.error.LangId;
import com.github.neoflyingsaucer.extend.output.DisplayList;
import com.github.neoflyingsaucer.extend.output.DisplayListOuputDevice;
import com.github.neoflyingsaucer.extend.output.DlItem;
import com.github.neoflyingsaucer.extend.output.FSFont;
import com.github.neoflyingsaucer.extend.output.FSImage;
import com.github.neoflyingsaucer.extend.output.JustificationInfo;
import com.github.neoflyingsaucer.extend.output.ReplacedElement;
import com.github.neoflyingsaucer.pdf2dout.Pdf2FontResolver.FontDescription;
import com.github.neoflyingsaucer.pdf2dout.Pdf2ReplacedElementResolver.Pdf2ImageReplacedElement;

import static com.github.neoflyingsaucer.pdf2dout.Pdf2PdfBoxWrapper.*;

public class Pdf2Out implements DisplayListOuputDevice 
{
	private final float _dotsPerPoint;
	private final PdfOutMode _mode;
	private final Pdf2BookmarkManager bookmarkManager = new Pdf2BookmarkManager();

    private Stroke _stroke = STROKE_ONE;

    private float _opacity = 1;
    private float _pageHeight;
    private AffineTransform _transform = new AffineTransform();
    private DlItem _color;
    private Area _clip;
    private Pdf2Font _font;
	private PDDocument _pdf;
	private OutputStream _os;
	private PDPage _currentPg;
	private PDPageContentStream _content;

	private int lGradientObjNumber = 0;
	private int specialPatternCount = 0;
	private int nextGStateNumber = 0;
	private Map<Float, String> opacityExtGStates = new HashMap<Float, String>();
    
    private static final BasicStroke STROKE_ONE = new BasicStroke(1);
    private static final AffineTransform IDENTITY = new AffineTransform();
    private static final int FILL = 1;
    private static final int STROKE = 2;
    private static final int CLIP = 3;
	
    public static enum PdfOutMode
    {
    	PRODUCTION_MODE,
    	TEST_MODE;
    }
    
	public Pdf2Out(float dotsPerPoint, PdfOutMode mode)
	{
		this._dotsPerPoint = dotsPerPoint;
		this._mode = mode;
	}
	
	@Override
	public void render(DisplayList dl)
	{
		for (DlItem item : dl.getDisplayList())
		{
			FSCancelController.cancelOpportunity(Pdf2Out.class);
			
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

	/**
	 * Creates an internal link.
	 * @param link
	 */
	protected void createInternalLink(DlInternalLink link)
	{
		Point2D docCorner = new Point2D.Double(link.x1, link.y1);
		Point2D pdfCorner = new Point2D.Double();
		
		_transform.transform(docCorner, pdfCorner);

		pdfCorner.setLocation(pdfCorner.getX(), normalizeY((float) pdfCorner.getY()));

		Rectangle2D targetArea = new Rectangle2D.Float((float) pdfCorner.getX(), (float) pdfCorner.getY(),
	                (float) getDeviceLength(link.w), (float) getDeviceLength(link.h));
		
		float destY = _pageHeight - link.y / _dotsPerPoint;
		
		PDPageXYZDestination destination = new PDPageXYZDestination();
		destination.setPageNumber(link.pageNo);
		destination.setTop((int) destY);
		
		PDRectangle rect = new PDRectangle();
		rect.setLowerLeftX((float) targetArea.getMinX());
		rect.setLowerLeftY((float) targetArea.getMinY());
		rect.setUpperRightX((float) targetArea.getMaxX());
		rect.setUpperRightY((float) targetArea.getMaxY());
		
		PDBorderStyleDictionary borderNone = new PDBorderStyleDictionary();
		borderNone.setWidth(0);
		
		PDAnnotationLink annotation = new PDAnnotationLink();
		annotation.setDestination(destination);
		annotation.setPage(_currentPg);
		annotation.setRectangle(rect); 
		annotation.setBorderStyle(borderNone);
		
		pdfAddAnnotation(_currentPg, annotation);
	}

	/**
	 * Creates an external link.
	 * @param link
	 */
	protected void createLink(DlExternalLink link)
	{
		Point2D docCorner = new Point2D.Double(link.x1, link.y1);
		Point2D pdfCorner = new Point2D.Double();
		
		_transform.transform(docCorner, pdfCorner);

		pdfCorner.setLocation(pdfCorner.getX(), normalizeY((float) pdfCorner.getY()));

		Rectangle2D targetArea = new Rectangle2D.Float((float) pdfCorner.getX(), (float) pdfCorner.getY(),
	                (float) getDeviceLength(link.w), (float) getDeviceLength(link.h));
		
		PDRectangle rect = new PDRectangle();
		rect.setLowerLeftX((float) targetArea.getMinX());
		rect.setLowerLeftY((float) targetArea.getMinY());
		rect.setUpperRightX((float) targetArea.getMaxX());
		rect.setUpperRightY((float) targetArea.getMaxY());
		
		PDBorderStyleDictionary borderNone = new PDBorderStyleDictionary();
		borderNone.setWidth(0);
		
		PDActionURI action = new PDActionURI();
		action.setURI(link.uri);
		
		PDAnnotationLink annotation = new PDAnnotationLink();
		annotation.setAction(action);
		annotation.setBorderStyle(borderNone);
		annotation.setRectangle(rect);

		pdfAddAnnotation(_currentPg, annotation);
	}
	
    private float getDeviceLength(float length) 
    {
        return length / _dotsPerPoint;
    }
	
    /**
     * Creates a PDF bookmark. Some PDF viewers use this as the table of contents.
     * @param bm
     */
	protected void createBookmark(DlBookmark bm)
	{
		PDPageXYZDestination destination = new PDPageXYZDestination();
		destination.setTop((int) (_pageHeight - (bm.y / _dotsPerPoint)));
		destination.setPageNumber(bm.pageNo);
		
		bookmarkManager.addBookmark(bm, destination);
	}

	public void finish()
	{
		bookmarkManager.outputBookmarks(_pdf);
		pdfSavePdf(_pdf, _os);
		pdfCloseDocument(_pdf);
	}
	
	/**
	 * MUST be called after rendering every page.
	 */
	public void finishPage()
	{
		pdfCloseContent(_content);
	}
	
    /**
	 * Draws a linear gradient on the PDF page.
	 * @param g DlLinearGradient using display list coordinate system.
	 */
	protected void drawLinearGradient(DlLinearGradient g)
	{
        Rectangle2D.Float rect = new Rectangle2D.Float(g.x, g.y, g.width, g.height);
        Shape s = _transform.createTransformedShape(rect);
        Rectangle2D rect2 = s.getBounds2D();

        float y1 = (float) (_pageHeight - rect2.getMinY());
        float y2 = (float) (_pageHeight - rect2.getMaxY());
        float y3 = Math.min(y1,  y2);

        Pdf2LinearGradient lg = new Pdf2LinearGradient(g, (float) rect2.getMinX(), y3, 
        		(float) rect2.getWidth(), (float) rect2.getHeight(),
        		_dotsPerPoint, specialPatternCount, _opacity);
        
        lg.paint(_currentPg, _content, nextGStateNumber, lGradientObjNumber);
        
        lGradientObjNumber++;
        
        if (lg.hasAlpha())
        {
        	nextGStateNumber++;
        	specialPatternCount++;
        }
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

    
    /**
     * Draws a string on the PDF page. This entails ouputting the string and a transformation matrix specifying its
     * position and size.
     * @param s
     * @param x
     * @param y
     * @param info An optional spacing info for custom letter spacing.
     */
	protected void drawString(String s, float x, float y, JustificationInfo info)
	{
		if (s.isEmpty())
			return;

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

	    pdfBeginText(_content);
	    
	    // Check if bold or italic need to be emulated
	    boolean resetMode = false;
//
	    FontDescription desc = _font.getFontDescription();
	    float fontSize = _font.getSize2D() / _dotsPerPoint;
	    pdfSetFont(desc.getFont(), fontSize, _content);

	    float b = (float) mx[1];
	    float c = (float) mx[2];
//
////     TODO
////	    final FontSpecificationI fontSpec = getFontSpecification();
////
////	    if (fontSpec != null) 
////	    {
////	        final int need = fontSpec.getFontWeight();
////	        final int have = desc.getWeight();
////
////	        if (need > have) {
////                cb.setTextRenderingMode(2); // TEXT_RENDER_MODE_FILL_STROKE
////                final float lineWidth = fontSize * 0.04f; // 4% of font size
////                cb.setPenWidth(lineWidth);
////                ensureStrokeColor();
////                resetMode = true;
////            }
////
////	        if ((fontSpec.getStyle() == FontStyle.ITALIC) && (desc.getStyle() != FontStyle.ITALIC)) 
////	        {
////	        	b = 0f;
////	            c = 0.21256f;
////	        }
////	    }
//
	    pdfSetTextMatrix((float) mx[0], b, c, (float) mx[3], (float) mx[4], (float) mx[5], _content);
	    
	    if (info == null) 
	    {
	    	pdfDrawString(s, _content);
	    }
	    else
	    {
	    	final char[] cc = s.toCharArray();
	    	final float[] justification = makeJustificationArray(cc, info);

	    	// TODO: cb.showText(cc, justification);
	    	pdfDrawString(s, _content);
	    }
//	    
//	    if (resetMode)
//	    {
//	    	cb.setTextRenderingMode(0 /* TEXT_RENDER_MODE_FILL */);
//	        cb.setPenWidth(1);
//	    }
//
	    pdfEndText(_content);
	}
	

	protected void setFont(FSFont font) 
	{
		_font = (Pdf2Font) font;
	}
	
	/**
	 * Adds a flip y transformaton to the given AffineTransform.
	 * @see {@link #normalizeY(float)}
	 * @param current
	 * @return The flipped y transformation.
	 */
    private AffineTransform normalizeMatrix(final AffineTransform current) {
        double[] mx = new double[6];
        AffineTransform result = new AffineTransform();
        result.getMatrix(mx);
        mx[3] = -1;
        mx[5] = _pageHeight;
        result = new AffineTransform(mx);
        result.concatenate(current);
        return result;
    }

    /**
     * Draws an image. JPEGs can be directly inserted into a PDF document, while other image types (PNG, GIF)
     * must be decompressed and then compressed in PDF format.
     * 
     * Currently, we add images in their original size and then instruct the PDF to scale them to the desired
     * size and position with a transformation matrix.
     * @param fsImage
     * @param x
     * @param y
     */
	protected void drawImage(FSImage fsImage, int x, int y) 
	{
		Pdf2Image image = ((Pdf2Image) fsImage);

        if (fsImage.getHeight() <= 0 || fsImage.getWidth() <= 0) {
            return;
        }

        AffineTransform at = AffineTransform.getTranslateInstance(x, y);
        at.translate(0, fsImage.getHeight());
        at.scale(fsImage.getWidth(), fsImage.getHeight());

        AffineTransform inverse = normalizeMatrix(_transform);
        AffineTransform flipper = AffineTransform.getScaleInstance(1, -1);
        inverse.concatenate(at);
        inverse.concatenate(flipper);

        String name = registerExtGState(_opacity, _currentPg);
		pdfAppendRawCommand("/" + name + " gs\n", _content);
        
        if (image.isJpeg())
        {
        	PDImageXObject jpeg = pdfCreateJpeg(_pdf, new ByteArrayInputStream(image.getBytes()));
        	pdfDrawXObject(jpeg, inverse, _content);
        }
        else
        {
        	
        	PDImageXObject pixel;
        	
        	try
        	{
        		BufferedImage img = ImageIO.read(new ByteArrayInputStream(image.getBytes()));
        		pixel = LosslessFactory.createFromImage( _pdf, img );
        	}
        	catch (IOException e)
        	{
        		FSErrorController.log(Pdf2Out.class, FSErrorLevel.ERROR, LangId.COULDNT_LOAD_IMAGE, image.getUri());
        		return;
        	}
        	
        	pdfDrawXObject(pixel, inverse, _content);
        }
    }

	public void initializePdf(OutputStream os)
	{
		_pdf = new PDDocument();
		_os = os;
	}
	
	public void initializePage(float w, float h)
	{
		_currentPg = new PDPage();
		_pdf.addPage(_currentPg);
		
		_currentPg.setMediaBox(new PDRectangle(w / _dotsPerPoint, h / _dotsPerPoint));
		_content = pdfCreateContentStream(_pdf, _currentPg, _mode);

		_transform = new AffineTransform();
		_transform.scale(1.0d / _dotsPerPoint, 1.0d / _dotsPerPoint);

		_pageHeight = h / _dotsPerPoint;
		
		opacityExtGStates.clear();
		
		pdfSaveGraphics(_content);
		//_linkTargetAreas = new HashSet<Rectangle2D>();
	}
	
	protected void drawReplaced(ReplacedElement replaced)
	{
		if (replaced instanceof Pdf2ImageReplacedElement)
		{
			Pdf2ImageReplacedElement repImage = (Pdf2ImageReplacedElement) replaced;
            FSImage image = ((Pdf2ImageReplacedElement) replaced).getScaledImage();
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
	
	protected void drawOval(int x1, int y1, int width, int height) 
	{
		drawEllipse(x1, y1, width, height, STROKE);
	}
	
	private void drawEllipse(int x1, int y1, int width, int height, int operation)
	{
	   assert(operation == STROKE || operation == FILL);
		
		// The best 4-spline magic number
       final float m4 = 0.551784f;

       Point2D point = new Point2D.Float(x1, y1);
       Point2D correctedPoint = new Point2D.Float();
       _transform.transform(point, correctedPoint);
       
       float x = (float) correctedPoint.getX();
       float y = (float) correctedPoint.getY();
       float r1 = (float) (width * _transform.getScaleX());
       float r2 = (float) (height * _transform.getScaleY());

       y += r2;
       x += r1;
       r1 *= 0.5f;
       r2 *= 0.5f;
       
       pdfMoveTo(x, normalizeY(y - r2), _content);

       float[] coords = new float[] { x + m4 * r1, y - r2, x + r1, y - m4 * r2, x + r1, y };  
       normalizeY(coords);
       
       pdfCurveTo(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5], _content);

       coords = new float[] { x + r1, y + m4*r2, x + m4*r1, y + r2,x, y + r2 };
       normalizeY(coords);
      
       pdfCurveTo(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5], _content);
       
       coords = new float[] { x - m4 * r1, y + r2, x - r1, y + m4 * r2, x - r1, y }; 
       normalizeY(coords);
       
       pdfCurveTo(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5], _content);
       
       coords = new float[] { x - r1, y - m4 * r2, x - m4 * r1, y - r2, x, y - r2 }; 
       normalizeY(coords);
       
       pdfCurveTo(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5], _content);
       pdfCloseSubpath(_content);

       if (operation == FILL)
			pdfFillEvenOdd(_content);
       else if (operation == STROKE)
			pdfStroke(_content);
    }
	
	protected void fillOval(int x, int y, int width, int height) 
	{
		drawEllipse(x, y, width, height, FILL);
	}
	
	protected void setClip(Shape s) 
	{
		pdfRestoreGraphics(_content);
		pdfSaveGraphics(_content);

		Shape s2 = null;
		
		if (s != null)
            s2 = _transform.createTransformedShape(s);

        if (s2 == null) {
            _clip = null;
        } else {
            _clip = new Area(s2);
            followPath(s2, CLIP);
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
	
	/**
	 * Transforms stroke details into PDF coordinates.
	 * @param stroke Stroke in display list units.
	 * @return Stroke in PDF units.
	 */
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
	
	/**
	 * Converts an AWT stroke to a PDF compatible stroke setting string.
	 * @param newStroke
	 */
	private void setStrokeDiff(Stroke newStroke) 
	{
		if (!(newStroke instanceof BasicStroke))
			return;

		final BasicStroke nStroke = (BasicStroke) newStroke;

		pdfSetLineWidth(nStroke.getLineWidth(), _content);
		
		switch (nStroke.getEndCap()) {
        case BasicStroke.CAP_BUTT:
            pdfSetLineCap(0, _content);
            break;
        case BasicStroke.CAP_SQUARE:
            pdfSetLineCap(2, _content);
            break;
        default:
            pdfSetLineCap(1, _content);
            break;
        }
		
        switch (nStroke.getLineJoin()) {
        case BasicStroke.JOIN_MITER:
            pdfSetLineJoin(0, _content);
            break;
        case BasicStroke.JOIN_BEVEL:
            pdfSetLineJoin(2, _content);
            break;
        default:
            pdfSetLineJoin(1, _content);
            break;
        }

        // TODO cb.setMiterLimit(nStroke.getMiterLimit());
        float dash[] = nStroke.getDashArray();

        if (dash == null)
        {
        	pdfSetLineDash(new float[] {}, 0, _content);
        }
        else 
        {
        	pdfSetLineDash(dash, nStroke.getDashPhase(), _content);
        }
    }

	/**
	 * PDF documents use a system where y is zero at the bottom of the document and counts up
	 * to the top of the document. The display list provides a top to bottom system of units.
	 * This method converts from display list to PDF y units.
	 * @param y The y position in display list system.
	 * @return The y position in PDF y units.
	 */
	private float normalizeY(final float y) 
	{
		return _pageHeight - y;
    }

	/**
	 * @see {@link #normalizeY(float)}
	 * @param coords An array of x y coordinates.
	 */
    private void normalizeY(final float[] coords) 
    {
        coords[1] = normalizeY(coords[1]);
        coords[3] = normalizeY(coords[3]);
        coords[5] = normalizeY(coords[5]);
    }
	
    String registerExtGState(float opacity, PDPage page)
	{
		String name = opacityExtGStates.get(opacity);
		
		if (name != null)
			return name;
		
		PDResources resources = _currentPg.getResources();

		PDExtendedGraphicsState extgstate = new PDExtendedGraphicsState();
		extgstate.setStrokingAlphaConstant(opacity);
		extgstate.setNonStrokingAlphaConstant(opacity);

		name = "MYGS" + nextGStateNumber++;
		resources.put( COSName.getPDFName( name ), extgstate );
		opacityExtGStates.put(opacity, name);
		
		return name;
	}

	private void ensureFillColor()
	{
		if (_color instanceof DlRGBColor)
		{
			DlRGBColor rgba = (DlRGBColor) _color;
			pdfSetFillColor(rgba.r, rgba.g, rgba.b, _content);

			float opac = _opacity * (rgba.a / 255);
			String name = registerExtGState(opac, _currentPg);
			pdfAppendRawCommand("/" + name + " gs\n", _content);
		}
		else if (_color instanceof DlCMYKColor)
		{
			DlCMYKColor cmyk = (DlCMYKColor) _color;
			pdfSetFillColor(cmyk.c, cmyk.m, cmyk.y, cmyk.k, _content);

			String name = registerExtGState(_opacity, _currentPg);
			pdfAppendRawCommand("/" + name + " gs\n", _content);
		}
	}
	
	private void ensureStrokeColor()
	{
		if (_color instanceof DlRGBColor)
		{
			DlRGBColor rgba = (DlRGBColor) _color;
			pdfSetStrokingColor(rgba.r, rgba.g, rgba.b, _content);
			
			float opac = _opacity * (rgba.a / 255);
			String name = registerExtGState(opac, _currentPg);
			pdfAppendRawCommand("/" + name + " gs\n", _content);
		}
		else if (_color instanceof DlCMYKColor)
		{
			DlCMYKColor cmyk = (DlCMYKColor) _color;
			pdfSetStrokingColor(cmyk.c, cmyk.m, cmyk.y, cmyk.k, _content);

			String name = registerExtGState(_opacity, _currentPg);
			pdfAppendRawCommand("/" + name + " gs\n", _content);
		}
	}
	
	/**
	 * Follows an AWT shape, converting into PDF operations.
	 * @param s The shape to follow.
	 * @param drawType One of STROKE, FILL, CLIP.
	 */
    private void followPath(Shape s, final int drawType) 
    {
        if (s == null)
            return;
        
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

        while (!points.isDone())
        {
            ++traces;
            final int segtype = points.currentSegment(coords);
            normalizeY(coords);
            
            switch (segtype)
            {
            case PathIterator.SEG_CLOSE:
            	pdfCloseSubpath(_content);
            	break;

            case PathIterator.SEG_CUBICTO:
            	pdfCurveTo(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5], _content);
                break;

            case PathIterator.SEG_LINETO:
            	pdfLineTo(coords[0], coords[1], _content);
                break;

            case PathIterator.SEG_MOVETO:
            	pdfMoveTo(coords[0], coords[1], _content);
            	break;

            case PathIterator.SEG_QUADTO:
            	pdfCurveTo(coords[0], coords[1], coords[2], coords[3], _content);
            	break;
            }

            FSCancelController.cancelOpportunity(Pdf2Out.class);
            points.next();
        }

        switch (drawType) {
        case FILL:
            if (traces > 0) {
                if (points.getWindingRule() == PathIterator.WIND_EVEN_ODD)
                	pdfFillEvenOdd(_content);
                else
                	pdfFillNonZero(_content);
            }
            break;
        case STROKE:
            if (traces > 0)
                pdfStroke(_content);
            break;
        default: // drawType==CLIP
            if (traces == 0)
                ;//cb.rectangle(0, 0, 0, 0);
            if (points.getWindingRule() == PathIterator.WIND_EVEN_ODD)
            	pdfClipEvenOdd(_content);
            else
                pdfClipNonZero(_content);
        }
    }

    /**
     * Sets document information in a PDF document.
     * @param meta A map which may contain values for title, subject, author and keywords.
     * Usually obtained from html meta items in the head section of an html document.
     */
	public void setDocumentInformationDictionary(Map<String, String> meta) 
	{
		String title = meta.get("title");
		String subject = meta.get("subject");
		String author = meta.get("author");
		String keywords = meta.get("keywords");
		
		PDDocumentInformation info = _pdf.getDocumentInformation();
		
		if (title != null)
			info.setTitle(title);
			
		if (subject != null)
			info.setSubject(subject);
			
		if (author != null)
			info.setAuthor(author);
		
		if (keywords != null)
			info.setKeywords(keywords);
		
		info.setProducer("neoFlyingSaucer (https://github.com/danfickle/neoflyingsaucer)");
	}

	public PDDocument getDocument() 
	{
		return _pdf;
	}
}
