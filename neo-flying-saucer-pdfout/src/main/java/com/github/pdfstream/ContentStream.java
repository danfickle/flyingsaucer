package com.github.pdfstream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ContentStream 
{
    protected ByteArrayOutputStream buf;
    
    // Graphics state stack.
    private List<State> savedStates = new ArrayList<State>();
    
    // Graphics state variables.
    private PdfColor pen = PdfGreyScaleColor.BLACK;
    private PdfColor brush = PdfGreyScaleColor.WHITE;
    private float penWidth = -1.0f;
    private int lineCapStyle = 0;
    private int lineJoinStyle = 0;
    private String linePattern = "[] 0";
    private float miterLimit = 10;
    private Font font;
    protected int renderingMode = 0;
    protected float[] tm = new float[] {1f, 0f, 0f, 1f};
    final PDF pdf;
    
    private boolean isPathOpen = false;
    
    public ContentStream(PDF pdf)
    {
    	this.pdf = pdf;
    	buf = new ByteArrayOutputStream(8192);
    }
    
    public byte[] getContent()
    {
        return buf.toByteArray();
    }
    
    public void pathOpen()
    {
    	isPathOpen = true;
    }

    public void pathMoveTo(float x, float y)
    {
        append(x);
        append(' ');
        append(y);
        append(" m\n");
    }
    
    public void pathCloseSubpath()
    {
    	append('h');
    	append('\n');
    	isPathOpen = false;
    }
    
    public void pathCurveTo(float x1, float y1, float x2, float y2, float x3, float y3)
    {
    	append(x1);
    	append(' ');
    	append(y1);
    	append(' ');
    	append(x2);
    	append(' ');
    	append(y2);
    	append(' ');
    	append(x3);
    	append(' ');
    	append(y3);
    	append(' ');
    	append('c');
    	append('\n');
    }
    
    public void pathCurveTo(float x1, float y1, float x3, float y3)
    {
    	append(x1);
    	append(' ');
    	append(y1);
    	append(' ');
    	append(x3);
    	append(' ');
    	append(y3);
    	append(' ');
    	append('y');
    	append('\n');
    }    

    public void pathLineTo(float x, float y)
    {
    	append(x);
    	append(' ');
    	append(y);
    	append(' ');
    	append('l');
    	append('\n');
    }
    
    /**
     * Sets the color for stroking operations.
     * The pen color is used when drawing lines and splines.
     */
    public void setPenColor(PdfColor color) 
    {
    	if (pen.equals(color))
    		return;
    	
    	color.setStrokeColorOnPage(this);
    	color.setAlphaColorOnPage(this);
    }
    
    /**
     * Sets the color for brush operations.
     * This is the color used when drawing regular text and filling shapes.
     */
    public void setBrushColor(PdfColor color) 
    {
    	if (brush.equals(color))
    		return;
    	
    	color.setNonStrokeColorOnPage(this);
    	color.setAlphaColorOnPage(this);
    }
    
    /**
     *  Sets the line width to the default.
     *  The default is the finest line width.
     */
    public void setDefaultLineWidth() 
    {
        if (penWidth != 0f) {
            penWidth = 0f;
            append(penWidth);
            append(" w\n");
        }
    }
    
    /**
     *  The line dash pattern controls the pattern of dashes and gaps used to stroke paths.
     *  It is specified by a dash array and a dash phase.
     *  The elements of the dash array are positive numbers that specify the lengths of
     *  alternating dashes and gaps.
     *  The dash phase specifies the distance into the dash pattern at which to start the dash.
     *  The elements of both the dash array and the dash phase are expressed in user space units.
     *  <pre>
     *  Examples of line dash patterns:
     *
     *      "[Array] Phase"     Appearance          Description
     *      _______________     _________________   ____________________________________
     *
     *      "[] 0"              -----------------   Solid line
     *      "[3] 0"             ---   ---   ---     3 units on, 3 units off, ...
     *      "[2] 1"             -  --  --  --  --   1 on, 2 off, 2 on, 2 off, ...
     *      "[2 1] 0"           -- -- -- -- -- --   2 on, 1 off, 2 on, 1 off, ...
     *      "[3 5] 6"             ---     ---       2 off, 3 on, 5 off, 3 on, 5 off, ...
     *      "[2 3] 11"          -   --   --   --    1 on, 3 off, 2 on, 3 off, 2 on, ...
     *  </pre>
     *
     *  @param pattern the line dash pattern.
     */
    public void setLinePattern(String pattern) {
        if (!pattern.equals(linePattern)) {
            linePattern = pattern;
            append(linePattern);
            append(" d\n");
        }
    }
    
    /**
     *  Sets the default line dash pattern - solid line.
     */
    public void setDefaultLinePattern()
    {
        append("[] 0");
        append(" d\n");
    }
    
    /**
     *  Sets the pen width that will be used to draw lines and splines on this page.
     *
     *  @param width the pen width.
     */
    public void setPenWidth(float width) {
        if (penWidth != width) {
            penWidth = width;
            append(penWidth);
            append(" w\n");
        }
    }
    
    /**
     *  Sets the current line cap style.
     *
     *  @param style the cap style of the current line. Supported values: Cap.BUTT, Cap.ROUND and Cap.PROJECTING_SQUARE
     */
    public void setLineCapStyle(int style) {
        if (lineCapStyle != style) {
            lineCapStyle = style;
            append(lineCapStyle);
            append(" J\n");
        }
    }
    
    /**
     *  Sets the line join style.
     *
     *  @param style the line join style code. Supported values: Join.MITER, Join.ROUND and Join.BEVEL
     */
    public void setLineJoinStyle(int style) {
        if (lineJoinStyle != style) {
            lineJoinStyle = style;
            append(lineJoinStyle);
            append(" j\n");
        }
    }
    
    /**
     *  Sets the text rendering mode.
     *
     *  @param mode the rendering mode.
     */
    public void setTextRenderingMode(int mode)
    {
        if (mode >= 0 && mode <= 7) {
            this.renderingMode = mode;
            
            if (renderingMode != 0) {
                append(renderingMode);
                append(" Tr\n");
            }
        }
        else {
            throw new RuntimeException("Invalid text rendering mode: " + mode);
        }
    }
    
    /**
     *  Sets the text direction.
     *
     *  @param degrees the angle.
     */
    public void setTextDirection(int degrees)
    {
        if (degrees > 360) degrees %= 360;
        if (degrees == 0) {
            tm = new float[] { 1f,  0f,  0f,  1f};
        }
        else if (degrees == 90) {
            tm = new float[] { 0f,  1f, -1f,  0f};
        }
        else if (degrees == 180) {
            tm = new float[] {-1f,  0f,  0f, -1f};
        }
        else if (degrees == 270) {
            tm = new float[] { 0f, -1f,  1f,  0f};
        }
        else if (degrees == 360) {
            tm = new float[] { 1f,  0f,  0f,  1f};
        }
        else {
            float sinOfAngle = (float) Math.sin(degrees * (Math.PI / 180));
            float cosOfAngle = (float) Math.cos(degrees * (Math.PI / 180));
            tm = new float[] {cosOfAngle, sinOfAngle, -sinOfAngle, cosOfAngle};
        }
    }
    
    public void save() {
        append("q\n");
        savedStates.add(new State(
                pen, brush, penWidth, lineCapStyle, lineJoinStyle, linePattern, miterLimit));
    }


    public void restore() {
        append("Q\n");
        if (savedStates.size() > 0) {
            State savedState = savedStates.remove(savedStates.size() - 1);
            pen = savedState.getPen();
            brush = savedState.getBrush();
            penWidth = savedState.getPenWidth();
            lineCapStyle = savedState.getLineCapStyle();
            lineJoinStyle = savedState.getLineJoinStyle();
            linePattern = savedState.getLinePattern();
            miterLimit = savedState.getMiterLimit();
        }
    }
    
    protected void append(int num) 
    {
        append(Integer.toString(num));
    }


    protected void append(float val)
    {
        append(PDF.df.format(val));
    }


    protected void append(char ch) 
    {
        buf.write((byte) ch);
    }


    protected void append(byte b) 
    {
        buf.write(b);
    }

    /**
     *  Appends the specified array of bytes to the page.
     */
    protected void append(byte[] buffer) {
        try {
			buf.write(buffer);
		} catch (IOException e) {
			throw new PdfException(e);
		}
    }

    /**
     * Scale the coordinate space by sx and sy.
     */
    public void scale(float sx, float sy)
    {
    	// PDF Out: sx 0 0 sy 0 0 cm 
    	append(sx);
    	append(' ');
    	append(0);
    	append(' ');
    	append(0);
    	append(' ');
    	append(sy);
    	append(' ');
    	append(0);
    	append(' ');
    	append(0);
    	append(' ');
    	append("cm\n");
    }
    
    /**
     * Translate the coordinate space by tx and ty.
     */
    public void translate(float tx, float ty)
    {
    	// PDF Out: 1 0 0 1 tx ty cm
    	append(1);
    	append(' ');
    	append(0);
    	append(' ');
    	append(0);
    	append(' ');
    	append(1);
    	append(' ');
    	append(tx);
    	append(' ');
    	append(ty);
    	append(' ');
    	append("cm\n");
    }
    
	public void pathFillEvenOdd() 
	{
		if (isPathOpen)
			pathCloseSubpath();
		
		append('f');
		append('*');
		append('\n');
	}


	public void pathFillNonZero() 
	{
		if (isPathOpen)
			pathCloseSubpath();
		
		append('f');
		append('\n');
	}
	
	public void pathStroke()
	{
		/* Don't explicity close path as it seems to cause the dash 
		 * array to be ignored (at least with my viewer, could be a bug in viewer?). */
		if (isPathOpen)
			isPathOpen = false;

		append('S');
		append('\n');
	}

	public void pathClipEvenOdd() 
	{
		if (isPathOpen)
			pathCloseSubpath();
		
		append('W');
		append('*');
		append('\n');
		append('n');
		append('\n');
	}


	public void pathClipNonZero()
	{
		if (isPathOpen)
			pathCloseSubpath();
		
		append('W');
		append('\n');
		append('n');
		append('\n');
	}


	public void setMiterLimit(float miterLimit) 
	{
		if (this.miterLimit != miterLimit)
		{
			this.miterLimit = miterLimit;

			append(PDF.formatFloat(miterLimit));
			append(' ');
			append('M');
			append('\n');
		}
	}

	public void beginText() 
	{
		append("BT\n");
	}
	
	public void endText()
	{
		append("ET\n");
	}


	public void setTextMatrix(float a, float b, float c, float d, float e, float f) 
	{
        append(a);
        append(' ');
        append(b);
        append(' ');
        append(c);
        append(' ');
        append(d);
        append(' ');
        append(e);
        append(' ');
        append(f);
        append(" Tm\n");
	}


	public void showText(String s)
	{
        append("[ (");
        drawString(font, s);
        append(") ] TJ\n");
	}
	
	public void addImage(String imageName, float a, float b, float c, float d, float e, float f) 
	{
		// Save state.
		append("q\n");

		// Transformation matrix.
		append(a);
        append(' ');
        append(b);
        append(' ');
        append(c);
        append(' ');
        append(d);
        append(' ');
        append(e);
        append(' ');
        append(f);
        append(" cm\n");
		
        // Show image.
        append('/');
        append(imageName);
        append(" Do\n");

        // Restore state.
        append("Q\n");
	}

	/**
	 * Opacity must be between 0 and 1.
	 * Used for images, etc. If you need a color set as well
	 * be sure to use setPenColor or setBrushColor instead.
	 */
	public void setExtGState(String extg)
	{
		append('/');
		append(extg);
		append(" gs\n");
	}
	
	public void drawLinearGradient(LinearGradient ln) 
	{
			append("q\n");
			
			if (ln.isSpecial)
			{
				append("/" + ln.gname);
				append(" gs\n");
			}
			
			append("/Pattern cs\n");
			append("/" + ln.name + " scn\n"); // Choose our pattern.

			append(ln.x);
			append(' ');
			append(ln.y);
			append(' ');
			append(ln.w);
			append(' ');
			append(ln.h);

			append(" re\nf\n");
			append("Q\n");
	}
	
	public void showText(char[] cc, float[] justification) 
	{
        append("[ ");

        if (font.isComposite)
        {
            for (int i = 0; i < cc.length; i++) {
            	append(justification[i]);
            	append(' ');
            	append('(');
            	drawTwoByteChar(cc[i], font);
            	append(')');
            	append(' ');
            }
        }
        else
        {
            for (int i = 0; i < cc.length; i++) {
            	append(justification[i]);
            	append(' ');
            	append('(');
            	drawOneByteChar(cc[i], font);
            	append(')');
            	append(' ');
            }
        }
        
        append(" ] TJ\n");
	}
	
    protected void append(String str)
    {
        int len = str.length();
        for (int i = 0; i < len; i++) {
            buf.write((byte) str.charAt(i));
        }
    }
    
    /**
     *  Sets the text leading.
     *  Please see Example_32.
     *
     *  @param leading the leading.
     */
    public void setTextLeading(float leading) throws IOException {
        append(leading);
        append(" TL\n");
    }


    public void setCharSpacing(float spacing) throws IOException {
        append(spacing);
        append(" Tc\n");
    }


    public void setWordSpacing(float spacing) throws IOException {
        append(spacing);
        append(" Tw\n");
    }


    public void setTextScaling(float scaling) {
        append(scaling);
        append(" Tz\n");
    }


    public void setTextRise(float rise) throws IOException {
        append(rise);
        append(" Ts\n");
    }


    public void setTextFont(Font font, float size) {
        this.font = font;
        append("/F");
        append(font.objNumber);
        append(' ');
        append(size);
        append(" Tf\n");
    }
    
    public void drawString(Font font, String str)
    {
        int len = str.length();
        for (int i = 0; i < len; i++) {
            int c1 = str.charAt(i);
            if (font.isComposite) {
                drawTwoByteChar(c1, font);
            }
            else {
                drawOneByteChar(c1, font, str, i);
            }
        }
    }


    private void drawTwoByteChar(int c1, Font font) {
        if (c1 < font.firstChar || c1 > font.lastChar) {
            if (font.isCJK) {
                append((byte) 0x0000);
                append((byte) 0x0020);
            }
            else {
                append((byte) font.unicodeToGID[0x0000]);
                append((byte) font.unicodeToGID[0x0020]);
            }
        }
        else {
            byte hi;
            byte lo;
            if (font.isCJK) {
                hi = (byte) (c1 >> 8);
                lo = (byte) (c1);
            }
            else {
                int gid = font.unicodeToGID[c1];
                hi = (byte) (gid >> 8);
                lo = (byte) (gid);
            }
            if (hi == '(' || hi == ')' || hi == '\\') {
                append((byte) '\\');
            }
            append(hi);
    
            if (lo == '\r') {
                append("\\015");
            }
            else {
                if (lo == '(' || lo == ')' || lo == '\\') {
                    append((byte) '\\');
                }
                append(lo);
            }
        }
    }

    private void drawOneByteChar(int c1, Font font) 
    {
        if (c1 < font.firstChar || c1 > font.lastChar) {
            c1 = font.mapUnicodeChar(c1);
        }

        if (c1 == '(' || c1 == ')' || c1 == '\\') {
            append((byte) '\\');
        }

        append((byte) c1);
    }
    
    private void drawOneByteChar(int c1, Font font, String str, int i) {
        if (c1 < font.firstChar || c1 > font.lastChar) {
            c1 = font.mapUnicodeChar(c1);
        }
        if (c1 == '(' || c1 == ')' || c1 == '\\') {
            append((byte) '\\');
        }
        append((byte) c1);

        if (font.isStandard && font.kernPairs && i < (str.length() - 1)) {
            c1 -= 32;
            int c2 = str.charAt(i + 1);
            if (c2 < font.firstChar || c2 > font.lastChar) {
                c2 = 32;
            }
            for (int j = 2; j < font.metrics[c1].length; j += 2) {
                if (font.metrics[c1][j] == c2) {
                    append(") ");
                    append(-font.metrics[c1][j + 1]);
                    append(" (");
                    break;
                }
            }
        }
    }
    
    /**
     *  Draws the text given by the specified string,
     *  using the specified main font and the current brush color.
     *  If the main font is missing some glyphs - the fallback font is used.
     *  The baseline of the leftmost character is at position (x, y) on the page.
     *
     *  @param font1 the main font.
     *  @param font2 the fallback font.
     *  @param str the string to be drawn.
     *  @param x the x coordinate.
     *  @param y the y coordinate.
     */
    public void drawString(
            Font font1,
            Font font2,
            String str,
            float x,
            float y) throws IOException {
        boolean usingFont1 = true;
        StringBuilder buf = new StringBuilder();
        int len = str.length();
        for (int i = 0; i < len; i++) {
            int ch = str.charAt(i);
            if ((font1.isCJK && ch >= 0x4E00 && ch <= 0x9FCC)
                    || (!font1.isCJK && font1.unicodeToGID[ch] != 0)) {
                if (!usingFont1) {
                    String s1 = buf.toString();
                    drawString(font2, s1, x, y);
                    x += font2.stringWidth(s1);
                    buf.setLength(0);
                    usingFont1 = true;
                }
            }
            else {
                if (usingFont1) {
                    String s1 = buf.toString();
                    drawString(font1, s1, x, y);
                    x += font1.stringWidth(s1);
                    buf.setLength(0);
                    usingFont1 = false;
                }
            }
            buf.append((char) ch);
        }

        if (usingFont1) {
            drawString(font1, buf.toString(), x, y);
        }
        else {
            drawString(font2, buf.toString(), x, y);
        }
    }


    /**
     *  Draws the text given by the specified string,
     *  using the specified font and the current brush color.
     *  The baseline of the leftmost character is at position (x, y) on the page.
     *
     *  @param font the font to use.
     *  @param str the string to be drawn.
     *  @param x the x coordinate.
     *  @param y the y coordinate.
     */
    public void drawString(
            Font font,
            String str,
            double x,
            double y) throws IOException {
        drawString(font, str, (float) x, (float) y);
    }
}
