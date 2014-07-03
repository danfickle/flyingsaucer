package org.xhtmlrenderer.css.mediaquery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xhtmlrenderer.css.constants.CSSPrimitiveUnit;
import org.xhtmlrenderer.css.parser.PropertyValue;
import org.xhtmlrenderer.css.parser.PropertyValueImp;
import org.xhtmlrenderer.css.parser.Token;
import org.xhtmlrenderer.layout.SharedContext;
import org.xhtmlrenderer.util.GeneralUtil;

public class MediaQueryEvaluator 
{
	private static final Logger LOGGER = LoggerFactory.getLogger(MediaQueryEvaluator.class);
	
	// Pseudo BNF from: https://developer.mozilla.org/en-US/docs/Web/Guide/CSS/Media_queries
	//
	// media_query_list: <media_query> [, <media_query> ]*
	//	media_query: [[only | not]? <media_type> [ and <expression> ]*]
	//			  | <expression> [ and <expression> ]*
	//			expression: ( <media_feature> [: <value>]? )
	//			media_type: all | aural | braille | handheld | print |
	//			  projection | screen | tty | tv | embossed
	//			media_feature: width | min-width | max-width
	//			  | height | min-height | max-height
	//			  | device-width | min-device-width | max-device-width
	//			  | device-height | min-device-height | max-device-height
	//			  | aspect-ratio | min-aspect-ratio | max-aspect-ratio
	//			  | device-aspect-ratio | min-device-aspect-ratio | max-device-aspect-ratio
	//			  | color | min-color | max-color
	//			  | color-index | min-color-index | max-color-index
	//			  | monochrome | min-monochrome | max-monochrome
	//			  | resolution | min-resolution | max-resolution
	//			  | scan | grid

	// Matches the following types of media query expressions:
	// (max-width:600px)
	// (color) and 
	// (min-width: 500px) and
	private static final Pattern EXPR_PATTERN = Pattern.compile("\\s*\\(\\s*([a-z\\-]+)\\s*(\\:?)\\s*([a-z0-9/\\s\\.]*)\\s*\\)\\s*(and)?.*");

	// Matches 'not' or 'only'.
	private static final Pattern NOT_ONLY_PATTERN = Pattern.compile("\\s*((not)|(only)).*");

	// Matches an ascii media type optionally followed by 'and'.
	private static final Pattern MEDIA_TYPE_PATTERN = Pattern.compile("\\s*([a-z]+)\\s*(and)?.*");

	// Matches a ratio value.
	private static final Pattern RATIO_PATTERN = Pattern.compile("\\s*(\\d+)\\s*/\\s*(\\d+).*");

	// Matches a measurement or number. Examples: 1cm, 1, landscape
	private static final Pattern VALUE_PATTERN = Pattern.compile("\\s*([\\d\\.]*)\\s*([a-z]*)\\s*");

	private static enum MediaQueryQualifier
	{
		NOT,
		ONLY,
		NONE;
	}

	private static class MediaQueryItem
	{
		private MediaQueryQualifier qualifier = MediaQueryQualifier.NONE;

		// The media type defaults to 'all' if left out.
		private String mediaType = "all";

		// Expressions in a media query are combined with AND semantics.
		private final List<MediaQueryExpression> expressions = new ArrayList<>(2);
		
		@Override
		public String toString() 
		{
			return "Qualifier: " + qualifier.toString() + ", Media Type: " + mediaType +
					", Expressions: " + expressions.toString();
		}
		
		public boolean eval(SharedContext ctx)
		{
			boolean res = false;
			
			// Webkit uses a case insensitive compare.
			if (GeneralUtil.ciEquals(mediaType, "all") ||
				GeneralUtil.ciEquals(mediaType, ctx.getMedia()) ||
				GeneralUtil.ciEquals(ctx.getMedia(), "all"))
			{
				res = true;
			}

			if (res == true)
			{
				for (MediaQueryExpression expr : expressions)
				{
					// AND semantics.
					
					boolean exprRes = expr.eval(ctx);
				
					if (!exprRes)
					{
						res = false;
						break;
					}
				}
			}
			
			if (qualifier == MediaQueryQualifier.NOT)
				res = !res;

			return res;
		}
	}

	// Each media query is combined together with OR semantics.
	private final List<MediaQueryItem> queryItems = new ArrayList<>(2);
	
	public MediaQueryEvaluator(String mediaQuery) 
	{
		String mq = mediaQuery.toLowerCase(Locale.US);
		String[] rawExprs = mq.split(Pattern.quote(","));
		
		for (String rawExpr : rawExprs)
		{
			MediaQueryItem queryItem = new MediaQueryItem();
			queryItems.add(queryItem);
			
			// First we check for a qualifer such as 'not' or 'only'.
			Matcher qualMatcher = NOT_ONLY_PATTERN.matcher(rawExpr);

			if (qualMatcher.matches())
			{
				if (qualMatcher.group(1).equals("not"))
				{
					queryItem.qualifier = MediaQueryQualifier.NOT;
				}
				else 
				{
					assert(qualMatcher.group(1).equals("only"));
					queryItem.qualifier = MediaQueryQualifier.ONLY;
				}

				rawExpr = rawExpr.substring(qualMatcher.end(1));
			}
				
			// Second we check for a media type, such as:
			// screen and
			// print and
			// tv and
			// braille
			Matcher matcher = MEDIA_TYPE_PATTERN.matcher(rawExpr);

			if (matcher.matches())
			{
				queryItem.mediaType = matcher.group(1);
				boolean hasAnd = matcher.group(2) != null && !matcher.group(2).isEmpty();

				// If not followed by 'and' then it is only a media type.
				if (!hasAnd)
					continue;

				// Continue at the end of 'and'.
				rawExpr = rawExpr.substring(matcher.end(2));
			}
			
			// Third we keep going until all the 'and' expressions are evaluated.
			while (true)
			{
				Matcher matcherQuery = EXPR_PATTERN.matcher(rawExpr);
			
				if (!matcherQuery.matches())
				{
					LOGGER.warn("Invalid media query: " + mq);
					break;
				}

				String exprName = matcherQuery.group(1);
				boolean hasValue = matcherQuery.group(2) != null && !matcherQuery.group(2).isEmpty();
				String value = matcherQuery.group(3);
				boolean hasAnd = matcherQuery.group(4) != null && !matcherQuery.group(4).isEmpty();

				List<PropertyValue> values = Collections.emptyList();
				
				try
				{
					if (hasValue)
					{
						if (MediaFeatureName.isRatio(exprName))
						{
							Matcher matcherRatio = RATIO_PATTERN.matcher(value);

							if (matcherRatio.matches())
							{
								float firstRatio = Float.parseFloat(matcherRatio.group(1));
								float secondRatio = Float.parseFloat(matcherRatio.group(2));

								PropertyValue valFirst = new PropertyValueImp(CSSPrimitiveUnit.CSS_NUMBER, firstRatio, matcherRatio.group(1));
								PropertyValue valSecond = new PropertyValueImp(CSSPrimitiveUnit.CSS_NUMBER, secondRatio, matcherRatio.group(2));
								valSecond.setOperator(Token.TK_VIRGULE);

								values = Arrays.asList(valFirst, valSecond);
							}
						}
						else
						{
							Matcher matcherValue = VALUE_PATTERN.matcher(value);

							if (matcherValue.matches())
							{
								if (matcherValue.group(1) != null && !matcherValue.group(1).isEmpty())
								{
									float floatVal = Float.parseFloat(matcherValue.group(1));

									CSSPrimitiveUnit unit = CSSPrimitiveUnit.CSS_NUMBER;

									if (matcherValue.group(2) != null && !matcherValue.group(2).isEmpty())
									{
										unit = CSSPrimitiveUnit.fromString(matcherValue.group(2));
									}

									PropertyValue val = new PropertyValueImp(unit, floatVal, matcherValue.group(1) + matcherValue.group(2));
									values = Collections.singletonList(val);
								}
								else
								{
									// No number means it is is an ident, such as 'landscape'.
									PropertyValue val = new PropertyValueImp(CSSPrimitiveUnit.CSS_IDENT, matcherValue.group(2), matcherValue.group(2));
									values = Collections.singletonList(val);
								}
							}
						}
					}
				} catch (NumberFormatException e)
				{
					LOGGER.warn("Invalid media query expression value: " + value);
					// Do nothing else. Values is already an empty list.
				}

				queryItem.expressions.add(new MediaQueryExpression(exprName, values));

				if (!hasAnd)
					break;

				rawExpr = rawExpr.substring(matcherQuery.end(4));
			}
		}
		
		LOGGER.info("The following media queries were parsed: {}", queryItems);
	}

	public boolean eval(SharedContext ctx)
	{
		if (queryItems.isEmpty())
			return true;
		
		for (MediaQueryItem item : queryItems)
		{
			// OR Semantics.
			if (item.eval(ctx))
				return true;
		}
		
		return false;
	}
}
