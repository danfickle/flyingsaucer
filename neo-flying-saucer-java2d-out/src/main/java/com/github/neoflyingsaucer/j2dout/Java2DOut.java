package com.github.neoflyingsaucer.j2dout;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.GlyphVector;
import java.awt.geom.Point2D;

import com.github.neoflyingsaucer.displaylist.DlInstruction.DlClip;
import com.github.neoflyingsaucer.displaylist.DlInstruction.DlDrawShape;
import com.github.neoflyingsaucer.displaylist.DlInstruction.DlFont;
import com.github.neoflyingsaucer.displaylist.DlInstruction.DlGlyphVector;
import com.github.neoflyingsaucer.displaylist.DlInstruction.DlImage;
import com.github.neoflyingsaucer.displaylist.DlInstruction.DlLine;
import com.github.neoflyingsaucer.displaylist.DlInstruction.DlOpacity;
import com.github.neoflyingsaucer.displaylist.DlInstruction.DlOval;
import com.github.neoflyingsaucer.displaylist.DlInstruction.DlRGBColor;
import com.github.neoflyingsaucer.displaylist.DlInstruction.DlRectangle;
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
import com.github.neoflyingsaucer.extend.output.FSGlyphVector;
import com.github.neoflyingsaucer.extend.output.FSImage;
import com.github.neoflyingsaucer.extend.output.JustificationInfo;

public class Java2DOut implements DisplayListOuputDevice 
{
	protected final Graphics2D g2d;
	protected final Object aaHint;
	
	public Java2DOut(Graphics2D g2d, Object aaDefaultHint)
	{
		this.g2d = g2d;
		this.aaHint = aaDefaultHint;
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
				setRGBColor(obj.r, obj.g, obj.b, obj.a);
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
				drawString(s.txt, (int) s.x, (int) s.y);
				break;
			}
			case STRING_EX:
			{
				DlStringEx s = (DlStringEx) item;
				drawStringEx(s.txt, (int) s.x, (int) s.y, s.info);
				break;
			}
			case GLYPH_VECTOR:
			{
				DlGlyphVector g = (DlGlyphVector) item;
				drawGlyphVector(g.vec, (int) g.x, (int) g.y);
				break;
			}
			case AA_OFF:
			{
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
				break;
			}
			case AA_DEFAULT:
			{
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, aaHint);
				break;
			}
			case CMYKCOLOR:
			{
				// TODO: Convert color to rgb.
				break;
			}

			}
		}
	}
	
	protected void drawGlyphVector(FSGlyphVector vec, int x, int y)
	{
		GlyphVector vector = ((Java2DGlyphVector) vec).getGlyphVector();
        g2d.drawGlyphVector(vector, x, y);
	}
	
	protected void drawString(String txt, int x, int y)
	{
		g2d.drawString(txt, x, y);
	}
	
	protected void drawStringEx(String txt, int x, int y, JustificationInfo info)
	{
		GlyphVector vector = g2d.getFont().createGlyphVector(g2d.getFontRenderContext(), txt);
        
		if (vector.getNumGlyphs() == txt.length())
			adjustGlyphPositions(txt, info, vector);
		else
			adjustGlyphPositionsEx(txt, info, vector);

		g2d.drawGlyphVector(vector, x, y);
	}
	
	/* 
	 * Experimental, untested.
	 * Adjusts glyph positions, taking into account that there is not a one-to-one mapping between
	 * glyphs and characters.
	 */
	protected void adjustGlyphPositionsEx(String txt, JustificationInfo info, GlyphVector vector)
	{
		int numGlyphs = vector.getNumGlyphs();
        float adjust = 0.0f;
        
		for (int i = 0; i < numGlyphs; i++)
		{
			int ci = vector.getGlyphCharIndex(i);
			int c = txt.charAt(ci);
			
			if (i != 0)
            {
                Point2D point = vector.getGlyphPosition(i);
                vector.setGlyphPosition(i, new Point2D.Double(point.getX() + adjust, point.getY()));
            }
            
            if (c == ' ' || c == '\u00a0' || c == '\u3000')
                adjust += info.getSpaceAdjust();
            else
                adjust += info.getNonSpaceAdjust();
		}
	}
	
    protected void adjustGlyphPositions(String txt, JustificationInfo info, GlyphVector vector)
    {
        float adjust = 0.0f;
       
        for (int i = 0; i < txt.length(); i++)
        {
            final char c = txt.charAt(i);

            if (i != 0)
            {
                Point2D point = vector.getGlyphPosition(i);
                vector.setGlyphPosition(i, new Point2D.Double(point.getX() + adjust, point.getY()));
            }
            
            if (c == ' ' || c == '\u00a0' || c == '\u3000')
                adjust += info.getSpaceAdjust();
            else
                adjust += info.getNonSpaceAdjust();
        }
    }

    protected void setFont(FSFont font)
    {
       g2d.setFont(((Java2DFont) font).getAWTFont());
    }
	
    protected void drawImage(FSImage image, int x, int y)
    {
        g2d.drawImage(((Java2DImage) image).getAWTImage(), x, y, null);
    }
	
    protected void fillRect(int x, int y, int width, int height) 
    {
        g2d.fillRect(x, y, width, height);
    }
	
	protected void fill(Shape s) 
    {
        g2d.fill(s);
    }
	
	protected void draw(Shape s) 
	{
		g2d.draw(s);
	}
	
    protected void fillOval(int x, int y, int width, int height) 
    {
        g2d.fillOval(x, y, width, height);
    }
	
    protected void drawOval(int x, int y, int width, int height) 
    {
        g2d.drawOval(x, y, width, height);
    }
	
    protected void setClip(Shape s) 
    {
        g2d.setClip(s);
    }
    
    public void clip(Shape s) 
    {
        g2d.clip(s);
    }
	
	protected void translate(double tx, double ty) 
	{
		g2d.translate(tx, ty);
	}

	protected void drawRect(int x, int y, int width, int height) 
	{
        g2d.drawRect(x, y, width, height);
    }
	
	protected void drawLine(int x1, int y1, int x2, int y2)
	{
		g2d.drawLine(x1, y1, x2, y2);
	}
	
	protected void setRGBColor(int r, int g, int b, int a)
	{
        g2d.setColor(new Color(r, g, b, a));
	}
	
    protected void setStroke(BasicStroke s) 
    {
        g2d.setStroke(s);
    }
    
	protected void setOpacity(float opacity) 
	{
		if (opacity == 1)
		{
			g2d.setComposite(AlphaComposite.SrcOver);
		}
		else
		{
			g2d.setComposite(AlphaComposite.SrcOver.derive(opacity));
		}
	}
}
