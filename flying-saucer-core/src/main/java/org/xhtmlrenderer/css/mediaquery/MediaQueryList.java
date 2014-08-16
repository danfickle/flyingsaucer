package org.xhtmlrenderer.css.mediaquery;

import java.util.ArrayList;
import java.util.List;

import org.xhtmlrenderer.layout.SharedContext;

public class MediaQueryList
{
	// Each media query is combined together with OR semantics.
	private final List<MediaQueryItem> queryItems = new ArrayList<>(2);

	public void addMediaQueryItem(MediaQueryItem mediaQuery) 
	{
		queryItems.add(mediaQuery);
	}

	public boolean eval(SharedContext ctx) 
	{
		if (queryItems.isEmpty())
		{
			return true;
		}
		else
		{
			for (MediaQueryItem item : queryItems)
			{
				if (item.eval(ctx))
					return true;
			}

			return false;
		}
	}
	
	@Override
	public String toString() 
	{
		return queryItems.toString();
	}
}
