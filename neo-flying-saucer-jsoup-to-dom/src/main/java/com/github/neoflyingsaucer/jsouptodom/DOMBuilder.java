package com.github.neoflyingsaucer.jsouptodom;

/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements. See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.github.neoflyingsaucer.extend.controller.error.FSErrorController;
import com.github.neoflyingsaucer.extend.controller.error.FSError.FSErrorLevel;
import com.github.neoflyingsaucer.extend.useragent.LangId;

/**
*
* @author <a href="mailto:kasper@dfki.de">Walter Kasper</a>
*
*/
public class DOMBuilder {
  private DOMBuilder() { }
  
  /**
   * Returns a W3C DOM that exposes the same content as the supplied Jsoup
   * document into a W3C DOM.
   *
   * @param jsoupDocument
   * The Jsoup document to convert.
   * @return A W3C Document.
   */
  public static Document jsoup2DOM(org.jsoup.nodes.Document jsoupDocument) {

    Document document = null;

    try {

      /* Obtain the document builder for the configured XML parser. */
      DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
          .newInstance();

      DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();

      /* Create a document to contain the content. */
      document = docBuilder.newDocument();

      List<org.jsoup.nodes.Element> styleElements = new ArrayList<org.jsoup.nodes.Element>();
      Map<String, String> nsMap = new HashMap<String, String>();

      createDOM(jsoupDocument, document, document, nsMap, styleElements);

      // Now we insert the found style elements into the last head section.
      NodeList nl = document.getElementsByTagName("head");
      Element head;
      
      if (nl == null || nl.getLength() == 0)
      {
    	  head = document.createElement("head");
    	  document.appendChild(head);
      }
      else
      {
    	  head = (Element) nl.item(nl.getLength() - 1);
      }
      
      if (!styleElements.isEmpty())
      {
    	  FSErrorController.log(DOMBuilder.class, FSErrorLevel.INFO, LangId.STYLE_ELEM_MOVED_TO_HEAD, styleElements.size());
      }
      
      for(org.jsoup.nodes.Element e : styleElements)
      {
    	  createDOM(e, head, document, nsMap, new ArrayList<org.jsoup.nodes.Element>(0));
      }
      
    } catch (ParserConfigurationException pce) {
      throw new RuntimeException(pce);
    }

    return document;
  }

  /**
   * The internal helper that copies content from the specified Jsoup
   * <tt>Node</tt> into a W3C {@link Node}.
   *
   * @param node
   * The Jsoup node containing the content to copy to the specified W3C
   * {@link Node}.
   * @param out
   * The W3C {@link Node} that receives the DOM content.
   */
  private static void createDOM(org.jsoup.nodes.Node node, Node out,
      Document doc, Map<String, String> ns, java.util.List<org.jsoup.nodes.Element> styleElements) 
  {

    if (node instanceof org.jsoup.nodes.Document) {

      org.jsoup.nodes.Document d = ((org.jsoup.nodes.Document) node);
      for (org.jsoup.nodes.Node n : d.childNodes()) {
        createDOM(n, out, doc, ns, styleElements);
      }

    }
    else if (node instanceof org.jsoup.nodes.Element &&
    		 out instanceof Element &&
    		 ((org.jsoup.nodes.Element) node).tagName().equals("style") &&
    		 !((Element) out).getTagName().equals("head"))
    {
    	// We've found a style element outside head.
    	// Save it so we can add it to head at the end.
    	org.jsoup.nodes.Element e = ((org.jsoup.nodes.Element) node);
    	styleElements.add(e);
    }
    else if (node instanceof org.jsoup.nodes.Element) {

      org.jsoup.nodes.Element e = ((org.jsoup.nodes.Element) node);
      org.w3c.dom.Element _e = doc.createElement(e.tagName());
      out.appendChild(_e);
      org.jsoup.nodes.Attributes atts = e.attributes();

      for (org.jsoup.nodes.Attribute a : atts) {
        String attName = a.getKey();
        // omit xhtml namespace
        if (attName.equals("xmlns")) {
          continue;
        }
        String attPrefix = getNSPrefix(attName);
        if (attPrefix != null) {
          if (attPrefix.equals("xmlns")) {
            ns.put(getLocalName(attName), a.getValue());
          } else if (!attPrefix.equals("xml")) {
            String namespace = ns.get(attPrefix);
            if (namespace == null) {
              // fix attribute names looking like qnames
              attName = attName.replace(':', '_');
            }
          }
        }
        _e.setAttribute(attName, a.getValue());
        if ("id".equals(attName)) {
          _e.setIdAttribute(attName, true);
        }
      }

      for (org.jsoup.nodes.Node n : e.childNodes()) {
        createDOM(n, _e, doc, ns, styleElements);
      }

    } else if (node instanceof org.jsoup.nodes.TextNode) {

      org.jsoup.nodes.TextNode t = ((org.jsoup.nodes.TextNode) node);
      if (!(out instanceof Document)) {
        out.appendChild(doc.createTextNode(t.getWholeText()));
      }
    } else if (node instanceof org.jsoup.nodes.DataNode) {
      final org.jsoup.nodes.DataNode d = (org.jsoup.nodes.DataNode) node;
      out.appendChild(doc.createCDATASection(d.getWholeData()));
    } else if (node instanceof org.jsoup.nodes.DocumentType) {
      // Ignored
    } else if (node instanceof org.jsoup.nodes.Comment) {
      // Ignored
    } else {
      FSErrorController.log(DOMBuilder.class, FSErrorLevel.ERROR, LangId.NODE_TYPE_NOT_HANDLED, node.getClass());
      assert(false);
    }
  }

  // some hacks for handling namespace in jsoup2DOM conversion
  private static String getNSPrefix(String name) {
    if (name != null) {
      int pos = name.indexOf(':');
      if (pos > 0) {
        return name.substring(0, pos);
      }
    }
    return null;
  }

  private static String getLocalName(String name) {
    if (name != null) {
      int pos = name.lastIndexOf(':');
      if (pos > 0) {
        return name.substring(pos + 1);
      }
    }
    return name;
  }

}