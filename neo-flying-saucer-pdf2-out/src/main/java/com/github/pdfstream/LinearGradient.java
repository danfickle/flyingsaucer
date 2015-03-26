package com.github.pdfstream;

import com.github.neoflyingsaucer.displaylist.DlInstruction.DlLinearGradient;
import com.github.neoflyingsaucer.displaylist.DlInstruction.DlStopPoint;
import com.github.pdfstream.PDF.PColorSpace;

public class LinearGradient 
{
	final DlLinearGradient gradient;
	final float dotsPerPoint;
	final float x, y, w, h;
	final PDF pdf;
	final String name;
	final String gname;

	int shadingObjNumber;
	int gstateObjNumber;
	boolean isSpecial;
	
	public LinearGradient(PDF pdf, DlLinearGradient grad, float dotsPerPoint,
			float x, float y, float w, float h,
			int nameNumber) 
	{
		this.gradient = grad;
		this.dotsPerPoint = dotsPerPoint;
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
		this.pdf = pdf;
		this.name = "LGradient" + nameNumber;
		this.gname = "LGGS" + nameNumber;
		
   		for (DlStopPoint sv : gradient.stopPoints)
   		{
   			if (sv.rgb.a != 255)
   			{
   				isSpecial = true;
   				return;
   			}
   		}
   		
   		isSpecial = false;
	}
	
    void addShader()
    {
    	if (isSpecial)
    		addSpecialShader();
    		
    	pdf.newobj();
   		shadingObjNumber = pdf.objNumber;

   		pdf.append("<<\n");
    	pdf.append("/ShadingType 2\n");
    	pdf.append("/Extend [true true]\n");
    	pdf.append("/ColorSpace /DeviceRGB\n");
    		
    	pdf.append("/Coords [");
    	pdf.append((gradient.x1 + x * dotsPerPoint)  / dotsPerPoint);
    	pdf.append(' ');
    	pdf.append((gradient.y2 + y * dotsPerPoint) / dotsPerPoint);
    	pdf.append(' ');
    	pdf.append((gradient.x2 + x * dotsPerPoint) / dotsPerPoint);
    	pdf.append(' ');
    	pdf.append((gradient.y1 + y * dotsPerPoint) / dotsPerPoint);
    	pdf.append("]\n");

    	// The stitcher function immediately follows this object.
    	pdf.append("/Function " + (pdf.objNumber + 1) + " 0 R\n");
    	pdf.append(">>\n");
    	pdf.endobj();

    	addStitcherFunction(PColorSpace.RGB);
    	addLowerLevelFunctions(PColorSpace.RGB);
    }
	
    protected void addSpecialShader()
    {
    	pdf.newobj();
    	pdf.append("<<\n");
    	pdf.append("/BBox [");
    	pdf.append(0);
    	pdf.append(' ');
    	pdf.append(0);
    	pdf.append(' ');
    	pdf.append((x + w));
    	pdf.append(' ');
    	pdf.append((y + h));
    	pdf.append("]\n");
    	pdf.append("/FormType 1\n");
    	pdf.append("/Group <<\n");
    	pdf.append("/CS /DeviceGray\n");
    	pdf.append("/S /Transparency\n");
    	pdf.append("/Type /Group\n");
    	pdf.append(">>\n"); // End group.
    	pdf.append("/Resources <<\n");

    	pdf.append("/Pattern << /Pat" + pdf.objNumber + " <<\n");
    	pdf.append("/Type /Pattern\n");
    	pdf.append("/PatternType 2\n");

    	pdf.append("/Shading <<\n");
    	pdf.append("/ColorSpace /DeviceGray\n");
    	pdf.append("/Extend [true true]\n");
    	pdf.append("/ShadingType 2\n");

    	// The stitcher function for smask immediately follows these objects.
    	pdf.append("/Function " + (pdf.objNumber + 3));
    	pdf.append(" 0 R\n/Coords [");
    	pdf.append((gradient.x1 + x)  / dotsPerPoint);
    	pdf.append(' ');
    	pdf.append((gradient.y2 + y) / dotsPerPoint);
    	pdf.append(' ');
    	pdf.append((gradient.x2 + x) / dotsPerPoint);
    	pdf.append(' ');
    	pdf.append((gradient.y1 + y) / dotsPerPoint);
    	pdf.append("]\n");
    	pdf.append(">>\n"); // End shading.
    	
    	pdf.append(">>\n"); // End specific pattern.
    	pdf.append(">>\n"); // End pattern.
    	pdf.append(">>\n"); // End resources.

    	pdf.append("/Subtype /Form\n");
    	pdf.append("/Type /XObject\n");

    	StringBuilder sb = new StringBuilder();
    	sb.append("q\n/Pattern cs /Pat" + pdf.objNumber);
    	sb.append(" scn ");
    	sb.append(PDF.formatFloat(x));
    	sb.append(' ');
    	sb.append(PDF.formatFloat(y));
    	sb.append(' ');
    	sb.append(PDF.formatFloat(w));
    	sb.append(' ');
    	sb.append(PDF.formatFloat(h));
    	sb.append(" re f\nQ");

    	pdf.append("/Length " + sb.length());
    	pdf.append("\n>>\n");
    	
    	pdf.append("stream\n");
    	pdf.append(sb.toString());
    	pdf.append("\nendstream\n");

    	int xobjNum = pdf.objNumber;
    	pdf.endobj();
    	
    	pdf.newobj();
    	pdf.append("<<\n");
    	pdf.append("/G ");
    	pdf.append(xobjNum);
    	pdf.append(" 0 R\n");
    	pdf.append("/S /Luminosity\n");
    	pdf.append("/Type /Mask\n");
    	int maskObjNum = pdf.objNumber;
    	pdf.append(">>\n");
    	pdf.endobj();

    	pdf.newobj();
    	pdf.append("<<\n");
    	pdf.append("/AIS false\n");
    	pdf.append("/SMask ");
    	pdf.append(maskObjNum);
    	pdf.append(" 0 R\n");
    	pdf.append("/Type /ExtGState\n");
    	pdf.append(">>\n"); // End ExtGState
    	int gstateObjNum = pdf.objNumber;
    	pdf.endobj();
    	
    	addStitcherFunction(PColorSpace.OPACITY);
    	addLowerLevelFunctions(PColorSpace.OPACITY);

    	this.gstateObjNumber = gstateObjNum;
    	pdf.hasAdditionalGStates = true;
    }
    
    protected void addLowerLevelFunctions(PColorSpace colorSpace)
    {
		for (int i = 0; i < gradient.stopPoints.size() - 1; i++)
		{
			pdf.newobj();

			DlStopPoint sv = gradient.stopPoints.get(i);
			DlStopPoint nxt = gradient.stopPoints.get(i + 1);

			pdf.append("<<\n");
			pdf.append("/FunctionType 2\n");
			pdf.append("/Domain [0 1]\n");
			pdf.append("/N 1\n");

			if (colorSpace == PColorSpace.RGB)
			{
				// Color at start of function domain.
				pdf.append("/C0 [");
				pdf.append(sv.rgb.r / 255f);
				pdf.append(' ');
				pdf.append(sv.rgb.g / 255f);
				pdf.append(' ');
				pdf.append(sv.rgb.b / 255f);
				pdf.append("]\n");
			
				// Color at end of function domain.
				pdf.append("/C1 [");
				pdf.append(nxt.rgb.r / 255f);
				pdf.append(' ');
				pdf.append(nxt.rgb.g / 255f);
				pdf.append(' ');
				pdf.append(nxt.rgb.b / 255f);
				pdf.append("]\n");
			
			}
			else if (colorSpace == PColorSpace.OPACITY)
			{
				pdf.append("/C0 [");
				pdf.append(sv.rgb.a / 255f);
				pdf.append("]\n");
				
				pdf.append("/C1 [");
				pdf.append(nxt.rgb.a / 255f);
				pdf.append("]\n");
			}
			else
			{
				// TODO
				
			}
			
			pdf.append(">>\n");
			pdf.endobj();
		}
    }
    
    protected void addStitcherFunction(PColorSpace colorSpace)
    {
  		pdf.newobj();

		float lastStopPosition = gradient.stopPoints.get(gradient.stopPoints.size() - 1).dots;
  		
  		// The lower level functions immediately follow the stitcher function.
		int firstFunctionObj = pdf.objNumber + 1;

		pdf.append("<<\n");
		pdf.append("/FunctionType 3\n");
		pdf.append("/Domain [0 1]\n");
		
		pdf.append("/Bounds [");
		for (int i = 1; i < gradient.stopPoints.size() - 1;i++)
		{
			pdf.append(gradient.stopPoints.get(i).dots / lastStopPosition);
			pdf.append(' ');
		}
		pdf.append("]\n");

		pdf.append("/Encode [");
		for (int i = 0; i < gradient.stopPoints.size() - 1;i++)
		{
			pdf.append("0 1 ");
		}
		pdf.append("]\n");
		
		pdf.append("/Functions [\n");    		
		for (int i = 0; i < gradient.stopPoints.size() - 1; i++)
		{
			pdf.append(firstFunctionObj + i);
			pdf.append(" 0 R\n");
		}
		pdf.append("]\n");

		pdf.append(">>\n"); // End function dictionary.
		pdf.endobj();
    }
}
