package org.xhtmlrenderer.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class NodeHelper {

  public static boolean isRootNode(final Node n) {
    return n.getParentNode() instanceof Document;
  }

  public static boolean hasAttribute(final Node link, final String attrName) {
    return link.getAttributes().getNamedItem(attrName) != null;
  }

  public static boolean attributeContains(final Node link, final String attrName, final String searchStr) {
    final Attr attr = (Attr) link.getAttributes().getNamedItem(attrName);
    final String[] values = attr.getValue().split(" ");
    boolean found = false;
    int i = 0;
    while (!found && i < values.length) {
      found = searchStr.equals(values[i]);
      i++;
    }
    return found;
  }

  public static Iterable<Node> makeIterable(final NodeList nodeList) {
    return new Iterable<Node>() {
      @Override
      public Iterator<Node> iterator() {

        return new Iterator<Node>() {
          private int i = 0;

          @Override
          public boolean hasNext() {
            return i < nodeList.getLength();
          }

          @Override
          public Node next() {
            return nodeList.item(i++);
          }

          @Override
          public void remove() {
            throw new NotImplementedException();
          }
        };
      }
    };
  }


  public static Iterable<Node> getChildrenAsNodes(final Node n) {
    final NodeList childList = n.getChildNodes();
    return makeIterable(childList);
  }

  public static Iterable<Element> getChildrenAsElements(final Node n) {
    final NodeList childList = n.getChildNodes();
    return new Iterable<Element>() {
      int i = 0;

      @Override
      public Iterator<Element> iterator() {
        return new Iterator<Element>() {

          @Override
          public boolean hasNext() {
            return i < childList.getLength();
          }

          @Override
          public Element next() {
            return (Element) childList.item(i++);
          }

          @Override
          public void remove() {
            throw new NotImplementedException();
          }
        };
      }
    };
  }

  public static Element getFirstMatchingByTagName(final Element e, final String tagName) {
    NodeList matchingElements = e.getElementsByTagName(tagName);
    if (matchingElements != null) {
      return (Element) matchingElements.item(0);
    } else {
      return null;
    }
  }

  public static List<Element> getMatchingChildrenByTagName(final Element element, final String tagName) {
    NodeList matchingElements = element.getChildNodes();
    if (matchingElements != null) {
      List<Element> result = new ArrayList<Element>();
      int i = 0;
      int N = matchingElements.getLength();
      while (i < N) {
        final Node n = matchingElements.item(i);
        if (n instanceof Element) {
          final Element e = (Element) n;
          if (tagName.equals(e.getTagName())) {
            result.add(e);
          }
        }
        i++;
      }
      return result;
    } else {
      return null;
    }
  }
  public static Element getFirstMatchingChildByTagName(final Element element, final String tagName) {
    NodeList matchingElements = element.getChildNodes();
    if (matchingElements != null) {
      int i = 0;
      int N = matchingElements.getLength();
      Element result = null;
      while (i < N && result == null) {
        final Node n = matchingElements.item(i);
        if (n instanceof Element) {
          final Element e = (Element) n;
          if (tagName.equals(e.getTagName())) {
            result = e;
          }
        }
        i++;
      }
      return result;
    } else {
      return null;
    }
  }


  public static Element getHead(final Document doc) {
    return getFirstMatchingChildByTagName(doc.getDocumentElement(), "head");
  }

  public static Element getBody(Document doc) {
    return getFirstMatchingChildByTagName(doc.getDocumentElement(), "body");
  }

  public static boolean isElement(final Node n) {
    return n instanceof Element;
  }

  public static boolean isText(final Node n) {
    return n instanceof Text;
  }

}
