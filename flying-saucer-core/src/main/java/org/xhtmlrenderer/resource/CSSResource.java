package org.xhtmlrenderer.resource;

import java.io.IOException;
import java.io.Reader;

/**
 * Use this container to return a CSSResource from the user-agent
 * consisting of a uri and a reader with the CSS contents.
 */
public class CSSResource
{
	private final String _uri;
	private final Reader _reader;
	
	/**
	 * @param uri Final URI for the resource.
	 * @param reader Contents of the CSS as a java.io.Reader.
	 */
	public CSSResource(String uri, Reader reader)
	{
		_uri = uri;
		_reader = reader;
	}
	
	/**
	 * Internal use only.
	 */
	public String getUri()
	{
		return _uri;
	}

	/**
	 * Internal use only.
	 */
	public Reader getReader()
	{
		return _reader;
	}
	
	/**
	 * If needed, you can subclass this container and put code in onClose
	 * to close any underlying streams.
	 */
	public void onClose() throws IOException { }
}
