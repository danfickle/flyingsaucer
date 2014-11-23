package com.github.neoflyingsaucer.pdfout;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xhtmlrenderer.layout.SharedContext;
import com.github.neoflyingsaucer.extend.useragent.CSSResourceI;
import com.github.neoflyingsaucer.extend.useragent.FSErrorType;
import com.github.neoflyingsaucer.extend.useragent.HTMLResourceI;
import com.github.neoflyingsaucer.extend.useragent.ImageResourceI;
import com.github.neoflyingsaucer.extend.useragent.LangId;
import com.github.neoflyingsaucer.extend.useragent.Optional;
import com.github.neoflyingsaucer.extend.useragent.ResourceCache;
import com.github.neoflyingsaucer.extend.useragent.UserAgentCallback;

public class PdfOutUserAgent implements UserAgentCallback
{
	private final static Logger LOGGER = LoggerFactory.getLogger(PdfOutUserAgent.class);
	
	private final UserAgentCallback _inner;
	private SharedContext _sharedContext;
	
	public PdfOutUserAgent(UserAgentCallback inner)
	{
		_inner = inner;
	}
	
	@Override
	public Optional<CSSResourceI> getCSSResource(String uri) {
		return _inner.getCSSResource(uri);
	}

	@Override
	public Optional<ImageResourceI> getImageResource(String uri)
	{
		return _inner.getImageResource(uri);
		
		//img.scaleToOutputResolution(_sharedContext);
	}
	
	public void setSharedContext(SharedContext ctx)
	{
		_sharedContext = ctx;
	}

	@Override
	public Optional<HTMLResourceI> getHTMLResource(String uri) {
		return _inner.getHTMLResource(uri);
	}

	@Override
	public HTMLResourceI getErrorDocument(String uri, int errorCode) {
		return _inner.getErrorDocument(uri, errorCode);
	}

	@Override
	public Optional<byte[]> getBinaryResource(String uri) {
		return _inner.getBinaryResource(uri);
	}

	@Override
	public boolean isVisited(String uri) {
		return _inner.isVisited(uri);
	}

	@Override
	public Optional<String> resolveURI(String baseUri, String uri) {
		return _inner.resolveURI(baseUri, uri);
	}

	@Override
	public void onError(LangId msgId, int line, FSErrorType errorType,
			Object[] args) {
		_inner.onError(msgId, line, errorType, args);
	}

	@Override
	public ResourceCache getResourceCache() {
		return _inner.getResourceCache();
	}
}
