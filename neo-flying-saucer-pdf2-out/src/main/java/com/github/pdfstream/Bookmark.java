package com.github.pdfstream;

public class Bookmark 
{
	final Destination dest;
	final String name;
	final int level;
	
	public Bookmark(Destination dest, String name, int level)
	{
		this.dest = dest;
		this.name = name;
		this.level = level;
	}
	
	Bookmark parent;
	Bookmark prev;
	Bookmark next;
	Bookmark first;
	Bookmark last;
	int count;
	int offsetObjNumber;

}
