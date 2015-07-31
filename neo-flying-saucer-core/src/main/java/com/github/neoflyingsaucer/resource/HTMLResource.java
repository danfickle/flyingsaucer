package com.github.neoflyingsaucer.resource;

import org.w3c.dom.Document;

import com.github.neoflyingsaucer.extend.useragent.HTMLResourceI;

/**
 * Use this container to return a final URI and a w3c Document
 * from the user-agent when required.
 */
public class HTMLResource implements HTMLResourceI 
{
	private final Document _doc;
	private final String _uri;
	
	/**
	 * @param uri The final URI of the document.
	 * @param doc A W3C document.
	 */
	public HTMLResource(String uri, Document doc)
	{
		_doc = doc;
		_uri = uri;
	}
	
	/* (non-Javadoc)
	 * @see com.github.neoflyingsaucer.extend.HTMLResourceI#getURI()
	 */
	@Override
	public String getURI()
	{
		return _uri;
	}

	/* (non-Javadoc)
	 * @see com.github.neoflyingsaucer.extend.HTMLResourceI#getDocument()
	 */
	@Override
	public Document getDocument()
	{
		return _doc;
	}
}
