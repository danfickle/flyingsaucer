/*
 * {{{ header & license
 * Copyright (c) 2006 Wisconsin Court System
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
package com.github.neoflyingsaucer.context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.github.neoflyingsaucer.css.constants.CSSPrimitiveUnit;
import com.github.neoflyingsaucer.css.constants.IdentValue;
import com.github.neoflyingsaucer.css.extend.ContentFunction;
import com.github.neoflyingsaucer.css.parser.FSFunction;
import com.github.neoflyingsaucer.css.parser.PropertyValue;
import com.github.neoflyingsaucer.extend.useragent.Optional;
import com.github.neoflyingsaucer.layout.CounterFunction;
import com.github.neoflyingsaucer.layout.InlineBoxing;
import com.github.neoflyingsaucer.layout.LayoutContext;
import com.github.neoflyingsaucer.render.Box;
import com.github.neoflyingsaucer.render.InlineLayoutBox;
import com.github.neoflyingsaucer.render.InlineText;
import com.github.neoflyingsaucer.render.LineBox;
import com.github.neoflyingsaucer.render.RenderingContext;

import static com.github.neoflyingsaucer.util.GeneralUtil.ciEquals;

public class ContentFunctionFactory 
{
    private final List<ContentFunction> _functions = new ArrayList<ContentFunction>(Arrays.asList(
    		new PageCounterFunction(),
    		new PagesCounterFunction(),
    		new TargetCounterFunction(),
    		new LeaderFunction()));
    
    public Optional<ContentFunction> lookupFunction(final LayoutContext c, final FSFunction function) 
    {
    	for (ContentFunction func : _functions)
    	{
    		if (func.canHandle(c, function))
    			return Optional.of(func);
    	}
    	
    	return Optional.empty();
    }
    
    public void registerFunction(final ContentFunction function) {
        _functions.add(function);
    }
    
    private static abstract class PageNumberFunction implements ContentFunction {
    	@Override
    	public boolean isStatic() {
            return false;
        }
        
    	@Override
        public String calculate(final LayoutContext c, final FSFunction function) {
            return null;
        }
        
    	@Override
        public String getLayoutReplacementText() {
            return "999";
        }
        
        protected IdentValue getListStyleType(final FSFunction function) 
        {
            final List<PropertyValue> parameters = function.getParameters();

            if (parameters.size() == 2) {
                final PropertyValue pValue = parameters.get(1);
                final IdentValue iValue = IdentValue.fsValueOf(pValue.getStringValue());

                if (iValue != null)
                    return iValue;
            }
            
            return IdentValue.DECIMAL;
        }
        
        protected boolean isCounter(final FSFunction function, final String counterName) {
            if (ciEquals(function.getName(), "counter")) {
                final List<PropertyValue> parameters = function.getParameters();
                if (parameters.size() == 1 || parameters.size() == 2) {
                    PropertyValue param = parameters.get(0);
                    if (param.getPrimitiveTypeN() != CSSPrimitiveUnit.CSS_IDENT ||
                            ! param.getStringValue().equals(counterName)) {
                        return false;
                    }
                    
                    if (parameters.size() == 2) {
                        param = parameters.get(1);
                        if (param.getPrimitiveTypeN() != CSSPrimitiveUnit.CSS_IDENT) {
                            return false;
                        }
                    }
                    
                    return true;
                }
            }
            
            return false;
        }
    }
    
    private static class PageCounterFunction extends PageNumberFunction implements ContentFunction {
    	@Override
    	public String calculate(final RenderingContext c, final FSFunction function, final InlineText text) {
            final int value = c.getRootLayer().getRelativePageNo(c) + 1;
            return CounterFunction.createCounterText(getListStyleType(function), value);
        }

        @Override
        public boolean canHandle(final LayoutContext c, final FSFunction function) {
            return c.isPrint() && isCounter(function, "page");
        }
    }
    
    private static class PagesCounterFunction extends PageNumberFunction implements ContentFunction {
    	@Override
    	public String calculate(final RenderingContext c, final FSFunction function, final InlineText text) {
            final int value = c.getRootLayer().getRelativePageCount(c);
            return CounterFunction.createCounterText(getListStyleType(function), value);
        }

    	@Override
        public boolean canHandle(final LayoutContext c, final FSFunction function) {
            return c.isPrint() && isCounter(function, "pages");
        }
    }
    
    /**
     * Partially implements target counter as specified here:
     * http://www.w3.org/TR/2007/WD-css3-gcpm-20070504/#cross-references
     */
    private static class TargetCounterFunction implements ContentFunction {
    	@Override
    	public boolean isStatic() {
            return false;
        }
    	
    	@Override
        public String calculate(final RenderingContext c, final FSFunction function, final InlineText text) {
            final String uri = text.getParent().getElement().getAttribute("href");
            if (uri != null && uri.startsWith("#")) {
                final String anchor = uri.substring(1);
                final Box target = c.getBoxById(anchor);
                if (target != null) {
                    final int pageNo = c.getRootLayer().getRelativePageNo(c, target.getAbsY());
                    return CounterFunction.createCounterText(IdentValue.DECIMAL, pageNo + 1);
                }
            }
            return "";
        }

    	@Override
        public String calculate(final LayoutContext c, final FSFunction function) {
            return null;
        }
        
    	@Override
        public String getLayoutReplacementText() {
            return "999";
        }

    	@Override
        public boolean canHandle(final LayoutContext c, final FSFunction function) {
            if (c.isPrint() && function.getName().equals("target-counter")) {
                final List<PropertyValue> parameters = function.getParameters();
                if (parameters.size() == 2 || parameters.size() == 3) {
                    final FSFunction f = ((PropertyValue)parameters.get(0)).getFunction();
                    if (f == null ||
                            f.getParameters().size() != 1 ||
                            ((PropertyValue)f.getParameters().get(0)).getPrimitiveTypeN() != CSSPrimitiveUnit.CSS_IDENT ||
                            ! ((PropertyValue)f.getParameters().get(0)).getStringValue().equals("href")) {
                        return false;
                    }

                    final PropertyValue param = (PropertyValue)parameters.get(1);
                    if (param.getPrimitiveTypeN() != CSSPrimitiveUnit.CSS_IDENT ||
                            ! param.getStringValue().equals("page")) {
                        return false;
                    }
                    
                    return true;
                }
            }
            
            return false;
        }
    }

    /**
     * Partially implements leaders as specified here:
     * http://www.w3.org/TR/2007/WD-css3-gcpm-20070504/#leaders
     */
    private static class LeaderFunction implements ContentFunction {
    	@Override
    	public boolean isStatic() {
            return false;
        }

    	@Override
        public String calculate(final RenderingContext c, final FSFunction function, final InlineText text) {
            final InlineLayoutBox iB = text.getParent();
            final LineBox lineBox = iB.getLineBox();

            // There might be a target-counter function after this function.
            // Because the leader should fill up the line, we need the correct
            // width and must first compute the target-counter function.
            boolean dynamic = false;
            final Iterator<Box> childIterator = lineBox.getChildIterator();
            while (childIterator.hasNext()) {
                final Box child = (Box)childIterator.next();
                if (child == iB) {
                    dynamic = true;
                } else if (dynamic && child instanceof InlineLayoutBox) {
                    ((InlineLayoutBox)child).lookForDynamicFunctions(c);
                }
            }
            if (dynamic) {
                final int totalLineWidth = InlineBoxing.positionHorizontally(c, lineBox, 0);
                lineBox.setContentWidth(totalLineWidth);
            }

            // Get leader value and value width
            final PropertyValue param = function.getParameters().get(0);
            String value = param.getStringValue();
            if (param.getPrimitiveTypeN() == CSSPrimitiveUnit.CSS_IDENT) {
                if (value.equals("dotted")) {
                    value = ". ";
                } else if (value.equals("solid")) {
                    value = "_";
                } else if (value.equals("space")) {
                    value = " ";
                }
            }

            // Compute value width using 100x string to get more precise width.
            // Otherwise there might be a small gap at the right side. This is
            // necessary because a TextRenderer usually use double/float for width.
            final StringBuffer tmp = new StringBuffer(100 * value.length());
            for (int i = 0; i < 100; i++) {
                tmp.append(value);
            }
            final float valueWidth = c.getTextRenderer().getWidth(c.getFontContext(),
                    iB.getStyle().getFSFont(c), tmp.toString()) / 100f;
            final int spaceWidth = c.getTextRenderer().getWidth(c.getFontContext(),
                    iB.getStyle().getFSFont(c), " ");

            // compute leader width and necessary count of values
            final int leaderWidth = iB.getContainingBlockWidth() - iB.getLineBox().getWidth() + text.getWidth();
            final int count = (int) ((leaderWidth - (2 * spaceWidth)) / valueWidth);

            // build leader string
            final StringBuffer buf = new StringBuffer(count * value.length() + 2);
            buf.append(' ');
            for (int i = 0; i < count; i++) {
                buf.append(value);
            }
            buf.append(' ');
            final String leaderString = buf.toString();

            // set left margin to ensure that the leader is right aligned (for TOC)
            final int leaderStringWidth = c.getTextRenderer().getWidth(c.getFontContext(),
                    iB.getStyle().getFSFont(c), leaderString);
            iB.setMarginLeft(c, leaderWidth - leaderStringWidth);

            return leaderString;
        }

    	@Override
        public String calculate(final LayoutContext c, final FSFunction function) {
            return null;
        }
        
    	@Override
        public String getLayoutReplacementText() {
            return " . ";
        }

    	@Override
        public boolean canHandle(final LayoutContext c, final FSFunction function) {
            if (c.isPrint() && function.getName().equals("leader")) {
                final List<PropertyValue> parameters = function.getParameters();
                if (parameters.size() == 1) {
                    final PropertyValue param = (PropertyValue)parameters.get(0);
                    if (param.getPrimitiveTypeN() != CSSPrimitiveUnit.CSS_STRING &&
                            (param.getPrimitiveTypeN() != CSSPrimitiveUnit.CSS_IDENT ||
                                (!param.getStringValue().equals("dotted") &&
                                        !param.getStringValue().equals("solid") &&
                                        !param.getStringValue().equals("space")))) {
                        return false;
                    }
                    
                    return true;
                }
            }
            
            return false;
        }
    }
}
