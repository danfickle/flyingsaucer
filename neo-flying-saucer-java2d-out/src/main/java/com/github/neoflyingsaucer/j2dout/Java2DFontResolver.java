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
package com.github.neoflyingsaucer.j2dout;

import com.github.neoflyingsaucer.extend.controller.cancel.FSCancelController;
import com.github.neoflyingsaucer.extend.controller.error.FSErrorController;
import com.github.neoflyingsaucer.extend.controller.error.FSError.FSErrorLevel;
import com.github.neoflyingsaucer.extend.controller.error.LangId;
import com.github.neoflyingsaucer.extend.output.FSFont;
import com.github.neoflyingsaucer.extend.output.FSFontFaceItem;
import com.github.neoflyingsaucer.extend.output.FontResolver;
import com.github.neoflyingsaucer.extend.output.FontSpecificationI;
import com.github.neoflyingsaucer.extend.output.FontSpecificationI.FontStyle;
import com.github.neoflyingsaucer.extend.output.FontSpecificationI.FontVariant;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Java2DFontResolver implements FontResolver
{
    private final HashMap<String, Font> instanceStore = new HashMap<String, Font>();
    private final HashMap<String, List<FontDescription>> availableFontStore = new HashMap<String, List<FontDescription>>();;
    private float fontScale = 1f;
    
    public Java2DFontResolver() 
    {
        init();
    }
    
    private void init()
    {
        GraphicsEnvironment gfx = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] availableFonts = gfx.getAvailableFontFamilyNames();

        // preload the font map with the font names as keys
        // don't add the actual font objects because that would be a waste of memory
        // we will only add them once we need to use them
        for (String availableFont : availableFonts)
        {
            availableFontStore.put(availableFont, new ArrayList<FontDescription>(0));
        }
        
        FontDescription serif = new FontDescription(400, FontStyle.NORMAL, new Font("Serif", Font.PLAIN, 1));
        List<FontDescription> serifList = new ArrayList<FontDescription>(1);
        serifList.add(serif);

        FontDescription sansSerif = new FontDescription(400, FontStyle.NORMAL, new Font("SansSerif", Font.PLAIN, 1));
        List<FontDescription> sansSerifList = new ArrayList<FontDescription>(1); 
        sansSerifList.add(sansSerif);
        
        FontDescription monospace = new FontDescription(400, FontStyle.NORMAL, new Font("Monospaced", Font.PLAIN, 1));
        List<FontDescription> monospaceList = new ArrayList<FontDescription>(1);        
        monospaceList.add(monospace);
        
        // preload sans, serif, and monospace into the available font hash
        availableFontStore.put("Serif", serifList);
        availableFontStore.put("SansSerif", sansSerifList);
        availableFontStore.put("Monospaced", monospaceList);
    }
    
    public void flushCache() 
    {
        init();
    }

    public FSFont resolveFont(String[] families, float size, int weight, FontStyle style, FontVariant variant) 
    {
        // for each font family
        if (families != null) {
            for (String family : families)
            {
                Font font = resolveFont(family, size, weight, style, variant);
                if (font != null) 
                {
                    return new Java2DFont(font);
                }
                
                FSCancelController.cancelOpportunity(Java2DFontResolver.class);
            }
        }

        // if we get here then no font worked, so just return default sans
        String family = "SansSerif";
        if (style == FontStyle.ITALIC)
        {
            family = "Serif";
        }

        Font fnt = createFont(availableFontStore.get(family).get(0), size, weight, style, variant);
        instanceStore.put(getFontInstanceHashName(family, size, weight, style, variant), fnt);
        return new Java2DFont(fnt);
    }

    protected Font createFont(FontDescription baseFont, float size, int weight, FontStyle style, FontVariant variant) 
    {
    	int fontConst = Font.PLAIN;

    	if (weight >= 600) 
            fontConst = fontConst | Font.BOLD;
    	
    	Font created = null;
    	
    	if (baseFont.getStyle() == style &&
    		baseFont.getWeight() == weight)
    	{
    		// Just adjust size.
    		created = baseFont.getFont().deriveFont(size * fontScale);
    	}
    	else if (baseFont.getStyle() == style)
    	{
    		// Just adjust size and weight.
    		created = baseFont.getFont().deriveFont(baseFont.getFont().getStyle() | fontConst, size * fontScale);
    	}
    	else
    	{
            if (style != null && (style == FontStyle.ITALIC || style == FontStyle.OBLIQUE)) 
            {
                fontConst = fontConst | Font.ITALIC;
            }

            // Adjust size, weight and italic.
    		created = baseFont.getFont().deriveFont(baseFont.getFont().getStyle() | fontConst, size * fontScale);
    	}
    	
        if (variant != null &&
        	variant == FontVariant.SMALL_CAPS) 
        {
        	created = created.deriveFont((float) (((float) created.getSize()) * 0.6));
        }

        return created;
    }

    protected Font resolveFont(String font, final float size, final int weight, final FontStyle style, final FontVariant variant)
    {
        // strip off the "s if they are there
        if (font.startsWith("\"")) {
            font = font.substring(1);
        }
        if (font.endsWith("\"")) {
            font = font.substring(0, font.length() - 1);
        }

        // normalize the font name
        if (font.equals("serif")) {
            font = "Serif";
        }
        if (font.equals("sans-serif")) {
            font = "SansSerif";
        }
        if (font.equals("monospace")) {
            font = "Monospaced";
        }

        if (font.equals("Serif") && style == FontStyle.OBLIQUE)
        	font = "SansSerif";
        else if (font.equals("SansSerif") && style == FontStyle.ITALIC)
        	font = "Serif";

        // assemble a font instance hash name
        final String fontInstanceName = getFontInstanceHashName(font, size, weight, style, variant);
        //Uu.p("looking for font: " + font_instance_name);
        // check if the font instance exists in the hash table
        if (instanceStore.containsKey(fontInstanceName)) {
            // if so then return it
            return instanceStore.get(fontInstanceName);
        }

        FontDescription baseFont = null;
        
        // if not then
        //  does the font exist
        if (availableFontStore.containsKey(font)) 
        {
            List<FontDescription> description = availableFontStore.get(font);
            
            // First match on style and weight.
           	for (FontDescription item : description)
           	{
           		if (item.getStyle() == style &&
           			item.getWeight() == weight)
           		{
           			baseFont = item;
           		}
           		FSCancelController.cancelOpportunity(Java2DFontResolver.class);
           	}
            
           	// Next match on style alone.
           	if (baseFont == null)
           	{
           	   	for (FontDescription item : description)
           	   	{
           	   		if (item.getStyle() == style)
           	   		{
           	   			baseFont = item;
           	   		}
           	   		FSCancelController.cancelOpportunity(Java2DFontResolver.class);
           	   	}
           	}

           	// Finally, create the font if we have to.
           	if (baseFont == null)
           	{
           	  	Font rootFont = new Font(font, Font.PLAIN, 1);
            	baseFont = new FontDescription(400, FontStyle.NORMAL, rootFont);
            	description.add(baseFont);
            }
 
            // now that we have a root font, we need to create the correct version of it
            Font fnt = createFont(baseFont, size, weight, style, variant);

            // add the font to the hash so we don't have to do this again
            instanceStore.put(fontInstanceName, fnt);
            return fnt;
        }

        // we didn't find any possible matching font, so just return null
        return null;
    }

    protected String getFontInstanceHashName(final String name, final float size, final int weight, final FontStyle style, final FontVariant variant) 
    {
    	 return name + "-" + (size * fontScale) + "-" + weight + "-" + style + "-" + variant;
    }

    public void setFontScale(float scale)
    {
    	fontScale = scale;
    }
    
    public float getFontScale()
    {
    	return fontScale;
    }
    
    @Override
    public FSFont resolveFont(FontSpecificationI spec) 
    {
        return resolveFont(spec.getFamilies(), spec.getSize(), spec.getFontWeight(), spec.getStyle(), spec.getVariant());
    }

    private static class FontDescription
    {
    	private final int weight;
    	private final FontStyle style;
    	private final Font f;
    	
    	FontDescription(int weight, FontStyle style, Font f)
    	{
    		this.weight = weight;
    		this.style = style;
    		this.f = f;
    	}

    	private int getWeight()
    	{
    		return weight;
    	}

    	private FontStyle getStyle()
    	{
    		return style;
    	}
    	
    	private Font getFont()
    	{
    		return f;
    	}
    }
    
	@Override
	public void importFontFaceItems(List<FSFontFaceItem> fontFaces)
	{
		for (FSFontFaceItem item : fontFaces)
		{
			Font font = null;
			try {
				font = Font.createFont(Font.TRUETYPE_FONT, new ByteArrayInputStream(item.getFontBytes()));
			} catch (IOException e) {
				FSErrorController.log(Java2DFontResolver.class, FSErrorLevel.ERROR, LangId.COULDNT_LOAD_FONT, item.getFontFamily());
				continue;
			} catch (FontFormatException e) {
				FSErrorController.log(Java2DFontResolver.class, FSErrorLevel.ERROR, LangId.COULDNT_LOAD_FONT, item.getFontFamily());
				continue;
			}

			List<FontDescription> fontFamily = availableFontStore.get(item.getFontFamily());
			
			if (fontFamily == null)
			{
				fontFamily = new ArrayList<FontDescription>();
				availableFontStore.put(item.getFontFamily(), fontFamily);
			}
			
			FontDescription description = new FontDescription(item.getWeight(),
				item.getSpecification() == null ? FontStyle.NORMAL : item.getSpecification().getStyle(), font);

			fontFamily.add(description);
			
			FSCancelController.cancelOpportunity(Java2DFontResolver.class);
		}
	}
}
