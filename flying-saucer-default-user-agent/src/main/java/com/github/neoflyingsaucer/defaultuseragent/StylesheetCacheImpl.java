package com.github.neoflyingsaucer.defaultuseragent;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xhtmlrenderer.css.sheet.Stylesheet;
import org.xhtmlrenderer.swing.StylesheetCache;
import org.xhtmlrenderer.swing.StylesheetCacheKey;

public class StylesheetCacheImpl implements StylesheetCache
{
    private static final Logger LOGGER = LoggerFactory.getLogger(StylesheetCacheImpl.class);

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

	/* (non-Javadoc)
	 * @see org.xhtmlrenderer.swing.StylesheetCacheI#putStylesheet(org.xhtmlrenderer.swing.StylesheetCache.StylesheetCacheKey, org.xhtmlrenderer.css.sheet.Stylesheet)
	 */
	@Override
	public void putStylesheet(final StylesheetCacheKey key, final Stylesheet sheet) 
	{
		LOGGER.info("Receiving stylesheet for " + key);
		_cache.put(key, sheet);
	}

	/* (non-Javadoc)
	 * @see org.xhtmlrenderer.swing.StylesheetCacheI#containsStylesheet(org.xhtmlrenderer.swing.StylesheetCache.StylesheetCacheKey)
	 */
	@Override
	public boolean containsStylesheet(final StylesheetCacheKey key) 
	{
		return _cache.containsKey(key);
	}

	/* (non-Javadoc)
	 * @see org.xhtmlrenderer.swing.StylesheetCacheI#getStylesheet(org.xhtmlrenderer.swing.StylesheetCache.StylesheetCacheKey)
	 */
	@Override
	public Stylesheet getStylesheet(final StylesheetCacheKey key) 
	{
		if (_cache.containsKey(key))
			LOGGER.info("Stylesheet hit for " + key.toString());
		else
			LOGGER.info("Stylesheet miss for " + key.toString());
		
		return _cache.get(key);
	}

	/* (non-Javadoc)
	 * @see org.xhtmlrenderer.swing.StylesheetCacheI#removeCachedStylesheet(org.xhtmlrenderer.swing.StylesheetCache.StylesheetCacheKey)
	 */
	@Override
	public Stylesheet removeCachedStylesheet(final StylesheetCacheKey key) 
	{
		return _cache.remove(key);
	}

	/* (non-Javadoc)
	 * @see org.xhtmlrenderer.swing.StylesheetCacheI#flushCachedStylesheets()
	 */
	@Override
	public void flushCachedStylesheets() 
	{
		_cache.clear();
	}
}
