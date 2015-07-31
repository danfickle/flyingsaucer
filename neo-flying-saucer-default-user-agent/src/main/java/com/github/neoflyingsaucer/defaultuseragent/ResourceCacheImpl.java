package com.github.neoflyingsaucer.defaultuseragent;

import java.util.Map;

import org.w3c.dom.Document;

import com.github.neoflyingsaucer.extend.controller.error.FSError.FSErrorLevel;
import com.github.neoflyingsaucer.extend.controller.error.FSErrorController;
import com.github.neoflyingsaucer.extend.controller.error.LangId;
import com.github.neoflyingsaucer.extend.output.FSImage;
import com.github.neoflyingsaucer.extend.useragent.Optional;
import com.github.neoflyingsaucer.extend.useragent.ResourceCache;
import com.github.neoflyingsaucer.extend.useragent.StylesheetI;

public class ResourceCacheImpl implements ResourceCache
{
    private final int _cssCacheSize;
    private final int _htmlCacheSize;
    private final int _imgCacheSize;
    
	private final Map<String, StylesheetI> _cache;
	private final Map<String, Document> _docCache;	
    private final Map<ImageKey, FSImage> _imgCache;
	
    public ResourceCacheImpl(int cssCacheSize, int htmlCacheSize, int imgCacheSize)
    {
    	_cssCacheSize = cssCacheSize;
    	_htmlCacheSize = htmlCacheSize;
    	_imgCacheSize = imgCacheSize;
    	
    	_cache = new java.util.LinkedHashMap<String, StylesheetI>(
    			_cssCacheSize, 0.75f, true)
    	{
    		private static final long serialVersionUID = 1L;

    		@Override
    		protected boolean removeEldestEntry(final java.util.Map.Entry<String, StylesheetI> eldest) 
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

    	_imgCache = new java.util.LinkedHashMap<ImageKey, FSImage>(
    			_imgCacheSize, 0.75f, true)
    	{
    		private static final long serialVersionUID = 1L;

    		@Override
    		protected boolean removeEldestEntry(final java.util.Map.Entry<ImageKey, FSImage> eldest) 
    		{
    			return size() > _imgCacheSize;
    		}
    	};
    }
    
	@Override
	public void putCssStylesheet(String resolvedUri, StylesheetI sheet) 
	{
		if (resolvedUri != null)
		{
			FSErrorController.log(ResourceCacheImpl.class, FSErrorLevel.INFO, LangId.RECEIVING_STYLESHEET, resolvedUri);
			_cache.put(resolvedUri, sheet);
		}
		else
		{
			FSErrorController.log(ResourceCacheImpl.class, FSErrorLevel.WARNING, LangId.RESOURCE_WITH_NO_URI);
		}
	}

	@Override
	public Optional<StylesheetI> getCssStylesheet(String resolvedUri) 
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

	private static class ImageKey
	{
		private final Class<?> cls;
		private final String uri;
		
		private ImageKey(Class<?> cls, String uri)
		{
			this.cls = cls;
			this.uri = uri;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((cls == null) ? 0 : cls.hashCode());
			result = prime * result + ((uri == null) ? 0 : uri.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ImageKey other = (ImageKey) obj;
			if (cls == null) {
				if (other.cls != null)
					return false;
			} else if (!cls.equals(other.cls))
				return false;
			if (uri == null) {
				if (other.uri != null)
					return false;
			} else if (!uri.equals(other.uri))
				return false;
			return true;
		}
	}
	
	@Override
	public void putImage(String resolvedUri, Class<?> imgType, FSImage img) 
	{
		if (resolvedUri != null)
		{
			FSErrorController.log(ResourceCacheImpl.class, FSErrorLevel.INFO, LangId.RECEIVING_IMAGE, resolvedUri);

			ImageKey key = new ImageKey(imgType, resolvedUri);
			_imgCache.put(key, img);
		}
		else
		{
			FSErrorController.log(ResourceCacheImpl.class, FSErrorLevel.WARNING, LangId.RESOURCE_WITH_NO_URI);
		}
	}

	@Override
	public Optional<FSImage> getImage(String resolvedUri, Class<?> imgType) 
	{
		ImageKey key = new ImageKey(imgType, resolvedUri);
		return Optional.ofNullable(_imgCache.get(key));
	}
}
