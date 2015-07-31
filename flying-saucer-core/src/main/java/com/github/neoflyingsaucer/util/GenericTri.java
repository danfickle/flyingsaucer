package com.github.neoflyingsaucer.util;

public class GenericTri<T1, T2, T3> 
{
	private final T1 first;
	private final T2 second;
	private final T3 third;
	
	public T1 getFirst() 
	{
		return first;
	}

	public T2 getSecond() 
	{
		return second;
	}

	public T3 getThird()
	{
		return third;
	}
	
	public GenericTri(final T1 first, final T2 second, final T3 third)
	{
		this.first = first;
		this.second = second;
		this.third = third;
	}
}
