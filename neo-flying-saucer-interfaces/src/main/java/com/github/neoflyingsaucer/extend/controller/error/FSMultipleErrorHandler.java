package com.github.neoflyingsaucer.extend.controller.error;

/**
 * Use this class to combine multiple error handlers. For example the List error handler
 * and default (logging) handler.
 */
public class FSMultipleErrorHandler implements FSErrorHandler
{
	private final FSErrorHandler[] handlers;
	
	public FSMultipleErrorHandler(FSErrorHandler[] handlers)
	{
		this.handlers = handlers;
	}
	
	@Override
	public void onError(FSError error) 
	{
		for (FSErrorHandler handler : handlers)
		{
			handler.onError(error);
		}
	}
}
