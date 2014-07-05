package org.xhtmlrenderer.swing;

import org.xhtmlrenderer.resource.ImageResource;

public interface ImageResourceLoader {

	public abstract void shrink();

	public abstract void clear();

	public abstract ImageResource get(String uri);

	public abstract ImageResource get(String uri, int width, int height);

	public abstract void loaded(ImageResource ir, int width, int height);

}