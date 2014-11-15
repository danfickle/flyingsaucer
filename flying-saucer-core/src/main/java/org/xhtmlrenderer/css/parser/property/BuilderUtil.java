package org.xhtmlrenderer.css.parser.property;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.constants.CSSPrimitiveUnit;
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.css.parser.CSSParseException;
import org.xhtmlrenderer.css.parser.FSFunction;
import org.xhtmlrenderer.css.parser.PropertyValue;
import org.xhtmlrenderer.css.parser.PropertyValueImp.CSSValueType;
import org.xhtmlrenderer.css.sheet.PropertyDeclaration;
import org.xhtmlrenderer.css.sheet.StylesheetInfo.CSSOrigin;
import org.xhtmlrenderer.util.LangId;

/**
 * Static utility functions to check types, etc for builders to use.
 */
public class BuilderUtil {

	private BuilderUtil() {
	}

	public static final EnumSet<CSSPrimitiveUnit> LENGTH_UNITS = EnumSet.of(
			CSSPrimitiveUnit.CSS_EMS,
			CSSPrimitiveUnit.CSS_EXS,
			CSSPrimitiveUnit.CSS_PX,
			CSSPrimitiveUnit.CSS_IN,
			CSSPrimitiveUnit.CSS_CM,
			CSSPrimitiveUnit.CSS_MM,
			CSSPrimitiveUnit.CSS_PT,
			CSSPrimitiveUnit.CSS_PC);
	
	public static enum GenericType
	{
		LengthType,
		IdentType,
		PercentageType,
		UriType,
		NumberType,
		StringType,
		ColorType,
		AngleType,
		ResolutionType,
		IntegerType;
	}
	
	public static void cssThrowError(final LangId key, final Object... args) 
	{
		throw new CSSParseException(key, -1, args);
	}

	@Deprecated
	public static void checkValueType(final CSSName cssName, final PropertyValue value, final EnumSet<CSSPrimitiveUnit> in)
	{
		if (!in.contains(value.getPrimitiveTypeN()))
			cssThrowError(LangId.UNSUPPORTED_TYPE, value.getPrimitiveTypeN(), cssName);
	}

	@Deprecated
	public static void checkValueType(final CSSName cssName, final PropertyValue value, final EnumSet<CSSPrimitiveUnit> in, final EnumSet<CSSPrimitiveUnit> in2)
	{
		if (!in.contains(value.getPrimitiveTypeN()) && !in2.contains(value.getPrimitiveTypeN()))
			cssThrowError(LangId.UNSUPPORTED_TYPE, value.getPrimitiveTypeN(), cssName);
	}
	
	public static void checkGenericType(CSSName cssName, PropertyValue val, EnumSet<GenericType> typesAllowed)
	{
		if (!checkType(val.getPrimitiveTypeN(), typesAllowed) &&
			 !(val.getPrimitiveTypeN() == CSSPrimitiveUnit.CSS_NUMBER && 
			   val.getFloatValue() == 0f && 
			   typesAllowed.contains(GenericType.LengthType))
			)
			cssThrowError(LangId.UNSUPPORTED_TYPE, val.getPrimitiveTypeN(), cssName);
	}
	
	public static List<PropertyDeclaration> sList(CSSName cssName, PropertyValue value, boolean important, CSSOrigin origin)
	{
		return Collections.singletonList(new PropertyDeclaration(cssName, value, important, origin));
	}
	
	private static boolean checkType(CSSPrimitiveUnit primitive, EnumSet<GenericType> typesAllowed)
	{
		switch (primitive)
		{
		case CSS_IDENT:
			return typesAllowed.contains(GenericType.IdentType);
		case CSS_RGBCOLOR:
			return typesAllowed.contains(GenericType.ColorType);
		case CSS_CM:
			return typesAllowed.contains(GenericType.LengthType);
		case CSS_DEG:
			return typesAllowed.contains(GenericType.AngleType);
		case CSS_EMS:
			return typesAllowed.contains(GenericType.LengthType);
		case CSS_EXS:
			return typesAllowed.contains(GenericType.LengthType);
		case CSS_GRAD:
			return typesAllowed.contains(GenericType.AngleType);
		case CSS_IN:
			return typesAllowed.contains(GenericType.LengthType);
		case CSS_KHZ:
			break;
		case CSS_MM:
			return typesAllowed.contains(GenericType.LengthType);
		case CSS_MS:
			break;
		case CSS_NUMBER:
			return typesAllowed.contains(GenericType.NumberType);
		case CSS_PAIR:
			break;
		case CSS_PC:
			return typesAllowed.contains(GenericType.LengthType);
		case CSS_PERCENTAGE:
			return typesAllowed.contains(GenericType.PercentageType);
		case CSS_PT:
			return typesAllowed.contains(GenericType.LengthType);
		case CSS_PX:
			return typesAllowed.contains(GenericType.LengthType);
		case CSS_RAD:
			return typesAllowed.contains(GenericType.AngleType);
		case CSS_REMS:
			return typesAllowed.contains(GenericType.LengthType);
		case CSS_STRING:
			return typesAllowed.contains(GenericType.StringType);
		case CSS_TURN:
			return typesAllowed.contains(GenericType.AngleType);
		case CSS_URI:
			return typesAllowed.contains(GenericType.UriType);
		case CSS_VH:
			return typesAllowed.contains(GenericType.LengthType);
		case CSS_VMAX:
			return typesAllowed.contains(GenericType.LengthType);
		case CSS_VMIN:
			return typesAllowed.contains(GenericType.LengthType);
		case CSS_VW:
			return typesAllowed.contains(GenericType.LengthType);
		case CSS_DPCM:
		case CSS_DPI:
		case CSS_DPPX:
			return typesAllowed.contains(GenericType.ResolutionType);

		case CSS_PROPERTY_ID:
			break;
		case CSS_FR:
			break;
		case CSS_COUNTER:
			break;
		case CSS_RECT:
			break;
		case CSS_HZ:
			break;
		case CSS_QUAD:
			break;
		case CSS_S:
			break;
		case CSS_SHAPE:
			break;
		case CSS_ATTR:
			break;
		case CSS_CALC:
			break;
		case CSS_CALC_PERCENTAGE_WITH_LENGTH:
			break;
		case CSS_CALC_PERCENTAGE_WITH_NUMBER:
			break;
		case CSS_CHS:
			break;
		case CSS_VALUE_ID:
			break;
		case CSS_UNKNOWN:
			break;
		case CSS_UNICODE_RANGE:
			break;
		case CSS_DIMENSION:
			break;
		}
			
		return false;	
	}
	
	
	public static PropertyValue checkGetSingleProperty(CSSName cssName, List<PropertyValue> values,
			boolean inheritAllowed, EnumSet<GenericType> typesAllowed)
	{
		checkValueCount(cssName, 1, values.size());
		PropertyValue value = values.get(0);
		checkInheritAllowed(value, inheritAllowed);

		if (value.getCssValueTypeN() != CSSValueType.CSS_INHERIT)
		{
			CSSPrimitiveUnit primitive = value.getPrimitiveTypeN();

			if (!checkType(primitive, typesAllowed) &&
				 !(primitive == CSSPrimitiveUnit.CSS_NUMBER && 
				   value.getFloatValue() == 0f && 
				   typesAllowed.contains(GenericType.LengthType)) &&
				 !(primitive == CSSPrimitiveUnit.CSS_NUMBER && 
				   value.getFloatValue() % 1f == 0f && 
				   typesAllowed.contains(GenericType.IntegerType))
				)
				cssThrowError(LangId.UNSUPPORTED_TYPE, value.getPrimitiveTypeN(), cssName);
		}

		return value;
	}
	
	public static void checkValueCount(final CSSName cssName, final int expected, final int found) {
		if (expected != found)
			cssThrowError(LangId.VALUE_COUNT_MISMATCH, found, cssName, expected);
	}

	public static void checkValueCount(final CSSName cssName, final int min, final int max, final int found) {
		if (!(found >= min && found <= max))
			cssThrowError(LangId.MIN_MAX_VALUE_COUNT_MISMATCH, found, cssName, min, max);
	}

	@Deprecated
	public static void checkIdentType(final CSSName cssName, final PropertyValue value) {
		if (value.getPrimitiveTypeN() != CSSPrimitiveUnit.CSS_IDENT) 
			cssThrowError(LangId.MUST_BE_IDENTIFIER, cssName);
	}

	@Deprecated
	public static void checkIdentOrURIType(final CSSName cssName, final PropertyValue value) {
		final CSSPrimitiveUnit type = value.getPrimitiveTypeN();

		if (type != CSSPrimitiveUnit.CSS_IDENT &&
			type != CSSPrimitiveUnit.CSS_URI)
			cssThrowError(LangId.MUST_BE_URI_OR_IDENTIFIER, cssName);
	}

	@Deprecated
	public static void checkIdentOrColorType(final CSSName cssName, final PropertyValue value) {
		final CSSPrimitiveUnit type = value.getPrimitiveTypeN();

		if (type != CSSPrimitiveUnit.CSS_IDENT
			&& type != CSSPrimitiveUnit.CSS_RGBCOLOR)
		cssThrowError(LangId.MUST_BE_COLOR_OR_IDENTIFIER, cssName);
	}

	@Deprecated
	public static void checkIdentOrIntegerType(final CSSName cssName, final PropertyValue value) {
		final CSSPrimitiveUnit type = value.getPrimitiveTypeN();
		if ((type != CSSPrimitiveUnit.CSS_IDENT && type != CSSPrimitiveUnit.CSS_NUMBER)
			|| (type == CSSPrimitiveUnit.CSS_NUMBER &&
			(int) value.getFloatValue() != 
			Math.round(value.getFloatValue()))) 
		{
			cssThrowError(LangId.MUST_BE_INT_OR_IDENTIFIER, cssName);
		}
	}

	@Deprecated
	public static void checkInteger(final CSSName cssName, final PropertyValue value) {
		final CSSPrimitiveUnit type = value.getPrimitiveTypeN();
		if (type != CSSPrimitiveUnit.CSS_NUMBER ||
		   (type == CSSPrimitiveUnit.CSS_NUMBER && 
		    value.getFloatValue() % 1f != 0f))
		{
			cssThrowError(LangId.MUST_BE_INT, cssName);
		}
	}

	@Deprecated
	public static void checkIdentOrLengthType(final CSSName cssName, final PropertyValue value) {
		final CSSPrimitiveUnit type = value.getPrimitiveTypeN();
		if (type != CSSPrimitiveUnit.CSS_IDENT && !isLength(value)) {
			cssThrowError(LangId.MUST_BE_LENGTH_OR_IDENTIFIER, cssName);
		}
	}

	@Deprecated
	public static void checkIdentOrNumberType(final CSSName cssName, final PropertyValue value) {
		final CSSPrimitiveUnit type = value.getPrimitiveTypeN();
		if (type != CSSPrimitiveUnit.CSS_IDENT
				&& type != CSSPrimitiveUnit.CSS_NUMBER) {
			cssThrowError(LangId.MUST_BE_NUMBER_OR_IDENTIFIER, cssName);
		}
	}

	@Deprecated
	public static void checkIdentLengthOrPercentType(final CSSName cssName,
			final PropertyValue value) {
		final CSSPrimitiveUnit type = value.getPrimitiveTypeN();
		if (type != CSSPrimitiveUnit.CSS_IDENT && !isLength(value)
				&& type != CSSPrimitiveUnit.CSS_PERCENTAGE) {
			cssThrowError(LangId.MUST_BE_LENGTH_PERCENT_OR_IDENTIFIER, cssName);
		}
	}

	@Deprecated
	public static void checkLengthOrPercentType(final CSSName cssName,
			final PropertyValue value) {
		final CSSPrimitiveUnit type = value.getPrimitiveTypeN();
		if (!isLength(value) && type != CSSPrimitiveUnit.CSS_PERCENTAGE) {
			cssThrowError(LangId.MUST_BE_LENGTH_OR_PERCENT, cssName);
		}
	}

	@Deprecated
	public static void checkLengthType(final CSSName cssName, final PropertyValue value) {
		if (!isLength(value)) {
			cssThrowError(LangId.MUST_BE_LENGTH, cssName);
		}
	}

	@Deprecated
	public static void checkNumberType(final CSSName cssName, final PropertyValue value) {
		if (value.getPrimitiveTypeN() != CSSPrimitiveUnit.CSS_NUMBER) {
			cssThrowError(LangId.MUST_BE_NUMBER, cssName);
		}
	}

	@Deprecated
	public static void checkStringType(final CSSName cssName, final PropertyValue value) {
		if (value.getPrimitiveTypeN() != CSSPrimitiveUnit.CSS_STRING) {
			cssThrowError(LangId.MUST_BE_STRING, cssName);
		}
	}

	@Deprecated
	public static void checkIdentOrString(final CSSName cssName, final PropertyValue value) {
		final CSSPrimitiveUnit type = value.getPrimitiveTypeN();
		if (type != CSSPrimitiveUnit.CSS_STRING
				&& type != CSSPrimitiveUnit.CSS_IDENT) {
			cssThrowError(LangId.MUST_BE_STRING_OR_IDENTIFIER, cssName);
		}
	}

	@Deprecated
	public static void checkIdentLengthNumberOrPercentType(final CSSName cssName,
			final PropertyValue value) {
		final CSSPrimitiveUnit type = value.getPrimitiveTypeN();
		if (type != CSSPrimitiveUnit.CSS_IDENT && !isLength(value)
				&& type != CSSPrimitiveUnit.CSS_PERCENTAGE
				&& type != CSSPrimitiveUnit.CSS_NUMBER) {
			cssThrowError(LangId.MUST_BE_LENGTH_NUMBER_PERCENT_OR_IDENTIFIER, cssName);
		}
	}

	public static boolean isLength(final PropertyValue value) {
		final CSSPrimitiveUnit unit = value.getPrimitiveTypeN();
		return unit == CSSPrimitiveUnit.CSS_EMS
				|| unit == CSSPrimitiveUnit.CSS_EXS
				|| unit == CSSPrimitiveUnit.CSS_PX
				|| unit == CSSPrimitiveUnit.CSS_IN
				|| unit == CSSPrimitiveUnit.CSS_CM
				|| unit == CSSPrimitiveUnit.CSS_MM
				|| unit == CSSPrimitiveUnit.CSS_PT
				|| unit == CSSPrimitiveUnit.CSS_PC
				|| (unit == CSSPrimitiveUnit.CSS_NUMBER && value
					.getFloatValue() == 0.0f);
	}

	public static void checkValidity(final CSSName cssName, final EnumSet<IdentValue> validValues,
			final IdentValue value) {
		if (!validValues.contains(value)) {
			cssThrowError(LangId.UNSUPPORTED_IDENTIFIER, value, cssName);
		}
	}

	public static void checkIdentValidity(final CSSName cssName, final EnumSet<IdentValue> validValues,
			final PropertyValue value) {
		final IdentValue ident = checkIdent(cssName, value);
		
		if (ident == null || !validValues.contains(ident)) {
			cssThrowError(LangId.UNSUPPORTED_IDENTIFIER, value, cssName);
		}
	}
	
	public static IdentValue checkIdent(final CSSName cssName, final PropertyValue value) {
		final IdentValue result = IdentValue.fsValueOf(value.getStringValue());
		if (result == null) {
			cssThrowError(LangId.UNRECOGNIZED_IDENTIFIER, value.getStringValue(), cssName);
		}
		value.setIdentValue(result);
		return result;
	}

	public static PropertyDeclaration copyOf(final PropertyDeclaration decl, final CSSName newName) {
		return new PropertyDeclaration(newName, decl.getValue(),
				decl.isImportant(), decl.getOrigin());
	}

	public static void checkInheritAllowed(final PropertyValue value,
			final boolean inheritAllowed) {
		if (value.getCssValueTypeN() == CSSValueType.CSS_INHERIT
				&& !inheritAllowed) {
			cssThrowError(LangId.INVALID_INHERIT);
		}
	}
	
	public static void checkFunctionsAllowed(final FSFunction func, String... allowed)
	{
		for (String allow : allowed)
		{
			if (allow.equals(func.getName()))
				return;
		}

		cssThrowError(LangId.FUNCTION_NOT_SUPPORTED, func.getName());
	}

	public static List<PropertyDeclaration> checkInheritAll(final CSSName[] all,
			final List<PropertyValue> values, final CSSOrigin origin, final boolean important,
			final boolean inheritAllowed) {
		if (values.size() == 1) {
			final PropertyValue value = values.get(0);
			checkInheritAllowed(value, inheritAllowed);
			if (value.getCssValueTypeN() == CSSValueType.CSS_INHERIT) {
				final List<PropertyDeclaration> result = new ArrayList<PropertyDeclaration>( all.length);
				for (final CSSName element : all) {
					result.add(new PropertyDeclaration(element, value, important, origin));
				}
				return result;
			}
		}

		return null;
	}
}
