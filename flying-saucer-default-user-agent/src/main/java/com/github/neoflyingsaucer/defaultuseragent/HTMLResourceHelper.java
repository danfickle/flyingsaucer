/*
 * {{{ header & license
 * Copyright (c) 2004, 2005 Who?
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
package com.github.neoflyingsaucer.defaultuseragent;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xhtmlrenderer.util.XRRuntimeException;

/**
 * @author Patrick Wright
 */
// TODO Charsets.
public class HTMLResourceHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(HTMLResourceHelper.class);
    private Document document;
    
    private HTMLResourceHelper(final String html)
    {
    	setDocument(Jsoup.parse(html));
    }

    private HTMLResourceHelper(final InputStream stream) {
    	try {
			document = Jsoup.parse(stream, null, "");
		} catch (final IOException e) {
			LOGGER.warn("Unable to parse input stream", e);
			throw new XRRuntimeException("Unable to parse input stream", e);
		}
    }
    
    private HTMLResourceHelper(final File file)
    {
    	try {
			document = Jsoup.parse(file, null);
		} catch (final IOException e) {
			LOGGER.warn("Unable to parse file", e);
			throw new XRRuntimeException("Unable to parse file", e);
		}
    }

    public static HTMLResourceHelper load(final String html)
    {
    	return new HTMLResourceHelper(html);
    }
    
    public static HTMLResourceHelper load(final InputStream stream) {
        return new HTMLResourceHelper(stream);
    }

    public static HTMLResourceHelper load(final Reader reader) {
    	final char[] cbuf = new char[4096];
    	int numChars;
    	
    	final StringBuilder builder = new StringBuilder(4096);

    	try {
			while ((numChars = reader.read(cbuf)) >= 0) {
			    builder.append(cbuf, 0, numChars);
			}
		} catch (final IOException e) {
			LOGGER.warn("Unable to parse reader", e);
			throw new XRRuntimeException("Unable to parse reader", e);
		}

    	return new HTMLResourceHelper(builder.toString());
    }
 
    public Document getDocument() {
        return document;
    }

    private void setDocument(final Document document) {
        this.document = document;
    }

	public static HTMLResourceHelper load(final File file) {
		return new HTMLResourceHelper(file);
	}
}
