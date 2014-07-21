package org.xhtmlrenderer.resource;

import org.w3c.dom.Document;
import org.xhtmlrenderer.extend.UserAgentCallback;

public class ResourceLoadHelper 
{
	public static HTMLResource loadHtmlDocument(String uri, UserAgentCallback uac)
	{
		// First give the uac a chance to resolve the uri.
		String resolved = uac.resolveURI(null, uri);
		
		if (resolved != null)
		{
			// Second, try to get it from the uac cache.
			Document doc = uac.getResourceCache().getHtmlDocument(resolved);

			if (doc != null)
				return new HTMLResource(resolved, doc);
		
			// Third try to get it from the uac proper.
			HTMLResource res = uac.getHTMLResource(resolved);
			
			if (res != null)
				return res;
		}
		
		// Finally, return a 404 (not-found) error document.
		return uac.getErrorDocument(resolved, 404);
	}
}
