package com.github.neoflyingsaucer.extend.controller.cancel;

public class FSTimedCancelHandler implements FSCancelHandler 
{
	private final long endTime;
	
	public FSTimedCancelHandler(int timeOutMs) 
	{
		endTime = System.currentTimeMillis() + timeOutMs;
	}
	
	@Override
	public void cancelOpportunity(Class<?> sourceClass)
	{
		if (System.currentTimeMillis() > endTime)
			throw new FSCancelledException("Thread interrupted", sourceClass);
	}
}
