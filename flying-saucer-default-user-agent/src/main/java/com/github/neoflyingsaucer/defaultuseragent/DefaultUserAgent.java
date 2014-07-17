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

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xhtmlrenderer.event.DocumentListener;
import org.xhtmlrenderer.extend.FSErrorType;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.resource.CSSResource;
import org.xhtmlrenderer.resource.HTMLResource;
import org.xhtmlrenderer.resource.ImageResource;
import org.xhtmlrenderer.swing.AWTFSImage;
import org.xhtmlrenderer.swing.ImageResourceLoader;
import org.xhtmlrenderer.swing.StylesheetCache;
import org.xhtmlrenderer.util.GeneralUtil;
import org.xhtmlrenderer.util.ImageUtil;
import org.xhtmlrenderer.util.LangId;
import org.xhtmlrenderer.util.XRRuntimeException;

/**
 * <p>NaiveUserAgent is a simple implementation of {@link UserAgentCallback} which places no restrictions on what
 * XML, CSS or images are loaded, and reports visited links without any filtering. The most straightforward process
 * available in the JDK is used to load the resources in question--either using java.io or java.net classes.
 *
 * <p>The NaiveUserAgent has a small cache for images,
 * the size of which (number of images) can be passed as a constructor argument. There is no automatic cleaning of
 * the cache; call {@link #shrinkImageCache()} to remove the least-accessed elements--for example, you might do this
 * when a new document is about to be loaded. The NaiveUserAgent is also a DocumentListener; if registered with a
 * source of document events (like the panel hierarchy), it will respond to the
 * {@link org.xhtmlrenderer.event.DocumentListener#documentStarted()} call and attempt to shrink its cache.
 *
 * <p>This class is meant as a starting point--it will work out of the box, but you should really implement your
 * own, tuned to your application's needs.
 *
 * @author Torbjoern Gannholm
 */
public class DefaultUserAgent implements UserAgentCallback, DocumentListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultUserAgent.class);

    private static final int DEFAULT_IMAGE_CACHE_SIZE = 16;

    /**
     * a (simple) LRU cache
     */
    protected LinkedHashMap<String, ImageResource> _imageCache;
    protected StylesheetCache _styleCache = new StylesheetCacheImpl();
    protected ImageResourceLoader _imageCache2 = new ImageResourceLoaderImpl();

    // TODO: Customize resource locale.
    protected ResourceBundle messages = ResourceBundle.getBundle("languages.ErrorMessages", Locale.US);
    
    
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
    public CSSResource getCSSResource(String uri)
    {
        try
        {
        	StreamResource sr = new StreamResource(uri);
        	sr.connect();
        	final InputStream bs = sr.bufferedStream();
        	
        	return new CSSResource(sr.getFinalUri(), new InputStreamReader(bs, "UTF-8"))
        	{
        		@Override
        		public void onClose() throws IOException 
        		{
        			bs.close();
        		}
        	};
		}
        catch (UnsupportedEncodingException e) 
        {
			throw new XRRuntimeException("UTF-8 not supported", e);
		}
        catch (IOException e) 
        {
			// TODO
			throw new XRRuntimeException("I/O problem", e);
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
    public ImageResource getImageResource(String uri) {
        ImageResource ir;
        if (ImageUtil.isEmbeddedBase64Image(uri)) {
            final BufferedImage image = ImageUtil.loadEmbeddedBase64Image(uri);
            ir = createImageResource(null, image);
        } else {
            ir = _imageCache.get(uri);
            //TODO: check that cached image is still valid
            if (ir == null) {
            	StreamResource sr = new StreamResource(uri);
            	sr.connect();
            	InputStream is = null;
				try 
				{
					is = sr.bufferedStream();
					final BufferedImage img = ImageIO.read(is);

					if (img == null) 
						throw new IOException("ImageIO.read() returned null");

					ir = createImageResource(uri, img);
					_imageCache.put(uri, ir);
				}
				catch (final FileNotFoundException e)
				{
					LOGGER.error("Can't read image file; image at URI '" + uri
							+ "' not found");
				} 
				catch (final IOException e) 
				{
					LOGGER.error(
							"Can't read image file; unexpected problem for URI '"
									+ uri + "'", e);
				}
				finally {
					try {
						if (is != null)
							is.close();
					} catch (final IOException e) {
						// ignore
					}
				}
            }
            if (ir == null) {
                ir = createImageResource(uri, null);
            }
        }
        return ir;
    }

    /**
     * Factory method to generate ImageResources from a given Image. May be overridden in subclass. 
     *
     * @param uri The URI for the image, resolved to an absolute URI.
     * @param img The image to package; may be null (for example, if image could not be loaded).
     *
     * @return An ImageResource containing the image.
     */
    protected ImageResource createImageResource(final String uri, final Image img) {
        return new ImageResource(uri, AWTFSImage.createImage(img));
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
    public HTMLResource getHTMLResource(String uri) 
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
			// TODO
			throw new XRRuntimeException("I/O Problem", e);
		} finally {
            if (bs != null) {
                try {
                    bs.close();
                } catch (final IOException e) {
                    // swallow
                }
            }
        }
        return new HTMLResource(sr.getFinalUri(), xmlResource.getDocument()); 
    }

    @Override
    public byte[] getBinaryResource(String uri) {

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

            return result.toByteArray();
        } catch (final IOException e) {
            return null;
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
    public String resolveURI(String baseUri, String uri) 
    {
        if (uri == null && baseUri == null)
        	return null;

        if (baseUri == null) 
        {
        	try 
        	{
        		URI result = new URI(uri);
        		return result.normalize().toString();
        	}
        	catch (URISyntaxException e)
        	{
        		LOGGER.warn("Unable to parse URI: {}", uri, e);
        		return null;
        	}
        }
        else
        {
        	try
        	{
        		URI base = new URI(baseUri);
        		URI rel = new URI(uri);

        		URI absolute = base.resolve(rel);
        		return absolute.normalize().toString();
        	}
        	catch (URISyntaxException e)
        	{
        		LOGGER.warn("Unable to parse URI base/rel pair: {} => {}", baseUri, uri, e);
        		return null;
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

	@Override
	public StylesheetCache getStylesheetCache() {
		return _styleCache;
	}

	@Override
	public ImageResourceLoader getImageResourceCache() 
	{
		return _imageCache2;
	}

	/**
	 * Used internally when a document can't be loaded--returns XHTML as an XMLResource indicating that fact.
	 *
	 * @param uri The URI which could not be loaded.
	 *
	 * @return An XMLResource containing XML which about the failure.
	 */
	@Override
	public HTMLResource getErrorDocument(final String uri, int errorCode) 
	{
        HTMLResourceHelper xr;

        // URI may contain & symbols which can "break" the XHTML we're creating
        final String cleanUri = GeneralUtil.escapeHTML(uri);
        final String notFound = "<html><h1>Document not found</h1><h2>" + errorCode + "</h2>" + "<p>Could not access URI <pre>" + cleanUri + "</pre></p></html>";

        xr = HTMLResourceHelper.load(notFound);
        return new HTMLResource("about:error", xr.getDocument());
    }

	@Override
	public void onError(LangId msgId, int line, FSErrorType errorType, Object[] args) 
	{
		if (msgId == null)
		{
			LOGGER.error("NO error message here");
			return;
		}
		
		String msgUnformatted = messages.getString(msgId.toString());
		String msg = MessageFormat.format(msgUnformatted, args);
		
		if (errorType == FSErrorType.CSS_ERROR)
		{
			String cssUnformattedMsg = messages.getString(LangId.CSS_ERROR.toString());
			String cssMsg = MessageFormat.format(cssUnformattedMsg, line);
			LOGGER.warn(cssMsg + " " + msg);
		}
		else
		{
			LOGGER.warn(msg);
		}
	}
}
