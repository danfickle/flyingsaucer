package com.github.neoflyingsaucer.defaultuseragent;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xhtmlrenderer.css.sheet.Stylesheet;
import org.xhtmlrenderer.resource.ResourceCache;
import org.xhtmlrenderer.util.Optional;

public class ResourceCacheImpl implements ResourceCache
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceCacheImpl.class);

    private final int _cssCacheSize;
    private final int _htmlCacheSize;
	private final Map<String, Stylesheet> _cache;
	private final Map<String, Document> _docCache;	
    
    public ResourceCacheImpl(int cssCacheSize, int htmlCacheSize)
    {
    	_cssCacheSize = cssCacheSize;
    	_htmlCacheSize = htmlCacheSize;
    	
    	_cache = new java.util.LinkedHashMap<String, Stylesheet>(
    			_cssCacheSize, 0.75f, true)
    	{
    		private static final long serialVersionUID = 1L;

    		@Override
    		protected boolean removeEldestEntry(final java.util.Map.Entry<String, Stylesheet> eldest) 
    		{
    			return size() > _cssCacheSize;
    		}
    	};

    	_docCache = new java.util.LinkedHashMap<String, Document>(
    			_htmlCacheSize, 0.75f, true)
    	{
    		private static final long serialVersionUID = 1L;

    		@Override
    		protected boolean removeEldestEntry(final java.util.Map.Entry<String, Document> eldest) 
    		{
    			return size() > _htmlCacheSize;
    		}
    	};
    }
    
	@Override
	public void putCssStylesheet(String resolvedUri, Stylesheet sheet) 
	{
		if (resolvedUri != null)
		{
			LOGGER.info("Receiving stylesheet for {}", resolvedUri);
			_cache.put(resolvedUri, sheet);
		}
		else
		{
			LOGGER.error("Tried to put a stylesheet in cache without uri");
		}
	}

	@Override
	public Optional<Stylesheet> getCssStylesheet(String resolvedUri) 
	{
		return Optional.ofNullable(_cache.get(resolvedUri));
	}

	@Override
	public Optional<Document> getHtmlDocument(String resolvedUri) 
	{
		return Optional.ofNullable(_docCache.get(resolvedUri));
	}

	@Override
	public void putHtmlDocument(String resolvedUri, Document doc) 
	{
		_docCache.put(resolvedUri, doc);
	}
}
