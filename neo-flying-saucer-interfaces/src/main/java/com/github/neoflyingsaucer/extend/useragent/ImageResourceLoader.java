package com.github.neoflyingsaucer.extend.useragent;

public interface ImageResourceLoader {

	public abstract void shrink();

	public abstract void clear();

	public abstract ImageResourceI get(String uri);

	public abstract ImageResourceI get(String uri, int width, int height);

	public abstract void loaded(ImageResourceI ir, int width, int height);

}