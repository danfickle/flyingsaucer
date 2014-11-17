package com.github.neoflyingsaucer.pdfout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.neoflyingsaucer.extend.output.FSFont;
import com.github.neoflyingsaucer.extend.output.FontResolver;
import com.github.neoflyingsaucer.extend.output.FontSpecificationI;
import com.github.neoflyingsaucer.extend.output.FontSpecificationI.FontStyle;
import com.github.neoflyingsaucer.extend.output.FontSpecificationI.FontVariant;
import com.github.pdfstream.CoreFont;
import com.github.pdfstream.Font;

public class PdfFontResolver implements FontResolver 
{
	private Map<String, FontFamily> _fontFamilies = createInitialFontMap();
	
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
        		PdfFont font = (PdfFont) resolveFont(family, size, weight, styleN, variant);
        		
        		if (font != null)
        			return font;
        	}
        }

        // Default font.
        return resolveFont("Serif", size, weight, style, variant);
    }
	
	private FSFont resolveFont(String fontFamily, float size,
			int weight, FontStyle style, FontVariant variant)
	{
        final String normalizedFontFamily = normalizeFontFamily(fontFamily);

        final FontFamily family = _fontFamilies.get(normalizedFontFamily);

        if (family != null) {
            FontDescription result = family.match(weight, style);
            if (result != null) {
                return new PdfFont(result, size);
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
                createFont(CoreFont.COURIER_BOLD_OBLIQUE), FontStyle.OBLIQUE, 700));
        courier.addFontDescription(new FontDescription(
                createFont(CoreFont.COURIER_OBLIQUE), FontStyle.OBLIQUE, 400));
        courier.addFontDescription(new FontDescription(
                createFont(CoreFont.COURIER_BOLD), FontStyle.NORMAL, 700));
        courier.addFontDescription(new FontDescription(
                createFont(CoreFont.COURIER), FontStyle.NORMAL, 400));

        result.put("DialogInput", courier);
        result.put("Monospaced", courier);
        result.put("Courier", courier);
    }

    private static Font createFont(CoreFont coreFont) 
    {
    	return new Font(coreFont);
    }

	private static void addTimes(final HashMap<String, FontFamily> result) 
	{
        final FontFamily times = new FontFamily();
        times.setName("Times");

        times.addFontDescription(new FontDescription(
                createFont(CoreFont.TIMES_BOLD_ITALIC), FontStyle.ITALIC, 700));
        times.addFontDescription(new FontDescription(
                createFont(CoreFont.TIMES_ITALIC), FontStyle.ITALIC, 400));
        times.addFontDescription(new FontDescription(
                createFont(CoreFont.TIMES_BOLD), FontStyle.NORMAL, 700));
        times.addFontDescription(new FontDescription(
                createFont(CoreFont.TIMES_ROMAN), FontStyle.NORMAL, 400));

        result.put("Serif", times);
        result.put("TimesRoman", times);
    }

    private static void addHelvetica(final HashMap<String, FontFamily> result)
    {
        final FontFamily helvetica = new FontFamily();
        helvetica.setName("Helvetica");

        helvetica.addFontDescription(new FontDescription(
                createFont(CoreFont.HELVETICA_BOLD_OBLIQUE), FontStyle.OBLIQUE, 700));
        helvetica.addFontDescription(new FontDescription(
                createFont(CoreFont.HELVETICA_OBLIQUE), FontStyle.OBLIQUE, 400));
        helvetica.addFontDescription(new FontDescription(
                createFont(CoreFont.HELVETICA_BOLD), FontStyle.NORMAL, 700));
        helvetica.addFontDescription(new FontDescription(
                createFont(CoreFont.HELVETICA), FontStyle.NORMAL, 400));

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

        private Font _font;

        private float _underlinePosition;
        private float _underlineThickness;

        private float _yStrikeoutSize;
        private float _yStrikeoutPosition;

        private boolean _isFromFontFace;

        public FontDescription() {
        }

        public FontDescription(final Font font) {
            this(font, FontStyle.NORMAL, 400);
        }

        public FontDescription(final Font font, final FontStyle style, final int weight) {
            _font = font;
            _style = style;
            _weight = weight;
            setMetricDefaults();
        }

        public Font getFont() {
            return _font;
        }

        public void setFont(final Font font) {
            _font = font;
        }

        public int getWeight() {
            return _weight;
        }

        public void setWeight(final int weight) {
            _weight = weight;
        }

        public FontStyle getStyle() {
            return _style;
        }

        public void setStyle(final FontStyle style) {
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
                _yStrikeoutPosition = (_font.getBBoxURy() * 1000f) / 3.0f;
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
}
