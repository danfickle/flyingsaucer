/*
 * StylesheetFactoryImpl.java
 * Copyright (c) 2004, 2005 Torbjoern Gannholm
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
 *
 */
package org.xhtmlrenderer.context;

import java.io.IOException;
import java.io.Reader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xhtmlrenderer.css.extend.StylesheetFactory;
import org.xhtmlrenderer.css.parser.CSSErrorHandler;
import org.xhtmlrenderer.css.parser.CSSParser;
import org.xhtmlrenderer.css.sheet.Ruleset;
import org.xhtmlrenderer.css.sheet.Stylesheet;
import org.xhtmlrenderer.css.sheet.StylesheetInfo;
import org.xhtmlrenderer.css.sheet.StylesheetInfo.CSSOrigin;
import org.xhtmlrenderer.extend.FSErrorType;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.resource.CSSResource;
import org.xhtmlrenderer.swing.StylesheetCacheKey;
import org.xhtmlrenderer.util.LangId;

/**
 * A Factory class for Cascading Style Sheets. Sheets are parsed using a single
 * parser instance for all sheets. Sheets are cached by URI using a LRU test,
 * but timestamp of file is not checked.
 *
 * @author Torbjoern Gannholm
 */
public class StylesheetFactoryImpl implements StylesheetFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(StylesheetFactoryImpl.class);
	/**
     * the UserAgentCallback to resolve uris
     */
    private UserAgentCallback _userAgentCallback;
    private final CSSParser _cssParser;

    public StylesheetFactoryImpl(final UserAgentCallback userAgentCallback) {
        _userAgentCallback = userAgentCallback;
        _cssParser = new CSSParser(new CSSErrorHandler() {
        	@Override
        	public void error(String uri, int line, LangId msgId, Object... args) {
        		if (_userAgentCallback != null)
        		{
        			// Send it back to the user agent to decide what to do with it.
        			_userAgentCallback.onError(msgId, line, FSErrorType.CSS_ERROR, args);
        		}
        	}
        }, _userAgentCallback);
    }

    public synchronized Stylesheet parse(final Reader reader, final StylesheetInfo info, boolean isInline) {
        try 
        {
        	final Stylesheet s1 = _cssParser.parseStylesheet(info.getUri(), info.getOrigin(), reader);

        	if (!isInline)
        	{
            	// TODO
        		final StylesheetCacheKey key = new StylesheetCacheKey(info.getUri(), 0, 0, info.getMedia().toString());
        	   	_userAgentCallback.getStylesheetCache().putStylesheet(key, s1);
        	}

        	return s1; 
        }
        catch (final IOException e) 
        {
            LOGGER.warn("Couldn't parse stylesheet at URI " + info.getUri() + ": " + e.getMessage(), e);
            e.printStackTrace();
            return new Stylesheet(info.getUri(), info.getOrigin());
        }
    }

    /**
     * @return Returns null if uri could not be loaded
     */
    private Stylesheet parse(final StylesheetInfo info) {
        final CSSResource cr = _userAgentCallback.getCSSResource(info.getUri());

        // Q: Do @import rules use the original URI as the base for importing
        // other stylesheets or do they use the redirected URI.
        // If the former, we should remove this call.
        // A: According to:
        // http://stackoverflow.com/questions/7350994/ie-not-using-redirected-url-for-resolving-relative-urls
        // IE uses the previous URI while other browsers use the redirected URI.
        // So we'll go with the majority and screw IE.
        info.setUri(cr.getUri());
        
        try {
            final Stylesheet s1 = parse(cr.getReader(), info, false);
            return s1;
        }
        finally {
            if (cr != null) {
                try {
                    cr.getReader().close();
                    cr.onClose();
                } catch (final IOException e) {
                    // ignore
                }
            }
        }
    }

    public Ruleset parseStyleDeclaration(final String uri, final CSSOrigin origin, final String styleDeclaration) 
    {
        return _cssParser.parseDeclaration(uri, origin, styleDeclaration);
    }

    public Stylesheet getStylesheet(final StylesheetInfo info) 
    {
        // Give the user agent the chance to return a cached Stylesheet
        // instance.
        // TODO
        final StylesheetCacheKey key = new StylesheetCacheKey(info.getUri(), 0, 0, info.getMedia().toString());
        final Stylesheet s1 = _userAgentCallback.getStylesheetCache().getStylesheet(key);

        if (s1 == null)
        	return parse(info);

        return s1;
    }

    public void setUserAgentCallback(final UserAgentCallback userAgent) {
        _userAgentCallback = userAgent;
    }
    
    public UserAgentCallback getUac()
    {
    	return _userAgentCallback;
    }
    
    public void setSupportCMYKColors(final boolean b) {
        _cssParser.setSupportCMYKColors(b);
    }
}
