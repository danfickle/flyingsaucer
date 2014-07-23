/*
 * DOMTreeResolver.java
 * Copyright (c) 2005 Scott Cytacki
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
package org.xhtmlrenderer.css.extend.lib;

import java.util.Optional;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xhtmlrenderer.css.extend.TreeResolver;

/**
 * @author scott
 *         <p/>
 *         works for a w3c DOM tree
 */
public class DOMTreeResolver implements TreeResolver {
    @Override
	public Optional<Element> getParentElement(final Element element) {
        Node parent = ((Element) element).getParentNode();

        if (!(parent instanceof Element) || parent instanceof Document)
        	parent = null;
        
        return Optional.ofNullable((Element) parent);
    }

    @Override
     public Optional<Element> getPreviousSiblingElement(final Element element) {
        Node sibling = ((Element) element).getPreviousSibling();
        while (sibling != null && !(sibling instanceof Element)) {
            sibling = sibling.getPreviousSibling();
        }
        if (sibling == null || !(sibling instanceof Element)) {
            return Optional.empty();
        }
        return Optional.of((Element) sibling);
    }

    @Override
    public String getElementName(final Element element) {
        return ((Element) element).getNodeName();
    }
    
    @Override
    public boolean isFirstChildElement(final Object element) {
        final Node parent = ((Element) element).getParentNode();
        Node currentChild = parent.getFirstChild();
        while (currentChild != null && !(currentChild instanceof Element)) {
            currentChild = currentChild.getNextSibling();
        }
        return currentChild == element;
    }

    @Override
    public boolean isLastChildElement(final Object element) {
        final Node parent = ((Element) element).getParentNode();
        Node currentChild = parent.getLastChild();
        while (currentChild != null && !(currentChild instanceof Element)) {
            currentChild = currentChild.getPreviousSibling();
        }
        return currentChild == element;
    }

    @Override
    public boolean matchesElement(final Object element, final String namespaceURI, final String name) {
        final Element e = (Element)element;
        final String localName = e.getNodeName();
        final String eName = localName;

//        if (localName == null) {
//            eName = e.getNodeName();
//        } else {
//            eName = localName;
//        }

        if (namespaceURI != null) {
        	return (namespaceURI + ':' + name).equals(localName);
//        	return name.equals(localName) && namespaceURI.equals(e.getNamespaceURI());
        } else if (namespaceURI == TreeResolver.NO_NAMESPACE) {
            return name.equals(eName) && eName.indexOf(':') == -1;
        } else /* if (namespaceURI == null) */ {
            return name.equals(eName);
        }
    }
    
    @Override
    public int getPositionOfElement(final Object element) {
        final Node parent = ((Element) element).getParentNode();
        final NodeList nl = parent.getChildNodes();

        int elt_count = 0;
        int i = 0;
        while (i < nl.getLength()) {
            if (nl.item(i) instanceof Element) {
                if(nl.item(i) == element) {
                    return elt_count;
                } else {
                    elt_count++;
                }
            }
            i++;
        }
        
        //should not happen
        return -1;
    }
}
