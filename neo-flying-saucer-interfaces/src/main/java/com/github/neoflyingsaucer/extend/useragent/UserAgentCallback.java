/*
 * UserAgentCallback.java
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
package com.github.neoflyingsaucer.extend.useragent;

/**
 * <p>To be implemented by any user agent using the panel. "User agent" is a
 * term defined by the W3C in the documentation for XHTML and CSS; in most
 * cases, you can think of this as the rendering component for a browser.</p>
 *
 * <p>This interface defines a simple callback mechanism for Flying Saucer to
 * interact with a user agent. The FS toolkit provides a default implementation
 * for this interface which in most cases you can leave as is. 
 * </p>
 *
 * <p>The user agent in this case is responsible for retrieving external resources. For
 * privacy reasons, if using the library in an application that can access URIs
 * in an unrestricted fashion, you may decide to restrict access to XML, CSS or images
 * retrieved from external sources; that's one of the purposes of the UAC.</p>
 *
 * <p>To understand how to create your own UAC, it's best to look at some of the
 * implemetations shipped with the library.
 * </p>
 *
 * @author Torbjoern Gannholm
 */
public interface UserAgentCallback {

	/**
     * Retrieves the CSS at the given URI. This is a synchronous call.
     *
     * @param uri Location of the CSS (returned from resolveURI).
     * @return A CSSResource for the CSS at the uri or null if not available.
     */
    Optional<CSSResourceI> getCSSResource(String uri);

    /**
     * Retrieves the Image at the given URI. This is a synchronous call.
     *
     * @param uri Location of the image (returned from resolveURI).
     * @return An ImageResource for the content at the URI.
     */
    Optional<ImageResourceI> getImageResource(String uri);

    /**
     * Retrieves the HTML at the given URI.
     * This is a synchronous call.
     *
     * @param uri Location of the HTML (returned from resolveURI).
     * @return A HTMLResource for the content at the URI.
     */
    Optional<HTMLResourceI> getHTMLResource(String uri);
    
    /**
     * Gets a error document for a specific error code such as 404.
	 * May NOT return null.
     */
    HTMLResourceI getErrorDocument(String uri, int errorCode);
    
    /**
     * Retrieves a binary resource located at a given URI and returns its contents
     * as a byte array or <code>null</code> if the resource could not be loaded.
     */
    Optional<byte[]> getBinaryResource(String uri);

    /**
     * Normally, returns true if the user agent has visited this URI. UserAgent should consider
     * if it should answer truthfully or not for privacy reasons.
     *  
     * @param uri A URI which may have been visited by this user agent (returned from resolveURI).
     * @return The visited value
     */
    boolean isVisited(String uri);

    /**
     * Used to find a uri that may be relative to the baseUri.
     * The returned value will always only be used via methods in the same
     * implementation of this interface, therefore may be a private uri-space.
     *
     * @param uri an absolute or relative uri to be resolved.
     * @return the full uri in uri-spaces known to the current implementation.
     */
    Optional<String> resolveURI(String baseUri, String uri);

	/**
	 * May NOT return null.
	 */
	ResourceCache getResourceCache();
}
