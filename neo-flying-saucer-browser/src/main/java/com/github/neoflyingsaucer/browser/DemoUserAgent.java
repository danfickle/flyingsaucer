package com.github.neoflyingsaucer.browser;

import java.net.URL;

import com.github.neoflyingsaucer.defaultuseragent.DefaultUserAgent;
import com.github.neoflyingsaucer.extend.useragent.Optional;

public class DemoUserAgent extends DefaultUserAgent
{
	/**
     * This is a sample of converting a private URI namespace to
     * a public one: demo: to jar:.
     */
    @Override
    public Optional<String> resolveURI(String baseUri, String uriRel) {

    	Optional<String> oResolvedUri = super.resolveURI(baseUri, uriRel);
    	
    	if (!oResolvedUri.isPresent())
    	{
    		return Optional.empty();
    	}
    	String resolvedUri = oResolvedUri.get();
    	
    	if (resolvedUri.startsWith("demo:")) 
        {
            Object marker = this;
            String shortUrl = resolvedUri.substring(5);
            if (!shortUrl.startsWith("/")) {
                shortUrl = "/" + shortUrl;
            }
            URL ref = marker.getClass().getResource(shortUrl);
            return Optional.ofNullable(ref == null ? null : ref.toString());
        }
        else if (resolvedUri.startsWith("demoNav:")) 
        {
            Object marker = this;
            String shortUrl = resolvedUri.substring(8);
            if (!shortUrl.startsWith("/")) {
                shortUrl = "/" + shortUrl;
            }
            URL ref = marker.getClass().getResource(shortUrl);
            return Optional.ofNullable(ref == null ? null : ref.toString());
        }
        else
        {
        	return oResolvedUri;
        }
    }
}
