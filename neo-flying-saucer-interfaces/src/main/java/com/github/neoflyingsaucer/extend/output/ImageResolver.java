package com.github.neoflyingsaucer.extend.output;

import java.io.InputStream;

public interface ImageResolver 
{
	public FSImage resolveImage(String uri, InputStream strm);
}
