package com.github.neoflyingsaucer.extend.controller.cancel;

public class FSCancelledException extends RuntimeException
{
	private static final long serialVersionUID = 1L;
	private final Class<?> sourceClass;
	
	public FSCancelledException(String msg, Class<?> sourceClass)
	{
		super(msg);
		this.sourceClass = sourceClass;
	}

	public Class<?> getSourceClass()
	{
		return sourceClass;
	}
}
