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


class State {

    private PdfColor pen;
    private PdfColor brush;
    private float pen_width;
    private int line_cap_style;
    private int line_join_style;
    private String linePattern;


    public State(
            PdfColor pen,
            PdfColor brush,
            float pen_width,
            int line_cap_style,
            int line_join_style,
            String linePattern) {
        this.pen = pen;
        this.brush = brush;
        this.pen_width = pen_width;
        this.line_cap_style = line_cap_style;
        this.line_join_style = line_join_style;
        this.linePattern = linePattern;
    }


    public PdfColor getPen() {
        return pen;
    }


    public PdfColor getBrush() {
        return brush;
    }


    public float getPenWidth() {
        return pen_width;
    }


    public int getLineCapStyle() {
        return line_cap_style;
    }


    public int getLineJoinStyle() {
        return line_join_style;
    }


    public String getLinePattern() {
        return linePattern;
    }

}   // End of State.java
