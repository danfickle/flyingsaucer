package com.github.neoflyingsaucer.resource;

import org.w3c.dom.Document;

import com.github.neoflyingsaucer.extend.useragent.HTMLResourceI;
import com.github.neoflyingsaucer.extend.useragent.Optional;
import com.github.neoflyingsaucer.extend.useragent.UserAgentCallback;

public class ResourceLoadHelper 
{
	public static HTMLResourceI loadHtmlDocument(String uri, UserAgentCallback uac)
	{
		// First give the uac a chance to resolve the uri.
		Optional<String> resolved = uac.resolveURI(null, uri);
		
		if (resolved.isPresent())
		{
			// Second, try to get it from the uac cache.
			Optional<Document> doc = uac.getResourceCache().getHtmlDocument(resolved.get());

			if (doc.isPresent())
				return new HTMLResource(resolved.get(), doc.get());
		
			// Third try to get it from the uac proper.
			Optional<HTMLResourceI> res = uac.getHTMLResource(resolved.get());
			
			if (res.isPresent())
				return res.get();
		}
		
		// Finally, return a 404 (not-found) error document.
		return uac.getErrorDocument(resolved.get(), 404);
	}
}
