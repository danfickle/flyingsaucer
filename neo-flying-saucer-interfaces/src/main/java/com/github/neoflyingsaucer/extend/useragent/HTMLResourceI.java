package com.github.neoflyingsaucer.extend.useragent;

import org.w3c.dom.Document;

public interface HTMLResourceI {

	/**
	 * Internal use only.
	 */
	public abstract String getURI();

	/**
	 * Internal use only.
	 */
	public abstract Document getDocument();

}