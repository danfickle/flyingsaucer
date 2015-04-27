/*
 * NaiveUserAgent.java
 * Copyright (c) 2004, 2005 Torbjoern Gannholm
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 */
package com.github.neoflyingsaucer.defaultuseragent;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.xhtmlrenderer.event.DocumentListener;
import org.xhtmlrenderer.resource.CSSResource;
import org.xhtmlrenderer.resource.HTMLResource;
import org.xhtmlrenderer.resource.ImageResource;
import org.xhtmlrenderer.util.GeneralUtil;
import org.xhtmlrenderer.util.ImageUtil;

import com.github.neoflyingsaucer.extend.controller.error.FSErrorController;
import com.github.neoflyingsaucer.extend.controller.error.LangId;
import com.github.neoflyingsaucer.extend.controller.error.FSError.FSErrorLevel;
import com.github.neoflyingsaucer.extend.useragent.CSSResourceI;
import com.github.neoflyingsaucer.extend.useragent.HTMLResourceI;
import com.github.neoflyingsaucer.extend.useragent.ImageResourceI;
import com.github.neoflyingsaucer.extend.useragent.Optional;
import com.github.neoflyingsaucer.extend.useragent.ResourceCache;
import com.github.neoflyingsaucer.extend.useragent.UserAgentCallback;

public class DefaultUserAgent implements UserAgentCallback, DocumentListener {

    private static final int DEFAULT_IMAGE_CACHE_SIZE = 16;

    /**
     * a (simple) LRU cache
     */
    protected LinkedHashMap<String, ImageResource> _imageCache;

    // TODO: Make this configurable.
    protected ResourceCache _resourceCache = new ResourceCacheImpl(32, 5);

    private final int _imageCacheCapacity;

    /**
     * Creates a new instance of NaiveUserAgent with a max image cache of 16 images.
     */
    public DefaultUserAgent() {
        this(DEFAULT_IMAGE_CACHE_SIZE);
    }

    /**
     * Creates a new NaiveUserAgent with a cache of a specific size.
     *
     * @param imgCacheSize Number of images to hold in cache before LRU images are released.
     */
    public DefaultUserAgent(final int imgCacheSize) {
        this._imageCacheCapacity = imgCacheSize;

        // note we do *not* override removeEldestEntry() here--users of this class must call shrinkImageCache().
        // that's because we don't know when is a good time to flush the cache
        this._imageCache = new java.util.LinkedHashMap<String, ImageResource>(_imageCacheCapacity, 0.75f, true);
    }

    /**
     * If the image cache has more items than the limit specified for this class, the least-recently used will
     * be dropped from cache until it reaches the desired size.
     */
    public void shrinkImageCache() {
        int ovr = _imageCache.size() - _imageCacheCapacity;
        final Iterator<String> it = _imageCache.keySet().iterator();
        while (it.hasNext() && ovr-- > 0) {
            it.next();
            it.remove();
        }
    }

    /**
     * Empties the image cache entirely.
     */
    public void clearImageCache() {
        _imageCache.clear();
    }

    /**
     * Retrieves the CSS located at the given URI.  It's assumed the URI does point to a CSS file--the URI will
     * be accessed (using java.io or java.net), opened, read and then passed into the CSS parser.
     * The result is packed up into an CSSResource for later consumption.
     *
     * @param uri Location of the CSS source (returned from resolveURI).
     * @return A CSSResource containing the parsed CSS.
     */
    @Override
    public Optional<CSSResourceI> getCSSResource(String uri)
    {
        try
        {
        	StreamResource sr = new StreamResource(uri);
        	sr.connect();
        	final InputStream bs = sr.bufferedStream();
        	
        	return Optional.<CSSResourceI>of(new CSSResource(sr.getFinalUri(), new InputStreamReader(bs, "UTF-8"))
        	{
        		@Override
        		public void onClose() throws IOException 
        		{
        			bs.close();
        		}
        	});
		}
        catch (IOException e) 
        {
        	FSErrorController.log(DefaultUserAgent.class, FSErrorLevel.ERROR, LangId.COULDNT_LOAD_CSS, uri);
			return Optional.empty();
		}
    }

    /**
     * Retrieves the image located at the given URI. It's assumed the URI does point to an image--the URI will
     * be accessed (using java.io or java.net), opened, read and then passed into the JDK image-parsing routines.
     * The result is packed up into an ImageResource for later consumption.
     *
     * @param uri Location of the image source (returned from resolveURI).
     * @return An ImageResource containing the image.
     */
    @Override
    public Optional<ImageResourceI> getImageResource(String uri) {
        ImageResource ir;
        if (ImageUtil.isEmbeddedBase64Image(uri)) {
            final InputStream image = ImageUtil.loadEmbeddedBase64Image(uri);
            ir = createImageResource(null, image);
        } else {
            ir = null;
            //TODO: check that cached image is still valid
            if (ir == null) {
            	StreamResource sr = new StreamResource(uri);
            	sr.connect();
            	InputStream is = null;
				try 
				{
					is = sr.bufferedStream();
					ir = createImageResource(uri, is);
					_imageCache.put(uri, ir);
				}
				catch (FileNotFoundException e)
				{
					FSErrorController.log(DefaultUserAgent.class, FSErrorLevel.ERROR, LangId.COULDNT_LOAD_IMAGE, uri);
				} 
				catch (final IOException e) 
				{
					FSErrorController.log(DefaultUserAgent.class, FSErrorLevel.ERROR, LangId.COULDNT_LOAD_IMAGE, uri);
				}
				finally {
//					try {
////						if (is != null)
////							is.close();
//					} catch (final IOException e) {
//						// ignore
//					}
				}
            }
            if (ir == null) {
                ir = createImageResource(uri, null);
            }
        }
        return Optional.ofNullable((ImageResourceI) ir);
    }

    /**
     * Factory method to generate ImageResources from a given Image. May be overridden in subclass. 
     *
     * @param uri The URI for the image, resolved to an absolute URI.
     * @param img The image to package; may be null (for example, if image could not be loaded).
     *
     * @return An ImageResource containing the image.
     */
    protected ImageResource createImageResource(String uri, InputStream img) 
    {
        return new ImageResource(uri, img);
    }

    /**
     * Retrieves the XML located at the given URI. It's assumed the URI does point to a XML--the URI will
     * be accessed (using java.io or java.net), opened, read and then passed into the XML parser (XMLReader)
     * configured for Flying Saucer. The result is packed up into an XMLResource for later consumption.
     *
     * @param uri Location of the XML source.
     * @return An XMLResource containing the image.
     */
    @Override
    public Optional<HTMLResourceI> getHTMLResource(String uri) 
    {
        HTMLResourceHelper xmlResource;
        InputStream bs = null;
        StreamResource sr;
        
        try {
        	sr = new StreamResource(uri);
        	sr.connect();
        	bs = sr.bufferedStream();
        	xmlResource = HTMLResourceHelper.load(bs);
        } catch (IOException e) {
        	FSErrorController.log(DefaultUserAgent.class, FSErrorLevel.ERROR, LangId.COULDNT_LOAD_HTML_DOCUMENT, uri);
			return Optional.empty();
		} finally {
            if (bs != null) {
                try {
                    bs.close();
                } catch (final IOException e) {
                    // swallow
                }
            }
        }
        return Optional.of((HTMLResourceI) new HTMLResource(sr.getFinalUri(), xmlResource.getDocument())); 
    }

    @Override
    public Optional<byte[]> getBinaryResource(String uri) {

    	StreamResource sr = new StreamResource(uri);
    	sr.connect();
    	InputStream is = null;
    	
        try {
        	is = sr.bufferedStream();
        	final ByteArrayOutputStream result = new ByteArrayOutputStream();

            final byte[] buf = new byte[10240];
            int i;
            while ((i = is.read(buf)) != -1) {
                result.write(buf, 0, i);
            }
            is.close();
            is = null;

            return Optional.of(result.toByteArray());
        } catch (final IOException e) {
            return Optional.empty();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (final IOException e) {
                    // ignore
                }
            }
        }
    }

    /**
     * Returns true if the given URI was visited, meaning it was requested at some point since initialization.
     *
     * @param uri A URI which might have been visited.
     * @return Always false; visits are not tracked in the NaiveUserAgent.
     */
    @Override
    public boolean isVisited(String uri) 
    {
        return false;
    }

    /**
     * Resolves the base URI/absolute URI pair.
     * If absolute, leaves as is, if relative, returns an absolute URI
     * based on the baseUri and uri.
     * You may need to only override this method if your URIs resolve to 
     * to one of the following URL protocols: HTTP, HTTPS, JAR, FILE.
	 * This method is always called before requesting a resource.
	 * 
     * @param baseUri A base URI. May be null, in which case the uri must be absolute.
     * @param uri A URI, possibly relative.
	 *
     * @return A URI as String, resolved, or null if there was an exception (for example if the URI is malformed).
     */
    @Override
    public Optional<String> resolveURI(String baseUri, String uri) 
    {
        if (uri == null && baseUri == null)
        	return Optional.empty();

        if (baseUri == null) 
        {
        	try 
        	{
        		URI result = new URI(uri);
        		return Optional.of(result.normalize().toString());
        	}
        	catch (URISyntaxException e)
        	{
        		FSErrorController.log(DefaultUserAgent.class, FSErrorLevel.ERROR, LangId.INVALID_URI, uri);
        		return Optional.empty();
        	}
        }
        else
        {
        	try
        	{
        		URI base = new URI(baseUri);
        		URI rel = new URI(uri);

        		URI absolute = base.resolve(rel);
        		return Optional.of(absolute.normalize().toString());
        	}
        	catch (URISyntaxException e)
        	{
        		FSErrorController.log(DefaultUserAgent.class, FSErrorLevel.ERROR, LangId.INVALID_BASE_URI_PAIR, baseUri, uri);
        		return Optional.empty();
        	}
        }
    }

    @Override
    public void documentStarted() {
        shrinkImageCache();
    }

    @Override
    public void documentLoaded() { /* ignore*/ }

    @Override
    public void onLayoutException(final Throwable t) { /* ignore*/ }

    @Override
    public void onRenderException(final Throwable t) { /* ignore*/ }

	/**
	 * Used internally when a document can't be loaded--returns XHTML as an XMLResource indicating that fact.
	 *
	 * @param uri The URI which could not be loaded.
	 *
	 * @return An XMLResource containing XML which about the failure.
	 */
	@Override
	public HTMLResourceI getErrorDocument(final String uri, int errorCode) 
	{
        HTMLResourceHelper xr;

        // URI may contain & symbols which can "break" the XHTML we're creating
        final String cleanUri = GeneralUtil.escapeHTML(uri);
        final String notFound = "<html><h1>Document not found</h1><h2>" + errorCode + "</h2>" + "<p>Could not access URI <pre>" + cleanUri + "</pre></p></html>";

        xr = HTMLResourceHelper.load(notFound);
        return new HTMLResource("about:error", xr.getDocument());
    }

	@Override
	public ResourceCache getResourceCache() 
	{
		return _resourceCache;
	}

	@Override
	public Optional<HTMLResourceI> parseHTMLResource(String uri, String html) 
	{
		HTMLResourceHelper helper = HTMLResourceHelper.load(html); 

		if (helper.getDocument() != null)
			return Optional.<HTMLResourceI>of(new HTMLResource(uri, helper.getDocument()));
		else
			return Optional.empty();
	}

	@Override
	public Optional<HTMLResourceI> parseHTMLResource(String uri, File html) 
	{
		HTMLResourceHelper helper = HTMLResourceHelper.load(html); 

		if (helper.getDocument() != null)
			return Optional.<HTMLResourceI>of(new HTMLResource(uri, helper.getDocument()));
		else
			return Optional.empty();
	}

	@Override
	public Optional<HTMLResourceI> parseHTMLResource(String uri, Reader html) 
	{
		HTMLResourceHelper helper = HTMLResourceHelper.load(html); 

		if (helper.getDocument() != null)
			return Optional.<HTMLResourceI>of(new HTMLResource(uri, helper.getDocument()));
		else
			return Optional.empty();
	}
}
