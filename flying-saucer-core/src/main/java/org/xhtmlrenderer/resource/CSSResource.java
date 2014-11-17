package org.xhtmlrenderer.resource;

import java.io.IOException;
import java.io.Reader;

import com.github.neoflyingsaucer.extend.useragent.CSSResourceI;

/**
 * Use this container to return a CSSResource from the user-agent
 * consisting of a uri and a reader with the CSS contents.
 */
public class CSSResource implements CSSResourceI
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
	
	/* (non-Javadoc)
	 * @see com.github.neoflyingsaucer.extend.CSSResourceI#getUri()
	 */
	@Override
	public String getUri()
	{
		return _uri;
	}

	/* (non-Javadoc)
	 * @see com.github.neoflyingsaucer.extend.CSSResourceI#getReader()
	 */
	@Override
	public Reader getReader()
	{
		return _reader;
	}
	
	/* (non-Javadoc)
	 * @see com.github.neoflyingsaucer.extend.CSSResourceI#onClose()
	 */
	@Override
	public void onClose() throws IOException { }
}
