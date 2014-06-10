package org.xhtmlrenderer.css.mediaquery;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MediaQueryEvaluator 
{
	private static final Logger LOGGER = LoggerFactory.getLogger(MediaQueryEvaluator.class);
	
	// Matches the following types of media query expressions.
	// (max-width:600px)
	// (color)
	// (color) and 
	//
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
	private static final Pattern EXPR_PATTERN = Pattern.compile("\\s*\\(\\s*([a-z\\-]+)\\s*(\\:?)\\s*([a-z0-9]*)\\s*\\)\\s*(and)?.*");
	private static final Pattern MEDIA_TYPE_PATTERN = Pattern.compile("([a-z]+)\\s*(and).*");
	
	private final List<MediaQueryExpression> expressions = new ArrayList<>();

	private static enum MediaQueryQualifier
	{
		NOT,
		ONLY,
		NONE;
	}
	
	private final MediaQueryQualifier qualifier;
	
	public MediaQueryEvaluator(String mediaQuery) 
	{
		String mq = mediaQuery.toLowerCase(Locale.US).trim();

		if (mq.startsWith("only"))
		{
			qualifier = MediaQueryQualifier.ONLY;
			mq = mq.substring(4).trim();
		}
		else if (mq.startsWith("not"))
		{
			qualifier = MediaQueryQualifier.NOT;
			mq = mq.substring(3).trim();
		}
		else
		{
			qualifier = MediaQueryQualifier.NONE;
		}
		
		if (!mq.startsWith("("))
		{
			Matcher matcher = MEDIA_TYPE_PATTERN.matcher(mq);

			if (!matcher.matches())
			{
				LOGGER.warn("Invalid media query: " + mq);
				return;
			}

			String mediaType = matcher.group(1);
			mq = mq.substring(matcher.end(2));
		}
		
		String[] rawExprs = mq.split(Pattern.quote(","));
		
		for (String rawExpr : rawExprs)
		{
			while (true)
			{
				Matcher matcher = EXPR_PATTERN.matcher(rawExpr);
			
				if (!matcher.matches())
				{
					LOGGER.warn("Invalid media query: " + mq);
					break;
				}

				String exprName = matcher.group(1);
				boolean hasValue = matcher.group(2) != null && !matcher.group(2).isEmpty();
				String value = matcher.group(3);
				boolean hasAnd = matcher.group(4) != null && !matcher.group(4).isEmpty();

				// TODO
				
				if (!hasAnd)
					break;

				rawExpr = rawExpr.substring(matcher.end(4));
			}
		}
	}
}
