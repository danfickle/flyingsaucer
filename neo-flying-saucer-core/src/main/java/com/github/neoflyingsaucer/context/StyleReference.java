/*
 * StyleReference.java
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
package com.github.neoflyingsaucer.context;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.github.neoflyingsaucer.css.constants.CSSName;
import com.github.neoflyingsaucer.css.extend.AttributeResolver;
import com.github.neoflyingsaucer.css.extend.lib.DOMTreeResolver;
import com.github.neoflyingsaucer.css.newmatch.CascadedStyle;
import com.github.neoflyingsaucer.css.newmatch.PageInfo;
import com.github.neoflyingsaucer.css.parser.PropertyValue;
import com.github.neoflyingsaucer.css.sheet.FontFaceRule;
import com.github.neoflyingsaucer.css.sheet.PropertyDeclaration;
import com.github.neoflyingsaucer.css.sheet.Stylesheet;
import com.github.neoflyingsaucer.css.sheet.StylesheetInfo;
import com.github.neoflyingsaucer.extend.NamespaceHandler;
import com.github.neoflyingsaucer.extend.controller.error.FSError.FSErrorLevel;
import com.github.neoflyingsaucer.extend.controller.error.FSErrorController;
import com.github.neoflyingsaucer.extend.controller.error.LangId;
import com.github.neoflyingsaucer.extend.useragent.Optional;
import com.github.neoflyingsaucer.extend.useragent.StylesheetI;
import com.github.neoflyingsaucer.extend.useragent.UserAgentCallback;
import com.github.neoflyingsaucer.layout.SharedContext;


/**
 * @author Torbjoern Gannholm
 */
public class StyleReference {

    /**
     * The Context this StyleReference operates in; used for property
     * resolution.
     */
    private SharedContext _context;
    private NamespaceHandler _nsh;
    private Document _doc;
    private final StylesheetFactoryImpl _stylesheetFactory;

    /**
     * Instance of our element-styles matching class. Will be null if new rules
     * have been added since last match.
     */
    private com.github.neoflyingsaucer.css.newmatch.Matcher _matcher;

    /** */
    private UserAgentCallback _uac;
    
    public StyleReference(final UserAgentCallback userAgent) {
        _uac = userAgent;
        _stylesheetFactory = new StylesheetFactoryImpl(userAgent);
    }

    /**
     * Sets the documentContext attribute of the StyleReference object
     */
    public void setDocumentContext(final SharedContext context, final NamespaceHandler nsh, final Document doc) {
        _context = context;
        _nsh = nsh;
        _doc = doc;
        final AttributeResolver attRes = new StandardAttributeResolver(_nsh, _uac);

        final List<StylesheetInfo> infos = getStylesheets();
        
        _matcher = new com.github.neoflyingsaucer.css.newmatch.Matcher(
                new DOMTreeResolver(),
                attRes, 
                _stylesheetFactory, 
                readAndParseAll(infos, _context.getMedia()), 
                _context);
    }
    
    private List<Stylesheet> readAndParseAll(final List<StylesheetInfo> infos, final String medium) 
    {
        final List<Stylesheet> result = new ArrayList<Stylesheet>(infos.size() + 15);

        for (StylesheetInfo info : infos)
        {
          if (!info.appliesToMedia(_context))
        		continue;
        	
      	  Optional<StylesheetI> sheet = info.getStylesheet();
        	
    	  if (!sheet.isPresent()) {
              sheet = _stylesheetFactory.getStylesheet(info);
          }
          
          if (sheet.isPresent())
          {
        	Stylesheet s2 = (Stylesheet) sheet.get();
        	  
        	if (!s2.getImportRules().isEmpty()) 
          	{
          		result.addAll(readAndParseAll(s2.getImportRules(), medium));
          	}

          	result.add(s2);
          }         	
        }
        
        return result;
    }

    /**
     * Returns a Map keyed by CSS property names (e.g. 'border-width'), and the
     * assigned value as a SAC CSSValue instance. The properties should have
     * been matched to the element when the Context was established for this
     * StyleReference on the Document to which the Element belongs.
     *
     * @param e The DOM Element for which to find properties
     * @return Map of CSS property names to CSSValue instance assigned to it.
     */
    public java.util.Map<String, PropertyValue> getCascadedPropertiesMap(final String uri, final Element e) {
        final CascadedStyle cs = _matcher.getCascadedStyle(uri, e, false);//this is only for debug, I think
        final java.util.LinkedHashMap<String, PropertyValue> props = new java.util.LinkedHashMap<String, PropertyValue>();
        for (final java.util.Iterator<PropertyDeclaration> i = cs.getCascadedPropertyDeclarations(); i.hasNext();) {
            final PropertyDeclaration pd = i.next();

            final String propName = pd.getPropertyName();
            final CSSName cssName = CSSName.getByPropertyName(propName);
            props.put(propName, cs.propertyByName(cssName).getValue());
        }
        return props;
    }

    /**
     * Gets the pseudoElementStyle attribute of the StyleReference object
     */
    public CascadedStyle getPseudoElementStyle(final Node node, final String pseudoElement) {
        Element e = null;
        if (node instanceof Element) {
            e = (Element) node;
        } else {
            e = (Element) node.getParentNode();
        }
        return _matcher.getPECascadedStyle(e, pseudoElement);
    }

    /**
     * Gets the CascadedStyle for an element. This must then be converted in the
     * current context to a CalculatedStyle (use getDerivedStyle)
     */
    public CascadedStyle getCascadedStyle(final String uri, final Element e, final boolean restyle) {
        if (e == null) return CascadedStyle.emptyCascadedStyle;
        return _matcher.getCascadedStyle(uri, e, restyle);
    }
    
    public PageInfo getPageStyle(final String pageName, final String pseudoPage) {
        return _matcher.getPageCascadedStyle(pageName, pseudoPage);
    }

    /**
     * Gets StylesheetInfos for all stylesheets and inline styles associated
     * with the current document. Default (user agent) stylesheet and the inline
     * style for the current media are loaded and cached in the
     * StyleSheetFactory by URI.
     *
     * @return The stylesheets value
     */
    private List<StylesheetInfo> getStylesheets() {
        final List<StylesheetInfo> infos = new LinkedList<StylesheetInfo>();
        final long st = System.currentTimeMillis();

        if (!_context.haveLookedUpDefaultStylesheet())
        {
        	_context.setLookedUpDefaultStylesheet(true);
        	final StylesheetInfo defaultStylesheet = _nsh.getDefaultStylesheet(_stylesheetFactory);

        	if (defaultStylesheet != null) 
        	{
        		_context.setDefaultStylesheet(defaultStylesheet);
        		infos.add(defaultStylesheet);
        	}
        }
        else if (_context.getDefaultStylesheet() != null)
        {
        	infos.add(_context.getDefaultStylesheet());
        }
        
        
        final List<StylesheetInfo> refs = _nsh.getStylesheets(_doc);
        int inlineStyleCount = 0;
        if (refs != null) {
            for (int i = 0; i < refs.size(); i++) {
                Optional<String> uri;
                
                if (! refs.get(i).isInline()) 
                {
                	if (refs.get(i).getUri().isPresent())
                	{
                		// TODO: Make sure we have the correct base url.
                		uri = _uac.resolveURI(_context.getBaseURL(), refs.get(i).getUri().get());
                		refs.get(i).setUri(uri);
                	}
                }
                else {
                	// TODO: Make sure we have the correct base url.
                	refs.get(i).setUri(Optional.of(_context.getBaseURL() + "#inline_style_" + (++inlineStyleCount)));

                	final Optional<StylesheetI> sheet = _stylesheetFactory.parse(
                            new StringReader(refs.get(i).getContent()), refs.get(i), true);

                    if (sheet.isPresent())
                    	refs.get(i).setStylesheet(sheet.get());
                }
            }
        }
        infos.addAll(refs);

        // TODO: here we should also get user stylesheet from userAgent

        long el = System.currentTimeMillis() - st;
        FSErrorController.log(StyleReference.class, FSErrorLevel.INFO, LangId.TIME_TO_PARSE_STYLESHEETS, el);
        return infos;
    }
    
    public void removeStyle(final Element e) {
        if (_matcher != null) {
            _matcher.removeStyle(e);
        }
    }
    
    public List<FontFaceRule> getFontFaceRules() {
        return _matcher.getFontFaceRules();
    }
    
    public void setUserAgentCallback(final UserAgentCallback userAgentCallback) {
        _uac = userAgentCallback;
        _stylesheetFactory.setUserAgentCallback(userAgentCallback);
    }
    
    public void setSupportCMYKColors(final boolean b) {
        _stylesheetFactory.setSupportCMYKColors(b);
    }
}
