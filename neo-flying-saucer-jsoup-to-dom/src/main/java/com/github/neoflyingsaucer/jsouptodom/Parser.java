package com.github.neoflyingsaucer.jsouptodom;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.jsoup.Jsoup;

public class Parser 
{
	private Parser() { }

	public static org.w3c.dom.Document parseHtml(String html)
	{
		org.jsoup.nodes.Document doc = Jsoup.parse(html);
		return DOMBuilder.jsoup2DOM(doc); 
	}

	public static org.w3c.dom.Document parseHtml(InputStream html) throws IOException
	{
		org.jsoup.nodes.Document doc = Jsoup.parse(html, "UTF-8", "");
		return DOMBuilder.jsoup2DOM(doc); 
	}

	public static org.w3c.dom.Document parseHtml(File html) throws IOException
	{
		org.jsoup.nodes.Document doc = Jsoup.parse(html, "UTF-8", "");
		return DOMBuilder.jsoup2DOM(doc); 
	}
}
