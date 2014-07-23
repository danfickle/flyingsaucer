package org.xhtmlrenderer.util;

public class GenericPair<T1, T2>
{
	private final T1 first;
	private final T2 second;
	
	public T1 getFirst() 
	{
		return first;
	}

	public T2 getSecond() 
	{
		return second;
	}
	
	public GenericPair(final T1 first, final T2 second)
	{
		this.first = first;
		this.second = second;
	}
}
