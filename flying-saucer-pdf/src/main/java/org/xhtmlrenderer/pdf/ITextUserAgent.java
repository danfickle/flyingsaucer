/*
 * {{{ header & license
 * Copyright (c) 2004, 2005 Torbjoern Gannholm
 * Copyright (c) 2006 Wisconsin Court System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.xhtmlrenderer.pdf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xhtmlrenderer.extend.FSErrorType;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.layout.SharedContext;
import org.xhtmlrenderer.resource.CSSResource;
import org.xhtmlrenderer.resource.HTMLResource;
import org.xhtmlrenderer.resource.ImageResource;
import org.xhtmlrenderer.resource.ResourceCache;
import org.xhtmlrenderer.swing.ImageResourceLoader;

import com.lowagie.text.BadElementException;
import com.lowagie.text.Image;
import org.xhtmlrenderer.util.ImageUtil;
import org.xhtmlrenderer.util.LangId;

public class ITextUserAgent implements UserAgentCallback {

    private static final Logger LOGGER = LoggerFactory.getLogger(ITextUserAgent.class);

    private SharedContext _sharedContext;

    private final ITextOutputDevice _outputDevice;
    private final UserAgentCallback _chainedUac;
    private final Map<String, ImageResource> _imageCache = new java.util.LinkedHashMap<String, ImageResource>(
			64 /* TODO: Configurable size. */, 0.75f, true)
	{
		private static final long serialVersionUID = 1L;

		@Override
		protected boolean removeEldestEntry(final java.util.Map.Entry<String, ImageResource> eldest) 
		{
			return size() > 64;
		}
	};
    
    public ITextUserAgent(final ITextOutputDevice outputDevice, UserAgentCallback uacInner) {
        _chainedUac = uacInner;
        _outputDevice = outputDevice;
    }

    private byte[] readStream(final InputStream is) throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream(is.available());
        final byte[] buf = new byte[10240];
        int i;
        while ((i = is.read(buf)) != -1) {
            out.write(buf, 0, i);
        }
        out.close();
        return out.toByteArray();
    }

    @Override
    public Optional<ImageResource> getImageResource(String uri) {
        ImageResource resource = null;
        if (ImageUtil.isEmbeddedBase64Image(uri)) {
            resource = loadEmbeddedBase64ImageResource(uri);
        } else {
            resource = _imageCache.get(uri);
            if (resource == null) {
            	Optional<byte[]> bytes = _chainedUac.getBinaryResource(uri);
				
            	if (!bytes.isPresent())
            		return Optional.of(new ImageResource(uri, null));
            	
            	Image image;
				try {
					image = Image.getInstance(bytes.get());
					scaleToOutputResolution(image);
					resource = new ImageResource(uri, new ITextFSImage(
							image));
					_imageCache.put(uri, resource);

				} catch (BadElementException | IOException e) {
					return Optional.of(new ImageResource(uri, null));
				}
            }
        }
        return Optional.of(resource);
    }
    
    private ImageResource loadEmbeddedBase64ImageResource(final String uri) {
        try {
            final byte[] buffer = ImageUtil.getEmbeddedBase64Image(uri);
            final Image image = Image.getInstance(buffer);
            scaleToOutputResolution(image);
            return new ImageResource(null, new ITextFSImage(image));
        } catch (final Exception e) {
            LOGGER.error("Can't read XHTML embedded image.", e);
        }
        return new ImageResource(null, null);
    }

    private void scaleToOutputResolution(final Image image) {
        final float factor = _sharedContext.getDotsPerPixel();
        if (factor != 1.0f) {
            image.scaleAbsolute(image.getPlainWidth() * factor, image.getPlainHeight() * factor);
        }
    }

    public SharedContext getSharedContext() {
        return _sharedContext;
    }

    public void setSharedContext(final SharedContext sharedContext) {
        _sharedContext = sharedContext;
    }

	@Override
	public Optional<CSSResource> getCSSResource(String uri) {
		return _chainedUac.getCSSResource(uri);
	}

	@Override
	public Optional<HTMLResource> getHTMLResource(String uri) {
		return _chainedUac.getHTMLResource(uri);
	}

	@Override
	public HTMLResource getErrorDocument(String uri, int errorCode) {
		return _chainedUac.getErrorDocument(uri, errorCode);
	}

	@Override
	public Optional<byte[]> getBinaryResource(String uri) {
		return _chainedUac.getBinaryResource(uri);
	}

	@Override
	public boolean isVisited(String uri) {
		return _chainedUac.isVisited(uri);
	}

	@Override
	public Optional<String> resolveURI(String baseUri, String uri) {
		return _chainedUac.resolveURI(baseUri, uri);
	}

	@Override
	public ImageResourceLoader getImageResourceCache() {
		return _chainedUac.getImageResourceCache();
	}

	@Override
	public void onError(LangId msgId, int line, FSErrorType errorType,
			Object[] args) {
		_chainedUac.onError(msgId, line, errorType, args);
	}

	@Override
	public ResourceCache getResourceCache() {
		return _chainedUac.getResourceCache();
	}
}
