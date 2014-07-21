package org.xhtmlrenderer.resource;

import org.w3c.dom.Document;

/**
 * Use this container to return a final URI and a w3c Document
 * from the user-agent when required.
 */
public class HTMLResource 
{
	private final Document _doc;
	private final String _uri;
	
	/**
	 * @param uri The final URI of the document.
	 * @param doc A Jsoup document.
	 */
	public HTMLResource(String uri, Document doc)
	{
		_doc = doc;
		_uri = uri;
	}
	
	/**
	 * Internal use only.
	 */
	public String getURI()
	{
		return _uri;
	}

	/**
	 * Internal use only.
	 */
	public Document getDocument()
	{
		return _doc;
	}
}
