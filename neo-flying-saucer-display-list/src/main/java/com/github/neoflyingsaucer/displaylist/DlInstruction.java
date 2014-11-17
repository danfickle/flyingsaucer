package com.github.neoflyingsaucer.displaylist;

import java.awt.BasicStroke;
import java.util.Arrays;
import java.util.Locale;

import com.github.neoflyingsaucer.extend.output.DlItem;

public class DlInstruction
{
	public static class DlLine implements DlItem
	{
		public final int x1;
		public final int y1;
		public final int x2;
		public final int y2;
	
		public DlLine(int x1, int y1, int x2, int y2)
		{
			this.x1 = x1;
			this.y1 = y1;
			this.x2 = x2;
			this.y2 = y2;
		}
		
		@Override
		public String toString() 
		{
			return String.format(Locale.US, "{%d, %d, %d, %d}", x1, y1, x2, y2);
		}
	}
	
	public static class DlTranslate implements DlItem
	{
		final double tx, ty;
		
		public DlTranslate(double tx, double ty)
		{
			this.tx = tx;
			this.ty = ty;
		}
		
		@Override
		public String toString() 
		{
			return String.format(Locale.US, "{%f %f}", tx, ty);
		}
	}
	
	public static class DlStroke implements DlItem
	{
		final BasicStroke stroke;
		
		public DlStroke(BasicStroke stroke)
		{
			this.stroke = stroke;
		}
		
		@Override
		public String toString() 
		{
			return String.format(Locale.US, "{%f %d %d %f %s %f}", 
					stroke.getLineWidth(), stroke.getEndCap(), stroke.getLineJoin(), 
					stroke.getMiterLimit(), Arrays.asList(stroke.getDashArray()), stroke.getDashPhase());
		}
	}
	
	public static class DlOpacity implements DlItem
	{
		final float opacity;
		
		public DlOpacity(float opacity)
		{
			this.opacity = opacity;
		}
		
		@Override
		public String toString() 
		{
			return String.format(Locale.US, "{%f}", opacity);
		}
	}
}
