/*
 * {{{ header & license
 * Copyright (c) 2007 Wisconsin Court System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.xhtmlrenderer.css.parser;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.constants.CSSPrimitiveUnit;
import org.xhtmlrenderer.css.constants.MarginBoxName;
import org.xhtmlrenderer.css.extend.TreeResolver;
import org.xhtmlrenderer.css.mediaquery.MediaFeatureName;
import org.xhtmlrenderer.css.mediaquery.MediaQueryExpression;
import org.xhtmlrenderer.css.mediaquery.MediaQueryItem;
import org.xhtmlrenderer.css.mediaquery.MediaQueryList;
import org.xhtmlrenderer.css.mediaquery.MediaQueryItem.MediaQueryQualifier;
import org.xhtmlrenderer.css.newmatch.Selector;
import org.xhtmlrenderer.css.parser.property.PropertyBuilder;
import org.xhtmlrenderer.css.sheet.FontFaceRule;
import org.xhtmlrenderer.css.sheet.MediaRule;
import org.xhtmlrenderer.css.sheet.PageRule;
import org.xhtmlrenderer.css.sheet.PropertyDeclaration;
import org.xhtmlrenderer.css.sheet.Ruleset;
import org.xhtmlrenderer.css.sheet.RulesetContainer;
import org.xhtmlrenderer.css.sheet.Stylesheet;
import org.xhtmlrenderer.css.sheet.StylesheetInfo;
import org.xhtmlrenderer.css.sheet.StylesheetInfo.CSSOrigin;
import org.xhtmlrenderer.util.GeneralUtil;

import com.github.neoflyingsaucer.extend.controller.error.LangId;
import com.github.neoflyingsaucer.extend.useragent.Optional;
import com.github.neoflyingsaucer.extend.useragent.UserAgentCallback;

public class CSSParser {
    private static final Set<String> SUPPORTED_PSEUDO_ELEMENTS;
    private static final Set<String> CSS21_PSEUDO_ELEMENTS;

    static {
        SUPPORTED_PSEUDO_ELEMENTS = new HashSet<String>(4);
        SUPPORTED_PSEUDO_ELEMENTS.add("first-line");
        SUPPORTED_PSEUDO_ELEMENTS.add("first-letter");
        SUPPORTED_PSEUDO_ELEMENTS.add("before");
        SUPPORTED_PSEUDO_ELEMENTS.add("after");

        CSS21_PSEUDO_ELEMENTS = new HashSet<String>(4);
        CSS21_PSEUDO_ELEMENTS.add("first-line");
        CSS21_PSEUDO_ELEMENTS.add("first-letter");
        CSS21_PSEUDO_ELEMENTS.add("before");
        CSS21_PSEUDO_ELEMENTS.add("after");
    }

    private Token _saved;
    private final Lexer _lexer;

    private CSSErrorHandler _errorHandler;
    private String _URI;
    private final UserAgentCallback _uac; // May be null.

    private final Map<String, String> _namespaces = new HashMap<String, String>();
    private boolean _supportCMYKColors;

    public CSSParser(final CSSErrorHandler errorHandler, final UserAgentCallback uac) 
    {
        _lexer = new Lexer(new StringReader(""));
        _errorHandler = errorHandler;
        _uac = uac;
    }

    public Stylesheet parseStylesheet(final String uri, final CSSOrigin origin, final Reader reader)
            throws IOException {
        _URI = uri;
        reset(reader);

        final Stylesheet result = new Stylesheet(uri, origin);
        stylesheet(result);

        return result;
    }

    public Ruleset parseDeclaration(final String uri, final CSSOrigin origin, final String text) 
    {
        try {
            _URI = uri;
            reset(new StringReader(text));

            skipWhitespace();

            final Ruleset result = new Ruleset(origin);

            try {
                declaration_list(result, true, false, false);
            } catch (final CSSParseException e) {
                // ignore, already handled
            }

            return result;
        } catch (final IOException e) {
            // "Shouldn't" happen
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public PropertyValue parsePropertyValue(final CSSName cssName, final CSSOrigin origin, final String expr) {
        _URI = cssName + " property value";
        try {
            reset(new StringReader(expr));
            final List<PropertyValue> values = expr(
                    cssName == CSSName.FONT_FAMILY ||
                    cssName == CSSName.FONT_SHORTHAND ||
                    cssName == CSSName.FS_PDF_FONT_ENCODING);

            final PropertyBuilder builder = CSSName.getPropertyBuilder(cssName);
            List<PropertyDeclaration> props;
            try {
                props = builder.buildDeclarations(cssName, values, origin, false, true);
            } catch (final CSSParseException e) {
                e.setLine(getCurrentLine());
                throw e;
            }

            if (props.size() != 1) {
            	throw new CSSParseException(LangId.BUILDER_BUG, getCurrentLine(), cssName);
            }

            final PropertyDeclaration decl = (PropertyDeclaration)props.get(0);

            return (PropertyValue)decl.getValue();
        } catch (final IOException e) {
            // "Shouldn't" happen
            throw new RuntimeException(e.getMessage(), e);
        } catch (final CSSParseException e) {
            error(e, "property value", false);
            return null;
        }
    }

//    stylesheet
//    : [ CHARSET_SYM S* STRING S* ';' ]?
//      [S|CDO|CDC]* [ import [S|CDO|CDC]* ]*
//      [ namespace [S|CDO|CDC]* ]*
//      [ [ ruleset | media | page | font_face ] [S|CDO|CDC]* ]*
    private void stylesheet(final Stylesheet stylesheet) throws IOException {
        //System.out.println("stylesheet()");
        Token t = la();
        try {
            if (t == Token.TK_CHARSET_SYM) {
                try {
                    t = next();
                    skipWhitespace();
                    t = next();
                    if (t == Token.TK_STRING) {
                        /* String charset = getTokenValue(t); */

                        skipWhitespace();
                        t = next();
                        if (t != Token.TK_SEMICOLON) {
                            push(t);
                            throw new CSSParseException(t, Token.TK_SEMICOLON, getCurrentLine());
                        }
                        // TODO: Do something
                    } else {
                        push(t);
                        throw new CSSParseException(t, Token.TK_STRING, getCurrentLine());
                    }
                } catch (final CSSParseException e) {
                    error(e, "@charset rule", true);
                    recover(false, false);
                }
            }
            skipWhitespaceAndCdocdc();
            while (true) {
                t = la();
                if (t == Token.TK_IMPORT_SYM) {
                    importRule(stylesheet);
                    skipWhitespaceAndCdocdc();
                } else {
                    break;
                }
            }
            while (true) {
                t = la();
                if (t == Token.TK_NAMESPACE_SYM) {
                    namespace();
                    skipWhitespaceAndCdocdc();
                } else {
                    break;
                }
            }
            while (true) {
                t = la();
                if (t == Token.TK_EOF) {
                    break;
                }
                switch (t.getType()) {
                    case Token.PAGE_SYM:
                        page(stylesheet);
                        break;
                    case Token.MEDIA_SYM:
                        media(stylesheet);
                        break;
                    case Token.FONT_FACE_SYM:
                        fontFace(stylesheet);
                        break;
                    case Token.IMPORT_SYM:
                        next();
                        error(new CSSParseException(LangId.AT_RULE_NOT_SUPPORTED_HERE, getCurrentLine(), "import"),
                                "@import rule", true);
                        recover(false, false);
                        break;
                    case Token.NAMESPACE_SYM:
                        next();
                        error(new CSSParseException(LangId.AT_RULE_NOT_SUPPORTED_HERE, getCurrentLine(), "namespace"),
                                "@namespace rule", true);
                        recover(false, false);
                        break;
                    case Token.AT_RULE:
                        next();
                        error(new CSSParseException(LangId.AT_RULE_NOT_SUPPORTED_HERE, getCurrentLine(), "@"), "at-rule", true);
                        recover(false, false);
                        // fall through
                    default:
                        ruleset(stylesheet);
                }
                skipWhitespaceAndCdocdc();
            }
        } catch (final CSSParseException e) {
            // "shouldn't" happen
            if (! e.isCallerNotified()) {
                error(e, "stylesheet", false);
            }
        }
    }

//  import
//  : IMPORT_SYM S*
//    [STRING|URI] S* [ medium [ COMMA S* medium]* ]? ';' S*
//  ;
    private void importRule(final Stylesheet stylesheet) throws IOException {
        //System.out.println("import()");
        try {
            Token t = next();
            if (t == Token.TK_IMPORT_SYM) {
                final StylesheetInfo info = new StylesheetInfo();
                info.setOrigin(stylesheet.getOrigin());
                info.setType("text/css");

                skipWhitespace();
                t = next();
                switch (t.getType()) {
                    case Token.STRING:
                    case Token.URI:
                    	// @imports are resolved relative to the current stylesheet.
                    	info.setUri(_uac.resolveURI(_URI, getTokenValue(t)));

                    	skipWhitespace();
                        t = la();
                        if (t == Token.TK_IDENT) {
                            info.setMediaQueryList(mediaQueryList());
                            while (true) {
                                t = la();
                                if (t == Token.TK_COMMA) {
                                    next();
                                    skipWhitespace();
                                    t = la();
                                    if (t == Token.TK_IDENT) {
                                        info.setMediaQueryList(mediaQueryList());
                                    } else {
                                        throw new CSSParseException(
                                                t, Token.TK_IDENT, getCurrentLine());
                                    }
                                } else {
                                    break;
                                }
                            }
                        }
                        t = next();
                        if (t == Token.TK_SEMICOLON) {
                            skipWhitespace();
                        } else {
                            push(t);
                            throw new CSSParseException(
                                    t, Token.TK_SEMICOLON, getCurrentLine());
                        }
                        break;
                    default:
                        push(t);
                        throw new CSSParseException(
                            t, new Token[] { Token.TK_STRING, Token.TK_URI }, getCurrentLine());
                }

                stylesheet.addImportRule(info);
            } else {
                push(t);
                throw new CSSParseException(
                        t, Token.TK_IMPORT_SYM, getCurrentLine());
            }
        } catch (final CSSParseException e) {
            error(e, "@import rule", true);
            recover(false, false);
        }
    }

//  namespace
//  : NAMESPACE_SYM S* [namespace_prefix S*]? [STRING|URI] S* ';' S*
//  ;
//  namespace_prefix
//  : IDENT
//  ;
    private void namespace() throws IOException {
        try {
            Token t = next();
            if (t == Token.TK_NAMESPACE_SYM) {
                String prefix = null;
                String url = null;

                skipWhitespace();
                t = next();

                if (t == Token.TK_IDENT) {
                    prefix = getTokenValue(t);
                    skipWhitespace();
                    t = next();
                }

                if (t == Token.TK_STRING || t == Token.TK_URI) {
                    url = getTokenValue(t);
                } else {
                    throw new CSSParseException(
                            t, new Token[] { Token.TK_STRING, Token.TK_URI }, getCurrentLine());
                }

                skipWhitespace();

                t = next();
                if (t == Token.TK_SEMICOLON) {
                    skipWhitespace();

                    _namespaces.put(prefix, url);
                } else {
                    throw new CSSParseException(
                            t, Token.TK_SEMICOLON, getCurrentLine());
                }
            } else {
                throw new CSSParseException(t, Token.TK_NAMESPACE_SYM, getCurrentLine());
            }
        } catch (final CSSParseException e) {
            error(e, "@namespace rule", true);
            recover(false, false);
        }
    }

    /*
     * media
     * : MEDIA_SYM S* media_query_list S* '{' S* ruleset* '}' S*
     * ;
     */
    private void media(final Stylesheet stylesheet) throws IOException {
        //System.out.println("media()");
        Token t = next();
        try {
            if (t == Token.TK_MEDIA_SYM) {
            	final MediaRule mediaRule = new MediaRule(stylesheet.getOrigin());

            	skipWhitespace();
               	mediaRule.setMediaQueryList(mediaQueryList());
                skipWhitespace();

                // TODO: Are we allowed @ rules here?
                ruleset(mediaRule);
                next();
                stylesheet.addContent(mediaRule);

            } else {
                push(t);
                throw new CSSParseException(t, Token.TK_MEDIA_SYM, getCurrentLine());
            }
        } catch (final CSSParseException e) {
            error(e, "@media rule", true);
            recover(false, false);
        }
    }


    public static MediaQueryList parseMediaQueryList(String mediaQueryList)
    {
       	CSSParser parser = new CSSParser(new CSSErrorHandler() {
			@Override
			public void error(String uri, int line, LangId msgId, Object... args) 
			{
				// TODO: get a uac here.
			}
		}, null);
    	
       	return parser.parseMediaQueryListInternal(mediaQueryList);
    }
    
    // public only for testing.
    public MediaQueryList parseMediaQueryListInternal(String mediaQueryList)
    {
    	reset(new StringReader(mediaQueryList));
    	try {
			return mediaQueryList();
		} catch (IOException e) {
			assert(false);
			return null;
		}
    }
    
    /*
     * media_query_list
     * : S* [media_query [ ',' S* media_query ]* ]?
     * ;
     */
    private MediaQueryList mediaQueryList() throws IOException {
        //System.out.println("medium()");
    	MediaQueryList mediaQueryList = new MediaQueryList();

    	skipWhitespaceAndCdocdc();

    	Token t = la();

    	if (t == Token.TK_IDENT || t == Token.TK_LPAREN) {
    		mediaQueryList.addMediaQueryItem(mediaQuery());
        }
    	
    	t = next();
    	while (t == Token.TK_COMMA)
    	{
    		skipWhitespaceAndCdocdc();
    		mediaQueryList.addMediaQueryItem(mediaQuery());
    		t = next();
    	}
    	return mediaQueryList;
    }

    /*
     * media_type
     * : IDENT
     * ;
     */
    private String mediaType() throws IOException
    {
    	final Token t = next();
    	assert(t == Token.TK_IDENT);
    	return getTokenValue(t);
    }
    
    
    /*
     * media_query
     * : [ONLY | NOT]? S* media_type S* [ AND S* expression ]*
     * | expression [ AND S* expression ]*
     * ;
     */
    private MediaQueryItem mediaQuery() throws IOException
    {
    	Token t = next();
    	MediaQueryQualifier qualifier = MediaQueryQualifier.NONE;
    	String type = null;
    	List<MediaQueryExpression> expressions = new ArrayList<MediaQueryExpression>(2);
    	
    	// [ONLY | NOT]?
    	if (t == Token.TK_IDENT)
    	{
    		if (GeneralUtil.ciEquals("only", getTokenValue(t)))
    		{
    			qualifier = MediaQueryQualifier.ONLY;
    		}
    		else if (GeneralUtil.ciEquals("not", getTokenValue(t)))
    		{
    			qualifier = MediaQueryQualifier.NOT;
    		}
    		else
    		{
    			push(t);
    		}
    	}
    	else
    	{
    		push(t);
    	}
    	
    	// S*
    	skipWhitespaceAndCdocdc();
    	
    	t = la();

    	if (t == Token.TK_IDENT)
    	{
        	// media_type
    		type = mediaType();
    	}
    	else if (t == Token.TK_LPAREN)
    	{
    		// expression
    		expressions.add(mediaQueryExpression());
    	}
    	
    	if (type == null && expressions.isEmpty())
    	{
    		return new MediaQueryItem(qualifier, "all", Collections.<MediaQueryExpression>emptyList());
    	}
    	
    	skipWhitespaceAndCdocdc();
    	
    	t = next();

    	while (t == Token.TK_IDENT)
    	{
    		// AND S*
    		if (!GeneralUtil.ciEquals(getTokenValue(t), "and"))
    		{
    			throw new CSSParseException(Token.TK_IDENT, Token.TK_IDENT, getCurrentLine());
    		}

    		skipWhitespaceAndCdocdc();
    		
    		expressions.add(mediaQueryExpression());
    		
    		t = next();
    	}
    	
    	push(t);
    	
    	return new MediaQueryItem(qualifier, type == null ? "all" : type, expressions);
    }
    
    /*
     * expression
     * : '(' S* media_feature S* [ ':' S* expr ]? ')' S*
     * ;
     */
    private MediaQueryExpression mediaQueryExpression() throws IOException
    {
    	Token t = next();
    	List<PropertyValue> expr = Collections.emptyList();
    	
    	if (t != Token.TK_LPAREN)
    	{
    		throw new CSSParseException(t, Token.TK_LPAREN, getCurrentLine());    		
    	}

    	skipWhitespaceAndCdocdc();
    	
    	MediaFeatureName feature = mediaQueryFeature();

    	skipWhitespaceAndCdocdc();
    	
    	t = la();
    	
    	if (t == Token.TK_COLON)
    	{
    		skipWhitespaceAndCdocdc();
    		t = next();
    		skipWhitespaceAndCdocdc();
    		expr = expr(false);
    		t = la();
    	}
    	
    	if (t != Token.TK_RPAREN)
    	{
    		throw new CSSParseException(t, Token.TK_RPAREN, getCurrentLine());
    	}
    	
    	t = next();
    	
    	return new MediaQueryExpression(feature, expr);
    }
    
    /*
     * media_feature
     * : IDENT
     * ;
     */ 
    private MediaFeatureName mediaQueryFeature() throws IOException
    {
    	Token t = la();
    	
    	if (t != Token.TK_IDENT)
    	{
    		throw new CSSParseException(t, Token.TK_IDENT, getCurrentLine());
    	}
    	
    	t = next();
    	String val = getTokenValue(t);
    	
    	MediaFeatureName nm = MediaFeatureName.fsValueOf(val);
    	
    	if (nm == null)
    	{
    		throw new CSSParseException(LangId.UNRECOGNIZED_IDENTIFIER, getCurrentLine(), val, "media feature name");
    	}

    	return nm;
    }
    
//  font_face
//    : FONT_FACE_SYM S*
//      '{' S* declaration [ ';' S* declaration ]* '}' S*
//    ;
    private void fontFace(final Stylesheet stylesheet) throws IOException {
        //System.out.println("font_face()");
        Token t = next();
        try {
            final FontFaceRule fontFaceRule = new FontFaceRule(stylesheet.getOrigin());
            if (t == Token.TK_FONT_FACE_SYM) {
                skipWhitespace();

                final Ruleset ruleset = new Ruleset(stylesheet.getOrigin());

                skipWhitespace();
                t = next();
                if (t == Token.TK_LBRACE) {
                    LOOP:
                    while (true) {
                        skipWhitespace();
                        t = la();
                        if (t == Token.TK_RBRACE) {
                            next();
                            skipWhitespace();
                            break LOOP;
                        } else {
                            declaration_list(ruleset, false, true, true);
                        }
                    }
                } else {
                    push(t);
                    throw new CSSParseException(t, Token.TK_LBRACE, getCurrentLine());
                }

                fontFaceRule.addContent(ruleset);
                stylesheet.addFontFaceRule(fontFaceRule);
            } else {
                push(t);
                throw new CSSParseException(t, Token.TK_FONT_FACE_SYM, getCurrentLine());
            }
        } catch (final CSSParseException e) {
            error(e, "@font-face rule", true);
            recover(false, false);
        }
    }

//  page :
//    PAGE_SYM S* IDENT? pseudo_page? S* 
//    '{' S* [ declaration | margin ]? [ ';' S* [ declaration | margin ]? ]* '}' S*
//
    private void page(final Stylesheet stylesheet) throws IOException {
        //System.out.println("page()");
        Token t = next();
        try {
            final PageRule pageRule = new PageRule(stylesheet.getOrigin());
            if (t == Token.TK_PAGE_SYM) {
                skipWhitespace();
                t = la();
                if (t == Token.TK_IDENT) {
                    final String pageName = getTokenValue(t);
                    if (pageName.equals("auto")) {
                    	throw new CSSParseException(LangId.PAGE_NAME_NO_AUTO, getCurrentLine());
                    }
                    next();
                    pageRule.setName(pageName);
                    t = la();
                }
                if (t == Token.TK_COLON) {
                    pageRule.setPseudoPage(pseudo_page());
                }
                final Ruleset ruleset = new Ruleset(stylesheet.getOrigin());

                skipWhitespace();
                t = next();
                if (t == Token.TK_LBRACE) {
                    LOOP:
                    while (true) {
                        skipWhitespace();
                        t = la();
                        if (t == Token.TK_RBRACE) {
                            next();
                            skipWhitespace();
                            break LOOP;
                        } else if (t == Token.TK_AT_RULE) {
                            margin(stylesheet, pageRule);
                        } else {
                            declaration_list(ruleset, false, true, false);
                        }
                    }
                } else {
                    push(t);
                    throw new CSSParseException(t, Token.TK_LBRACE, getCurrentLine());
                }

                pageRule.addContent(ruleset);
                stylesheet.addContent(pageRule);
            } else {
                push(t);
                throw new CSSParseException(t, Token.TK_PAGE_SYM, getCurrentLine());
            }
        } catch (final CSSParseException e) {
            error(e, "@page rule", true);
            recover(false, false);
        }
    }

//  margin :
//    margin_sym S* '{' declaration [ ';' S* declaration? ]* '}' S*
//    ;
    private void margin(final Stylesheet stylesheet, final PageRule pageRule) throws IOException {
        Token t = next();
        if (t != Token.TK_AT_RULE) {
            error(new CSSParseException(t, Token.TK_AT_RULE, getCurrentLine()), "at rule", true);
            recover(true, false);
            return;
        }
        final String name = getTokenValue(t);
        final MarginBoxName marginBoxName = MarginBoxName.valueOf(name);
        if (marginBoxName == null) {
            error(new CSSParseException(LangId.MARGIN_BOX_NAME, getCurrentLine(), name), "at rule", true);
            recover(true, false);
            return;
        }

        skipWhitespace();
        try {
            t = next();
            if (t == Token.TK_LBRACE) {
                skipWhitespace();
                final Ruleset ruleset = new Ruleset(stylesheet.getOrigin());
                declaration_list(ruleset, false, false, false);
                t = next();
                if (t != Token.TK_RBRACE) {
                    push(t);
                    throw new CSSParseException(t, Token.TK_RBRACE, getCurrentLine());
                }
                pageRule.addMarginBoxProperties(marginBoxName, ruleset.getPropertyDeclarations());
            } else {
                push(t);
                throw new CSSParseException(t, Token.TK_LBRACE, getCurrentLine());
            }
        } catch (final CSSParseException e) {
            error(e, "margin box", true);
            recover(false, false);
        }
    }


//  pseudo_page
//    : ':' IDENT
//    ;
    private String pseudo_page() throws IOException {
        //System.out.println("pseudo_page()");
        String result = null;
        Token t = next();
        if (t == Token.TK_COLON) {
            t = next();
            if (t == Token.TK_IDENT) {
                result = getTokenValue(t);
                if (! (result.equals("first") || result.equals("left") || result.equals("right"))) {
                	throw new CSSParseException(LangId.PAGE_NAME_MUST_BE, getCurrentLine(), result);
                }
            } else {
                push(t);
                throw new CSSParseException(t, Token.TK_IDENT, getCurrentLine());
            }
        } else {
            push(t);
            throw new CSSParseException(t, Token.TK_COLON, getCurrentLine());
        }
        return result;
    }
//  operator
//    : '/' S* | COMMA S* | /* empty */
//    ;
    private void operator() throws IOException {
        //System.out.println("operator()");
        final Token t = la();
        switch (t.getType()) {
            case Token.VIRGULE:
            case Token.COMMA:
                next();
                skipWhitespace();
                break;
        }
    }

//  combinator
//    : PLUS S*
//    | GREATER S*
//    | S
//    ;
    private Token combinator() throws IOException {
        //System.out.println("combinator()");
        final Token t = next();
        if (t == Token.TK_PLUS || t == Token.TK_GREATER) {
            skipWhitespace();
        } else if (t != Token.TK_S) {
            push(t);
            throw new CSSParseException(
                    t,
                    new Token[] { Token.TK_PLUS, Token.TK_GREATER, Token.TK_S },
                    getCurrentLine());
        }
        return t;
    }

//  unary_operator
//    : '-' | PLUS
//    ;
    private int unaryOperator() throws IOException {
        //System.out.println("unary_operator()");
        final Token t = next();
        if (! (t == Token.TK_MINUS || t == Token.TK_PLUS)) {
            push(t);
            throw new CSSParseException(
                    t, new Token[] { Token.TK_MINUS, Token.TK_PLUS}, getCurrentLine());
        }
        if (t == Token.TK_MINUS) {
            return -1;
        } else { /* t == Token.TK_PLUS */
            return 1;
        }
    }

//  property
//    : IDENT S*
//    ;
    private String property() throws IOException {
        //System.out.println("property()");
        final Token t = next();
        String result;
        if (t == Token.TK_IDENT) {
            result = getTokenValue(t);
            skipWhitespace();
        } else {
            push(t);
            throw new CSSParseException(
                    t, Token.TK_IDENT, getCurrentLine());
        }

        return result;
    }

//  declaration_list
//    : [ declaration ';' S* ]*
    private void declaration_list(
            final Ruleset ruleset, final boolean expectEOF, final boolean expectAtRule, final boolean inFontFace) throws IOException {
        //System.out.println("declaration_list()");
        Token t;
        LOOP:
        while (true) {
            t = la();
            switch (t.getType()) {
                case Token.SEMICOLON:
                    next();
                    skipWhitespace();
                    continue;
                case Token.RBRACE:
                    break LOOP;
                case Token.AT_RULE:
                    if (expectAtRule) {
                        break LOOP;
                    } else {
                        declaration(ruleset, inFontFace);
                    }
                    // FIXME: intentional fall-thru here?
                case Token.EOF:
                    if (expectEOF) {
                        break LOOP;
                    }
                    // fall through
                default:
                    declaration(ruleset, inFontFace);
            }
        }
    }

//  ruleset
//    : selector [ COMMA S* selector ]*
//      LBRACE S* [ declaration ';' S* ]* '}' S*
//    ;
    private void ruleset(final RulesetContainer container) throws IOException {
        //System.out.println("ruleset()");
        try {
            final Ruleset ruleset = new Ruleset(container.getOrigin());

            selector(ruleset);
            Token t;
            while (true) {
                t = la();
                if (t == Token.TK_COMMA) {
                    next();
                    skipWhitespace();
                    selector(ruleset);
                } else {
                    break;
                }
            }
            t = next();
            if (t == Token.TK_LBRACE) {
                skipWhitespace();
                declaration_list(ruleset, false, false, false);
                t = next();
                if (t == Token.TK_RBRACE) {
                    skipWhitespace();
                } else {
                    push(t);
                    throw new CSSParseException(t, Token.TK_RBRACE, getCurrentLine());
                }
            } else {
                push(t);
                throw new CSSParseException(
                        t, new Token[] { Token.TK_COMMA, Token.TK_LBRACE }, getCurrentLine());
            }

            if (ruleset.getPropertyDeclarations().size() > 0) {
                container.addContent(ruleset);
            }
        } catch (final CSSParseException e) {
            error(e, "ruleset", true);
            recover(true, false);
        }
    }

//  selector
//    : simple_selector [ combinator simple_selector ]*
//    ;
    private void selector(final Ruleset ruleset) throws IOException {
        //System.out.println("selector()");
        final List<Selector> selectors = new ArrayList<Selector>();
        final List<Token> combinators = new ArrayList<Token>();
        selectors.add(simple_selector(ruleset));
        LOOP:
        while (true) {
            Token t = la();
            switch (t.getType()) {
                case Token.PLUS:
                case Token.GREATER:
                case Token.S:
                    combinators.add(combinator());
                    t = la();
                    switch (t.getType()) {
                        case Token.IDENT:
                        case Token.ASTERISK:
                        case Token.HASH:
                        case Token.PERIOD:
                        case Token.LBRACKET:
                        case Token.COLON:
                            selectors.add(simple_selector(ruleset));
                            break;
                        default:
                            throw new CSSParseException(t, new Token[] { Token.TK_IDENT,
                                    Token.TK_ASTERISK, Token.TK_HASH, Token.TK_PERIOD,
                                    Token.TK_LBRACKET, Token.TK_COLON }, getCurrentLine());
                    }
                    break;
                default:
                    break LOOP;
            }
        }
        ruleset.addFSSelector(mergeSimpleSelectors(selectors, combinators));
    }

    private Selector mergeSimpleSelectors(final List<Selector> selectors, final List<Token> combinators) {
        final int count = selectors.size();
        if (count == 1) {
            return selectors.get(0);
        }

        int lastDescendantOrChildAxis = Selector.DESCENDANT_AXIS;
        Selector result = null;
        for (int i = 0; i < count - 1; i++) {
            final Selector first = selectors.get(i);
            final Selector second = selectors.get(i+1);
            final Token combinator = combinators.get(i);

            if (first.getPseudoElement() != null) {
            	throw new CSSParseException(LangId.NO_DUEL_PSEUDOS, getCurrentLine());
            }

            boolean sibling = false;
            if (combinator == Token.TK_S) {
                second.setAxis(Selector.DESCENDANT_AXIS);
                lastDescendantOrChildAxis = Selector.DESCENDANT_AXIS;
            } else if (combinator == Token.TK_GREATER) {
                second.setAxis(Selector.CHILD_AXIS);
                lastDescendantOrChildAxis = Selector.CHILD_AXIS;
            } else if (combinator == Token.TK_PLUS) {
                first.setAxis(Selector.IMMEDIATE_SIBLING_AXIS);
                sibling = true;
            }

            second.setSpecificityB(second.getSpecificityB() + first.getSpecificityB());
            second.setSpecificityC(second.getSpecificityC() + first.getSpecificityC());
            second.setSpecificityD(second.getSpecificityD() + first.getSpecificityD());

            if (! sibling) {
                if (result == null) {
                    result = first;
                }
                first.setChainedSelector(second);
            } else {
                second.setSiblingSelector(first);
                if (result == null || result == first) {
                    result = second;
                }
                if (i > 0) {
                    for (int j = i-1; j >= 0; j--) {
                        final Selector selector = selectors.get(j);
                        if (selector.getChainedSelector() == first) {
                            selector.setChainedSelector(second);
                            second.setAxis(lastDescendantOrChildAxis);
                            break;
                        }
                    }
                }
            }
        }

        return result;
    }

//  simple_selector
//    : typed_value [ HASH | class | attrib | pseudo ]*
//    | [ HASH | class | attrib | pseudo ]+
//    ;
    private Selector simple_selector(final Ruleset ruleset) throws IOException {
        //System.out.println("simple_selector()");
        final Selector selector = new Selector();
        selector.setParent(ruleset);
        Token t = la();
        switch (t.getType()) {
            case Token.ASTERISK:
            case Token.IDENT:
            case Token.VERTICAL_BAR:
                final NamespacePair pair = typedValue(false);
                selector.setNamespaceURI(pair.getNamespaceURI());
                selector.setName(pair.getName());

                LOOP: while (true) {
                    t = la();
                    switch (t.getType()) {
                        case Token.HASH:
                            t = next();
                            selector.addIDCondition(getTokenValue(t, true));
                            break;
                        case Token.PERIOD:
                            classSelector(selector);
                            break;
                        case Token.LBRACKET:
                            attrib(selector);
                            break;
                        case Token.COLON:
                            pseudo(selector);
                            break;
                        default:
                            break LOOP;
                    }
                }
                break;
            default:
                boolean found = false;
                LOOP: while (true) {
                    t = la();
                    switch (t.getType()) {
                        case Token.HASH:
                            t = next();
                            selector.addIDCondition(getTokenValue(t, true));
                            found = true;
                            break;
                        case Token.PERIOD:
                            classSelector(selector);
                            found = true;
                            break;
                        case Token.LBRACKET:
                            attrib(selector);
                            found = true;
                            break;
                        case Token.COLON:
                            pseudo(selector);
                            found = true;
                            break;
                        default:
                            if (!found) {
                                throw new CSSParseException(t, new Token[] { Token.TK_HASH,
                                        Token.TK_PERIOD, Token.TK_LBRACKET, Token.TK_COLON },
                                        getCurrentLine());
                            }
                            break LOOP;
                    }
                }
        }
        return selector;
    }

//    type_selector
//    : [ namespace_prefix ]? element_name | IDENT
//    ;
//    namespace_prefix
//    : [ IDENT | '*' ]? '|'
//    ;
    private NamespacePair typedValue(final boolean matchAttribute) throws IOException {
        String prefix = null;
        String name = null;

        Token t = la();
        if (t == Token.TK_ASTERISK || t == Token.TK_IDENT) {
            next();
            if (t == Token.TK_IDENT) {
                name = getTokenValue(t, true);
            }
            t = la();
        } else if (t == Token.TK_VERTICAL_BAR) {
            prefix = TreeResolver.NO_NAMESPACE;
        } else {
            throw new CSSParseException(
                    t, new Token[] { Token.TK_ASTERISK, Token.TK_IDENT, Token.TK_VERTICAL_BAR },
                    getCurrentLine());
        }

        if (t == Token.TK_VERTICAL_BAR) {
            next();
            t = next();
            if (t == Token.TK_ASTERISK || t == Token.TK_IDENT) {
                if (prefix == null) {
                    prefix = name;
                }
                if (t == Token.TK_IDENT) {
                    name = getTokenValue(t, true);
                }
            } else {
                throw new CSSParseException(
                        t, new Token[] { Token.TK_ASTERISK, Token.TK_IDENT }, getCurrentLine());
            }
        }

        String namespaceURI = null;
        if (prefix != null && prefix != TreeResolver.NO_NAMESPACE) {
            namespaceURI = _namespaces.get(prefix.toLowerCase());
            if (namespaceURI == null) {
            	throw new CSSParseException(LangId.NO_NAMESPACE_FOUND, getCurrentLine(), prefix);
            }
        } else if (prefix == null && ! matchAttribute) {
            namespaceURI = _namespaces.get(null);
        }

        if (matchAttribute && name == null) {
            throw new CSSParseException(LangId.ATTR_REQUIRED, getCurrentLine());
        }

        return new NamespacePair(namespaceURI, name);
    }

//  class
//    : '.' IDENT
//    ;
    private void classSelector(final Selector selector) throws IOException {
        //System.out.println("class_selector()");
        Token t = next();
        if (t == Token.TK_PERIOD) {
            t = next();
            if (t == Token.TK_IDENT) {
                selector.addClassCondition(getTokenValue(t, true));
            } else {
                push(t);
                throw new CSSParseException(t, Token.TK_IDENT, getCurrentLine());
            }
        } else {
            push(t);
            throw new CSSParseException(t, Token.TK_PERIOD, getCurrentLine());
        }
    }

//  element_name
//    : IDENT | '*'
//    ;
    /*
    private String element_name() throws IOException {
        //System.out.println("element_name()");
        Token t = next();
        if (t == Token.TK_IDENT || t == Token.TK_ASTERISK) {
            return getTokenValue(t, true);
        } else {
            push(t);
            throw new CSSParseException(
                    t, new Token[] { Token.TK_IDENT, Token.TK_ASTERISK }, getCurrentLine());
        }
    }
    */

//    attrib
//    : '[' S* [ namespace_prefix ]? IDENT S*
//          [ [ PREFIXMATCH |
//              SUFFIXMATCH |
//              SUBSTRINGMATCH |
//              '=' |
//              INCLUDES |
//              DASHMATCH ] S* [ IDENT | STRING ] S*
//          ]? ']'
//    ;
    private void attrib(final Selector selector) throws IOException {
        //System.out.println("attrib()");
        Token t = next();
        if (t == Token.TK_LBRACKET) {
            skipWhitespace();
            t = la();
            if (t == Token.TK_IDENT || t == Token.TK_ASTERISK || t == Token.TK_VERTICAL_BAR) {
                boolean existenceMatch = true;
                final NamespacePair pair = typedValue(true);
                final String attrNamespaceURI = pair.getNamespaceURI();
                final String attrName = pair.getName();
                skipWhitespace();
                t = la();
                switch (t.getType()) {
                    case Token.EQUALS:
                    case Token.INCLUDES:
                    case Token.DASHMATCH:
                    case Token.PREFIXMATCH:
                    case Token.SUFFIXMATCH:
                    case Token.SUBSTRINGMATCH:
                        existenceMatch = false;
                        final Token selectorType = next();
                        skipWhitespace();
                        t = next();
                        if (t == Token.TK_IDENT || t == Token.TK_STRING) {
                            final String value = getTokenValue(t, true);
                            switch (selectorType.getType()) {
                                case Token.EQUALS:
                                    selector.addAttributeEqualsCondition(attrNamespaceURI, attrName, value);
                                    break;
                                case Token.DASHMATCH:
                                    selector.addAttributeMatchesFirstPartCondition(attrNamespaceURI, attrName, value);
                                    break;
                                case Token.INCLUDES:
                                    selector.addAttributeMatchesListCondition(attrNamespaceURI, attrName, value);
                                    break;
                                case Token.PREFIXMATCH:
                                    selector.addAttributePrefixCondition(attrNamespaceURI, attrName, value);
                                    break;
                                case Token.SUFFIXMATCH:
                                    selector.addAttributeSuffixCondition(attrNamespaceURI, attrName, value);
                                    break;
                                case Token.SUBSTRINGMATCH:
                                    selector.addAttributeSubstringCondition(attrNamespaceURI, attrName, value);
                                    break;
                            }
                            skipWhitespace();
                        } else {
                            push(t);
                            throw new CSSParseException(t,
                                    new Token[] { Token.TK_IDENT, Token.TK_STRING },
                                    getCurrentLine());
                        }
                        skipWhitespace();
                        t = la();
                        break;
                }
                if (existenceMatch) {
                    selector.addAttributeExistsCondition(attrNamespaceURI, attrName);
                }
                if (t == Token.TK_RBRACKET) {
                    next();
                } else {
                    throw new CSSParseException(t, new Token[] { Token.TK_EQUALS,
                            Token.TK_INCLUDES, Token.TK_DASHMATCH, Token.TK_PREFIXMATCH,
                            Token.TK_SUFFIXMATCH, Token.TK_SUBSTRINGMATCH, Token.TK_RBRACKET },
                            getCurrentLine());
                }
            } else {
                throw new CSSParseException(
                        t, new Token[] { Token.TK_IDENT, Token.TK_ASTERISK }, getCurrentLine());
            }
        } else {
            push(t);
            throw new CSSParseException(t, Token.TK_LBRACKET, getCurrentLine());
        }
    }

    private void addPseudoClassOrElement(final Token t, final Selector selector) {
        final String value = getTokenValue(t);
        if (value.equals("link")) {
            selector.addLinkCondition();
        } else if (value.equals("visited")) {
            selector.setPseudoClass(Selector.VISITED_PSEUDOCLASS);
        } else if (value.equals("hover")) {
            selector.setPseudoClass(Selector.HOVER_PSEUDOCLASS);
        } else if (value.equals("focus")) {
            selector.setPseudoClass(Selector.FOCUS_PSEUDOCLASS);
        } else if (value.equals("active")) {
            selector.setPseudoClass(Selector.ACTIVE_PSEUDOCLASS);
        } else if (value.equals("first-child")) {
            selector.addFirstChildCondition();
        } else if (value.equals("even")) {
            selector.addEvenChildCondition();
        } else if (value.equals("odd")) {
            selector.addOddChildCondition();
        } else if (value.equals("last-child")) {
            selector.addLastChildCondition();
        } else if (CSS21_PSEUDO_ELEMENTS.contains(value)){
            selector.setPseudoElement(value);
        } else {
            throw new CSSParseException(LangId.UNRECOGNIZED_PSEUDO, getCurrentLine(), value);
        }
    }

    private void addPseudoClassOrElementFunction(Token t, final Selector selector) throws IOException {
        String f = getTokenValue(t);
        f = f.substring(0, f.length()-1);

        if (f.equals("lang")) {
            skipWhitespace();
            t = next();
            if (t == Token.TK_IDENT) {
                final String lang = getTokenValue(t);
                selector.addLangCondition(lang);
                skipWhitespace();
                t = next();
            } else {
                push(t);
                throw new CSSParseException(t, Token.TK_IDENT, getCurrentLine());
            }
        } else if (f.equals("nth-child")) {
            final StringBuilder number = new StringBuilder();
            while ((t = next()) != null && (t == Token.TK_IDENT || t == Token.TK_S || t == Token.TK_NUMBER || t == Token.TK_DIMENSION || t == Token.TK_PLUS || t == Token.TK_MINUS)) {
                number.append(getTokenValue(t));
            }

            try {
                selector.addNthChildCondition(number.toString());
            } catch (final CSSParseException e) {
                e.setLine(getCurrentLine());
                push(t);
                throw e;
            }
        } else {
            push(t);
            throw new CSSParseException(LangId.FUNCTION_NOT_SUPPORTED, getCurrentLine(), f.toString());
        }

        if (t != Token.TK_RPAREN) {
            push(t);
            throw new CSSParseException(t, Token.TK_RPAREN, getCurrentLine());
        }
    }

    private void addPseudoElement(final Token t, final Selector selector) {
        final String value = getTokenValue(t);
        if (SUPPORTED_PSEUDO_ELEMENTS.contains(value)) {
            selector.setPseudoElement(value);
        } else {
        	throw new CSSParseException(LangId.UNRECOGNIZED_PSEUDO, getCurrentLine(), value);
        }
    }

//  pseudo
//    : ':' ':'? [ IDENT | FUNCTION S* IDENT? S* ')' ]
//    ;
    private void pseudo(final Selector selector) throws IOException {
        //System.out.println("pseudo()");
        Token t = next();
        if (t == Token.TK_COLON) {
            t = next();
            switch (t.getType()) {
                case Token.COLON:
                    t = next();
                    addPseudoElement(t, selector);
                    break;
                case Token.IDENT:
                    addPseudoClassOrElement(t, selector);
                    break;
                case Token.FUNCTION:
                    addPseudoClassOrElementFunction(t, selector);
                    break;
                default:
                    push(t);
                    throw new CSSParseException(t,
                            new Token[] { Token.TK_IDENT, Token.TK_FUNCTION }, getCurrentLine());
            }
        } else {
            push(t);
            throw new CSSParseException(t, Token.TK_COLON, getCurrentLine());
        }
    }

    private boolean checkCSSName(final CSSName cssName, final String propertyName) {
        if (cssName == null) {
            _errorHandler.error(
                    _URI,
                    getCurrentLine(),
                    LangId.UNRECOGNIZED_PROPERTY, propertyName);
            return false;
        }

        if (! CSSName.isImplemented(cssName)) {
            _errorHandler.error(
                    _URI,
                    getCurrentLine(),
                    LangId.UNIMPLEMENTED_PROPERTY, propertyName);
            return false;
        }

        final PropertyBuilder builder = CSSName.getPropertyBuilder(cssName);
        if (builder == null) {
            _errorHandler.error(
                    _URI,
                    getCurrentLine(),
                    LangId.NO_PROPERTY_BUILDER, propertyName);
            return false;
        }

        return true;
    }

//  declaration
//    : property ':' S* expr prio?
//    ;
    private void declaration(final Ruleset ruleset, final boolean inFontFace) throws IOException {
        //System.out.println("declaration()");
        try {
            Token t = la();
            if (t == Token.TK_IDENT) {
                final String propertyName = property();
                final CSSName cssName = CSSName.getByPropertyName(propertyName);

                final boolean valid = checkCSSName(cssName, propertyName);

                t = next();
                if (t == Token.TK_COLON) {
                    skipWhitespace();

                    final List<PropertyValue> values = expr(
                            cssName == CSSName.FONT_FAMILY ||
                            cssName == CSSName.FONT_SHORTHAND ||
                            cssName == CSSName.FS_PDF_FONT_ENCODING);
                    boolean important = false;

                    t = la();
                    if (t == Token.TK_IMPORTANT_SYM) {
                        prio();
                        important = true;
                    }

                    t = la();
                    if (! (t == Token.TK_SEMICOLON || t == Token.TK_RBRACE || t == Token.TK_EOF)) {
                        throw new CSSParseException(
                                t,
                                new Token[] { Token.TK_SEMICOLON, Token.TK_RBRACE },
                                getCurrentLine());
                    }

                    if (valid) {
                        try {
                            final PropertyBuilder builder = CSSName.getPropertyBuilder(cssName);
                            ruleset.addAllProperties(builder.buildDeclarations(
                                    cssName, values, ruleset.getOrigin(), important, !inFontFace));
                        } catch (final CSSParseException e) {
                            e.setLine(getCurrentLine());
                            error(e, "declaration", true);
                        }
                    }
                } else {
                    push(t);
                    throw new CSSParseException(t, Token.TK_COLON, getCurrentLine());
                }
            } else {
                throw new CSSParseException(t, Token.TK_IDENT, getCurrentLine());
            }
        } catch (final CSSParseException e) {
            error(e, "declaration", true);
            recover(false, true);
        }
    }

//  prio
//    : IMPORTANT_SYM S*
//    ;
    private void prio() throws IOException {
        //System.out.println("prio()");
        final Token t = next();
        if (t == Token.TK_IMPORTANT_SYM) {
            skipWhitespace();
        } else {
            push(t);
            throw new CSSParseException(t, Token.TK_IMPORTANT_SYM, getCurrentLine());
        }
    }

//  expr
//    : term [ operator term ]*
//    ;
    private List<PropertyValue> expr(final boolean literal) throws IOException {
        //System.out.println("expr()");
        final List<PropertyValue> result = new ArrayList<PropertyValue>(10);
        result.add(term(literal));
        LOOP: while (true) {
            Token t = la();
            boolean operator = false;
            Token operatorToken = null;
            switch (t.getType()) {
                case Token.VIRGULE:
                case Token.COMMA:
                    operatorToken = t;
                    operator();
                    t = la();
                    operator = true;
                    break;
            }
            switch (t.getType()) {
                case Token.PLUS:
                case Token.MINUS:
                case Token.NUMBER:
                case Token.PERCENTAGE:
                case Token.PX:
                case Token.CM:
                case Token.MM:
                case Token.IN:
                case Token.PT:
                case Token.PC:
                case Token.EMS:
                case Token.EXS:
                case Token.ANGLE:
                case Token.TIME:
                case Token.FREQ:
                case Token.STRING:
                case Token.IDENT:
                case Token.URI:
                case Token.HASH:
                case Token.FUNCTION:
                    final PropertyValue term = term(literal);
                    if (operatorToken != null) {
                        term.setOperator(operatorToken);
                    }
                    result.add(term);
                    break;
                default:
                    if (operator) {
                        throw new CSSParseException(t, new Token[] {
                                Token.TK_NUMBER, Token.TK_PLUS, Token.TK_MINUS,
                                Token.TK_PERCENTAGE, Token.TK_PX, Token.TK_EMS, Token.TK_EXS,
                                Token.TK_PC, Token.TK_MM, Token.TK_CM, Token.TK_IN, Token.TK_PT,
                                Token.TK_ANGLE, Token.TK_TIME, Token.TK_FREQ, Token.TK_STRING,
                                Token.TK_IDENT, Token.TK_URI, Token.TK_HASH, Token.TK_FUNCTION },
                                getCurrentLine());
                    } else {
                        break LOOP;
                    }
            }
        }

        return result;
    }

    private String extractNumber(final Token t) {
        final String token = getTokenValue(t);

        int offset = 0;
        final char[] ch = token.toCharArray();
        for (final char c : ch) {
            if (c < '0' || c > '9') {
                break;
            }
            offset++;
        }
        if (ch[offset] == '.') {
            offset++;

            for (int i = offset; i < ch.length; i++) {
                final char c = ch[i];
                if (c < '0' || c > '9') {
                    break;
                }
                offset++;
            }
        }

        return token.substring(0, offset);
    }

    private String extractUnit(final Token t) {
        final String s = extractNumber(t);
        return getTokenValue(t).substring(s.length());
    }

    private String sign(final float sign) {
        return sign == -1.0f ? "-" : "";
    }

//  term
//    : unary_operator?
//      [ NUMBER S* | PERCENTAGE S* | LENGTH S* | EMS S* | EXS S* | ANGLE S* |
//        TIME S* | FREQ S* ]
//    | STRING S* | IDENT S* | URI S* | hexcolor | function
//    ;
    private PropertyValue term(final boolean literal) throws IOException {
        //System.out.println("term()");
        float sign = 1;
        Token t = la();
        if (t == Token.TK_PLUS || t == Token.TK_MINUS) {
            sign = unaryOperator();
            t = la();
        }
        
        PropertyValue result = null;
        
        switch (t.getType()) {
            case Token.ANGLE:
            {
            	String unit = extractUnit(t);
            	CSSPrimitiveUnit type;
            	
            	if ("deg".equals(unit))
            	{
            		type = CSSPrimitiveUnit.CSS_DEG;
            	}
            	else if ("rad".equals(unit))
            	{
            		type = CSSPrimitiveUnit.CSS_RAD;
            	}
            	else
            	{
            		throw new CSSParseException(LangId.UNSUPPORTED_CSS_UNIT, getCurrentLine(), unit);
            	}
 
            	result = new PropertyValueImp(type,
                        sign * Float.parseFloat(extractNumber(t)),
                        sign(sign) + getTokenValue(t));

            	next();
                skipWhitespace();
                break;
            }
            case Token.TIME:
            case Token.FREQ:
            	throw new CSSParseException(LangId.UNSUPPORTED_CSS_UNIT, getCurrentLine(), extractUnit(t));
            case Token.DIMENSION:
            {
            	String unit = extractUnit(t);
            	CSSPrimitiveUnit type;
            	
            	if ("dppx".equals(unit))
            	{
            		type = CSSPrimitiveUnit.CSS_DPPX;
            	}
            	else if ("dpi".equals(unit))
            	{
            		type = CSSPrimitiveUnit.CSS_DPI;
            	}
            	else if ("dpcm".equals(unit))
            	{
            		type = CSSPrimitiveUnit.CSS_DPCM;
            	}
            	else
            	{
            		throw new CSSParseException(LangId.UNSUPPORTED_CSS_UNIT, getCurrentLine(), unit);
            	}
 
            	result = new PropertyValueImp(type,
                        sign * Float.parseFloat(extractNumber(t)),
                        sign(sign) + getTokenValue(t));

            	next();
                skipWhitespace();
                break;
            }
            case Token.NUMBER:
                result = new PropertyValueImp(
                        CSSPrimitiveUnit.CSS_NUMBER,
                        sign*Float.parseFloat(getTokenValue(t)),
                        sign(sign) + getTokenValue(t));
                next();
                skipWhitespace();
                break;
            case Token.PERCENTAGE:
                result = new PropertyValueImp(
                		CSSPrimitiveUnit.CSS_PERCENTAGE,
                        sign*Float.parseFloat(extractNumber(t)),
                        sign(sign) + getTokenValue(t));
                next();
                skipWhitespace();
                break;
            case Token.EMS:
                result = new PropertyValueImp(
                		CSSPrimitiveUnit.CSS_EMS,
                        sign*Float.parseFloat(extractNumber(t)),
                        sign(sign) + getTokenValue(t));
                next();
                skipWhitespace();
                break;
            case Token.EXS:
                result = new PropertyValueImp(
                		CSSPrimitiveUnit.CSS_EXS,
                        sign*Float.parseFloat(extractNumber(t)),
                        sign(sign) + getTokenValue(t));
                next();
                skipWhitespace();
                break;
            case Token.PX:
                result = new PropertyValueImp(
                		CSSPrimitiveUnit.CSS_PX,
                        sign*Float.parseFloat(extractNumber(t)),
                        sign(sign) + getTokenValue(t));
                next();
                skipWhitespace();
                break;
            case Token.CM:
                result = new PropertyValueImp(
                		CSSPrimitiveUnit.CSS_CM,
                        sign*Float.parseFloat(extractNumber(t)),
                        sign(sign) + getTokenValue(t));
                next();
                skipWhitespace();
                break;
            case Token.MM:
                result = new PropertyValueImp(
                		CSSPrimitiveUnit.CSS_MM,
                        sign*Float.parseFloat(extractNumber(t)),
                        sign(sign) + getTokenValue(t));
                next();
                skipWhitespace();
                break;
            case Token.IN:
                result = new PropertyValueImp(
                		CSSPrimitiveUnit.CSS_IN,
                        sign*Float.parseFloat(extractNumber(t)),
                        sign(sign) + getTokenValue(t));
                next();
                skipWhitespace();
                break;
            case Token.PT:
                result = new PropertyValueImp(
                		CSSPrimitiveUnit.CSS_PT,
                        sign*Float.parseFloat(extractNumber(t)),
                        sign(sign) + getTokenValue(t));
                next();
                skipWhitespace();
                break;
            case Token.PC:
                result = new PropertyValueImp(
                		CSSPrimitiveUnit.CSS_PC,
                        sign*Float.parseFloat(extractNumber(t)),
                        sign(sign) + getTokenValue(t));
                next();
                skipWhitespace();
                break;
            case Token.STRING:
                final String s = getTokenValue(t);
                result = new PropertyValueImp(
                		CSSPrimitiveUnit.CSS_STRING,
                        s,
                        getRawTokenValue());
                next();
                skipWhitespace();
                break;
            case Token.IDENT:
                final String value = getTokenValue(t, literal);
                result = new PropertyValueImp(
                		CSSPrimitiveUnit.CSS_IDENT,
                        value,
                        value);
                next();
                skipWhitespace();
                break;
            case Token.URI:
                result = new PropertyValueImp(
                		CSSPrimitiveUnit.CSS_URI,
                        getTokenValue(t),
                        getRawTokenValue());
                next();
                skipWhitespace();
                break;
            case Token.HASH:
                result = hexcolor();
                break;
            case Token.FUNCTION:
                result = function();
                break;
            default:
                throw new CSSParseException(t, new Token[] { Token.TK_NUMBER,
                        Token.TK_PERCENTAGE, Token.TK_PX, Token.TK_EMS, Token.TK_EXS,
                        Token.TK_PC, Token.TK_MM, Token.TK_CM, Token.TK_IN, Token.TK_PT,
                        Token.TK_ANGLE, Token.TK_TIME, Token.TK_FREQ, Token.TK_STRING,
                        Token.TK_IDENT, Token.TK_URI, Token.TK_HASH, Token.TK_FUNCTION },
                        getCurrentLine());
        }
        return result;
    }

    
    /**
     * Parses a CSS function such as rgb, rgba, linear-gradient.
     */
//  function
//    : FUNCTION S* expr ')' S*
//    ;
    private PropertyValue function() throws IOException {
        //System.out.println("function()");
        PropertyValue result = null;
        Token t = next();
        
        assert(t == Token.TK_FUNCTION);
        
		final String f = getTokenValue(t);
		skipWhitespace();
		final List<PropertyValue> params = expr(false);
		t = next();

		if (t != Token.TK_RPAREN) 
		{
			push(t);
			throw new CSSParseException(t, Token.TK_RPAREN, getCurrentLine());
		}

		if ("rgb(".equals(f)) 
		{
			result = new PropertyValueImp(createRGBColorFromRGBFunction(params));
		}
		else if ("rgba(".equals(f)) {
			result = new PropertyValueImp(
					createRGBColorFromRGBAFunction(params));
		}
		else if ("cmyk(".equals(f)) 
		{
			if (!isSupportCMYKColors()) 
			{
				throw new CSSParseException(LangId.CMYK_NOT_SUPPORTED, getCurrentLine());
			}
			// in accordance to http://www.w3.org/TR/css3-gcpm/#cmyk-colors
			result = new PropertyValueImp(createCMYKColorFromFunction(params));
		}
		else 
		{
			result = new PropertyValueImp(new FSFunction(f.substring(0,
					f.length() - 1), params));
		}

		skipWhitespace();
        return result;
    }

    /**
     * Parses CMYK parameters to a CMYK color.
     */
    private FSCMYKColor createCMYKColorFromFunction(final List<PropertyValue> params) {
        if (params.size() != 4) {
        	throw new CSSParseException(LangId.EXACTLY_PARAMS_REQUIRED, getCurrentLine(), "cmyk", 4);
        }

        final float[] colorComponents = new float[4];

        for (int i = 0; i < params.size(); i++) {
            colorComponents[i] = parseCMYKColorComponent(params.get(i), (i+1)); //Warning on the truncation?
        }

        return new FSCMYKColor(colorComponents[0], colorComponents[1], colorComponents[2], colorComponents[3]);
    }

    /**
     * Parses a CMYK color component.
     */
    private float parseCMYKColorComponent(final PropertyValue value, final int paramNo) {
        final CSSPrimitiveUnit type = value.getPrimitiveTypeN();
        float result;
        if (type == CSSPrimitiveUnit.CSS_NUMBER) {
            result = value.getFloatValue();
        } else if (type == CSSPrimitiveUnit.CSS_PERCENTAGE) {
            result = value.getFloatValue() / 100.0f;
        } else {
        	throw new CSSParseException(LangId.COLOR_FUNC_COMPONENT_INVALID, getCurrentLine(), paramNo, "cmyk");
        }

        if (result < 0.0f || result > 1.0f) {
        	throw new CSSParseException(LangId.COLOR_FUNC_COMPONENT_INVALID, getCurrentLine(), paramNo, "cmyk");
        }

        return result;
    }

    /**
     * Returns an RGBA color from the rgba() function.
     */
    private FSRGBColor createRGBColorFromRGBAFunction(final List<PropertyValue> params) 
    {
    	if (params.size() != 4) 
    	{
    		throw new CSSParseException(LangId.EXACTLY_PARAMS_REQUIRED, getCurrentLine(), "rgba", 4);
        }
    
    	return createRGBColorFromFunction(params);
    }
    	
    /**
     * Returns an RGBA color from the rgb() function.
     */
    private FSRGBColor createRGBColorFromRGBFunction(final List<PropertyValue> params) 
    {
    	if (params.size() != 3) 
    	{
    		throw new CSSParseException(LangId.EXACTLY_PARAMS_REQUIRED, getCurrentLine(), "rgb", 3);
        }
    
    	return createRGBColorFromFunction(params);
    }

    /**
     * Returns an RGBA color from a function.
     * Do not call this directly but one of the above methods for
     * either rgb or rgba functions.
     */
    private FSRGBColor createRGBColorFromFunction(final List<PropertyValue> params) 
    {
    	assert(params.size() == 3 || params.size() == 4);
    	
        int red = 0;
        int green = 0;
        int blue = 0;
        float alpha = 1;
        
        for (int i = 0; i < params.size(); i++) {
            final PropertyValue value = params.get(i);
            final CSSPrimitiveUnit type = value.getPrimitiveTypeN();
            if (type != CSSPrimitiveUnit.CSS_PERCENTAGE &&
                type != CSSPrimitiveUnit.CSS_NUMBER) 
            {
            	throw new CSSParseException(LangId.COLOR_FUNC_COMPONENT_INVALID, getCurrentLine(), (i + 1), "rgb/rgba");
            }

			if (type != CSSPrimitiveUnit.CSS_PERCENTAGE &&
				type != CSSPrimitiveUnit.CSS_NUMBER && i == 3) 
			{
				throw new CSSParseException(LangId.COLOR_FUNC_COMPONENT_INVALID, getCurrentLine(), (i + 1), "rgba");
			}
            
            float f = value.getFloatValue();
            if (type == CSSPrimitiveUnit.CSS_PERCENTAGE) {
                f = f / 100f * (i == 3 ? 1f : 255);
            }

            if (f < 0 || (i == 3 && f > 1) || f > 255) 
            {
				if (f < 0)
					f = 0;
				else if (i == 3 && f > 1)
					f = 1;
				else if (f > 255) 
					f = 255;
            }

            switch (i) {
                case 0:
                    red = (int)f;
                    break;
                case 1:
                    green = (int)f;
                    break;
                case 2:
                    blue = (int)f;
                    break;
                case 3:
                	alpha = f;
                	break;
            }
        }

        return new FSRGBColor(red, green, blue, alpha);
    }

	/**
	 * Returns a RGB color from a hex color string.
	 * There is a constraint on the color that it must have either 3 or 6
	 * hex-digits (i.e., [0-9a-fA-F]) after the "#"; e.g., "#000" is OK, but
	 * "#abcd" is not.
	 */
	// hexcolor
    //   : HASH S*
    //   ;
    private PropertyValue hexcolor() throws IOException {
        //System.out.println("hexcolor()");
        final Token t = next();

        // We only get here on a peeked hash token.
        assert(t == Token.TK_HASH);

        final String s = getTokenValue(t);

        if ((s.length() != 3 && s.length() != 6) || !isHexString(s)) 
        {
			push(t);
			throw new CSSParseException(LangId.INVALID_HEX_COLOR, getCurrentLine(), s);
		}

        FSRGBColor color;

        if (s.length() == 3) 
        {
			color = new FSRGBColor(convertToInteger(s.charAt(0), s.charAt(0)),
					convertToInteger(s.charAt(1), s.charAt(1)),
					convertToInteger(s.charAt(2), s.charAt(2)));
		}
        else 
        {
			assert (s.length() == 6);
			color = new FSRGBColor(convertToInteger(s.charAt(0), s.charAt(1)),
					convertToInteger(s.charAt(2), s.charAt(3)),
					convertToInteger(s.charAt(4), s.charAt(5)));
		}
        
		PropertyValue result = new PropertyValueImp(color);
		skipWhitespace();
        return result;
    }

    /**
     * Checks if a string consists entirely of a hexadecimal number.
	 * No numeric range checking is performed so the string number
	 * might overflow a java integer type.
     */
    private boolean isHexString(final String s) {
        for (int i = 0; i < s.length(); i++) {
            if (! isHexChar(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Convert a two char hexadecimal number to an integer
     * in the range (0 - 255).
     */
    private int convertToInteger(final char hexchar1, final char hexchar2) {
        int result = convertToInteger(hexchar1);
        result <<= 4;
        result |= convertToInteger(hexchar2);
        return result;
    }

    /**
     * Converts a hexadecimal character to a number (1 - 15)
     * Must be a hex character [0-9A-Fa-f].
     */
    private int convertToInteger(final char hexchar1) {
        if (hexchar1 >= '0' && hexchar1 <= '9') {
            return hexchar1 - '0';
        } else if (hexchar1 >= 'a' && hexchar1 <= 'f') {
            return hexchar1 - 'a' + 10;
        } else {
        	assert(hexchar1 >= 'A' && hexchar1 <= 'F');
        	return hexchar1 - 'A' + 10;
        }
    }

    /**
     * Skips whitespace characters.
     * Note that CSS style comments are ignored by the tokenizer.
     */
    private void skipWhitespace() throws IOException
    {
        Token t;
        while ( (t = next()) == Token.TK_S) {
            // skip
        }
        push(t);
    }

    /**
     * Skips whitespace and HTML/XML style comments.
     * Note that CSS style comments are ignored by the tokenizer.
     */
    private void skipWhitespaceAndCdocdc() throws IOException 
    {
        Token t;
        while (true) {
            t = next();
            if (! (t == Token.TK_S || t == Token.TK_CDO || t == Token.TK_CDC)) {
                break;
            }
        }
        push(t);
    }

    /** 
     * Returns the next token from the lexer or the
     * pushback token (placed there by push).
     */
    private Token next() throws IOException {
        if (_saved != null) {
            final Token result = _saved;
            _saved = null;
            return result;
        } else {
            return _lexer.yylex();
        }
    }

    /**
     * Pushes a token back. There can only be one saved
     * token.
     * @param t
     */
    private void push(final Token t) {
    	assert(_saved == null);
        _saved = t;
    }

    /**
     * Peeks at the next token.
     */
    private Token la() throws IOException {
        final Token result = next();
        push(result);
        return result;
    }

    private void error(final CSSParseException e, final String what, final boolean rethrowEOF) {
        if (! e.isCallerNotified()) {
            _errorHandler.error(_URI, e.getLine(), e.getMessageId(), e.getMessageArguments());
        }
        e.setCallerNotified(true);
        if (e.isEOF() && rethrowEOF) {
            throw e;
        }
    }

    private void recover(final boolean needBlock, final boolean stopBeforeBlockClose) throws IOException {
        int braces = 0;
        boolean foundBlock = false;
        LOOP:
        while (true) {
            final Token t = next();
            if (t == Token.TK_EOF) {
                return;
            }
            switch (t.getType()) {
                case Token.LBRACE:
                    foundBlock = true;
                    braces++;
                    break;
                case Token.RBRACE:
                    if (braces == 0) {
                        if (stopBeforeBlockClose) {
                            push(t);
                            break LOOP;
                        }
                    } else {
                        braces--;
                        if (braces == 0) {
                            break LOOP;
                        }
                    }
                    break;
                case Token.SEMICOLON:
                    if (braces == 0 && ((! needBlock) || foundBlock)) {
                        break LOOP;
                    }
                    break;
            }
        }
        skipWhitespace();
    }

    public void reset(final Reader r) {
        _saved = null;
        _namespaces.clear();
        _lexer.yyreset(r);
        _lexer.setyyline(0);
    }

    public CSSErrorHandler getErrorHandler() {
        return _errorHandler;
    }

    public void setErrorHandler(final CSSErrorHandler errorHandler) {
        _errorHandler = errorHandler;
    }

    private String getRawTokenValue() {
        return _lexer.yytext();
    }

    private String getTokenValue(final Token t) {
        return getTokenValue(t, false);
    }

    private String getTokenValue(final Token t, final boolean literal) {
        int start;
        int count;
        switch (t.getType()) {
            case Token.STRING:
                count = _lexer.yylength();
                return processEscapes(_lexer.yytext().toCharArray(), 1, count-1);
            case Token.HASH:
                count = _lexer.yylength();
                return processEscapes(_lexer.yytext().toCharArray(), 1, count);
            case Token.URI:
                final char[] ch = _lexer.yytext().toCharArray();
                start = 4;
                while (ch[start] == '\t' || ch[start] == '\r' ||
                        ch[start] == '\n' || ch[start] == '\f') {
                    start++;
                }
                if (ch[start] == '\'' || ch[start] == '"') {
                    start++;
                }
                int end = ch.length-2;
                while (ch[end] == '\t' || ch[end] == '\r' ||
                        ch[end] == '\n' || ch[end] == '\f') {
                    end--;
                }
                if (ch[end] == '\'' || ch[end] == '"') {
                    end--;
                }

                String uriResult = processEscapes(ch, start, end+1);

                // Relative URIs are resolved relative to CSS file, not XHTML file
                if (_uac != null) 
                {
                	Optional<String> oUriResult = _uac.resolveURI(_URI, uriResult);
                	uriResult = oUriResult.orElse(null);
                }
                else if (isRelativeURI(uriResult)) {
                    final int lastSlash = _URI.lastIndexOf('/');
                    if (lastSlash != -1) {
                        uriResult = _URI.substring(0, lastSlash+1) + uriResult;
                    }
                }

                return uriResult;
            case Token.AT_RULE:
            case Token.IDENT:
            case Token.FUNCTION:
                start = 0;
                count = _lexer.yylength();
                if (t.getType() == Token.AT_RULE) {
                    start++;
                }
                String result = processEscapes(_lexer.yytext().toCharArray(), start, count);
                if (! literal) {
                    result = result.toLowerCase();
                }
                return result;
            default:
                return _lexer.yytext();
        }
    }

    private boolean isRelativeURI(final String uri) {
        try {
            return uri.length() > 0 && (uri.charAt(0) != '/' && ! new URI(uri).isAbsolute());
        } catch (final URISyntaxException e) {
            return false;
        }
    }

    private int getCurrentLine() {
        return _lexer.yyline();
    }

    private static boolean isHexChar(final char c) {
        return (c >= '0' && c <= '9') || (c >= 'A' && c <= 'F') || (c >= 'a' && c <= 'f');
    }

    private static String processEscapes(final char[] ch, final int start, final int end) {
        final StringBuffer result = new StringBuffer(ch.length + 10);

        for (int i = start; i < end; i++) {
            final char c = ch[i];

            if (c == '\\') {
                // eat escaped newlines and handle te\st == test situations
                if (i < end - 2 && (ch[i+1] == '\r' && ch[i+2] == '\n')) {
                    i += 2;
                    continue;
                } else {
                    if ((ch[i+1] == '\n' || ch[i+1] == '\r' || ch[i+1] == '\f')) {
                        i++;
                        continue;
                    } else if (! isHexChar(ch[i+1])) {
                        continue;
                    }
                }

                // Unicode escapes
                final int current = ++i;
                while (i < end && isHexChar(ch[i]) && i - current < 6) {
                    i++;
                }

                final int cvalue = Integer.parseInt(new String(ch, current, i - current), 16);
                if (cvalue < 0xFFFF) {
                    result.append((char)cvalue);
                }

                i--;

                if (i < end - 2 && (ch[i+1] == '\r' && ch[i+2] == '\n')) {
                    i += 2;
                } else if (i < end - 1 &&
                        (ch[i+1] == ' ' || ch[i+1] == '\t' ||
                                ch[i+1] == '\n' || ch[i+1] == '\r' ||
                                ch[i+1] == '\f')) {
                    i++;
                }
            } else {
                result.append(c);
            }
        }

        return result.toString();
    }

    public boolean isSupportCMYKColors() {
        return _supportCMYKColors;
    }

    public void setSupportCMYKColors(final boolean b) {
        _supportCMYKColors = b;
    }

    private static class NamespacePair {
        private final String _namespaceURI;
        private final String _name;

        public NamespacePair(final String namespaceURI, final String name) {
            _namespaceURI = namespaceURI;
            _name = name;
        }

        public String getNamespaceURI() {
            return _namespaceURI;
        }

        public String getName() {
            return _name;
        }
    }
}
