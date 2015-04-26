package com.github.neoflyingsaucer.extend.controller.cancel;

public class FSDefaultCancelHandler implements FSCancelHandler 
{
	@Override
	public void cancelOpportunity(Class<?> sourceClass)
	{
		if (Thread.currentThread().isInterrupted())
			throw new FSCancelledException("Thread interrupted", sourceClass);
	}
}
