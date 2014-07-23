package org.xhtmlrenderer.util;

public class GenericPair<T>
{
	private final T first;
	private final T second;
	
	public T getFirst() 
	{
		return first;
	}

	public T getSecond() 
	{
		return second;
	}
	
	public GenericPair(final T first, final T second)
	{
		this.first = first;
		this.second = second;
	}
}
