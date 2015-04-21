package com.github.neoflyingsaucer.pdf2dout;

import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.pattern.PDPatternResources;
import org.apache.pdfbox.pdmodel.graphics.pattern.PDShadingPatternResources;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDJpeg;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObject;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;

import com.github.neoflyingsaucer.pdf2dout.Pdf2Out.PdfOutMode;

public class Pdf2PdfBoxWrapper 
{
	public static void pdfCloseSubpath(PDPageContentStream strm)
	{
		try {
			strm.closeSubPath();
		} catch (IOException e) {
			throw new PdfException(e);
		}
	}

	public static void pdfCurveTo(float x1, float y1, float x2, float y2, float x3, float y3, PDPageContentStream strm)
	{
		try {
			strm.addBezier312(x1, y1, x2, y2, x3, y3);
		} catch (IOException e) {
			throw new PdfException(e);
		}
	}

	public static void pdfCurveTo(float x1, float y1, float x3, float y3, PDPageContentStream strm)
	{
		try {
			strm.addBezier31(x1, y1, x3, y3);
		} catch (IOException e) {
			throw new PdfException(e);
		}
	}
	
	public static void pdfCloseContent(PDPageContentStream strm)
	{
		try {
			strm.close();
		} catch (IOException e) {
			throw new PdfException(e);
		}
	}
	
	public static void pdfSavePdf(PDDocument doc, OutputStream os)
	{
		try {
			doc.save(os);
		} catch (COSVisitorException e) {
			throw new PdfException(e);
		} catch (IOException e) {
			throw new PdfException(e);
		}
	}

	public static PDPageContentStream pdfCreateContentStream(PDDocument pdf, PDPage page, PdfOutMode mode)
	{
		try {
			return new PDPageContentStream(pdf, page, true, mode == PdfOutMode.PRODUCTION_MODE, true);
		} catch (IOException e) {
			throw new PdfException(e);
		}
	}
	
	public static void pdfAddAnnotation(PDPage page, PDAnnotationLink link)
	{
		try {
			page.getAnnotations().add(link);
		} catch (IOException e) {
			throw new PdfException(e);
		}
	}

	public static void pdfLineTo(float x1, float y1, PDPageContentStream strm)
	{
		try {
			strm.lineTo(x1, y1);
		} catch (IOException e) {
			throw new PdfException(e);
		}
	}
	
	public static void pdfMoveTo(float x1, float y1, PDPageContentStream strm)
	{
		try {
			strm.moveTo(x1, y1);
		} catch (IOException e) {
			throw new PdfException(e);
		}
	}
	
	public static void pdfFillEvenOdd(PDPageContentStream strm)
	{
		try {
			strm.fill(PathIterator.WIND_EVEN_ODD);
		} catch (IOException e) {
			throw new PdfException(e);
		}
	}

	public static void pdfFillNonZero(PDPageContentStream strm)
	{
		try {
			strm.fill(PathIterator.WIND_NON_ZERO);
		} catch (IOException e) {
			throw new PdfException(e);
		}
	}

	public static void pdfStroke(PDPageContentStream strm)
	{
		try {
			strm.stroke();
		} catch (IOException e) {
			throw new PdfException(e);
		}
	}

	public static void pdfClipNonZero(PDPageContentStream strm)
	{
		try {
			strm.clipPath(PathIterator.WIND_NON_ZERO);
		} catch (IOException e) {
			throw new PdfException(e);
		}
	}

	public static void pdfClipEvenOdd(PDPageContentStream strm)
	{
		try {
			strm.clipPath(PathIterator.WIND_EVEN_ODD);
		} catch (IOException e) {
			throw new PdfException(e);
		}
	}
	
	public static void pdfSetStrokingColor(int r, int g, int b, PDPageContentStream strm)
	{
		try {
			strm.setStrokingColor(r, g, b);
		} catch (IOException e) {
			throw new PdfException(e);
		}
	}
	
	public static void pdfSetStrokingColor(float c, float m, float y, float k, PDPageContentStream strm)
	{
		try {
			strm.setStrokingColor(c, m, y, k);
		} catch (IOException e) {
			throw new PdfException(e);
		}
	}
	
	public static void pdfAppendRawCommand(String command, PDPageContentStream strm)
	{
		try {
			strm.appendRawCommands(command);
		} catch (IOException e) {
			throw new PdfException(e);
		}
	}
	
	public static void pdfSetFillColor(int r, int g, int b, PDPageContentStream strm)
	{
		try {
			strm.setNonStrokingColor(r, g, b);
		} catch (IOException e) {
			throw new PdfException(e);
		}
	}
	
	public static void pdfSetFillColor(float c, float m, float y, float k, PDPageContentStream strm)
	{
		try {
			strm.setNonStrokingColor(c, m, y, k);
		} catch (IOException e) {
			throw new PdfException(e);
		}
	}
	
	public static void pdfSetLineWidth(float width, PDPageContentStream strm)
	{
		try {
			strm.setLineWidth(width);
		} catch (IOException e) {
			throw new PdfException(e);
		}
	}
	
	public static void pdfSetLineCap(int capStyle, PDPageContentStream strm)
	{
		try {
			strm.setLineCapStyle(capStyle);
		} catch (IOException e) {
			throw new PdfException(e);
		}
	}
	
	public static void pdfSetLineJoin(int joinStyle, PDPageContentStream strm)
	{
		try {
			strm.setLineJoinStyle(joinStyle);
		} catch (IOException e) {
			throw new PdfException(e);
		}
	}
	
	public static void pdfSetLineDash(float[] dash, float phase, PDPageContentStream strm)
	{
		try {
			strm.setLineDashPattern(dash, phase);
		} catch (IOException e) {
			throw new PdfException(e);
		}
	}
	
	public static void pdfRestoreGraphics(PDPageContentStream strm)
	{
		try {
			strm.restoreGraphicsState();
		} catch (IOException e) {
			throw new PdfException(e);
		}
	}

	public static void pdfSaveGraphics(PDPageContentStream strm)
	{
		try {
			strm.saveGraphicsState();
		} catch (IOException e) {
			throw new PdfException(e);
		}
	}
	
	public static PDRectangle pdfGetFontBoundingBox(PDFont font)
	{
		try {
			return font.getFontBoundingBox();
		} catch (IOException e) {
			throw new PdfException(e);
		}
	}
	
	public static float pdfGetStringWidth(PDFont font, String s)
	{
		try {
			return font.getStringWidth(s);
		} catch (IOException e) {
			throw new PdfException(e);
		}
	}
	
	public static void pdfBeginText(PDPageContentStream strm)
	{
		try {
			strm.beginText();
		} catch (IOException e) {
			throw new PdfException(e);
		}
	}
	
	public static void pdfEndText(PDPageContentStream strm)
	{
		try {
			strm.endText();
		} catch (IOException e) {
			throw new PdfException(e);
		}
	}

	public static void pdfSetFont(PDFont font, float size, PDPageContentStream strm)
	{
		try {
			strm.setFont(font, size);
		} catch (IOException e) {
			throw new PdfException(e);
		}
	}
	
	public static void pdfSetTextMatrix(float a, float b, float c, float d, float e, float f, PDPageContentStream strm)
	{
		try {
			strm.setTextMatrix(a, b, c, d, e, f);
		} catch (IOException e1) {
			throw new PdfException(e1);
		}
	}
	
	public static void pdfDrawString(String s, PDPageContentStream strm)
	{
		try {
			strm.drawString(s);
		} catch (IOException e) {
			throw new PdfException(e);
		}
	}
	
	public static void pdfCloseDocument(PDDocument doc)
	{
		try {
			doc.close();
		} catch (IOException e) {
			throw new PdfException(e);
		}
	}
	
	public static void pdfDrawXObject(PDXObject xobject, AffineTransform transform, PDPageContentStream strm)
	{
		try {
			strm.drawXObject(xobject, transform);
		} catch (IOException e) {
			throw new PdfException(e);
		}
	}
	
	public static PDJpeg pdfCreateJpeg(PDDocument doc, InputStream is)
	{
		try {
			return new PDJpeg(doc, is);
		} catch (IOException e) {
			throw new PdfException(e);
		}
	}
	
	public static void pdfFillRect(float x, float y, float w, float h, PDPageContentStream strm)
	{
		try {
			strm.fillRect(x, y, w, h);
		} catch (IOException e) {
			throw new PdfException(e);
		}
	}

	public static Map<String, PDPatternResources> pdfGetPatterns(PDResources res)
	{
		try {
			return res.getPatterns();
		} catch (IOException e) {
			throw new PdfException(e);
		}
	}
	
	public static PDPatternResources pdfCreatePatterns(COSDictionary dict)
	{
		return new PDShadingPatternResources(dict);
	}
}
