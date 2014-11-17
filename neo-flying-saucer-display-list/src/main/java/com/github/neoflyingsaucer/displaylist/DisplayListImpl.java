package com.github.neoflyingsaucer.displaylist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.github.neoflyingsaucer.extend.output.DisplayList;
import com.github.neoflyingsaucer.extend.output.DlItem;

public class DisplayListImpl implements DisplayList
{
	private List<DlItem> dl = new ArrayList<DlItem>(100);

	@Override
	public void add(DlItem item)
	{
		dl.add(item);
	}
	
	@Override
	public List<DlItem> getDisplayList()
	{
		return Collections.unmodifiableList(dl);
	}
	
	@Override
	public String toString()
	{
		return dl.toString();
	}
}
