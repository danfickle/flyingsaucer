package com.github.neoflyingsaucer.extend.output;

import org.w3c.dom.Element;

import com.github.neoflyingsaucer.extend.useragent.UserAgentCallback;

public interface ReplacedElementResolver 
{
	public ReplacedElement createReplacedElement(Element e, String baseUri, UserAgentCallback uac, ImageResolver imgResolver, float cssWidth, float cssHeight);
	public void reset();
}
