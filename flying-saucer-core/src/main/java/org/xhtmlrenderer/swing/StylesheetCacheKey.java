package org.xhtmlrenderer.swing;

public class StylesheetCacheKey
{
	private final String uri;
	private final int targetWidth;
	private final int targetHeight;
	private final String media;
	
	public StylesheetCacheKey(String uri, int targetWidth, int targetHeight, String media) 
	{
		this.uri = uri;
		this.targetWidth = targetWidth;
		this.targetHeight = targetHeight;
		this.media = media;
	}

	@Override
	public int hashCode() 
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((media == null) ? 0 : media.hashCode());
		result = prime * result + targetHeight;
		result = prime * result + targetWidth;
		result = prime * result + ((uri == null) ? 0 : uri.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) 
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StylesheetCacheKey other = (StylesheetCacheKey) obj;
		if (media == null) {
			if (other.media != null)
				return false;
		} else if (!media.equals(other.media))
			return false;
		if (targetHeight != other.targetHeight)
			return false;
		if (targetWidth != other.targetWidth)
			return false;
		if (uri == null) {
			if (other.uri != null)
				return false;
		} else if (!uri.equals(other.uri))
			return false;
		return true;
	}
	
	@Override
	public String toString() 
	{
		return uri + ":" + targetWidth + ":" + targetHeight + (media == null ? "" : media);
	}
}
