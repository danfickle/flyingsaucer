package com.github.neoflyingsaucer.defaultuseragent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.net.MalformedURLException;
import java.io.File;

public class UriResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(UriResolver.class);
    private String _baseUri;

    public String resolve(final String uri) {
        if (uri == null) return null;
        String ret = null;
        if (_baseUri == null) {//first try to set a base URL
            try {
                final URL result = new URL(uri);
                setBaseUri(result.toExternalForm());
            } catch (final MalformedURLException e) {
                try {
                    setBaseUri(new File(".").toURI().toURL().toExternalForm());
                } catch (final Exception e1) {
                    LOGGER.error("The default NaiveUserAgent doesn't know how to resolve the base URL for " + uri);
                    return null;
                }
            }
        }
        // test if the URI is valid; if not, try to assign the base url as its parent
        try {
            return new URL(uri).toString();
        } catch (final MalformedURLException e) {
            LOGGER.debug("Could not read " + uri + " as a URL; may be relative. Testing using parent URL " + _baseUri);
            try {
                final URL result = new URL(new URL(_baseUri), uri);
                ret = result.toString();
                LOGGER.debug("Was able to read from " + uri + " using parent URL " + _baseUri);
            } catch (final MalformedURLException e1) {
                LOGGER.error("The default NaiveUserAgent cannot resolve the URL " + uri + " with base URL " + _baseUri);
            }
        }
        return ret;

    }

    public void setBaseUri(final String baseUri) {
        _baseUri = baseUri;
    }

    public String getBaseUri() {
        return _baseUri;
    }
}
