/**
 *  Page.java
 *
Copyright (c) 2014, Innovatics Inc.
All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice,
      this list of conditions and the following disclaimer.

    * Redistributions in binary form must reproduce the above copyright notice,
      this list of conditions and the following disclaimer in the documentation
      and / or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package com.github.pdfstream;

import java.util.*;

/**
 *  Used to create PDF page objects.
 *
 *  Please note:
 *  <pre>
 *  The coordinate (0f, 0f) is the top left corner of the page.
 *  The size of the pages are represented in points.
 *  1 point is 1/72 inches.
 *  </pre>
 *
 */
public class Page {

    protected PDF pdf;

    protected final float width;
    protected final float height;

    protected final List<Integer> contents;
    protected final List<Annotation> annots;
    protected final List<PdfFormElement> fields;

    protected float[] cropBox = null;
    protected float[] bleedBox = null;
    protected float[] trimBox = null;
    protected float[] artBox = null;

    protected final ContentStream cos;

    public Page(float[] pageSize) 
    {
        this(null, pageSize);
    }
    
    public ContentStream getContentStream()
    {
    	return cos;
    }

    /**
     *  Creates page object and add it to the PDF document.
     *
     *  Please note:
     *  <pre>
     *  The coordinate (0f, 0f) is the top left corner of the page.
     *  The size of the pages are represented in points.
     *  1 point is 1/72 inches.
     *  </pre>
     *
     *  @param pdf the pdf object.
     *  @param pageSize the page size of this page.
     */
    public Page(PDF pdf, float[] pageSize) 
    {
        this.pdf = pdf;

        cos = new ContentStream(pdf);
        contents = new ArrayList<Integer>();
        annots = new ArrayList<Annotation>();
        fields = new ArrayList<PdfFormElement>();
        
        width = pageSize[0];
        height = pageSize[1];

        if (pdf != null) {
            pdf.addPage(this);
        }
    }

    public void pathOpen()
    {
    	cos.pathOpen();
    }
    
    public void pathMoveTo(float x, float y)
    {
    	cos.pathMoveTo(x, y);
    }

    public void pathCloseSubpath()
    {
    	cos.pathCloseSubpath();
    }
    
    public void pathCurveTo(float x1, float y1, float x2, float y2, float x3, float y3)
    {
    	cos.pathCurveTo(x1, y1, x2, y2, x3, y3);
    }
    
    public void pathCurveTo(float x1, float y1, float x3, float y3)
    {
    	cos.pathCurveTo(x1, y1, x3, y3);
    }

    public void pathLineTo(float x, float y)
    {
    	cos.pathLineTo(x, y);
    }
    
    /**
     *  Returns the width of this page.
     *
     *  @return the width of the page.
     */
    public float getWidth() {
        return width;
    }

    /**
     *  Returns the height of this page.
     *
     *  @return the height of the page.
     */
    public float getHeight() {
        return height;
    }

    /**
     * Sets the color for stroking operations.
     * The pen color is used when drawing lines and splines.
     */
    public void setPenColor(PdfColor color) 
    {
    	cos.setPenColor(color);
    }

    /**
     * Sets the color for brush operations.
     * This is the color used when drawing regular text and filling shapes.
     */
    public void setBrushColor(PdfColor color) 
    {
    	cos.setBrushColor(color);
    }

    /**
     *  Sets the line width to the default.
     *  The default is the finest line width.
     */
    public void setDefaultLineWidth()
    {
    	cos.setDefaultLineWidth();
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
    public void setLinePattern(String pattern) 
    {
    	cos.setLinePattern(pattern);
    }


    /**
     *  Sets the default line dash pattern - solid line.
     */
    public void setDefaultLinePattern() 
    {
    	cos.setDefaultLinePattern();
    }


    /**
     *  Sets the pen width that will be used to draw lines and splines on this page.
     *
     *  @param width the pen width.
     */
    public void setPenWidth(double width)
    {
        setPenWidth((float) width);
    }


    /**
     *  Sets the pen width that will be used to draw lines and splines on this page.
     *
     *  @param width the pen width.
     */
    public void setPenWidth(float width) 
    {
    	cos.setPenWidth(width);
    }


    /**
     *  Sets the current line cap style.
     *
     *  @param style the cap style of the current line. Supported values: Cap.BUTT, Cap.ROUND and Cap.PROJECTING_SQUARE
     */
    public void setLineCapStyle(int style) 
    {
    	cos.setLineCapStyle(style);
    }


    /**
     *  Sets the line join style.
     *
     *  @param style the line join style code. Supported values: Join.MITER, Join.ROUND and Join.BEVEL
     */
    public void setLineJoinStyle(int style) 
    {
    	cos.setLineJoinStyle(style);
    }

    /**
     *  Sets the text rendering mode.
     *
     *  @param mode the rendering mode.
     */
    public void setTextRenderingMode(int mode)
    {
    	cos.setTextRenderingMode(mode);
    }

    public void save()
    {
    	cos.save();
    }

    public void restore()
    {
    	cos.restore();
    }

    /**
     * Sets the page CropBox.
     * See page 77 of the PDF32000_2008.pdf specification.
     *
     * @param upperLeftX the top left X coordinate of the CropBox.
     * @param upperLeftY the top left Y coordinate of the CropBox.
     * @param lowerRightX the bottom right X coordinate of the CropBox.
     * @param lowerRightY the bottom right Y coordinate of the CropBox.
     */
    public void setCropBox(
            float upperLeftX, float upperLeftY, float lowerRightX, float lowerRightY) {
        this.cropBox = new float[] {upperLeftX, upperLeftY, lowerRightX, lowerRightY};
    }


    /**
     * Sets the page BleedBox.
     * See page 77 of the PDF32000_2008.pdf specification.
     *
     * @param upperLeftX the top left X coordinate of the BleedBox.
     * @param upperLeftY the top left Y coordinate of the BleedBox.
     * @param lowerRightX the bottom right X coordinate of the BleedBox.
     * @param lowerRightY the bottom right Y coordinate of the BleedBox.
     */
    public void setBleedBox(
            float upperLeftX, float upperLeftY, float lowerRightX, float lowerRightY) {
        this.bleedBox = new float[] {upperLeftX, upperLeftY, lowerRightX, lowerRightY};
    }


    /**
     * Sets the page TrimBox.
     * See page 77 of the PDF32000_2008.pdf specification.
     *
     * @param upperLeftX the top left X coordinate of the TrimBox.
     * @param upperLeftY the top left Y coordinate of the TrimBox.
     * @param lowerRightX the bottom right X coordinate of the TrimBox.
     * @param lowerRightY the bottom right Y coordinate of the TrimBox.
     */
    public void setTrimBox(
            float upperLeftX, float upperLeftY, float lowerRightX, float lowerRightY) {
        this.trimBox = new float[] {upperLeftX, upperLeftY, lowerRightX, lowerRightY};
    }


    /**
     * Sets the page ArtBox.
     * See page 77 of the PDF32000_2008.pdf specification.
     *
     * @param upperLeftX the top left X coordinate of the ArtBox.
     * @param upperLeftY the top left Y coordinate of the ArtBox.
     * @param lowerRightX the bottom right X coordinate of the ArtBox.
     * @param lowerRightY the bottom right Y coordinate of the ArtBox.
     */
    public void setArtBox(
            float upperLeftX, float upperLeftY, float lowerRightX, float lowerRightY) {
        this.artBox = new float[] {upperLeftX, upperLeftY, lowerRightX, lowerRightY};
    }

	public void pathFillEvenOdd() 
	{
		cos.pathFillEvenOdd();
	}

	public void pathFillNonZero() 
	{
		cos.pathFillNonZero();
	}
	
	public void pathStroke()
	{
		cos.pathStroke();
	}

	public void pathClipEvenOdd() 
	{
		cos.pathClipEvenOdd();
	}


	public void pathClipNonZero()
	{
		cos.pathClipNonZero();
	}

	public void setMiterLimit(float miterLimit) 
	{
		cos.setMiterLimit(miterLimit);
	}

	public void beginText() 
	{
		cos.beginText();
	}
	
	public void endText()
	{
		cos.endText();
	}

	public void setTextMatrix(float a, float b, float c, float d, float e, float f) 
	{
		cos.setTextMatrix(a, b, c, d, e, f);
	}

	public void showText(String s)
	{
		cos.showText(s);
	}

	public PDF getPdf() 
	{
		return pdf;
	}

	public void showText(char[] cc, float[] justification) 
	{
		cos.showText(cc, justification);
	}

	public void addImage(JPGImage img, float a, float b, float c, float d, float e, float f) 
	{
		String imageName = pdf.getOrRegisterImage(img);
		cos.addImage(imageName, a, b, c, d, e, f);
	}


	public void addImage(PNGImage png, float a, float b, float c, float d,
			float e, float f) 
	{
		String imageName = pdf.getOrRegisterImage(png);
		cos.addImage(imageName, a, b, c, d, e, f);
	}
	
	/**
	 * Opacity must be between 0 and 1.
	 * Used for images, etc. If you need a color set as well
	 * be sure to use setPenColor or setBrushColor instead.
	 */
	public void setOpacity(float opacity)
	{
		String extg = pdf.getExtGStateForAlpha(opacity);
		cos.setExtGState(extg);
	}

	public void addAnnotation(Annotation annot) 
	{
		annots.add(annot);
	}

	public void addFormField(PdfFormElement field) 
	{
		fields.add(field);
	}

	public void drawLinearGradient(LinearGradient ln) 
	{
		cos.drawLinearGradient(ln);
	}

	public void setTextFont(Font font, float fontSize) 
	{
		cos.setTextFont(font, fontSize);
	}
}   // End of Page.java
