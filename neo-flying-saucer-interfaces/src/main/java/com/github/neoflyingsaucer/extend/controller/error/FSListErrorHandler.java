package com.github.neoflyingsaucer.extend.controller.error;

import java.util.ArrayList;
import java.util.List;

/**
 * Use this class when you want to get emitted errors into a list.
 * For example, to present to a tenant or template author
 * in a multi-tenanted environment.
 */
public class FSListErrorHandler implements FSErrorHandler 
{
	private final List<FSError> list = new ArrayList<FSError>();
	
	@Override
	public void onError(FSError error) 
	{
		list.add(error);
	}

	public List<FSError> getErrorList()
	{
		return list;
	}
}
