package org.xhtmlrenderer.swing;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xhtmlrenderer.css.sheet.Stylesheet;

public class StylesheetCache
{
    private static final Logger LOGGER = LoggerFactory.getLogger(StylesheetCache.class);

    /**
     * A stylesheet will only resolve the same if its uri & media is the same. Also with
     * media queries the viewport width and height must also be the same.
     */
    public static class StylesheetCacheKey
    {
    	private final String uri;
    	private final int targetWidth;
    	private final int targetHeight;
    	private final String media;
    	
    	public StylesheetCacheKey(String uri, int targetWidth, int targetHeight, String media) 
    	{
    		this.uri = uri;
    		this.targetWidth = targetWidth;
    		this.targetHeight = targetHeight;
    		this.media = media;
    	}

		@Override
		public int hashCode() 
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + ((media == null) ? 0 : media.hashCode());
			result = prime * result + targetHeight;
			result = prime * result + targetWidth;
			result = prime * result + ((uri == null) ? 0 : uri.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) 
		{
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			StylesheetCacheKey other = (StylesheetCacheKey) obj;
			if (media == null) {
				if (other.media != null)
					return false;
			} else if (!media.equals(other.media))
				return false;
			if (targetHeight != other.targetHeight)
				return false;
			if (targetWidth != other.targetWidth)
				return false;
			if (uri == null) {
				if (other.uri != null)
					return false;
			} else if (!uri.equals(other.uri))
				return false;
			return true;
		}
		
		@Override
		public String toString() 
		{
			return uri + ":" + targetWidth + ":" + targetHeight + (media == null ? "" : media);
		}
    }
    
    /**
	 * an LRU cache
	 */
	private static final int DEFAULT_CSS_CACHE_SIZE = 64;
	private final Map<StylesheetCacheKey, Stylesheet> _cache = new java.util.LinkedHashMap<StylesheetCacheKey, Stylesheet>(
			DEFAULT_CSS_CACHE_SIZE, 0.75f, true)
	{
		private static final long serialVersionUID = 1L;

		@Override
		protected boolean removeEldestEntry(final java.util.Map.Entry<StylesheetCacheKey, Stylesheet> eldest) 
		{
			return size() > DEFAULT_CSS_CACHE_SIZE;
		}
	};

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
	public void putStylesheet(final StylesheetCacheKey key, final Stylesheet sheet) 
	{
		LOGGER.info("Receiving stylesheet for " + key);
		_cache.put(key, sheet);
	}

	/**
	 * @param key
	 * @return true if a Stylesheet with this key has been put in the cache.
	 *         Note that the Stylesheet may be null.
	 */
	public boolean containsStylesheet(final StylesheetCacheKey key) 
	{
		return _cache.containsKey(key);
	}

	/**
	 * Returns a cached sheet by its key; null if no entry for that key.
	 * 
	 * @param key
	 *            The key for this sheet; same as key passed to putStylesheet();
	 * @return The stylesheet
	 */
	public Stylesheet getStylesheet(final StylesheetCacheKey key) 
	{
		if (_cache.containsKey(key))
			LOGGER.info("Stylesheet hit for " + key.toString());
		else
			LOGGER.info("Stylesheet miss for " + key.toString());
		
		return _cache.get(key);
	}

	/**
	 * Removes a cached sheet by its key.
	 * 
	 * @param key
	 *            The key for this sheet; same as key passed to putStylesheet();
	 */
	public Stylesheet removeCachedStylesheet(final StylesheetCacheKey key) 
	{
		return _cache.remove(key);
	}

	public void flushCachedStylesheets() 
	{
		_cache.clear();
	}
}
