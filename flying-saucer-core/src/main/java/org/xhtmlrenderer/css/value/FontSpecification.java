package org.xhtmlrenderer.css.value;

import org.xhtmlrenderer.css.constants.IdentValue;

import com.github.neoflyingsaucer.extend.output.FontSpecificationI;

import java.util.Arrays;

public class FontSpecification implements FontSpecificationI
{
    public float size;
    public IdentValue fontWeight;
    public String[] families;
    public IdentValue fontStyle;
    public IdentValue variant;

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("Font specification: ");
        sb
                .append(" families: " + Arrays.asList(families).toString())
                .append(" size: " + size)
                .append(" weight: " + fontWeight)
                .append(" style: " + fontStyle)
                .append(" variant: " + variant);
        return sb.toString();
    }

	@Override
	public float getSize() 
	{
		return size;
	}

	@Override
	public int getFontWeight() 
	{
		switch (fontWeight)
		{
		case FONT_WEIGHT_100:
			return 100;
		case FONT_WEIGHT_200:
			return 200;
		case FONT_WEIGHT_300:
			return 300;
		case FONT_WEIGHT_400:
			return 400;
		case FONT_WEIGHT_500:
			return 500;
		case FONT_WEIGHT_600:
			return 600;
		case FONT_WEIGHT_700:
			return 700;
		case FONT_WEIGHT_800:
			return 800;
		case FONT_WEIGHT_900:
			return 900;
		case NORMAL:
			return 400;
		case LIGHTER:
			return 400; // TODO: Should be based on parent value.
		case BOLDER:
			return 700; // TODO
		case BOLD:
			return 700;
		default:
			return 400;
		}
	}

	@Override
	public String[] getFamilies() 
	{
		return families;
	}

	@Override
	public FontStyle getStyle() 
	{
		switch (fontStyle)
		{
		case NORMAL:
			return FontStyle.NORMAL;
		case OBLIQUE:
			return FontStyle.OBLIQUE;
		case ITALIC:
			return FontStyle.ITALIC;
		default:
			return FontStyle.NORMAL;
		}
	}

	@Override
	public FontVariant getVariant() {
		switch (variant)
		{
		case SMALL_CAPS:
			return FontVariant.SMALL_CAPS;
		default:
			return FontVariant.NORMAL;
		}
	}
}
