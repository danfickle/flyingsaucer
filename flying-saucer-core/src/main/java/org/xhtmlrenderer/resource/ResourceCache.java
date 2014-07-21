package org.xhtmlrenderer.resource;

import org.w3c.dom.Document;
import org.xhtmlrenderer.css.sheet.Stylesheet;

public interface ResourceCache
{
	/**
	 * This method should return a w3c Document if available
	 * or null otherwise.
	 */
	public Document getHtmlDocument(String resolvedUri);

	/**
	 * This method takes a w3c Document and optionally puts it in a cache.
	 */
	public void putHtmlDocument(String resolvedUri, Document doc);

	/**
	 * This method should return a Stylesheet if available
	 * or null otherwise.
	 */
	public Stylesheet getCssStylesheet(String resolvedUri);
	
	/**
	 * This method takes a Stylesheet and optionally puts it in a cache.
	 */
	public void putCssStylesheet(String resolvedUri, Stylesheet sheet);
}
