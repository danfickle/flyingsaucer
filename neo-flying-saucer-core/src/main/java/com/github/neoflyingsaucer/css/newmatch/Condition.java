/*
 * Condition.java
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
package com.github.neoflyingsaucer.css.newmatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Element;

import com.github.neoflyingsaucer.css.extend.AttributeResolver;
import com.github.neoflyingsaucer.css.extend.TreeResolver;
import com.github.neoflyingsaucer.css.parser.CSSParseException;
import com.github.neoflyingsaucer.extend.controller.error.LangId;
import com.github.neoflyingsaucer.extend.useragent.Optional;


/**
 * Part of a Selector
 *
 * @author tstgm
 */
abstract class Condition {

    abstract boolean matches(Element e, AttributeResolver attRes, TreeResolver treeRes);

    /**
     * the CSS condition [attribute]
     *
     * @param name PARAM
     * @return Returns
     */
    static Condition createAttributeExistsCondition(final String namespaceURI, final String name) {
        return new AttributeExistsCondition(namespaceURI, name);
    }

    /**
     * the CSS condition [attribute^=value]
     */
    static Condition createAttributePrefixCondition(final String namespaceURI, final String name, final String value) {
        return new AttributePrefixCondition(namespaceURI, name, value);
    }
    
    /**
     * the CSS condition [attribute$=value]
     */
    static Condition createAttributeSuffixCondition(final String namespaceURI, final String name, final String value) {
        return new AttributeSuffixCondition(namespaceURI, name, value);
    }
    
    /**
     * the CSS condition [attribute*=value]
     */
    static Condition createAttributeSubstringCondition(final String namespaceURI, final String name, final String value) {
        return new AttributeSubstringCondition(namespaceURI, name, value);
    }
    
    /**
     * the CSS condition [attribute=value]
     */
    static Condition createAttributeEqualsCondition(final String namespaceURI, final String name, final String value) {
        return new AttributeEqualsCondition(namespaceURI, name, value);
    }

    /**
     * the CSS condition [attribute~=value]
     *
     * @param name  PARAM
     * @param value PARAM
     * @return Returns
     */
    static Condition createAttributeMatchesListCondition(final String namespaceURI, final String name, final String value) {
        return new AttributeMatchesListCondition(namespaceURI, name, value);
    }

    /**
     * the CSS condition [attribute|=value]
     *
     * @param name  PARAM
     * @param value PARAM
     * @return Returns
     */
    static Condition createAttributeMatchesFirstPartCondition(final String namespaceURI, final String name, final String value) {
        return new AttributeMatchesFirstPartCondition(namespaceURI, name, value);
    }

    /**
     * the CSS condition .class
     *
     * @param className PARAM
     * @return Returns
     */
    static Condition createClassCondition(final String className) {
        return new ClassCondition(className);
    }

    /**
     * the CSS condition #ID
     *
     * @param id PARAM
     * @return Returns
     */
    static Condition createIDCondition(final String id) {
        return new IDCondition(id);
    }

    /**
     * the CSS condition lang(Xx)
     *
     * @param lang PARAM
     * @return Returns
     */
    static Condition createLangCondition(final String lang) {
        return new LangCondition(lang);
    }

    /**
     * the CSS condition that element has pseudo-class :first-child
     *
     * @return Returns
     */
    static Condition createFirstChildCondition() {
        return new FirstChildCondition();
    }
    
    /**
     * the CSS condition that element has pseudo-class :last-child
     *
     * @return Returns
     */
    static Condition createLastChildCondition() {
        return new LastChildCondition();
    }
    
    /**
     * the CSS condition that element has pseudo-class :nth-child(an+b)
     *
     * @param number PARAM
     * @return Returns
     */
    static Condition createNthChildCondition(final String number) {
        return NthChildCondition.fromString(number);
    }

    /**
     * the CSS condition that element has pseudo-class :even
     * 
     * @return Returns
     */
    static Condition createEvenChildCondition() {
        return new EvenChildCondition();
    }
    
    /**
     * the CSS condition that element has pseudo-class :odd
     * 
     * @return Returns
     */
    static Condition createOddChildCondition() {
        return new OddChildCondition();
    }

    /**
     * the CSS condition that element has pseudo-class :link
     *
     * @return Returns
     */
    static Condition createLinkCondition() {
        return new LinkCondition();
    }

    /**
     * for unsupported or invalid CSS
     *
     * @return Returns
     */
    static Condition createUnsupportedCondition() {
        return new UnsupportedCondition();
    }
    
    private static abstract class AttributeCompareCondition extends Condition {
        private final String _namespaceURI;
        private final String _name;
        private final String _value;
        
        protected abstract boolean compare(String attrValue, String conditionValue);

        AttributeCompareCondition(final String namespaceURI, final String name, final String value) {
            _namespaceURI = namespaceURI;
            _name = name;
            _value = value;
        }

        boolean matches(final Element e, final AttributeResolver attRes, final TreeResolver treeRes) {
            if (attRes == null) {
                return false;
            }
            final Optional<String> val = attRes.getAttributeValue(e, _namespaceURI, _name);
            if (!val.isPresent()) {
                return false;
            }
            
            return compare(val.get(), _value);
        }
    }

    private static class AttributeExistsCondition extends AttributeCompareCondition {
        AttributeExistsCondition(final String namespaceURI, final String name) {
            super(namespaceURI, name, null);
        }
        
        protected boolean compare(final String attrValue, final String conditionValue) {
            return ! attrValue.equals("");
        }
    }
    
    private static class AttributeEqualsCondition extends AttributeCompareCondition {
        AttributeEqualsCondition(final String namespaceURI, final String name, final String value) {
            super(namespaceURI, name, value);
        }

        protected boolean compare(final String attrValue, final String conditionValue) {
            return attrValue.equals(conditionValue);
        }
    }
    
    private static class AttributePrefixCondition extends AttributeCompareCondition {
        AttributePrefixCondition(final String namespaceURI, final String name, final String value) {
            super(namespaceURI, name, value);
        }

        protected boolean compare(final String attrValue, final String conditionValue) {
            return attrValue.startsWith(conditionValue);
        }
    }
    
    private static class AttributeSuffixCondition extends AttributeCompareCondition {
        AttributeSuffixCondition(final String namespaceURI, final String name, final String value) {
            super(namespaceURI, name, value);
        }

        protected boolean compare(final String attrValue, final String conditionValue) {
            return attrValue.endsWith(conditionValue);
        }
    }
    
    private static class AttributeSubstringCondition extends AttributeCompareCondition {
        AttributeSubstringCondition(final String namespaceURI, final String name, final String value) {
            super(namespaceURI, name, value);
        }

        protected boolean compare(final String attrValue, final String conditionValue) {
            return attrValue.indexOf(conditionValue) > -1;
        }
    }
    
    private static class AttributeMatchesListCondition extends AttributeCompareCondition {
        AttributeMatchesListCondition(final String namespaceURI, final String name, final String value) {
            super(namespaceURI, name, value);
        }
        
        protected boolean compare(final String attrValue, final String conditionValue) {
        	return (' ' + attrValue + ' ').indexOf(' ' + conditionValue + ' ') != -1;
        }
    }

    private static class AttributeMatchesFirstPartCondition extends AttributeCompareCondition {
        AttributeMatchesFirstPartCondition(final String namespaceURI, final String name, final String value) {
            super(namespaceURI, name, value);
        }
        
        protected boolean compare(final String attrValue, final String conditionValue) {
        	return attrValue.indexOf(conditionValue) == 0;
        }
    }

    private static class ClassCondition extends Condition {

        private final String _paddedClassName;

        ClassCondition(final String className) {
            _paddedClassName = ' ' + className + ' ';
        }

        boolean matches(final Element e, final AttributeResolver attRes, final TreeResolver treeRes) 
        {
            if (attRes == null) {
                return false;
            }
            
            final Optional<String> c = attRes.getClass(e);

            if (!c.isPresent()) {
                return false;
            }
            
            // TODO: Other space types.
            String compare = ' ' + c.get() + ' ';
            return compare.indexOf(_paddedClassName) != -1;
        }
    }

    private static class IDCondition extends Condition {

        private final String _id;

        IDCondition(final String id) {
            _id = id;
        }

        boolean matches(final Element e, final AttributeResolver attRes, final TreeResolver treeRes) {
            if (attRes == null) {
                return false;
            }
            Optional<String> id = attRes.getID(e);
            
            if (!id.isPresent() || !_id.equals(id.get())) {
                return false;
            }
            return true;
        }

    }

    private static class LangCondition extends Condition {
        private final String _lang;

        LangCondition(final String lang) {
            _lang = lang;
        }

        boolean matches(final Element e, final AttributeResolver attRes, final TreeResolver treeRes) {
            if (attRes == null) {
                return false;
            }
            final Optional<String> lang = attRes.getLang(e);
            if (!lang.isPresent()) {
                return false;
            }
            if(_lang.equalsIgnoreCase(lang.get())) {
                return true;
            }
            final String[] ca = split(lang.get(), '-');
            if (_lang.equalsIgnoreCase(ca[0])) {
                return true;
            }
            return false;
        }

    }

    private static class FirstChildCondition extends Condition {

        FirstChildCondition() {
        }

        boolean matches(final Element e, final AttributeResolver attRes, final TreeResolver treeRes) {
            return treeRes.isFirstChildElement(e);
        }

    }
    
    private static class LastChildCondition extends Condition {

        LastChildCondition() {
        }

        boolean matches(final Element e, final AttributeResolver attRes, final TreeResolver treeRes) {
            return treeRes.isLastChildElement(e);
        }

    }

    private static class NthChildCondition extends Condition {

        private static final Pattern pattern = Pattern.compile("([-+]?)(\\d*)n(\\s*([-+])\\s*(\\d+))?");

        private final int a;
        private final int b;

        NthChildCondition(final int a, final int b) {
            this.a = a;
            this.b = b;
        }

        boolean matches(final Element e, final AttributeResolver attRes, final TreeResolver treeRes) {
            // getPositionOfElement() starts at 0, CSS spec starts at 1
            int position = treeRes.getPositionOfElement(e)+1;

            position -= b;

            if (a == 0) {
                return position == 0;
            } else if ((a < 0) && (position > 0)) {
                return false; // n is negative
            } else {
                return position % a == 0;
            }
        }

        static NthChildCondition fromString(String number) {
            number = number.trim().toLowerCase(Locale.US);

            if ("even".equals(number)) {
                return new NthChildCondition(2, 0);
            } else if ("odd".equals(number)) {
                return new NthChildCondition(2, 1);
            } else {
                try {
                    return new NthChildCondition(0, Integer.parseInt(number));
                } catch (final NumberFormatException e) {
                    final Matcher m = pattern.matcher(number);

                    if (!m.matches()) {
                        throw new CSSParseException(LangId.INVALID_NTH_CHILD, -1, number);
                    } else {
                        int a = m.group(2).equals("") ? 1 : Integer.parseInt(m.group(2));
                        int b = (m.group(5) == null) ? 0 : Integer.parseInt(m.group(5));
                        if ("-".equals(m.group(1))) {
                            a *= -1;
                        }
                        if ("-".equals(m.group(4))) {
                            b *= -1;
                        }

                        return new NthChildCondition(a, b);
                    }
                }
            }
        }
    }

    private static class EvenChildCondition extends Condition {

        EvenChildCondition() {
        }

        boolean matches(final Element e, final AttributeResolver attRes, final TreeResolver treeRes) {
            final int position = treeRes.getPositionOfElement(e);
            return position >= 0 && position % 2 == 0;
        }
    }
    
    private static class OddChildCondition extends Condition {

        OddChildCondition() {
        }

        boolean matches(final Element e, final AttributeResolver attRes, final TreeResolver treeRes) {
            final int position = treeRes.getPositionOfElement(e);
            return position >= 0 && position % 2 == 1;
        }
    }

    private static class LinkCondition extends Condition {

        LinkCondition() {
        }

        boolean matches(final Element e, final AttributeResolver attRes, final TreeResolver treeRes) {
            return attRes.isLink(e);
        }

    }

    /**
     * represents unsupported (or invalid) css, never matches
     */
    private static class UnsupportedCondition extends Condition {

        UnsupportedCondition() {
        }

        boolean matches(final Element e, final AttributeResolver attRes, final TreeResolver treeRes) {
            return false;
        }

    }
    
    private static String[] split(final String s, final char ch) {
        if (s.indexOf(ch) == -1) {
            return new String[] { s };
        } else {
            final List<String> result = new ArrayList<String>();
            
            int last = 0;
            int next = 0;
            
            while ((next = s.indexOf(ch, last)) != -1) {
                if (next != last) {
                    result.add(s.substring(last, next));
                }
                last = next + 1;
            }
            
            if (last != s.length()) {
                result.add(s.substring(last));
            }
            
            return result.toArray(new String[result.size()]);
        }
    }    
}

