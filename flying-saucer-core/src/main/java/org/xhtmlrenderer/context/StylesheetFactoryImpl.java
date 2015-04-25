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

import com.github.neoflyingsaucer.extend.controller.error.FSError;
import com.github.neoflyingsaucer.extend.controller.error.FSErrorController;
import com.github.neoflyingsaucer.extend.controller.error.FSErrorType;
import com.github.neoflyingsaucer.extend.controller.error.FSError.FSErrorLevel;
import com.github.neoflyingsaucer.extend.useragent.CSSResourceI;
import com.github.neoflyingsaucer.extend.useragent.LangId;
import com.github.neoflyingsaucer.extend.useragent.Optional;
import com.github.neoflyingsaucer.extend.useragent.StylesheetI;
import com.github.neoflyingsaucer.extend.useragent.UserAgentCallback;

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
        			// Send it back to the user configurable error handler to decide what to do with it.
        			FSError err = new FSError(line, msgId, FSErrorType.CSS_ERROR, uri, StylesheetFactory.class, FSErrorLevel.WARNING, args);
        			FSErrorController.onError(err);
        		}
        	}
        }, _userAgentCallback);
    }

    /**
     * The caller is responsible for closing the Reader.
     */
    public Optional<StylesheetI> parse(final Reader reader, final StylesheetInfo info, boolean isInline) 
    {
        if (info.getUri().isPresent())
        {
        	Stylesheet s1;

        	try {
				s1 = _cssParser.parseStylesheet(info.getUri().get(), info.getOrigin(), reader);
			} catch (IOException e) {
				return Optional.empty();
			}


       		if (!isInline)
       		{
            	// We only cache external stylesheets.
       			_userAgentCallback.getResourceCache().putCssStylesheet(info.getUri().get(), s1);
       		}
       		
       		return Optional.of((StylesheetI) s1);
       	}

        LOGGER.warn("Couldn't parse stylesheet with no URI");
        return Optional.empty();
    }

    private Optional<StylesheetI> parse(final StylesheetInfo info) {

    	if (!info.getUri().isPresent())
    		return Optional.empty();
    	
    	final Optional<CSSResourceI> cr = _userAgentCallback.getCSSResource(info.getUri().get());

        if (!cr.isPresent())
        {
        	LOGGER.warn("Unable to retrieve stylesheet at url({})", info.getUri());
        	return Optional.empty();
        }
        
        final CSSResourceI cr2 = cr.get();
        
        // Q: Do @import rules use the original URI as the base for importing
        // other stylesheets or do they use the redirected URI.
        // If the former, we should remove this call.
        // A: According to:
        // http://stackoverflow.com/questions/7350994/ie-not-using-redirected-url-for-resolving-relative-urls
        // IE uses the previous URI while other browsers use the redirected URI.
        // So we'll go with the majority and screw IE.
        info.setUri(Optional.of(cr2.getUri()));
        
        try {
            return parse(cr2.getReader(), info, false);
        }
        finally {
            try {
            	cr2.getReader().close();
                cr2.onClose();
            } catch (final IOException e) {
                // ignore
            }
        }
    }

    public Optional<Ruleset> parseStyleDeclaration(final String uri, final CSSOrigin origin, final String styleDeclaration) 
    {
        return Optional.ofNullable(_cssParser.parseDeclaration(uri, origin, styleDeclaration));
    }

    public Optional<StylesheetI> getStylesheet(final StylesheetInfo info) 
    {
        // Give the user agent the chance to return a cached
    	// Stylesheet instance.
    	if (!info.getUri().isPresent())
    		return Optional.empty();
    	
    	final Optional<StylesheetI> s1 = _userAgentCallback.getResourceCache().getCssStylesheet(info.getUri().get());

        if (s1.isPresent())
        {
        	LOGGER.info("Stylesheet HIT for " + info.getUri().get());
        	return s1;
        }
        
        // Otherwise, we have to try to get it from the 
        // user agent proper.        
        LOGGER.info("Stylesheet MISS for " + info.getUri());
        return parse(info);
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
