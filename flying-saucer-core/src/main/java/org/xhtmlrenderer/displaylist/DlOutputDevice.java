package org.xhtmlrenderer.displaylist;

import java.awt.Rectangle;
import java.awt.RenderingHints.Key;
import java.awt.BasicStroke;
import java.awt.Shape;
import java.awt.Stroke;

import org.xhtmlrenderer.css.parser.FSColor;
import org.xhtmlrenderer.css.style.derived.FSLinearGradient;
import org.xhtmlrenderer.extend.OutputDevice;
import org.xhtmlrenderer.render.AbstractOutputDevice;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.render.BorderPainter;
import org.xhtmlrenderer.render.InlineText;
import org.xhtmlrenderer.render.RenderingContext;

import com.github.neoflyingsaucer.displaylist.DisplayListImpl;
import com.github.neoflyingsaucer.displaylist.DlInstruction;
import com.github.neoflyingsaucer.extend.output.FSFont;
import com.github.neoflyingsaucer.extend.output.FSImage;

public class DlOutputDevice extends AbstractOutputDevice implements OutputDevice 
{
	private final DisplayListImpl dl;
	
	public DlOutputDevice(DisplayListImpl displayList) 
	{
		this.dl = displayList;
	}

	@Override
	public void setOpacity(float opacity) 
	{
		dl.add(new DlInstruction.DlOpacity(opacity));
	}

	@Override
    protected void drawLine(int x1, int y1, int x2, int y2) 
    {
    	dl.add(new DlInstruction.DlLine(x1, y1, x2, y2));
    }

	@Override
	public void translate(double tx, double ty) 
	{
		dl.add(new DlInstruction.DlTranslate(tx, ty));
	}

	@Override
	public void setStroke(Stroke s) 
	{
		if (!(s instanceof BasicStroke))
			return;
		
		BasicStroke basic = (BasicStroke) s;
		
		dl.add(new DlInstruction.DlStroke(basic));
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
	public void setColor(FSColor color) {
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
	public void draw(Shape s) {
		// TODO Auto-generated method stub
		
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
	public void drawBorderLine(Shape bounds, int side, int width, boolean solid) {
		// TODO Auto-generated method stub
		
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
		// TODO Auto-generated method stub
		
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
}
