package org.xhtmlrenderer.swing;

import org.xhtmlrenderer.css.sheet.Stylesheet;

public interface StylesheetCache {

	/**
	 * Adds a stylesheet to the factory cache. Will overwrite older entry for
	 * same key.
	 * 
	 * @param key
	 *            Key to use to reference sheet later; must be unique in
	 *            factory.
	 * @param sheet
	 *            The sheet to cache.
	 */
	public abstract void putStylesheet(StylesheetCacheKey key, Stylesheet sheet);

	/**
	 * @param key
	 * @return true if a Stylesheet with this key has been put in the cache.
	 *         Note that the Stylesheet may be null.
	 */
	public abstract boolean containsStylesheet(StylesheetCacheKey key);

	/**
	 * Returns a cached sheet by its key; null if no entry for that key.
	 * 
	 * @param key
	 *            The key for this sheet; same as key passed to putStylesheet();
	 * @return The stylesheet
	 */
	public abstract Stylesheet getStylesheet(StylesheetCacheKey key);

	/**
	 * Removes a cached sheet by its key.
	 * 
	 * @param key
	 *            The key for this sheet; same as key passed to putStylesheet();
	 */
	public abstract Stylesheet removeCachedStylesheet(StylesheetCacheKey key);

	public abstract void flushCachedStylesheets();

}