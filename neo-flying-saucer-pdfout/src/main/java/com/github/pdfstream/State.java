/**
 *  State.java
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


class State 
{
    private final PdfColor pen;
    private final PdfColor brush;
    private final float penWidth;
    private final int lineCapStyle;
    private final int lineJoinStyle;
    private final String linePattern;
    private final float miterLimit;

    public State(
            PdfColor pen,
            PdfColor brush,
            float penWidth,
            int lineCapStyle,
            int lineJoinStyle,
            String linePattern,
            float miterLimit) 
    {
        this.pen = pen;
        this.brush = brush;
        this.penWidth = penWidth;
        this.lineCapStyle = lineCapStyle;
        this.lineJoinStyle = lineJoinStyle;
        this.linePattern = linePattern;
        this.miterLimit = miterLimit;
    }


    public PdfColor getPen() {
        return pen;
    }


    public PdfColor getBrush() {
        return brush;
    }


    public float getPenWidth() {
        return penWidth;
    }


    public int getLineCapStyle() {
        return lineCapStyle;
    }


    public int getLineJoinStyle() {
        return lineJoinStyle;
    }


    public String getLinePattern() {
        return linePattern;
    }
    
    public float getMiterLimit()
    {
    	return miterLimit;
    }
}   // End of State.java
