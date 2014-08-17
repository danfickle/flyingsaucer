package com.github.neoflyingsaucer.pdfout;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xhtmlrenderer.extend.FSErrorType;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.resource.CSSResource;
import org.xhtmlrenderer.resource.HTMLResource;
import org.xhtmlrenderer.resource.ImageResource;
import org.xhtmlrenderer.resource.ResourceCache;
import org.xhtmlrenderer.swing.ImageResourceLoader;
import org.xhtmlrenderer.util.LangId;
import org.xhtmlrenderer.util.Optional;

public class PdfOutUserAgent implements UserAgentCallback
{
	private final static Logger LOGGER = LoggerFactory.getLogger(PdfOutUserAgent.class);
	
	private final UserAgentCallback _inner;
	
	public PdfOutUserAgent(UserAgentCallback inner)
	{
		_inner = inner;
	}
	
	@Override
	public Optional<CSSResource> getCSSResource(String uri) {
		return _inner.getCSSResource(uri);
	}

	@Override
	public Optional<ImageResource> getImageResource(String uri)
	{
		Optional<byte[]> bytes = getBinaryResource(uri);
		
		if (!bytes.isPresent())
			return Optional.empty();

		PdfOutImage img;
		try {
			img = new PdfOutImage(bytes.get(), uri);
		} catch (IOException e) {
			LOGGER.warn("Unable to decode image at uri({})", uri);
			return Optional.empty();
		}
		
		return Optional.of(new ImageResource(uri, img));
	}

	@Override
	public Optional<HTMLResource> getHTMLResource(String uri) {
		return _inner.getHTMLResource(uri);
	}

	@Override
	public HTMLResource getErrorDocument(String uri, int errorCode) {
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
	public ImageResourceLoader getImageResourceCache() {
		return _inner.getImageResourceCache();
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
