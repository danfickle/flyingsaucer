package com.github.neoflyingsaucer.defaultuseragent;

import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xhtmlrenderer.resource.ImageResource;
import org.xhtmlrenderer.swing.AWTFSImage;
import org.xhtmlrenderer.util.ImageUtil;

import com.github.neoflyingsaucer.extend.output.FSImage;
import com.github.neoflyingsaucer.extend.useragent.ImageResourceI;

/**
 *
 */
public class ImageResourceLoaderImpl implements com.github.neoflyingsaucer.extend.useragent.ImageResourceLoader 
{

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageResourceLoaderImpl.class);
    private final Map<CacheKey, ImageResourceI> _imageCache;

    private final int _imageCacheCapacity;

    public ImageResourceLoaderImpl() {
        // FIXME
        this(16);
    }

    public ImageResourceLoaderImpl(final int cacheSize) {
        this._imageCacheCapacity = cacheSize;

        // note we do *not* override removeEldestEntry() here--users of this class must call shrinkImageCache().
        // that's because we don't know when is a good time to flush the cache
        this._imageCache = new LinkedHashMap<CacheKey, ImageResourceI>(cacheSize, 0.75f, true);
    }

    public static ImageResource loadImageResourceFromUri(final String uri) {
        if (ImageUtil.isEmbeddedBase64Image(uri)) {
            return loadEmbeddedBase64ImageResource(uri);
        } else {
            final StreamResource sr = new StreamResource(uri);
            InputStream is;
            ImageResource ir = null;
            try {
                sr.connect();
                is = sr.bufferedStream();
                try {
                    final BufferedImage img = ImageIO.read(is);
                    if (img == null) {
                        throw new IOException("ImageIO.read() returned null");
                    }
                    ir = createImageResource(uri, img);
                } catch (final FileNotFoundException e) {
                    LOGGER.error("Can't read image file; image at URI '" + uri + "' not found");
                } catch (final IOException e) {
                    LOGGER.error("Can't read image file; unexpected problem for URI '" + uri + "'", e);
                } finally {
                    sr.close();
                }
            } catch (final IOException e) {
                // couldnt open stream at URI...
                LOGGER.error("Can't open stream for URI '" + uri + "': " + e.getMessage());
            }
            if (ir == null) {
                ir = createImageResource(uri, null);
            }
            return ir;
        }
    }
    
    public static ImageResource loadEmbeddedBase64ImageResource(final String uri) {
        final BufferedImage bufferedImage = ImageUtil.loadEmbeddedBase64Image(uri);
        if (bufferedImage != null) {
            final FSImage image = AWTFSImage.createImage(bufferedImage);
            return new ImageResource(null, image);
        } else {
            return new ImageResource(null, null);
        }
    }

    /* (non-Javadoc)
	 * @see com.github.neoflyingsaucer.defaultuseragent.ImageResourceLoaderI#shrink()
	 */
    @Override
	public synchronized void shrink() {
        int ovr = _imageCache.size() - _imageCacheCapacity;
        final Iterator<CacheKey> it = _imageCache.keySet().iterator();
        while (it.hasNext() && ovr-- > 0) {
            it.next();
            it.remove();
        }
    }

    /* (non-Javadoc)
	 * @see com.github.neoflyingsaucer.defaultuseragent.ImageResourceLoaderI#clear()
	 */
    @Override
	public synchronized void clear() {
        _imageCache.clear();
    }

    /* (non-Javadoc)
	 * @see com.github.neoflyingsaucer.defaultuseragent.ImageResourceLoaderI#get(java.lang.String)
	 */
    @Override
	public ImageResourceI get(final String uri) {
        return get(uri, -1, -1);
    }

    /* (non-Javadoc)
	 * @see com.github.neoflyingsaucer.defaultuseragent.ImageResourceLoaderI#get(java.lang.String, int, int)
	 */
    @Override
	public ImageResourceI get(final String uri, final int width, final int height) 
    {
    	if (ImageUtil.isEmbeddedBase64Image(uri)) {
            final ImageResourceI resource = loadEmbeddedBase64ImageResource(uri);
            resource.getImage().scale(width, height);
            return resource;
        } else {
            final CacheKey key = new CacheKey(uri, width, height);
            ImageResourceI ir = _imageCache.get(key);
            if (ir == null) {
                // not loaded, or not loaded at target size

                // loaded a base size?
                ir = _imageCache.get(new CacheKey(uri, -1, -1));

                // no: loaded
                if (ir == null) {
                   LOGGER.debug("Load immediate: " + uri);
                   ir = loadImageResourceFromUri(uri);
                   final FSImage awtfsImage = ir.getImage();
                   BufferedImage newImg = ((AWTFSImage) awtfsImage).getImage();
                   loaded(ir, -1, -1);
                   if (width > -1 && height > -1) {
                       LOGGER.debug(this + ", scaling " + uri + " to " + width + ", " + height);
                       newImg = ImageUtil.getScaledInstance(newImg, width, height);
                       ir = new ImageResource(ir.getImageUri(), AWTFSImage.createImage(newImg));
                       loaded(ir, width, height);
                   }
                   _imageCache.put(key, ir);
                } else {
                    // loaded at base size, need to scale
                    LOGGER.debug(this + ", scaling " + uri + " to " + width + ", " + height);

                    if (width > -1 && height > -1) 
                    {
                    	final FSImage awtfsImage = ir.getImage();
                    	BufferedImage newImg = ((AWTFSImage) awtfsImage).getImage();

                    	newImg = ImageUtil.getScaledInstance(newImg, width, height);
                    	ir = new ImageResource(ir.getImageUri(), AWTFSImage.createImage(newImg));
                    	loaded(ir, width, height);
                    }
                }
            }
            return ir;
        }
    }

    /* (non-Javadoc)
	 * @see com.github.neoflyingsaucer.defaultuseragent.ImageResourceLoaderI#loaded(org.xhtmlrenderer.resource.ImageResource, int, int)
	 */
    @Override
	public void loaded(final ImageResourceI ir, final int width, final int height) {
        final String imageUri = ir.getImageUri();
        if (imageUri != null) {
            _imageCache.put(new CacheKey(imageUri, width, height), ir);
        }
    }

    public static ImageResource createImageResource(final String uri, final BufferedImage img) {
        if (img == null) {
            return new ImageResource(uri, AWTFSImage.createImage(ImageUtil.createTransparentImage(10, 10)));
        } else {
            return new ImageResource(uri, AWTFSImage.createImage(ImageUtil.makeCompatible(img)));
        }
    }

    private static class CacheKey {
        final String uri;
        final int width;
        final int height;

        public CacheKey(final String uri, final int width, final int height) {
            this.uri = uri;
            this.width = width;
            this.height = height;
        }

        public boolean equals(final Object o) {
            if (this == o) return true;
            if (!(o instanceof CacheKey)) return false;

            final CacheKey cacheKey = (CacheKey) o;

            if (height != cacheKey.height) return false;
            if (width != cacheKey.width) return false;
            if (!uri.equals(cacheKey.uri)) return false;

            return true;
        }

        public int hashCode() {
            int result = uri.hashCode();
            result = 31 * result + width;
            result = 31 * result + height;
            return result;
        }
    }
}

// from-io-loader
// from-cache-loader
// from-fs-loader
