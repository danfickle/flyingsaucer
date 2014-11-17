package com.github.neoflyingsaucer.extend.useragent;

import java.io.IOException;
import java.io.Reader;

public interface CSSResourceI {

	/**
	 * Internal use only.
	 */
	public abstract String getUri();

	/**
	 * Internal use only.
	 */
	public abstract Reader getReader();

	/**
	 * If needed, you can subclass this container and put code in onClose
	 * to close any underlying streams.
	 */
	public abstract void onClose() throws IOException;

}