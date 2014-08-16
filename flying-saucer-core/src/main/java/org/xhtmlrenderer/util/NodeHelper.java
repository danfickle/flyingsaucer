package org.xhtmlrenderer.util;

import java.util.Optional;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

public class NodeHelper {
	
  public static boolean isRootNode(final Node n) {
    return n.getParentNode() instanceof Document;
  }

  @Deprecated
  public static boolean hasAttribute(final Node link, final String attrName) {
    return link.getAttributes().getNamedItem(attrName) != null;
  }

  @Deprecated
  public static class NodeSpliterator implements Spliterator<Node>
  {
	@Override
	public boolean tryAdvance(Consumer<? super Node> action) 
	{
		if (_origin < _length)
		{
			action.accept(_children.item(_origin));
			_origin++;
			return true;
		}
		return false;
	}

	@Override
	public Spliterator<Node> trySplit()
	{
		int lo = _origin; // divide range in half
	    int mid = ((lo + _length + 1) >>> 1) & ~1;

	    if (lo < mid)
	    { 
	    	// split out left half
	    	// reset this Spliterator's origin
	    	_origin = mid; 
	         return new NodeSpliterator(_children, lo, mid);
	    }
	    else
	    {
	    	// too small to split
	    	return null;
	    }
	}

	@Override
	public long estimateSize() 
	{
		return _length - _origin;
	}

	@Override
	public int characteristics() 
	{
		return ORDERED | IMMUTABLE | NONNULL | SIZED | SUBSIZED;
	}
	  
	private final NodeList _children;
	private int _origin;
	private final int _length;
	
	public NodeSpliterator(final NodeList nl, final int start, final int length)
	{
		_children = nl;
		_origin = start;
		_length = length;
	}
  }
  
  @Deprecated
  public static Stream<Node> childNodeStream(final Node n)
  {
	  final NodeList nl = n.getChildNodes();
	  return StreamSupport.stream(new NodeSpliterator(nl, 0, nl.getLength()), true);
  }
  
  @Deprecated
  public static Stream<Element> childElemStream(final Node n, final String tagName)
  {
	  return childNodeStream(n)
			  .filter(node -> node instanceof Element)
			  .map(NodeHelper::elementCast)
			  .filter(e -> GeneralUtil.ciEquals(e.getNodeName(), tagName));
  }

  @Deprecated
  public static Stream<Element> childElemStream(final Node n)
  {
	  return childNodeStream(n)
			  .filter(node -> node instanceof Element)
			  .map(NodeHelper::elementCast);
  }
  
  @Deprecated
  public static Element elementCast(final Node n)
  {
	  return (Element) n;
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
