package com.github.neoflyingsaucer.extend.useragent;

import com.github.neoflyingsaucer.extend.output.FSImage;

public interface ImageResourceI {

	public abstract FSImage getImage();

	public abstract String getImageUri();

	public abstract boolean hasDimensions(int width, int height);

}