package org.xhtmlrenderer.css.mediaquery;

import java.util.HashMap;
import java.util.Map;

import org.xhtmlrenderer.css.constants.CSSPrimitiveUnit;
import org.xhtmlrenderer.css.parser.PropertyValue;
import org.xhtmlrenderer.css.parser.PropertyValueImp.CSSValueType;
import org.xhtmlrenderer.layout.SharedContext;

public enum MediaFeatureName
{
	COLOR("color") {
		@Override
		public boolean eval(SharedContext ctx, PropertyValue _cssValue) 
		{
			// Indicates the number of bits per color component
			// of the output device.
			// We assume we only support color and 32bit colors.
			// TODO: We don't yet support alpha for PDF output.
			if (_cssValue == null)
				return true;
			else
				return evalNumber(_cssValue, 8f, '=');
		}
	},
	COLOR_INDEX("color-index") {
		@Override
		public boolean eval(SharedContext ctx, PropertyValue _cssValue) 
		{
			// We don't support indexed colors, so only color-index: 0 should
			// return true.
			if (_cssValue == null)
				return false;
			else
				return evalNumber(_cssValue, 0f, '=');
		}
	},
	GRID("grid") {
		@Override
		public boolean eval(SharedContext ctx, PropertyValue _cssValue) 
		{
			// We are a bitmap device rather than a grid device, so only
			// grid: 0 should return true.
			if (_cssValue == null)
				return false;
			else
				return evalNumber(_cssValue, 0f, '=');
		}
	},
	MONOCHROME("monochrome") {
		@Override
		public boolean eval(SharedContext ctx, PropertyValue _cssValue)
		{
			// We don't support monochrome, so only monochrome: 0 should
			// return true.
			if (_cssValue == null)
				return false;
			else 
				return evalNumber(_cssValue, 0f, '=');
		}
	},
	HEIGHT("height") {
		@Override
		public boolean eval(SharedContext ctx, PropertyValue _cssValue)
		{
			// TODO Auto-generated method stub
			return false;
		}
	},
	HOVER("hover") {
		@Override
		public boolean eval(SharedContext ctx, PropertyValue _cssValue) {
			// TODO Auto-generated method stub
			return false;
		}
	},
	WIDTH("width") {
		@Override
		public boolean eval(SharedContext ctx, PropertyValue _cssValue) {
			// TODO Auto-generated method stub
			return false;
		}
	},
	ORIENTATION("orientation") {
		@Override
		public boolean eval(SharedContext ctx, PropertyValue _cssValue) {
			// TODO Auto-generated method stub
			return false;
		}
	},
	ASPECT_RATIO("aspect-ratio") {
		@Override
		public boolean eval(SharedContext ctx, PropertyValue _cssValue) {
			// TODO Auto-generated method stub
			return false;
		}
	},
	DEVICE_ASPECT_RATIO("device-aspect-ratio") {
		@Override
		public boolean eval(SharedContext ctx, PropertyValue _cssValue) {
			// TODO Auto-generated method stub
			return false;
		}
	},
	DEVICE_HEIGHT("device-height") {
		@Override
		public boolean eval(SharedContext ctx, PropertyValue _cssValue) {
			// TODO Auto-generated method stub
			return false;
		}
	},
	DEVICE_WIDTH("device-width") {
		@Override
		public boolean eval(SharedContext ctx, PropertyValue _cssValue) {
			// TODO Auto-generated method stub
			return false;
		}
	},
	MAX_COLOR("max-color") {
		@Override
		public boolean eval(SharedContext ctx, PropertyValue _cssValue) 
		{
			if (_cssValue == null)
				return true;
			else 
				return evalNumber(_cssValue, 8f, '<');
		}
	},
	MAX_COLOR_INDEX("max-color-index") {
		@Override
		public boolean eval(SharedContext ctx, PropertyValue _cssValue) 
		{
			if (_cssValue == null)
				return false;
			else
				return evalNumber(_cssValue, 0f, '<');
		}
	},
	MAX_ASPECT_RATIO("max-aspect-ratio") {
		@Override
		public boolean eval(SharedContext ctx, PropertyValue _cssValue) {
			// TODO Auto-generated method stub
			return false;
		}
	},
	MAX_DEVICE_ASPECT_RATIO("max-device-aspect-ratio") {
		@Override
		public boolean eval(SharedContext ctx, PropertyValue _cssValue) {
			// TODO Auto-generated method stub
			return false;
		}
	},
	MAX_DEVICE_HEIGHT("max-device-height") {
		@Override
		public boolean eval(SharedContext ctx, PropertyValue _cssValue) {
			// TODO Auto-generated method stub
			return false;
		}
	},
	MAX_DEVICE_WIDTH("max-device-width") {
		@Override
		public boolean eval(SharedContext ctx, PropertyValue _cssValue) {
			// TODO Auto-generated method stub
			return false;
		}
	},
	MAX_HEIGHT("max-height") {
		@Override
		public boolean eval(SharedContext ctx, PropertyValue _cssValue) {
			// TODO Auto-generated method stub
			return false;
		}
	},
	MAX_MONOCHROME("max-monochrome") {
		@Override
		public boolean eval(SharedContext ctx, PropertyValue _cssValue) 
		{
			if (_cssValue == null)
				return false;
			else
				return evalNumber(_cssValue, 0f, '<');
		}
	},
	MAX_WIDTH("max-width") {
		@Override
		public boolean eval(SharedContext ctx, PropertyValue _cssValue) {
			// TODO Auto-generated method stub
			return false;
		}
	},
	MAX_RESOLUTION("max-resolution") {
		@Override
		public boolean eval(SharedContext ctx, PropertyValue _cssValue) {
			// TODO Auto-generated method stub
			return false;
		}
	},
	MIN_COLOR("min-color") {
		@Override
		public boolean eval(SharedContext ctx, PropertyValue _cssValue) 
		{
			if (_cssValue == null)
				return true;
			else
				return evalNumber(_cssValue, 8f, '>');
		}
	},
	MIN_COLOR_INDEX("min-color-index") {
		@Override
		public boolean eval(SharedContext ctx, PropertyValue _cssValue) 
		{
			if (_cssValue == null)
				return false;
			else
				return evalNumber(_cssValue, 0f, '>');
		}
	},
	MIN_ASPECT_RATIO("min-aspect-ratio") {
		@Override
		public boolean eval(SharedContext ctx, PropertyValue _cssValue) {
			// TODO Auto-generated method stub
			return false;
		}
	},
	MIN_DEVICE_ASPECT_RATIO("min-device-aspect-ratio") {
		@Override
		public boolean eval(SharedContext ctx, PropertyValue _cssValue) {
			// TODO Auto-generated method stub
			return false;
		}
	},
	MIN_DEVICE_HEIGHT("min-device-height") {
		@Override
		public boolean eval(SharedContext ctx, PropertyValue _cssValue) {
			// TODO Auto-generated method stub
			return false;
		}
	},
	MIN_DEVICE_WIDTH("min-device-width") {
		@Override
		public boolean eval(SharedContext ctx, PropertyValue _cssValue) {
			// TODO Auto-generated method stub
			return false;
		}
	},
	MIN_HEIGHT("min-height") {
		@Override
		public boolean eval(SharedContext ctx, PropertyValue _cssValue) {
			// TODO Auto-generated method stub
			return false;
		}
	},
	MIN_MONOCHROME("min-monochrome") {
		@Override
		public boolean eval(SharedContext ctx, PropertyValue _cssValue) 
		{
			if (_cssValue == null)
				return false;
			else
				return evalNumber(_cssValue, 0f, '>');
		}
	},
	MIN_WIDTH("min-width") {
		@Override
		public boolean eval(SharedContext ctx, PropertyValue _cssValue) {
			// TODO Auto-generated method stub
			return false;
		}
	},
	MIN_RESOLUTION("min-resolution") {
		@Override
		public boolean eval(SharedContext ctx, PropertyValue _cssValue) {
			// TODO Auto-generated method stub
			return false;
		}
	},
	POINTER("pointer") {
		@Override
		public boolean eval(SharedContext ctx, PropertyValue _cssValue)
		{
			// Webkit says:
		    // If we're on a port that hasn't explicitly opted into providing pointer device information
		    // (or otherwise can't be confident in the pointer hardware available), then behave exactly
		    // as if this feature feature isn't supported.
			return false;
		}
	},
	RESOLUTION("resolution") {
		@Override
		public boolean eval(SharedContext ctx, PropertyValue _cssValue) {
			// TODO Auto-generated method stub
			return false;
		}
	},
	SCAN("scan") {
		@Override
		public boolean eval(SharedContext ctx, PropertyValue _cssValue) 
		{
			// We don't support this scan media (such as TV).
			return false;
		}
	};
	
	private final String cssName;
	
	private MediaFeatureName(final String name)
	{
		cssName = name;
	}

	@Override
	public String toString() 
	{
		return cssName;
	}

	private static final Map<String, MediaFeatureName> map = new HashMap<>(values().length);
	
	static 
	{
		for (final MediaFeatureName nm : values())
		{
			map.put(nm.cssName, nm);
		}
	}
	
	public static MediaFeatureName fsValueOf(final String mediaFeatureStr)
	{
		return map.get(mediaFeatureStr);
	}
	
	public static boolean isRatio(final String mediaFeatureString)
	{
		MediaFeatureName mediaFeature = fsValueOf(mediaFeatureString);

		return mediaFeature == MediaFeatureName.ASPECT_RATIO
                || mediaFeature == MediaFeatureName.DEVICE_ASPECT_RATIO
                || mediaFeature == MediaFeatureName.MIN_ASPECT_RATIO
                || mediaFeature == MediaFeatureName.MAX_ASPECT_RATIO
                || mediaFeature == MediaFeatureName.MIN_DEVICE_ASPECT_RATIO
                || mediaFeature == MediaFeatureName.MAX_DEVICE_ASPECT_RATIO;
	}

	private static boolean evalNumber(PropertyValue val, float number, char op)
	{
		if (op == '>') // min-prefix (greater or equal to)
		{
			return (val.getCssValueTypeN() == CSSValueType.CSS_PRIMITIVE_VALUE &&
					val.getPrimitiveTypeN() == CSSPrimitiveUnit.CSS_NUMBER &&
					val.getFloatValue() >= number);
		}
		else if (op == '<') // max-prefix (less than or equal to)
		{
			return (val.getCssValueTypeN() == CSSValueType.CSS_PRIMITIVE_VALUE &&
					val.getPrimitiveTypeN() == CSSPrimitiveUnit.CSS_NUMBER &&
					val.getFloatValue() <= number);	
		}
		else // no-prefix
		{
			assert(op == '=');
			return (val.getCssValueTypeN() == CSSValueType.CSS_PRIMITIVE_VALUE &&
					val.getPrimitiveTypeN() == CSSPrimitiveUnit.CSS_NUMBER &&
					val.getFloatValue() == number);	
			
		}
	}
	
	public abstract boolean eval(SharedContext ctx, PropertyValue _cssValue); 
}
