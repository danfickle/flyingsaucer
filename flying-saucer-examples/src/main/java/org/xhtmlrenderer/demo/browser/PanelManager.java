/*
 * PanelManager.java
 * Copyright (c) 2005 Torbjoern Gannholm
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
package org.xhtmlrenderer.demo.browser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xhtmlrenderer.resource.HTMLResource;
import com.github.neoflyingsaucer.defaultuseragent.DefaultUserAgent;
import java.net.*;
import java.util.ArrayList;
import java.util.List;


/**
 * PanelManager is a UserAgentCallback responsible for the Browser's resource (XML, image, CSS) lookup. Most of the
 * power is in the NaiveUserAgent; the PanelManager adds support for the demo:, file: and demoNav: protocols,
 * and keeps track of the history of visited links. There is always a "current" link, and one can use the
 * {@link #getBack()}, {@link #getForward()} and {@link #hasForward()} methods to navigate within the history.
 * As a NaiveUserAgent, the PanelManager is also a DocumentListener, but must be added to the source of document
 * events (like a RootPanel subclass).
 *  
 */
public class PanelManager extends DefaultUserAgent {

    private static final Logger LOGGER = LoggerFactory.getLogger(PanelManager.class);
    private int index = -1;
    private final List<String> history = new ArrayList<>();


    /**
     * This is a sample of converting a private URI namespace to
     * a public one: demo: to jar:.
     */
    @Override
    public String resolveURI(String baseUri, String uriRel) {

    	String resolvedUri = super.resolveURI(baseUri, uriRel);
    	
    	if (resolvedUri == null)
    	{
    		return null;
    	}
    	else if (resolvedUri.startsWith("demo:")) 
        {
            final DemoMarker marker = new DemoMarker();
            String shortUrl = resolvedUri.substring(5);
            if (!shortUrl.startsWith("/")) {
                shortUrl = "/" + shortUrl;
            }
            URL ref = marker.getClass().getResource(shortUrl);
            return ref.toString();
        }
        else if (resolvedUri.startsWith("demoNav:")) 
        {
            final DemoMarker marker = new DemoMarker();
            String shortUrl = resolvedUri.substring(8);
            if (!shortUrl.startsWith("/")) {
                shortUrl = "/" + shortUrl;
            }
            URL ref = marker.getClass().getResource(shortUrl);
            return ref.toString();
        }
        else
        {
        	return resolvedUri;
        }
    }

    @Override
    public HTMLResource getHTMLResource(String uri) 
    {
    	history.add(uri);
    	return super.getHTMLResource(uri);
    }



	/**
	 * Returns the "next" URI in the history of visiting URIs. Advances the URI tracking (as if browser "forward" was
	 * used).
	 */
	public String getForward() {
        index++;
        return history.get(index);
    }

	/**
	 * Returns the "previous" URI in the history of visiting URIs. Moves the URI tracking back (as if browser "back" was
	 * used).
	 */
	public String getBack() {
        index--;
        return history.get(index);
    }

	/**
	 * Returns true if there are visited URIs in history "after" the pointer the the current URI. This would be the case
	 * if multiple URIs were visited and the getBack() had been called at least once.
	 */
	public boolean hasForward() {
        if (index + 1 < history.size() && index >= 0) {
            return true;
        } else {
            return false;
        }
    }

	/**
	 * Returns true if there are visited URIs in history "before" the pointer the the current URI. This would be the case
	 * if multiple URIs were visited and the current URI pointer was not at the begininnig of the visited URI list. 
	 */
    public boolean hasBack() {
        if (index > 0) {
            return true;
        } else {
            return false;
        }
    }
}
