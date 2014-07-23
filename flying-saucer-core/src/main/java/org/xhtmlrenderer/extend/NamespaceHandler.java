/*
 * Document.java
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
package org.xhtmlrenderer.extend;

import java.util.List;
import java.util.Optional;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xhtmlrenderer.css.extend.StylesheetFactory;
import org.xhtmlrenderer.css.sheet.StylesheetInfo;

/**
 * Provides knowledge specific to a certain document type, 
 * like resolving style-sheets. Currently we only support html.
 *
 * @author Torbjoern Gannholm
 */
public interface NamespaceHandler
{
    /**
     * @return the default CSS stylesheet for this namespace
     * May NOT return null.
     */
    StylesheetInfo getDefaultStylesheet(StylesheetFactory factory);

    /**
     * @return the title for this document, if any exists
     */
    Optional<String> getDocumentTitle(Document doc);

    /**
     * @return all links to CSS stylesheets (type="text/css") in this
     *         document
     * May NOT return null.
     */
    List<StylesheetInfo> getStylesheets(Document doc);

    /**
     * may return null. Required to return null if attribute does not exist and
     * not null if attribute exists.
     */
    Optional<String> getAttributeValue(Element e, String attrName);
    
    Optional<String> getAttributeValue(Element e, String namespaceURI, String attrName);

    /**
     * may return null
     */
    Optional<String> getClass(Element e);

    /**
     * may return null
     */
    Optional<String> getID(Element e);

    /**
     * may return null
     */
    Optional<String> getElementStyling(Element e);

    /**
     * may return null
     */
    Optional<String> getNonCssStyling(Element e);

    /**
     * may return null
     */
    Optional<String> getLang(Element e);

    /**
     * should return null if element is not a link
     */
    Optional<String> getLinkUri(Element e);

    Optional<String> getAnchorName(Element e);

    /**
     * @return Returns true if the Element represents an image.
     */
    boolean isImageElement(Element e);

    /**
     * Determines whether or not the specified Element represents a
     * form element
     */
    boolean isFormElement(Element e);

    /**
     * For an element where isImageElement returns true, retrieves the URI associated with that Image, as
     * reported by the element; makes no guarrantee that the URI is correct, complete or points to anything in
     * particular. For elements where {@link #isImageElement(org.w3c.dom.Element)} returns false, this method may
     * return false, and may also return false if the Element is not correctly formed and contains no URI; check the
     * return value carefully.
     */
    Optional<String> getImageSourceURI(Element e);
}
