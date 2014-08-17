package org.xhtmlrenderer.util;

public class Optional<T> 
{
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static final Optional EMPTY = new Optional(null);
	private final T inner;
	
	private Optional(T inner)
	{
		this.inner = inner;
	}
	
	@SuppressWarnings("unchecked")
	public static <R> Optional<R> empty()
	{
		return EMPTY;
	}
	
	public static <R> Optional<R> of(R inner)
	{
		if (inner == null)
			throw new NullPointerException();
		
		return new Optional<R>(inner);
	}
	
	public static <R> Optional<R> ofNullable(R inner)
	{
		if (inner == null)
			return empty();
				
		return new Optional<R>(inner);
	}	
	
	public boolean isPresent()
	{
		return this != EMPTY;
	}

	public T get()
	{
		if (!isPresent())
			throw new NullPointerException();
		
		return this.inner;
	}

	@Override
	public int hashCode() 
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((inner == null) ? 0 : inner.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		@SuppressWarnings("rawtypes")
		Optional other = (Optional) obj;
		if (inner == null) {
			if (other.inner != null)
				return false;
		} else if (!inner.equals(other.inner))
			return false;
		return true;
	}

	public T orElse(T object) 
	{
		if (isPresent())
			return get();
		else
			return object;
	}
}
