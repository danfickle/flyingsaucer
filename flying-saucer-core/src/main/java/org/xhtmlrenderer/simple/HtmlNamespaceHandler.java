/*
 *
 * XhtmlDocument.java
 * Copyright (c) 2004, 2005 Torbjoern Gannholm
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 */

package org.xhtmlrenderer.simple;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xhtmlrenderer.css.extend.StylesheetFactory;
import org.xhtmlrenderer.css.extend.TreeResolver;
import org.xhtmlrenderer.css.mediaquery.MediaQueryList;
import org.xhtmlrenderer.css.parser.CSSParser;
import org.xhtmlrenderer.css.sheet.Stylesheet;
import org.xhtmlrenderer.css.sheet.StylesheetInfo;
import org.xhtmlrenderer.extend.NamespaceHandler;
import org.xhtmlrenderer.util.Configuration;
import org.xhtmlrenderer.util.GeneralUtil;
import org.xhtmlrenderer.util.NodeHelper;
import org.xhtmlrenderer.util.Optional;

import static org.xhtmlrenderer.util.GeneralUtil.ciEquals;

/**
 * Handles a general HTML document
 */
public class HtmlNamespaceHandler implements NamespaceHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(HtmlNamespaceHandler.class);

    private Map<String, String> _metadata = null;
	
	@Override
	public Optional<String> getAttributeValue(final Element e, final String attrName) 
    {
        return e.hasAttribute(attrName) ? Optional.of(e.getAttribute(attrName)) : Optional.<String>empty();
    }
    
    @Override
    public Optional<String> getClass(final Element e) 
    {
        return e.hasAttribute("class") ? Optional.of(e.getAttribute("class")) : Optional.<String>empty();
    }
    
    @Override
    public Optional<String> getID(final Element e) 
    {
    	if (!e.hasAttribute("id"))
    		return Optional.empty();
    	
    	final String result = e.getAttribute("id").trim();
        return result.isEmpty() ? Optional.<String>empty() : Optional.of(result);
    }
    
    @Override
    public Optional<String> getAttributeValue(final Element e, final String namespaceURI, final String attrName) 
    {
        if (namespaceURI == TreeResolver.NO_NAMESPACE) 
        {
            return Optional.of(e.getAttribute(attrName));
        }
        else if (namespaceURI == null)
        {
            if (e.getNodeName().indexOf(':') == -1)
            {
            	// No namespace case.
            	return Optional.of(e.getAttribute(attrName));
            }
            else
            {
            	// Has namespace, try stripping the namespace from attribute
            	// names and comparing with the local part only.
            	final NamedNodeMap attrMap = e.getAttributes();
            	
            	for (int i = 0, N = attrMap.getLength(); i < N; i++) {
	            	final Attr attr = (Attr) attrMap.item(i);
	            	String key = attr.getName();
	            	
	            	if (key.indexOf(':') != -1) {
	            		key = key.substring(key.indexOf(':') + 1);
	            	}
	            	
	            	// Namspaces other than HTML may be case sensitive.
	            	if (attrName.equals(key)) {
	            		return Optional.of(attr.getValue());
	            	}
            	}
                return Optional.empty();
            }
        } 
        else 
        {
            return Optional.of(e.getAttributeNS(namespaceURI, attrName));
        }
    }

    protected Optional<StylesheetInfo> readLinkElement(final Element link)
    {
    	if (link.getAttribute("rel").toLowerCase(Locale.US).indexOf("alternate") != -1)
    		return Optional.empty();
    	
    	if (!GeneralUtil.ciEquals(link.getAttribute("type"), "text/css"))
    		return Optional.empty();
    	
    	final StylesheetInfo info = new StylesheetInfo();

        info.setType("text/css");
        info.setOrigin(StylesheetInfo.CSSOrigin.AUTHOR);
        info.setUri(link.hasAttribute("href") ? Optional.of(link.getAttribute("href")) : Optional.<String>empty());
        info.setTitle(link.getAttribute("title"));
        
        if (!link.hasAttribute("media") || link.getAttribute("media").isEmpty()) 
        {
        	info.setMediaQueryList(new MediaQueryList());
        }
        else
        {
        	info.setMediaQueryList(CSSParser.parseMediaQueryList(link.getAttribute("media")));
        }

        return Optional.of(info);
    }
    
    @Override
    public List<StylesheetInfo> getStylesheets(final Document doc) 
    {
    	// Style and link elements should only appear in the head element.
    	final Optional<Element> head = NodeHelper.getHead(doc);
    	
    	if (!head.isPresent())
    		return Collections.emptyList();
  
    	NodeList nl = head.get().getChildNodes();
    	int length = nl.getLength();
    	
    	List<StylesheetInfo> list = new ArrayList<StylesheetInfo>();
    	
    	for (int i = 0; i < length; i++)
    	{
    		Node item = nl.item(i);
    		
    		if (!(item instanceof Element))
    			continue;
    		
    		Element elem = (Element) item;
    		
    		if (elem.getNodeName().equals("link"))
    		{
    			Optional<StylesheetInfo> sheet = readLinkElement(elem);
    			
    			if (!sheet.isPresent())
    				continue;
    				
    			list.add(sheet.get());
    		}
    		else if (elem.getNodeName().equals("style"))
    		{
    			if (elem.getAttribute("alternate").equalsIgnoreCase("yes"))
    				continue;
    			
    			if (elem.hasAttribute("type") && !elem.getAttribute("type").equalsIgnoreCase("text/css"))
    				continue;

    			final StylesheetInfo info = new StylesheetInfo();
	            
    			info.setOrigin(StylesheetInfo.CSSOrigin.AUTHOR);
    			info.setType("text/css");
    			info.setUri(elem.hasAttribute("href") ? Optional.of(elem.getAttribute("href")) : Optional.<String>empty());
    			info.setTitle(elem.getAttribute("title"));
	
    			if (elem.hasAttribute("media") &&
    				!elem.getAttribute("media").isEmpty())
    				info.setMediaQueryList(CSSParser.parseMediaQueryList(elem.getAttribute("media")));
    			else
    				info.setMediaQueryList(new MediaQueryList());
	
	            // Deal with the common case first.
    			if (elem.getChildNodes().getLength() == 1 &&
    				elem.getFirstChild() instanceof CDATASection)
    			{
    				info.setContent(((CDATASection) elem.getFirstChild()).getTextContent());
    			}
    			else
    			{
    				String content = readTextContent(elem);
	
    				if (!content.isEmpty())
    					info.setContent(content);
    			}
	          
    			list.add(info);
    		}
    	}

    	return list;
    }

    @Override
    public Optional<String> getLang(final Element e) 
    {
        if (!e.hasAttribute("lang") || e.getAttribute("lang").isEmpty()) 
        {
            final String lang = this.getMetaInfo(e.getOwnerDocument()).get("Content-Language");
            return lang == null ? Optional.<String>empty() : Optional.of(lang);
        }

        return Optional.of(e.getAttribute("lang"));
    }
    
    @Override
    public boolean isImageElement(final Element e) 
    {
        return (e != null && ciEquals(e.getNodeName(), "img"));
    }
    
    @Override
    public boolean isFormElement(final Element e) 
    {
        return (e != null && ciEquals(e.getNodeName(), "form"));
    }

    @Override
    public Optional<String> getImageSourceURI(final Element e) 
    {
        return (e != null && e.hasAttribute("src") ? Optional.of(e.getAttribute("src")) : Optional.<String>empty());
    }

    @Override
    public Optional<String> getNonCssStyling(final Element e) 
    {
    	switch(e.getNodeName().toLowerCase(Locale.US))
    	{
    	case "table":
    		return Optional.of(applyTableStyles(e));
    	case "tr":
    		return Optional.of(applyTableRowStyles(e));
    	case "td": /* Fall through */
    	case "th":
    		return Optional.of(applyTableCellStyles(e));
    	case "img":
    		return Optional.of(applyImgStyles(e));
    	case "p": /* Fall through */
    	case "div":
            return Optional.of(applyTextAlign(e));
    	default:
    		return Optional.empty();
    	}
    }
    
    private String applyTextAlign(final Element e) 
    {
    	final StringBuilder style = new StringBuilder();
    	final Optional<String> s = getAttribute(e, "align");

        if (s.isPresent()) {
            String ss = s.get().toLowerCase(Locale.US).trim();
            if (ss.equals("left") || ss.equals("right") || 
                ss.equals("center") || ss.equals("justify")) {
                style.append("text-align: ");
                style.append(ss);
                style.append(";");
            }
        }
        return style.toString();
    }
    
    private String applyImgStyles(final Element e)
    {
        final StringBuilder style = new StringBuilder();
        applyFloatingAlign(e, style);
        return style.toString();
    }

    private String applyTableCellStyles(final Element e) 
    {
        final StringBuilder style = new StringBuilder();
        Optional<String> s;

        // Check for cellpadding
        final Element table = findTable(e);
        
        if (table != null) 
        {
            s = getAttribute(table, "cellpadding");
            if (s.isPresent()) 
            {
                style.append("padding: ");
                style.append(convertToLength(s.get()));
                style.append(";");
            }

            s = getAttribute(table, "border");

            if (s.isPresent() && !s.get().equals("0")) 
            {
                style.append("border: 1px outset black;");
            }
        }

        s = getAttribute(e, "width");

        if (s.isPresent()) 
        {
            style.append("width: ");
            style.append(convertToLength(s.get()));
            style.append(";");
        }
        
        s = getAttribute(e, "height");

        if (s.isPresent()) 
        {
            style.append("height: ");
            style.append(convertToLength(s.get()));
            style.append(";");
        }        

        applyAlignment(e, style);
        s = getAttribute(e, "bgcolor");

        if (s.isPresent()) 
        {
            String ss = s.get().toLowerCase(Locale.US);
            style.append("background-color: ");

            if (looksLikeAMangledColor(ss)) 
            {
                style.append('#');
                style.append(ss);
            }
            else
            {
                style.append(ss);
            }
            style.append(';');
        }

        s = getAttribute(e, "background");
        
        if (s.isPresent()) 
        {
            style.append("background-image: url(");
            style.append(s.get());
            style.append(");");
        }

        return style.toString();
    }

    private String applyTableStyles(final Element e)
    {
        final StringBuilder style = new StringBuilder();
        Optional<String> s;
        
        s = getAttribute(e, "width");

        if (s.isPresent()) 
        {
            style.append("width: ");
            style.append(convertToLength(s.get()));
            style.append(";");
        }
        
        s = getAttribute(e, "border");

        if (s.isPresent()) 
        {
            style.append("border: ");
            style.append(convertToLength(s.get()));
            style.append(" inset black;");
        }
        
        s = getAttribute(e, "cellspacing");
        
        if (s.isPresent()) 
        {
        	style.append("border-collapse: separate; border-spacing: ");
            style.append(convertToLength(s.get()));
            style.append(";");
        }
        
        s = getAttribute(e, "bgcolor");

        if (s.isPresent()) 
        {
            String ss = s.get().toLowerCase(Locale.US);
            style.append("background-color: ");
            if (looksLikeAMangledColor(ss)) 
            {
                style.append('#');
                style.append(ss);
            }
            else
            {
                style.append(ss);
            }
            style.append(';');
        }

        s = getAttribute(e, "background");

        if (s.isPresent()) 
        {
            style.append("background-image: url(");
            style.append(s.get());
            style.append(");");
        }

        applyFloatingAlign(e, style);
        return style.toString();
    }
    
    private String applyTableRowStyles(final Element e)
    {
        final StringBuilder style = new StringBuilder();
        applyAlignment(e, style);
        return style.toString();
    }
    
    private void applyFloatingAlign(final Element e, final StringBuilder style) 
    {
        Optional<String> s;
        s = getAttribute(e, "align");
        if (s.isPresent()) {
            String ss = s.get().toLowerCase(Locale.US).trim();
            if (ss.equals("left")) {
                style.append("float: left;");
            } else if (ss.equals("right")) {
                style.append("float: right;");
            } else if (ss.equals("center")) {
                style.append("margin-left: auto; margin-right: auto;");
            }
        }
    }
    
    private void applyAlignment(final Element e, final StringBuilder style) 
    {
        Optional<String> s;
        s = getAttribute(e, "align");
        if (s.isPresent()) {
            style.append("text-align: ");
            style.append(s.get().toLowerCase(Locale.US));
            style.append(";");
        }
        s = getAttribute(e, "valign");
        if (s.isPresent()) {
            style.append("vertical-align: ");
            style.append(s.get().toLowerCase(Locale.US));
            style.append(";");
        }
    }
    
    private boolean looksLikeAMangledColor(final String s) 
    {
        if (s.length() != 6) {
            return false;
        }

        char[] chars = s.toCharArray();
        
        for (int i = 0; i < chars.length; i++)
        {
        	char c = chars[i];
        	
        	if (!((c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F')))
        		return false;
        }
        
        return true;
    }
    
    private Element findTable(final Element cell) 
    {
        Node n = cell.getParentNode();
        Element next;

        if (n instanceof Element) {
            next = (Element) n;
            if (ciEquals(next.getNodeName(), "tr")) 
            {
                n = next.getParentNode();
                if (n instanceof Element) {
                    next = (Element) n;
                    final String name = next.getNodeName();
                    if (ciEquals(name, "table")) {
                        return next;
                    }
                    
                    if (ciEquals(name, "tbody") || ciEquals(name, "tfoot") || ciEquals(name, "thead")) 
                    {
                        n = next.getParentNode();
                        if (n instanceof Element) {
                            next = (Element) n;
                            if (ciEquals(next.getNodeName(), "table")) {
                                return next;
                            }
                        }
                    }
                }
            }
        }
        
        return null;
    }
    
    protected String convertToLength(final String value)
    {
        if (isInteger(value)) {
            return value + "px";
        } else {
            return value;
        }
    }

    protected boolean isInteger(final String value) 
    {
    	char[] chars = value.toCharArray();
    	
    	for (int i = 0; i < chars.length; i++)
    	{
    		char c = chars[i];
    		
    		if (!(c >= '0' && c <= '9'))
    			return false;
    	}
    	
    	return true;
    }

    protected Optional<String> getAttribute(final Element e, final String attrName)
    {
        String result = e.getAttribute(attrName);
        result = result.trim();
        return result.isEmpty() ? Optional.<String>empty() : Optional.of(result);
    }

    @Override
    public Optional<String> getElementStyling(final Element e)
    {
        final StringBuilder style = new StringBuilder();
        if (ciEquals(e.getNodeName(), "td") || ciEquals(e.getNodeName(), "th")) {
            Optional<String> s;
            s = getAttribute(e, "colspan");
            if (s.isPresent()) {
                style.append("-fs-table-cell-colspan: ");
                style.append(s.get());
                style.append(";");
            }
            s = getAttribute(e, "rowspan");
            if (s.isPresent()) {
                style.append("-fs-table-cell-rowspan: ");
                style.append(s.get());
                style.append(";");
            }
        } else if (ciEquals(e.getNodeName(), "img")) {
            Optional<String> s;
            s = getAttribute(e, "width");
            if (s.isPresent()) {
                style.append("width: ");
                style.append(convertToLength(s.get()));
                style.append(";");
            }
            s = getAttribute(e, "height");
            if (s.isPresent()) {
                style.append("height: ");
                style.append(convertToLength(s.get()));
                style.append(";");
            }
        } else if (ciEquals(e.getNodeName(), "colgroup") || ciEquals(e.getNodeName(), "col")) {
            Optional<String> s;
            s = getAttribute(e, "span");
            if (s.isPresent()) {
                style.append("-fs-table-cell-colspan: ");
                style.append(s.get());
                style.append(";");
            }
            s = getAttribute(e, "width");
            if (s.isPresent()) {
                style.append("width: ");
                style.append(convertToLength(s.get()));
                style.append(";");
            }
        }

        style.append(e.getAttribute("style"));
        return Optional.of(style.toString());
    }

    @Override
    public Optional<String> getLinkUri(final Element e) 
    {
        if (ciEquals(e.getNodeName(), "a") && e.hasAttribute("href")) 
        	return Optional.of(e.getAttribute("href"));

        return Optional.empty();
    }

    @Override
    public Optional<String> getAnchorName(final Element e)
    {
        if (e != null && ciEquals(e.getNodeName(), "a") && e.hasAttribute("name")) 
            return Optional.of(e.getAttribute("name"));

        return Optional.empty();
    }

    private static String readTextContent(final Element element) 
    {
    	StringBuilder sb = new StringBuilder();
    	NodeList nl = element.getChildNodes();
    	int length = nl.getLength();

    	for (int i = 0; i < length; i++)
    	{
    		Node e = nl.item(i);
    		
    		if (e instanceof Text)
    		{
    			sb.append(((Text) e).getWholeText());
    		}
    		else if (e instanceof CDATASection)
    		{
    			sb.append(((CDATASection) e).getTextContent());
    		}
    	}
    	
    	return sb.toString();
    }

    private static String collapseWhiteSpace(final String text)
    {
        final StringBuilder result = new StringBuilder();
        final int l = text.length();
        for (int i = 0; i < l; i++) {
            char c = text.charAt(i);
            if (Character.isWhitespace(c)) {
                result.append(' ');
                while (++i < l) {
                    c = text.charAt(i);
                    if (! Character.isWhitespace(c)) {
                        i--;
                        break;
                    }
                }
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    /**
     * Returns the title of the document as located in the contents
     * of /html/head/title, or "" if none could be found.
     */
    @Override
    public Optional<String> getDocumentTitle(final Document doc) 
    {
        final Optional<Element> head = NodeHelper.getHead(doc);

        if (head.isPresent())
        {
        	Optional<Element> title = findFirstChild(head.get(), "title");

        	if (title.isPresent())
        	{
        		return Optional.of(collapseWhiteSpace(readTextContent(title.get()).trim()));
        	}
        }

        return Optional.empty();
    }

    private Optional<Element> findFirstChild(final Element parent, final String targetName)
    {
    	return NodeHelper.getFirstMatchingChildByTagName(parent, targetName);
    }

    @Override
    public StylesheetInfo getDefaultStylesheet(final StylesheetFactory factory) 
    {
		final StylesheetInfo info = new StylesheetInfo();
		info.setOrigin(StylesheetInfo.CSSOrigin.USER_AGENT);
		info.setMediaQueryList(null);
		info.setType("text/css");
		info.setUri(Optional.of("about:defaultstylesheet"));

		InputStream is = null;
		try {
			is = getDefaultStylesheetStream();

			if (is == null)
				return null;
			
			final Optional<Stylesheet> sheet = factory.parse(new InputStreamReader(is), info, false);

			if (sheet.isPresent())
				info.setStylesheet(sheet.get());

		} catch (final Exception e) {
			LOGGER.error("Could not parse default stylesheet", e);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (final IOException e) {
					// ignore
				}
			}
		}

		return info;
    }

    private InputStream getDefaultStylesheetStream() 
    {
        InputStream stream = null;
        final String defaultStyleSheet = Configuration.valueFor("xr.css.user-agent-default-css") + "XhtmlNamespaceHandler.css";
        stream = this.getClass().getResourceAsStream(defaultStyleSheet);

        if (stream == null)
        {
            LOGGER.error("Can't load default CSS from " + defaultStyleSheet + "." +
                    "This file must be on your CLASSPATH. Please check before continuing.");
        }

        return stream;
    }

    private Map<String, String> getMetaInfo(final Document doc)
    {
        if(this._metadata != null) 
            return this._metadata;

        final Optional<Element> ohead = NodeHelper.getHead(doc);

        if (!ohead.isPresent())
        {
        	this._metadata = Collections.emptyMap();
        	return this._metadata;
        }
        	
        NodeList nl = ohead.get().getChildNodes();
        int length = nl.getLength();

        this._metadata = new HashMap<String, String>();
        
        for (int i = 0; i < length; i++)
        {
        	Node item = nl.item(i);
        	
        	if (item instanceof Element &&
        		item.getNodeName().equals("meta"))
        	{
        		Element elem = (Element) item;
        		
        		String equiv = elem.getAttribute("http-equiv");
        		String content = elem.getAttribute("content");
        		
        		if (!equiv.isEmpty() && !content.isEmpty())
        		{
        			this._metadata.put(equiv, content);
        		}
        	}
        }

        return this._metadata;
    }
}
