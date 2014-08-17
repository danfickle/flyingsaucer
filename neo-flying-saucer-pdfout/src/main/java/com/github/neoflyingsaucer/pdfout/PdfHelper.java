
package com.github.neoflyingsaucer.pdfout;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xhtmlrenderer.util.NodeHelper;

import com.github.pdfstream.PDF;

public class PdfHelper 
{
    static class Metadata 
    {
        private final String _name;
        private final String _content;

        public Metadata(String name, String content) 
        {
            _name = name;
            _content = content;
        }

        public String getContent() {
            return _content;
        }

        public String getName() {
            return _name;
        }
    }
    
    private final List<Metadata> _metadata = new ArrayList<Metadata>();
    
    /**
     * Sets the document information dictionary values
     * from html metadata
     */
    public void setDidValues(final PDF doc) 
    {
        String v = getMetadataByName("title");
        if (v != null) {
            doc.setTitle(v);
        }

        v = getMetadataByName("author");
        if (v != null) {
            doc.setAuthor(v);
        }
        
        v = getMetadataByName("subject");
        if (v != null) {
            doc.setSubject(v);
        }
        
        v = getMetadataByName("keywords");
        if (v != null) {
            doc.setKeywords(v);
        }
    }
    
    /**
     * Loads meta information from the document head section.
     */
	public void loadMetadata(Document doc)
	{
        final Optional<Element> head = NodeHelper.getHead(doc);

        if (head != null) 
        {
        	NodeList nl = head.get().getChildNodes();
        	int length = nl.getLength();
        	
        	for (int i = 0; i < length; i++)
        	{
        		Node n = nl.item(i);
        		
        		if (!(n instanceof Element) ||
        			!n.getNodeName().equals("meta"))
        			continue;
        		
        		Element e = (Element) n;
        		
        		if (!e.hasAttribute("name") || !e.hasAttribute("content"))
        			continue;
        		
        		_metadata.add(new Metadata(e.getAttribute("name"), e.getAttribute("content")));
        	}
        	
            String title = getMetadataByName("title");

            // If there is no title meta data attribute,
        	// use the document title.
            if (title == null) 
            {
            	Optional<Element> tt = NodeHelper.getFirstMatchingChildByTagName(head.get(), "title");
            	
            	if (tt.isPresent())
            	{
            		String newTitle = tt.get().getTextContent().trim();
            		_metadata.add(new Metadata("title", newTitle));
            	}
            }
        }
	}
    
    /**
     * Searches the metadata name/content pairs of the current document and
     * returns the content value from the first pair with a matching name. The
     * search is case insensitive.
     *
     * @param name
     *            the metadata element name to locate.
     * @return the content value of the first found metadata element; otherwise
     *         null.
     */
    public String getMetadataByName(final String name) 
    {
    	if (name == null)
    		return null;
    	
        for (Metadata m : _metadata) 
        {
        	if (m.getName().equalsIgnoreCase(name)) 
        		return m.getContent();
        }

        return null;
    }
}
