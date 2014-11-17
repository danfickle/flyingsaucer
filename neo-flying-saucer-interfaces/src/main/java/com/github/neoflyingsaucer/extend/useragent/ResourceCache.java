package com.github.neoflyingsaucer.extend.useragent;

import org.w3c.dom.Document;

public interface ResourceCache
{
	/**
	 * This method should return a w3c Document if available
	 * or null otherwise.
	 */
	public Optional<Document> getHtmlDocument(String resolvedUri);

	/**
	 * This method takes a w3c Document and optionally puts it in a cache.
	 */
	public void putHtmlDocument(String resolvedUri, Document doc);

	/**
	 * This method should return a Stylesheet if available
	 * or null otherwise.
	 */
	public Optional<StylesheetI> getCssStylesheet(String resolvedUri);
	
	/**
	 * This method takes a Stylesheet and optionally puts it in a cache.
	 */
	public void putCssStylesheet(String resolvedUri, StylesheetI sheet);
}
