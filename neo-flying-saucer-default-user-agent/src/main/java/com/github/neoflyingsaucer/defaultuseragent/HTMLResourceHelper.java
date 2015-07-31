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

import org.w3c.dom.Document;
import com.github.neoflyingsaucer.extend.controller.error.FSError.FSErrorLevel;
import com.github.neoflyingsaucer.extend.controller.error.FSErrorController;
import com.github.neoflyingsaucer.extend.controller.error.LangId;
import com.github.neoflyingsaucer.jsouptodom.Parser;

/**
 * @author Patrick Wright
 */
// TODO Charsets.
public class HTMLResourceHelper 
{
    private Document document;
    
    private HTMLResourceHelper(String html)
    {
    	setDocument(Parser.parseHtml(html));
    }

    private HTMLResourceHelper(InputStream stream) {
    	try {
			document = Parser.parseHtml(stream);
		} catch (final IOException e) {
			FSErrorController.log(HTMLResourceHelper.class, FSErrorLevel.ERROR, LangId.COULDNT_LOAD_HTML_DOCUMENT, "{unknown}");
		}
    }
    
    private HTMLResourceHelper(File file)
    {
    	try {
			document = Parser.parseHtml(file);
		} catch (final IOException e) {
			FSErrorController.log(HTMLResourceHelper.class, FSErrorLevel.ERROR, LangId.COULDNT_LOAD_HTML_DOCUMENT, "{unknown}");
		}
    }

    public static HTMLResourceHelper load(String html)
    {
    	return new HTMLResourceHelper(html);
    }
    
    public static HTMLResourceHelper load(InputStream stream) {
        return new HTMLResourceHelper(stream);
    }

    public static HTMLResourceHelper load(Reader reader) {
    	char[] cbuf = new char[4096];
    	int numChars;
    	
    	StringBuilder builder = new StringBuilder(4096);

    	try {
			while ((numChars = reader.read(cbuf)) >= 0) {
			    builder.append(cbuf, 0, numChars);
			}
		} catch (final IOException e) {
			FSErrorController.log(HTMLResourceHelper.class, FSErrorLevel.ERROR, LangId.COULDNT_LOAD_HTML_DOCUMENT, "{unknown}");
		}

    	return new HTMLResourceHelper(builder.toString());
    }
 
    public Document getDocument() {
        return document;
    }

    private void setDocument(final Document document) {
        this.document = document;
    }

	public static HTMLResourceHelper load(File file) {
		return new HTMLResourceHelper(file);
	}
}
