package org.xhtmlrenderer.util;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

public class NodeHelper {
	
  public static boolean isRootNode(final Node n) {
    return n.getParentNode() instanceof Document;
  }

  public static Element getFirstMatchingDeepChildByTagName(final Element e, final String tagName) {
    NodeList matchingElements = e.getElementsByTagName(tagName);
    if (matchingElements != null) {
      return (Element) matchingElements.item(0);
    } else {
      return null;
    }
  }

  public static Optional<Element> getFirstMatchingChildByTagName(final Element parent, final String tagName) 
  {
  	NodeList nl = parent.getChildNodes();
  	int length = nl.getLength();
  	
  	for (int i = 0; i < length; i++)
  	{
  		Node item = nl.item(i);
  		
  		if (item instanceof Element &&
  			item.getNodeName().equals(tagName))
  			return Optional.of((Element) item);
  	}
  	
  	return Optional.empty();
  }

  public static Optional<Element> getHead(final Document doc) 
  {
    return NodeHelper.getFirstMatchingChildByTagName(doc.getDocumentElement(), "head");
  }

  public static Optional<Element> getBody(Document doc) 
  {
	return NodeHelper.getFirstMatchingChildByTagName(doc.getDocumentElement(), "body");
  }

  public static boolean isElement(final Node n) {
    return n instanceof Element;
  }

  public static boolean isText(final Node n) {
    return n instanceof Text;
  }
}
