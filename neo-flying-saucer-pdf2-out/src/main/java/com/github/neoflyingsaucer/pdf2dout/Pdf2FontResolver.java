/*
 * {{{ header & license
 * Copyright (c) 2004, 2005 Joshua Marinacci
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package com.github.neoflyingsaucer.pdf2dout;

import com.github.neoflyingsaucer.extend.controller.cancel.FSCancelController;
import com.github.neoflyingsaucer.extend.controller.error.FSErrorController;
import com.github.neoflyingsaucer.extend.controller.error.LangId;
import com.github.neoflyingsaucer.extend.controller.error.FSError.FSErrorLevel;
import com.github.neoflyingsaucer.extend.output.FSFont;
import com.github.neoflyingsaucer.extend.output.FSFontFaceItem;
import com.github.neoflyingsaucer.extend.output.FontResolver;
import com.github.neoflyingsaucer.extend.output.FontSpecificationI;
import com.github.neoflyingsaucer.extend.output.FontSpecificationI.FontStyle;
import com.github.neoflyingsaucer.extend.output.FontSpecificationI.FontVariant;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.encoding.Encoding;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDTrueTypeFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import static com.github.neoflyingsaucer.pdf2dout.Pdf2PdfBoxWrapper.*;

public class Pdf2FontResolver implements FontResolver
{
	private Map<String, FontFamily> _fontFamilies = createInitialFontMap();
	private final PDDocument doc;
	
	public Pdf2FontResolver(PDDocument doc)
	{
		this.doc = doc;
	}
	
	@Override
	public FSFont resolveFont(FontSpecificationI spec) 
	{
		return resolveFont(spec.getFamilies(), spec.getSize(), spec.getFontWeight(), spec.getStyle(), spec.getVariant());
	}
	
    private FSFont resolveFont(final String[] families, final float size, final int weight, FontStyle style, final FontVariant variant)
    {
        final FontStyle styleN = 
    	    (! (style == FontStyle.NORMAL || style == FontStyle.OBLIQUE
             || style == FontStyle.ITALIC)) ? FontStyle.NORMAL : style;
        
        if (families != null) 
        {
        	for (String family : families)
        	{
        		Pdf2Font font = (Pdf2Font) resolveFont(family, size, weight, styleN, variant);
        		
        		if (font != null)
        			return font;
        		
        		FSCancelController.cancelOpportunity(Pdf2BookmarkManager.class);
        	}
        }

        // Default font.
        return resolveFont("Serif", size, weight, style, variant);
    }
	
	private FSFont resolveFont(String fontFamily, float size,
			int weight, FontStyle style, FontVariant variant)
	{
        String normalizedFontFamily = normalizeFontFamily(fontFamily);

        FontFamily family = _fontFamilies.get(normalizedFontFamily);

        // TODO: Case insensitive comparison.
        
        if (family != null) {
            FontDescription result = family.match(weight, style);
            if (result != null) {
                return new Pdf2Font(result, size);
            }
        }
        return null;
	}
		
    private String normalizeFontFamily(final String fontFamily) {
        String result = fontFamily;
        
        // strip off the "s if they are there
        if (result.startsWith("\"")) {
            result = result.substring(1);
        }
        if (result.endsWith("\"")) {
            result = result.substring(0, result.length() - 1);
        }

        // normalize the font name
        if (result.equalsIgnoreCase("serif")) {
            result = "Serif";
        }
        else if (result.equalsIgnoreCase("sans-serif")) {
            result = "SansSerif";
        }
        else if (result.equalsIgnoreCase("monospace")) {
            result = "Monospaced";
        }

        return result;
    }

	@Override
	public void flushCache()
	{
        _fontFamilies = createInitialFontMap();
	}

    private static Map<String, FontFamily> createInitialFontMap() 
    {
        final HashMap<String, FontFamily> result = new HashMap<String, FontFamily>();

        addCourier(result);
        addTimes(result);
        addHelvetica(result);
        addSymbol(result);
        addZapfDingbats(result);

        return result;
    }
    
    private static void addCourier(final HashMap<String, FontFamily> result) 
    {
        final FontFamily courier = new FontFamily();
        courier.setName("Courier");

        courier.addFontDescription(new FontDescription(
                createFont(PDType1Font.COURIER_BOLD_OBLIQUE), FontStyle.OBLIQUE, 700));
        courier.addFontDescription(new FontDescription(
                createFont(PDType1Font.COURIER_OBLIQUE), FontStyle.OBLIQUE, 400));
        courier.addFontDescription(new FontDescription(
                createFont(PDType1Font.COURIER_BOLD), FontStyle.NORMAL, 700));
        courier.addFontDescription(new FontDescription(
                createFont(PDType1Font.COURIER), FontStyle.NORMAL, 400));

        result.put("DialogInput", courier);
        result.put("Monospaced", courier);
        result.put("Courier", courier);
    }

    private static PDFont createFont(PDType1Font coreFont) 
    {
    	return coreFont;
    }

	private static void addTimes(final HashMap<String, FontFamily> result) 
	{
        final FontFamily times = new FontFamily();
        times.setName("Times");

        times.addFontDescription(new FontDescription(
                createFont(PDType1Font.TIMES_BOLD_ITALIC), FontStyle.ITALIC, 700));
        times.addFontDescription(new FontDescription(
                createFont(PDType1Font.TIMES_ITALIC), FontStyle.ITALIC, 400));
        times.addFontDescription(new FontDescription(
                createFont(PDType1Font.TIMES_BOLD), FontStyle.NORMAL, 700));
        times.addFontDescription(new FontDescription(
                createFont(PDType1Font.TIMES_ROMAN), FontStyle.NORMAL, 400));

        result.put("Serif", times);
        result.put("TimesRoman", times);
    }

    private static void addHelvetica(final HashMap<String, FontFamily> result)
    {
        final FontFamily helvetica = new FontFamily();
        helvetica.setName("Helvetica");

        helvetica.addFontDescription(new FontDescription(
                createFont(PDType1Font.HELVETICA_BOLD_OBLIQUE), FontStyle.OBLIQUE, 700));
        helvetica.addFontDescription(new FontDescription(
                createFont(PDType1Font.HELVETICA_OBLIQUE), FontStyle.OBLIQUE, 400));
        helvetica.addFontDescription(new FontDescription(
                createFont(PDType1Font.HELVETICA_BOLD), FontStyle.NORMAL, 700));
        helvetica.addFontDescription(new FontDescription(
                createFont(PDType1Font.HELVETICA), FontStyle.NORMAL, 400));

        result.put("Dialog", helvetica);
        result.put("SansSerif", helvetica);
        result.put("Helvetica", helvetica);
        result.put("Arial", helvetica);
    }

    private static void addSymbol(final Map<String, FontFamily> result)
    {
        final FontFamily fontFamily = new FontFamily();
        fontFamily.setName("Symbol");

        // TODO fontFamily.addFontDescription(new FontDescription(createFont(CoreFont.SYMBOL, Font.CP1252, false), IdentValue.NORMAL, 400));

        result.put("Symbol", fontFamily);
    }

    private static void addZapfDingbats(final Map<String, FontFamily> result)
    {
        final FontFamily fontFamily = new FontFamily();
        fontFamily.setName("ZapfDingbats");

        // TODO fontFamily.addFontDescription(new FontDescription(createFont(CoreFont.ZAPF_DINGBATS, BaseFont.CP1252, false), IdentValue.NORMAL, 400));

        result.put("ZapfDingbats", fontFamily);
    }
    
    
    public static class FontDescription {
        private FontStyle _style;
        private int _weight;

        private PDFont _font;

        private float _underlinePosition;
        private float _underlineThickness;

        private float _yStrikeoutSize;
        private float _yStrikeoutPosition;

        private boolean _isFromFontFace;

        public FontDescription() {
        }

        public FontDescription(PDFont font) {
            this(font, FontStyle.NORMAL, 400);
        }

        public FontDescription(PDFont font, FontStyle style, int weight) {
            _font = font;
            _style = style;
            _weight = weight;
            setMetricDefaults();
        }

        public PDFont getFont() {
            return _font;
        }

        public void setFont(PDFont font) {
            _font = font;
        }

        public int getWeight() {
            return _weight;
        }

        public void setWeight(int weight) {
            _weight = weight;
        }

        public FontStyle getStyle() {
            return _style;
        }

        public void setStyle(FontStyle style) {
            _style = style;
        }

        public float getUnderlinePosition() {
            return _underlinePosition;
        }

        /**
         * This refers to the top of the underline stroke
         */
        public void setUnderlinePosition(final float underlinePosition) {
            _underlinePosition = underlinePosition;
        }

        public float getUnderlineThickness() {
            return _underlineThickness;
        }

        public void setUnderlineThickness(final float underlineThickness) {
            _underlineThickness = underlineThickness;
        }

        public float getYStrikeoutPosition() {
            return _yStrikeoutPosition;
        }

        public void setYStrikeoutPosition(final float strikeoutPosition) {
            _yStrikeoutPosition = strikeoutPosition;
        }

        public float getYStrikeoutSize() {
            return _yStrikeoutSize;
        }

        public void setYStrikeoutSize(final float strikeoutSize) {
            _yStrikeoutSize = strikeoutSize;
        }

        private void setMetricDefaults() {
            _underlinePosition = -50;
            _underlineThickness = 50;

//            final int[] box = _font.getCharBBox('x');
//            if (box != null) {
//                _yStrikeoutPosition = box[3] / 2 + 50;
//                _yStrikeoutSize = 100;
//            } else {
                // Do what the JDK does, size will be calculated by ITextTextRenderer
                _yStrikeoutPosition = (pdfGetFontBoundingBox(_font).getUpperRightY() * 1000f) / 3.0f;
//            }
        }

        public boolean isFromFontFace() {
            return _isFromFontFace;
        }

        public void setFromFontFace(final boolean isFromFontFace) {
            _isFromFontFace = isFromFontFace;
        }
    }
    
    private static class FontFamily {
        private String _name;
        private List<FontDescription> _fontDescriptions;

        public FontFamily() {
        }

        public List<FontDescription> getFontDescriptions() {
            return _fontDescriptions;
        }

        public void addFontDescription(final FontDescription descr) 
        {
            if (_fontDescriptions == null) {
                _fontDescriptions = new ArrayList<FontDescription>();
            }

            _fontDescriptions.add(descr);

            Collections.sort(_fontDescriptions,
            	new Comparator<FontDescription>() 
           		{
           			@Override
           			public int compare(FontDescription o1, FontDescription o2) {
           				return o1.getWeight() - o2.getWeight();
           			}
           		});
        }

        public String getName() {
            return _name;
        }

        public void setName(final String name) {
            _name = name;
        }

        public FontDescription match(final int desiredWeight, final FontStyle style) {
            if (_fontDescriptions == null) {
                throw new RuntimeException("fontDescriptions is null");
            }

            final List<FontDescription> candidates = new ArrayList<FontDescription>();

            for (final FontDescription description : _fontDescriptions) {
                if (description.getStyle() == style) {
                    candidates.add(description);
                }
                
                FSCancelController.cancelOpportunity(Pdf2BookmarkManager.class);
            }

            if (candidates.size() == 0) {
                if (style == FontStyle.ITALIC) {
                    return match(desiredWeight, FontStyle.OBLIQUE);
                } else if (style == FontStyle.OBLIQUE) {
                    return match(desiredWeight, FontStyle.NORMAL);
                } else {
                    candidates.addAll(_fontDescriptions);
                }
            }

            final FontDescription[] matches = candidates.toArray(new FontDescription[candidates.size()]);
            FontDescription result;

            result = findByWeight(matches, desiredWeight, SM_EXACT);

            if (result != null) {
                return result;
            } else {
                if (desiredWeight <= 500) {
                    return findByWeight(matches, desiredWeight, SM_LIGHTER_OR_DARKER);
                } else {
                    return findByWeight(matches, desiredWeight, SM_DARKER_OR_LIGHTER);
                }
            }
        }

        private static final int SM_EXACT = 1;
        private static final int SM_LIGHTER_OR_DARKER = 2;
        private static final int SM_DARKER_OR_LIGHTER = 3;

        private FontDescription findByWeight(final FontDescription[] matches,
                final int desiredWeight, final int searchMode) {
            if (searchMode == SM_EXACT) {
                for (final FontDescription descr : matches) {
                    if (descr.getWeight() == desiredWeight) {
                        return descr;
                    }
                    FSCancelController.cancelOpportunity(Pdf2BookmarkManager.class);
                }
                return null;
            } else if (searchMode == SM_LIGHTER_OR_DARKER){
                int offset = 0;
                FontDescription descr = null;
                for (offset = 0; offset < matches.length; offset++) {
                    descr = matches[offset];
                    if (descr.getWeight() > desiredWeight) {
                        break;
                    }
                    FSCancelController.cancelOpportunity(Pdf2BookmarkManager.class);
                }

                if (offset > 0 && descr.getWeight() > desiredWeight) {
                    return matches[offset-1];
                } else {
                    return descr;
                }

            } else if (searchMode == SM_DARKER_OR_LIGHTER) {
                int offset = 0;
                FontDescription descr = null;
                for (offset = matches.length - 1; offset >= 0; offset--) {
                    descr = matches[offset];
                    if (descr.getWeight() < desiredWeight) {
                        break;
                    }
                    FSCancelController.cancelOpportunity(Pdf2BookmarkManager.class);
                }

                if (offset != matches.length - 1 && descr.getWeight() < desiredWeight) {
                    return matches[offset+1];
                } else {
                    return descr;
                }
            }

            return null;
        }
    }

    @Override
	public void importFontFaceItems(List<FSFontFaceItem> fontFaces)
	{
		for (FSFontFaceItem item : fontFaces)
		{
			PDTrueTypeFont font = null;
			try {
				font = PDTrueTypeFont.loadTTF(doc, new ByteArrayInputStream(item.getFontBytes()));
			} catch (IOException e) {
				FSErrorController.log(Pdf2FontResolver.class, FSErrorLevel.ERROR, LangId.COULDNT_LOAD_FONT, item.getFontFamily());
				continue;
			}
			
			FontFamily family = _fontFamilies.get(item.getFontFamily());
			
			if (family == null)
				family = new FontFamily();
			else
				family.setName(item.getFontFamily());

			FontDescription description = new FontDescription(font);
			description.setWeight(item.getWeight());
			description.setFromFontFace(true);
			description.setStyle(item.getSpecification() == null ? FontStyle.NORMAL : item.getSpecification().getStyle());
			
			family.addFontDescription(description);
			
			_fontFamilies.put(item.getFontFamily(), family);
		}
	}
}
