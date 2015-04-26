package com.github.neoflyingsaucer.displaylist;

import java.awt.BasicStroke;
import java.awt.Shape;
import java.util.ArrayList;
import java.util.List;
import com.github.neoflyingsaucer.extend.output.DlItem;
import com.github.neoflyingsaucer.extend.output.FSFont;
import com.github.neoflyingsaucer.extend.output.FSGlyphVector;
import com.github.neoflyingsaucer.extend.output.FSImage;
import com.github.neoflyingsaucer.extend.output.JustificationInfo;
import com.github.neoflyingsaucer.extend.output.ReplacedElement;

public class DlInstruction
{
	public enum Operation
	{
		FILL,
		STROKE,
		CLIP;
	}
	
	public static class DlInternalLink implements DlItem
	{
		public final int pageNo; /* Destination page no. */
		public final float y;    /* Destination y position */
		public final float x1, y1; /* Link location */
		public final float w, h;   /* Link area */
		
		public DlInternalLink(int pageNo, float y, float x1, float y1, float w, float h)
		{
			this.pageNo = pageNo;
			this.y = y;
			this.x1 = x1;
			this.y1 = y1;
			this.w = w;
			this.h = h;
		}

		@Override
		public DlType getType()
		{
			return DlType.INTERNAL_LINK;
		}
	}
	
	public static class DlExternalLink implements DlItem
	{
		public final String uri;  /* Destination uri */
		public final float x1, y1;
		public final float w, h;
		
		public DlExternalLink(String uri, float x1, float y1, float w, float h)
		{
			this.uri = uri;
			this.x1 = x1;
			this.y1 = y1;
			this.w = w;
			this.h = h;
		}

		@Override
		public DlType getType()
		{
			return DlType.EXTERNAL_LINK;
		}
	}
	
	
	/*
	 * A PDF (or other) bookmark.
	 */
	public static class DlBookmark implements DlItem
	{
		public final int level;  /* PDF bookmarks can be nested. */
		public final String content;
		public final float y;
		public final int pageNo;
		
		public DlBookmark(int level, float y, String content, int pageNo)
		{
			this.level = level;
			this.y = y;
			this.content = content;
			this.pageNo = pageNo;
		}

		@Override
		public DlType getType() 
		{
			return DlType.BOOKMARK;
		}
	}
	
	public static class DlStopPoint
	{
		public final float dots;
		public final DlRGBColor rgb;

		public DlStopPoint(float dots, DlRGBColor rgb)
		{
			this.dots = dots;
			this.rgb = rgb;
		}
	}
	
	public static class DlLinearGradient implements DlItem
	{
		public final DlType type = DlType.LINEAR_GRADIENT;
		public final int x1, y1, x2, y2;
		public final int x, y, width, height;
		public final List<DlStopPoint> stopPoints = new ArrayList<DlStopPoint>(2);
		
		public DlLinearGradient(int x1, int y1, int x2, int y2,
				int x, int y, int width, int height)
		{
			this.x1 = x1;
			this.y1 = y1;
			this.x2 = x2;
			this.y2 = y2;
			
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
		}

		@Override
		public DlType getType() 
		{
			return type;
		}
	}
	
	public static class DlReplaced implements DlItem
	{
		public final DlType type = DlType.REPLACED;
		public final ReplacedElement replaced;
		
		public DlReplaced(ReplacedElement replaced)
		{
			this.replaced = replaced;
		}

		@Override
		public DlType getType()
		{
			return type;
		}
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
	}
}
