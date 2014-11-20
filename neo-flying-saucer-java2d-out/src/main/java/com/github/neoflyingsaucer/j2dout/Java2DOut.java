package com.github.neoflyingsaucer.j2dout;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;

import com.github.neoflyingsaucer.displaylist.DlInstruction.DlClip;
import com.github.neoflyingsaucer.displaylist.DlInstruction.DlDrawShape;
import com.github.neoflyingsaucer.displaylist.DlInstruction.DlFont;
import com.github.neoflyingsaucer.displaylist.DlInstruction.DlImage;
import com.github.neoflyingsaucer.displaylist.DlInstruction.DlLine;
import com.github.neoflyingsaucer.displaylist.DlInstruction.DlOpacity;
import com.github.neoflyingsaucer.displaylist.DlInstruction.DlOval;
import com.github.neoflyingsaucer.displaylist.DlInstruction.DlRGBColor;
import com.github.neoflyingsaucer.displaylist.DlInstruction.DlRectangle;
import com.github.neoflyingsaucer.displaylist.DlInstruction.DlSetClip;
import com.github.neoflyingsaucer.displaylist.DlInstruction.DlStroke;
import com.github.neoflyingsaucer.displaylist.DlInstruction.DlTranslate;
import com.github.neoflyingsaucer.displaylist.DlInstruction.Operation;
import com.github.neoflyingsaucer.extend.output.DisplayList;
import com.github.neoflyingsaucer.extend.output.DisplayListOuputDevice;
import com.github.neoflyingsaucer.extend.output.DlItem;
import com.github.neoflyingsaucer.extend.output.FSFont;
import com.github.neoflyingsaucer.extend.output.FSImage;

public class Java2DOut implements DisplayListOuputDevice 
{
	protected final Graphics2D g2d;
	
	public Java2DOut(Graphics2D g2d)
	{
		this.g2d = g2d;
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
			case CMYKCOLOR:
			{
				// TODO: Convert color to rgb.
				break;
			}

			}
		}
	}
	
    protected void setFont(FSFont font)
    {
       g2d.setFont(((Java2DFont) font).getAWTFont());
    }
	
    protected void drawImage(FSImage image, int x, int y)
    {
        // TODO g2d.drawImage(((AWTFSImage) image).getImage(), x, y, null);
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
