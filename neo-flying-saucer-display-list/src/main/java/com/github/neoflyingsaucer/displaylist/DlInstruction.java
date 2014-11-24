package com.github.neoflyingsaucer.displaylist;

import java.awt.BasicStroke;
import java.awt.Shape;
import java.util.Arrays;
import java.util.Locale;

import com.github.neoflyingsaucer.extend.output.DlItem;
import com.github.neoflyingsaucer.extend.output.FSFont;
import com.github.neoflyingsaucer.extend.output.FSGlyphVector;
import com.github.neoflyingsaucer.extend.output.FSImage;
import com.github.neoflyingsaucer.extend.output.JustificationInfo;

public class DlInstruction
{
	public enum Operation
	{
		FILL,
		STROKE,
		CLIP;
	}
	
	public static class DlAntiAliasOff implements DlItem
	{
		public final DlType type = DlType.AA_OFF;
		
		public DlAntiAliasOff()
		{
		}

		@Override
		public DlType getType()
		{
			return type;
		}

		@Override
		public String toString() 
		{
			return String.format(Locale.US, "");
		}
	}
	
	public static class DlAntiAliasDefault implements DlItem
	{
		public final DlType type = DlType.AA_DEFAULT;
		
		public DlAntiAliasDefault()
		{
		}

		@Override
		public DlType getType()
		{
			return type;
		}

		@Override
		public String toString() 
		{
			return String.format(Locale.US, "");
		}
	}
	
	public static class DlGlyphVector implements DlItem
	{
		public final FSGlyphVector vec;
		public final float x, y;
		public final DlType type = DlType.GLYPH_VECTOR;
		
		public DlGlyphVector(FSGlyphVector vec, float x, float y)
		{
			this.vec = vec;
			this.x = x;
			this.y = y;
		}
		
		@Override
		public DlType getType()
		{
			return type;
		}

		@Override
		public String toString() 
		{
			// TODO: Make sure FSGlyphVector::toString returns something sensible.
			return String.format(Locale.US, "{%s %f %f}", vec, x, y);
		}
	}
	
	
	public static class DlStringEx implements DlItem
	{
		public final String txt;
		public final float x, y;
		public final JustificationInfo info;
		public final DlType type = DlType.STRING_EX;
		
		public DlStringEx(String txt, float x, float y, JustificationInfo info)
		{
			this.txt = txt;
			this.x = x;
			this.y = y;
			this.info = info;
		}
		
		@Override
		public DlType getType()
		{
			return type;
		}
		
		@Override
		public String toString() 
		{
			// TODO: Make sure JustificationInfo::toString returns something sensible.
			return String.format(Locale.US, "{%s, %f, %f}", txt, x, y);
		}
	}
	
	public static class DlString implements DlItem
	{
		public final String txt;
		public final float x, y;
		public final DlType type = DlType.STRING;
		
		public DlString(String txt, float x, float y)
		{
			this.txt = txt;
			this.x = x;
			this.y = y;
		}
		
		@Override
		public DlType getType()
		{
			return type;
		}
		
		@Override
		public String toString() 
		{
			return String.format(Locale.US, "{%s, %f, %f}", txt, x, y);
		}
	}
	
	public static class DlLine implements DlItem
	{
		public final int x1;
		public final int y1;
		public final int x2;
		public final int y2;
		public final DlType type = DlType.LINE;
	
		public DlLine(int x1, int y1, int x2, int y2)
		{
			this.x1 = x1;
			this.y1 = y1;
			this.x2 = x2;
			this.y2 = y2;
		}
		
		@Override
		public DlType getType()
		{
			return type;
		}
		
		@Override
		public String toString() 
		{
			return String.format(Locale.US, "{%d, %d, %d, %d}", x1, y1, x2, y2);
		}
	}
	
	public static class DlTranslate implements DlItem
	{
		public final double tx, ty;
		public final DlType type = DlType.TRANSLATE;
		
		public DlTranslate(double tx, double ty)
		{
			this.tx = tx;
			this.ty = ty;
		}
		
		@Override
		public DlType getType()
		{
			return type;
		}
		
		@Override
		public String toString() 
		{
			return String.format(Locale.US, "{%f %f}", tx, ty);
		}
	}
	
	public static class DlStroke implements DlItem
	{
		public final BasicStroke stroke;
		public final DlType type = DlType.STROKE;
		
		public DlStroke(BasicStroke stroke)
		{
			this.stroke = stroke;
		}

		@Override
		public DlType getType()
		{
			return type;
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
		public final float opacity;
		public final DlType type = DlType.OPACITY;
		
		public DlOpacity(float opacity)
		{
			this.opacity = opacity;
		}
		
		@Override
		public DlType getType()
		{
			return type;
		}

		@Override
		public String toString() 
		{
			return String.format(Locale.US, "{%f}", opacity);
		}
	}
	
	public static class DlCMYKColor implements DlItem
	{
		public final float c, m, y, k;
		public final DlType type = DlType.CMYKCOLOR;

		public DlCMYKColor(float c, float m, float y, float k)
		{
			this.c = c;
			this.m = m;
			this.y = y;
			this.k = k;
		}

		@Override
		public DlType getType()
		{
			return type;
		}

		@Override
		public String toString()
		{
			return String.format(Locale.US, "{%f %f %f %f}", c, m, y, k);
		}
	}
	
	
	public static class DlRGBColor implements DlItem
	{
		public final int r, g, b, a;
		public final DlType type = DlType.RGBCOLOR;

		public DlRGBColor(int r, int g, int b, int a)
		{
			this.r = r;
			this.g = g;
			this.b = b;
			this.a = a;
		}

		@Override
		public DlType getType()
		{
			return type;
		}

		@Override
		public String toString()
		{
			return String.format(Locale.US, "{%d %d %d %d}", r, g, b, a);
		}
	}
	
	public static class DlSetClip implements DlItem
	{
		public final Shape clip;
		public final DlType type = DlType.SET_CLIP;
		
		public DlSetClip(Shape clip) 
		{
			this.clip = clip;
		}
		
		@Override
		public DlType getType()
		{
			return type;
		}

		@Override
		public String toString()
		{
			// TODO: More sensible string format.
			return String.format(Locale.US, "{%s}", clip);
		}
	}
	
	public static class DlDrawShape implements DlItem
	{
		public final Shape shape;
		public final DlType type = DlType.DRAW_SHAPE;
		public final Operation op;
		
		public DlDrawShape(Shape shape, Operation op) 
		{
			this.shape = shape;
			this.op = op;
		}
		
		@Override
		public DlType getType()
		{
			return type;
		}

		@Override
		public String toString()
		{
			// TODO: More sensible string format.
			return String.format(Locale.US, "{%s}", shape.toString());
		}
	}
	
	public static class DlClip implements DlItem
	{
		public final Shape clip;
		public final DlType type = DlType.CLIP;
		
		public DlClip(Shape clip) 
		{
			this.clip = clip;
		}
		
		@Override
		public DlType getType()
		{
			return type;
		}

		@Override
		public String toString()
		{
			// TODO: More sensible string format.
			return String.format(Locale.US, "{%s}", clip.toString());
		}
	}
	
	public static class DlImage implements DlItem
	{
		public final FSImage image;
		public final int x, y;
		public final DlType type = DlType.IMAGE;
		
		public DlImage(FSImage image, int x, int y)
		{
			this.image = image;
			this.x = x;
			this.y = y;
		}
		
		@Override
		public DlType getType()
		{
			return type;
		}

		@Override
		public String toString() 
		{
			return String.format(Locale.US, "{%s %d %d}", image, x, y);
		}
	}
	
	public static class DlFont implements DlItem
	{
		public final FSFont font;
		public final DlType type = DlType.FONT;
		
		public DlFont(FSFont font)
		{
			this.font = font;
		}
		
		@Override
		public DlType getType()
		{
			return type;
		}

		@Override
		public String toString() 
		{
			return String.format(Locale.US, "{%s}", font);
		}
	}
	
	public static class DlOval implements DlItem
	{
		public final DlType type = DlType.OVAL;
		public final int x, y, width, height;
		public final Operation op;
		
		public DlOval(int x, int y, int width, int height, Operation op)
		{
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
			this.op = op;
		}
		
		@Override
		public DlType getType()
		{
			return type;
		}

		@Override
		public String toString() 
		{
			return String.format(Locale.US, "{%d %d %d %d}", x, y, width, height);
		}
	}
	
	public static class DlRectangle implements DlItem
	{
		public final int x, y, width, height;
		public final DlType type = DlType.RECTANGLE;
		public final Operation op;
		
		public DlRectangle(int x, int y, int width, int height, Operation op)
		{
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
			this.op = op;
		}
		
		@Override
		public DlType getType()
		{
			return type;
		}

		@Override
		public String toString() 
		{
			return String.format(Locale.US, "{%d %d %d %d}", x, y, width, height);
		}
	}
}
