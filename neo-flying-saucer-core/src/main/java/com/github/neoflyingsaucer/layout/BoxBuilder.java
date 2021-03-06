/*
 * {{{ header & license
 * Copyright (c) 2004, 2005 Torbjoern Gannholm, Joshua Marinacci
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
package com.github.neoflyingsaucer.layout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.github.neoflyingsaucer.css.constants.CSSName;
import com.github.neoflyingsaucer.css.constants.CSSPrimitiveUnit;
import com.github.neoflyingsaucer.css.constants.IdentValue;
import com.github.neoflyingsaucer.css.constants.MarginBoxName;
import com.github.neoflyingsaucer.css.constants.PageElementPosition;
import com.github.neoflyingsaucer.css.extend.ContentFunction;
import com.github.neoflyingsaucer.css.newmatch.CascadedStyle;
import com.github.neoflyingsaucer.css.newmatch.PageInfo;
import com.github.neoflyingsaucer.css.parser.FSFunction;
import com.github.neoflyingsaucer.css.parser.PropertyValue;
import com.github.neoflyingsaucer.css.parser.PropertyValueImp;
import com.github.neoflyingsaucer.css.sheet.PropertyDeclaration;
import com.github.neoflyingsaucer.css.sheet.StylesheetInfo;
import com.github.neoflyingsaucer.css.style.CalculatedStyle;
import com.github.neoflyingsaucer.css.style.EmptyStyle;
import com.github.neoflyingsaucer.css.style.FSDerivedValue;
import com.github.neoflyingsaucer.extend.controller.cancel.FSCancelController;
import com.github.neoflyingsaucer.newtable.TableBox;
import com.github.neoflyingsaucer.newtable.TableCellBox;
import com.github.neoflyingsaucer.newtable.TableColumn;
import com.github.neoflyingsaucer.newtable.TableRowBox;
import com.github.neoflyingsaucer.newtable.TableSectionBox;
import com.github.neoflyingsaucer.render.AnonymousBlockBox;
import com.github.neoflyingsaucer.render.BlockBox;
import com.github.neoflyingsaucer.render.Box;
import com.github.neoflyingsaucer.render.FloatedBoxData;
import com.github.neoflyingsaucer.render.InlineBox;

/**
 * This class is responsible for creating the box tree from the DOM.  This is
 * mostly just a one-to-one translation from the <code>Element</code> to an
 * <code>InlineBox</code> or a <code>BlockBox</code> (or some subclass of
 * <code>BlockBox</code>), but the tree is reorganized according to the CSS rules.
 * This includes inserting anonymous block and inline boxes, anonymous table
 * content, and <code>:before</code> and <code>:after</code> content.  White
 * space is also normalized at this point.  Table columns and table column groups
 * are added to the table which owns them, but are not created as regular boxes.
 * Floated and absolutely positioned content is always treated as inline
 * content for purposes of inserting anonymous block boxes and calculating
 * the kind of content contained in a given block box.
 */
public class BoxBuilder {
    public static final int MARGIN_BOX_VERTICAL = 1;
    public static final int MARGIN_BOX_HORIZONTAL = 2;

    private static final int CONTENT_LIST_DOCUMENT = 1;
    private static final int CONTENT_LIST_MARGIN_BOX = 2;

    /**
     * Creates a <code>Box</code> for the root (typically an <code>html</code> tag)
     * element of the document.
     * @param c
     * @param document
     * @return <code>BlockBox</code> or <code>TableBox</code> depending on the <code>display</code> CSS property.
     */
    public static BlockBox createRootBox(LayoutContext c, Document document) 
    {
        Element root = document.getDocumentElement();
        CalculatedStyle style = c.getSharedContext().getStyle(root);

        BlockBox result;
        if (style.isTable() || style.isInlineTable()) {
            result = new TableBox();
        } else {
            result = new BlockBox();
        }

        result.setStyle(style);
        result.setElement(root);

        c.resolveCounters(style);
        c.pushLayer(result);

        if (c.isPrint()) {
            if (! style.isIdent(CSSName.PAGE, IdentValue.AUTO)) {
                c.setPageName(style.getStringProperty(CSSName.PAGE));
            }
            c.getRootLayer().addPage(c);
        }

        return result;
    }

    /**
     * 
     * @param c
     * @param parent
     */
    public static void createChildren(LayoutContext c, BlockBox parent)
    {
        List<Styleable> children = new ArrayList<Styleable>();

        ChildBoxInfo info = new ChildBoxInfo();

        createChildren(c, parent, parent.getElement(), children, info, false);

        boolean parentIsNestingTableContent = isNestingTableContent(parent.getStyle().getIdent(CSSName.DISPLAY));
        
        if (!parentIsNestingTableContent && !info.isContainsTableContent()) {
            resolveChildren(c, parent, children, info);
        } else {
            stripAllWhitespace(children);
            if (parentIsNestingTableContent) {
                resolveTableContent(c, parent, children, info);
            } else {
                resolveChildTableContent(c, parent, children, info, IdentValue.TABLE_CELL);
            }
        }
    }

    /**
     * For paged media. Page-margin boxes are boxes within the page margin that, like pseudo-elements, can contain generated content.
     * Page-margin boxes can be used to create page headers and footers, which are portions of the page set aside for supplementary 
     * information such as the page number or document title.
     * See <a href="http://www.w3.org/TR/css3-page/#margin-boxes">CSS3 Paged Media Module</a>
     * Example CSS: <code>@page {  @top-left { content: "Hamlet"; } }</code>
     * @param c
     * @param pageInfo
     * @param names
     * @param height
     * @param direction Either MARGIN_BOX_VERITCAL or MARGIN_BOX_HORIZONTAL
     * @return
     */
    public static TableBox createMarginTable(
            LayoutContext c,
            PageInfo pageInfo,
            MarginBoxName[] names,
            int height,
            int direction)
    {
        if (!pageInfo.hasAny(names)) {
            return null;
        }

        Element source = c.getRootLayer().getMaster().getElement(); // HACK

        ChildBoxInfo info = new ChildBoxInfo();
        CalculatedStyle pageStyle = new EmptyStyle().deriveStyle(pageInfo.getPageStyle());

        CalculatedStyle tableStyle = pageStyle.deriveStyle(
                CascadedStyle.createLayoutStyle(new PropertyDeclaration[] {
                        new PropertyDeclaration(
                                CSSName.DISPLAY,
                                new PropertyValueImp(IdentValue.TABLE),
                                true,
                                StylesheetInfo.CSSOrigin.USER),
                        new PropertyDeclaration(
                                CSSName.WIDTH,
                                new PropertyValueImp(CSSPrimitiveUnit.CSS_PERCENTAGE, 100.0f, "100%"),
                                true,
                                StylesheetInfo.CSSOrigin.USER),
                }));
        
        TableBox result = (TableBox) createBlockBox(tableStyle, info, false);
        result.setMarginAreaRoot(true);
        result.setStyle(tableStyle);
        result.setElement(source);
        result.setAnonymous(true);
        result.setChildrenContentType(BlockBox.CONTENT_BLOCK);

        CalculatedStyle tableSectionStyle = pageStyle.createAnonymousStyle(IdentValue.TABLE_ROW_GROUP);
        TableSectionBox section = (TableSectionBox) createBlockBox(tableSectionStyle, info, false);
        section.setStyle(tableSectionStyle);
        section.setElement(source);
        section.setAnonymous(true);
        section.setChildrenContentType(BlockBox.CONTENT_BLOCK);

        result.addChild(section);

        TableRowBox row = null;
        
        if (direction == MARGIN_BOX_HORIZONTAL) {
            CalculatedStyle tableRowStyle = pageStyle.createAnonymousStyle(IdentValue.TABLE_ROW);
            row = (TableRowBox)createBlockBox(tableRowStyle, info, false);
            row.setStyle(tableRowStyle);
            row.setElement(source);
            row.setAnonymous(true);
            row.setChildrenContentType(BlockBox.CONTENT_BLOCK);

            row.setHeightOverride(height);

            section.addChild(row);
        }

        int cellCount = 0;
        boolean alwaysCreate = names.length > 1 && direction == MARGIN_BOX_HORIZONTAL;

        for (MarginBoxName name : names)
        {
        	FSCancelController.cancelOpportunity(BoxBuilder.class);
        	
            CascadedStyle cellStyle = pageInfo.createMarginBoxStyle(name, alwaysCreate);
            
            if (cellStyle != null) {
                TableCellBox cell = createMarginBox(c, cellStyle, alwaysCreate);
                if (cell != null) {
                    if (direction == MARGIN_BOX_VERTICAL) {
                        CalculatedStyle tableRowStyle = pageStyle.createAnonymousStyle(IdentValue.TABLE_ROW);
                        row = (TableRowBox)createBlockBox(tableRowStyle, info, false);
                        row.setStyle(tableRowStyle);
                        row.setElement(source);
                        row.setAnonymous(true);
                        row.setChildrenContentType(BlockBox.CONTENT_BLOCK);

                        row.setHeightOverride(height);

                        section.addChild(row);
                    }
                    row.addChild(cell);
                    cellCount++;
                }
            }
        }

        if (direction == MARGIN_BOX_VERTICAL && cellCount > 0) {
            int rHeight = 0;
            for (Iterator<Box> i = section.getChildIterator(); i.hasNext(); )
            {
                TableRowBox r = (TableRowBox) i.next();
                r.setHeightOverride(height / cellCount);
                rHeight += r.getHeightOverride();
            }

            for (Iterator<Box> i = section.getChildIterator(); i.hasNext() && rHeight < height; )
            {
                TableRowBox r = (TableRowBox) i.next();
                r.setHeightOverride(r.getHeightOverride()+1);
                rHeight++;
            }
        }

        return cellCount > 0 ? result : null;
    }

    private static TableCellBox createMarginBox(
            LayoutContext c,
            CascadedStyle cascadedStyle,
            boolean alwaysCreate)
    {
        boolean hasContent = true;

        PropertyDeclaration contentDecl = cascadedStyle.propertyByName(CSSName.CONTENT);

        CalculatedStyle style = new EmptyStyle().deriveStyle(cascadedStyle);

        if (style.isDisplayNone() && ! alwaysCreate) {
            return null;
        }

        if (style.isIdent(CSSName.CONTENT, IdentValue.NONE) ||
            style.isIdent(CSSName.CONTENT, IdentValue.NORMAL)) {
            hasContent = false;
        }

        if (style.isAutoWidth() && ! alwaysCreate && ! hasContent) {
            return null;
        }

        List<Styleable> children = new ArrayList<Styleable>();

        ChildBoxInfo info = new ChildBoxInfo();
        info.setContainsTableContent(true);
        info.setLayoutRunningBlocks(true);

        TableCellBox result = new TableCellBox();
        result.setAnonymous(true);
        result.setStyle(style);
        result.setElement(c.getRootLayer().getMaster().getElement()); // XXX Doesn't make sense, but we need something here

        if (hasContent && ! style.isDisplayNone()) {
            children.addAll(createGeneratedMarginBoxContent(
                    c,
                    c.getRootLayer().getMaster().getElement(),
                    contentDecl.getValue(),
                    style,
                    info));

            stripAllWhitespace(children);
        }

        if (children.size() == 0 && style.isAutoWidth() && ! alwaysCreate) {
            return null;
        }

        resolveChildTableContent(c, result, children, info, IdentValue.TABLE_CELL);

        return result;
    }

    private static void resolveChildren(
            LayoutContext c, BlockBox owner, List<Styleable> children, ChildBoxInfo info) 
    {
        if (children.size() > 0) {
            if (info.isContainsBlockLevelContent()) {
                insertAnonymousBlocks(c.getSharedContext(), owner, children, info.isLayoutRunningBlocks());
                owner.setChildrenContentType(BlockBox.CONTENT_BLOCK);
            } else {
                WhitespaceStripper.stripInlineContent(children);
                if (!children.isEmpty()) {
                    owner.setInlineContent(children);
                    owner.setChildrenContentType(BlockBox.CONTENT_INLINE);
                } else {
                    owner.setChildrenContentType(BlockBox.CONTENT_EMPTY);
                }
            }
        } else {
            owner.setChildrenContentType(BlockBox.CONTENT_EMPTY);
        }
    }

    /**
     * Checks if the table content is properly nested.
     * @param parentDisplay
     * @param children
     * @return
     */
    private static boolean isAllProperTableNesting(IdentValue parentDisplay, List<Styleable> children) 
    {
    	for (Styleable child : children)
    	{
    		if (!isProperTableNesting(parentDisplay, child.getStyle().getIdent(CSSName.DISPLAY)))
   				return false;
    	}
    	
    	return true;
    }

    /**
     * Handles the situation when we find table content, but our parent is not
     * table related.  For example, <code>div</code> -&gt; <code>td</td></code>.
     * Anonymous tables are then constructed by repeatedly pulling together
     * consecutive same-table-level siblings and wrapping them in the next
     * highest table level (e.g. consecutive <code>td</code> elements will
     * be wrapped in an anonymous <code>tr</code>, then a <code>tbody</code>, and
     * finally a <code>table</code>).
     */
    private static void resolveChildTableContent(
            LayoutContext c, BlockBox parent, List<Styleable> children, ChildBoxInfo info, IdentValue target) 
    {
        List<Styleable> childrenForAnonymous = new ArrayList<Styleable>();
        List<Styleable> childrenWithAnonymous = new ArrayList<Styleable>();

        IdentValue nextUp = getPreviousTableNestingLevel(target);
        
        for (Styleable styleable : children) 
        {
        	FSCancelController.cancelOpportunity(BoxBuilder.class);
        	
            if (matchesTableLevel(target, styleable.getStyle().getIdent(CSSName.DISPLAY))) {
                childrenForAnonymous.add(styleable);
            } else {
                if (!childrenForAnonymous.isEmpty()) {
                    createAnonymousTableContent(c, (BlockBox) childrenForAnonymous.get(0), nextUp,
                            childrenForAnonymous, childrenWithAnonymous);

                    childrenForAnonymous = new ArrayList<Styleable>();
                }
                childrenWithAnonymous.add(styleable);
            }
        }

        if (childrenForAnonymous.size() > 0) {
            createAnonymousTableContent(c, (BlockBox) childrenForAnonymous.get(0), nextUp,
                    childrenForAnonymous, childrenWithAnonymous);
        }

        if (nextUp == IdentValue.TABLE) {
            rebalanceInlineContent(childrenWithAnonymous);
            info.setContainsBlockLevelContent(true);
            resolveChildren(c, parent, childrenWithAnonymous, info);
        } else {
            resolveChildTableContent(c, parent, childrenWithAnonymous, info, nextUp);
        }
    }

    /**
     * 
     * @param target
     * @param value
     * @return
     */
    private static boolean matchesTableLevel(IdentValue target, IdentValue value) {
        if (target == IdentValue.TABLE_ROW_GROUP) {
            return value == IdentValue.TABLE_ROW_GROUP || value == IdentValue.TABLE_HEADER_GROUP
                || value == IdentValue.TABLE_FOOTER_GROUP || value == IdentValue.TABLE_CAPTION;
        } else {
            return target == value;
        }
    }

    /**
     * Makes sure that any <code>InlineBox</code> in <code>content</code>
     * both starts and ends within <code>content</code>. Used to ensure that
     * it is always possible to construct anonymous blocks once an element's
     * children has been distributed among anonymous table objects.
     */
    private static void rebalanceInlineContent(List<Styleable> content) {
        Map<Element, InlineBox> boxesByElement = new HashMap<Element, InlineBox>();
        
        for (Styleable styleable : content)
        {
        	FSCancelController.cancelOpportunity(BoxBuilder.class);
        	
            if (styleable instanceof InlineBox) {
                InlineBox iB = (InlineBox) styleable;
                Element elem = iB.getElement();

                if (!boxesByElement.containsKey(elem)) {
                    iB.setStartsHere(true);
                }

                boxesByElement.put(elem, iB);
            }
        }

        for (InlineBox iB : boxesByElement.values())
        {
            iB.setEndsHere(true);
        }
    }

    private static void stripAllWhitespace(List<Styleable> content)
    {
        int start = 0;
        int current = 0;
        boolean started = false;
        
        for (current = 0; current < content.size(); current++) {
        	FSCancelController.cancelOpportunity(BoxBuilder.class);
        	
            Styleable styleable = content.get(current);
            if (! styleable.getStyle().isLayedOutInInlineContext()) {
                if (started) {
                    int before = content.size();
                    WhitespaceStripper.stripInlineContent(content.subList(start, current));
                    int after = content.size();
                    current -= (before - after);
                }
                started = false;
            } else {
                if (! started) {
                    started = true;
                    start = current;
                }
            }
        }

        if (started) {
            WhitespaceStripper.stripInlineContent(content.subList(start, current));
        }
    }

    /**
     * Handles the situation when our current parent is table related.  If
     * everything is properly nested (e.g. a <code>tr</code> contains only
     * <code>td</code> elements), nothing is done.  Otherwise anonymous boxes
     * are inserted to ensure the integrity of the table model.
     */
    private static void resolveTableContent(
            LayoutContext c, BlockBox parent, List<Styleable> children, ChildBoxInfo info)
    {
        IdentValue parentDisplay = parent.getStyle().getIdent(CSSName.DISPLAY);
        IdentValue next = getNextTableNestingLevel(parentDisplay);
        
        if (next == null && parent.isAnonymous() && containsOrphanedTableContent(children)) {
            resolveChildTableContent(c, parent, children, info, IdentValue.TABLE_CELL);
        } else if (next == null || isAllProperTableNesting(parentDisplay, children)) {
            if (parent.isAnonymous()) {
                rebalanceInlineContent(children);
            }
            resolveChildren(c, parent, children, info);
        } else {
            List<Styleable> childrenForAnonymous = new ArrayList<Styleable>();
            List<Styleable> childrenWithAnonymous = new ArrayList<Styleable>();
            
            for (Styleable child : children)
            {
                IdentValue childDisplay = child.getStyle().getIdent(CSSName.DISPLAY);

                if (isProperTableNesting(parentDisplay, childDisplay)) {
                    if (childrenForAnonymous.size() > 0) {
                        createAnonymousTableContent(c, parent, next, childrenForAnonymous,
                                childrenWithAnonymous);

                        childrenForAnonymous = new ArrayList<Styleable>();
                    }
                    childrenWithAnonymous.add(child);
                } else {
                    childrenForAnonymous.add(child);
                }
            }

            if (childrenForAnonymous.size() > 0) {
                createAnonymousTableContent(c, parent, next, childrenForAnonymous,
                        childrenWithAnonymous);
            }

            info.setContainsBlockLevelContent(true);
            resolveChildren(c, parent, childrenWithAnonymous, info);
        }
    }

    private static boolean containsOrphanedTableContent(List<Styleable> children) 
    {
        for (Styleable child : children) {
            IdentValue display = child.getStyle().getIdent(CSSName.DISPLAY);
            if (display == IdentValue.TABLE_HEADER_GROUP ||
                display == IdentValue.TABLE_ROW_GROUP ||
                display == IdentValue.TABLE_FOOTER_GROUP ||
                display == IdentValue.TABLE_ROW) {
                return true;
            }
        }

        return false;
    }

    private static boolean isParentInline(BlockBox box) 
    {
        CalculatedStyle parentStyle = box.getStyle().getParent();
        return parentStyle != null && parentStyle.isInline();
    }

    private static void createAnonymousTableContent(LayoutContext c, BlockBox source,
                                                    IdentValue next, List<Styleable> childrenForAnonymous, List<Styleable> childrenWithAnonymous)
    {
        ChildBoxInfo nested = lookForBlockContent(childrenForAnonymous);

        IdentValue anonDisplay;
        if (isParentInline(source) && next == IdentValue.TABLE) {
            anonDisplay = IdentValue.INLINE_TABLE;
        } else {
            anonDisplay = next;
        }
        
        CalculatedStyle anonStyle = source.getStyle().createAnonymousStyle(anonDisplay);
        BlockBox anonBox = createBlockBox(anonStyle, nested, false);
        anonBox.setStyle(anonStyle);
        anonBox.setAnonymous(true);
        
        // XXX Doesn't really make sense, but what to do?
        anonBox.setElement(source.getElement());
        resolveTableContent(c, anonBox, childrenForAnonymous, nested);

        if (next == IdentValue.TABLE) {
            childrenWithAnonymous.add(reorderTableContent(c, (TableBox) anonBox));
        } else {
            childrenWithAnonymous.add(anonBox);
        }
    }

    /**
     * Reorganizes a table so that the header is the first row group and the
     * footer the last.  If the table has caption boxes, they will be pulled
     * out and added to an anonymous block box along with the table itself.
     * If not, the table is returned.
     */
    private static BlockBox reorderTableContent(LayoutContext c, TableBox table) 
    {
        List<Box> topCaptions = new LinkedList<Box>();
        Box header = null;
        List<Box> bodies = new LinkedList<Box>();
        Box footer = null;
        List<Box> bottomCaptions = new LinkedList<Box>();

        for (Iterator<Box> i = table.getChildIterator(); i.hasNext();) {
        	FSCancelController.cancelOpportunity(BoxBuilder.class);
        	
            Box b = i.next();
            IdentValue display = b.getStyle().getIdent(CSSName.DISPLAY);
            
            if (display == IdentValue.TABLE_CAPTION) {
                IdentValue side = b.getStyle().getIdent(CSSName.CAPTION_SIDE);
                if (side == IdentValue.BOTTOM) {
                    bottomCaptions.add(b);
                } else { /* side == IdentValue.TOP */
                    topCaptions.add(b);
                }
            } else if (display == IdentValue.TABLE_HEADER_GROUP && header == null) {
                header = b;
            } else if (display == IdentValue.TABLE_FOOTER_GROUP && footer == null) {
                footer = b;
            } else {
                bodies.add(b);
            }
        }

        table.removeAllChildren();
        if (header != null) {
            ((TableSectionBox)header).setHeader(true);
            table.addChild(header);
        }
        table.addAllChildren(bodies);
        if (footer != null) {
            ((TableSectionBox)footer).setFooter(true);
            table.addChild(footer);
        }

        if (topCaptions.size() == 0 && bottomCaptions.size() == 0) {
            return table;
        } else {
            // If we have a floated table with a caption, we need to float the
            // outer anonymous box and not the table
            CalculatedStyle anonStyle;
            if (table.getStyle().isFloated()) {
                CascadedStyle cascadedStyle = CascadedStyle.createLayoutStyle(
                        new PropertyDeclaration[]{
                                CascadedStyle.createLayoutPropertyDeclaration(
                                        CSSName.DISPLAY, IdentValue.BLOCK),
                                CascadedStyle.createLayoutPropertyDeclaration(
                                        CSSName.FLOAT, table.getStyle().getIdent(CSSName.FLOAT))});

                anonStyle = table.getStyle().deriveStyle(cascadedStyle);
            } else {
                anonStyle = table.getStyle().createAnonymousStyle(IdentValue.BLOCK);
            }

            BlockBox anonBox = new BlockBox();
            anonBox.setStyle(anonStyle);
            anonBox.setAnonymous(true);
            anonBox.setFromCaptionedTable(true);
            anonBox.setElement(table.getElement());

            anonBox.setChildrenContentType(BlockBox.CONTENT_BLOCK);
            anonBox.addAllChildren(topCaptions);
            anonBox.addChild(table);
            anonBox.addAllChildren(bottomCaptions);

            if (table.getStyle().isFloated()) {
                anonBox.setFloatedBoxData(new FloatedBoxData());
                table.setFloatedBoxData(null);

                CascadedStyle original = c.getSharedContext().getCss().getCascadedStyle(
                        c.getSharedContext().getBaseURL(), table.getElement(), false);
                CascadedStyle modified = CascadedStyle.createLayoutStyle(
                        original,
                        new PropertyDeclaration[]{
                                CascadedStyle.createLayoutPropertyDeclaration(
                                        CSSName.FLOAT, IdentValue.NONE)
                        });
                table.setStyle(table.getStyle().getParent().deriveStyle(modified));
            }

            return anonBox;
        }
    }

    private static ChildBoxInfo lookForBlockContent(List<Styleable> styleables) 
    {
        ChildBoxInfo result = new ChildBoxInfo();
        boolean containsBlockLevelContent = false; 
        
        for (Styleable s : styleables)
        {
        	if (!s.getStyle().isLayedOutInInlineContext())
        	{
        		containsBlockLevelContent = true;
        		break;
        	}
        }
        
        result.setContainsBlockLevelContent(containsBlockLevelContent);
        return result;
    }

    private static IdentValue getNextTableNestingLevel(IdentValue display) 
    {
        if (display == IdentValue.TABLE || display == IdentValue.INLINE_TABLE) {
            return IdentValue.TABLE_ROW_GROUP;
        } else if (display == IdentValue.TABLE_HEADER_GROUP
                || display == IdentValue.TABLE_ROW_GROUP
                || display == IdentValue.TABLE_FOOTER_GROUP) {
            return IdentValue.TABLE_ROW;
        } else if (display == IdentValue.TABLE_ROW) {
            return IdentValue.TABLE_CELL;
        } else {
            return null;
        }
    }

    private static IdentValue getPreviousTableNestingLevel(IdentValue display) 
    {
        if (display == IdentValue.TABLE_CELL) {
            return IdentValue.TABLE_ROW;
        } else if (display == IdentValue.TABLE_ROW) {
            return IdentValue.TABLE_ROW_GROUP;
        } else if (display == IdentValue.TABLE_HEADER_GROUP
                || display == IdentValue.TABLE_ROW_GROUP
                || display == IdentValue.TABLE_FOOTER_GROUP) {
            return IdentValue.TABLE;
        } else {
            return null;
        }
    }

    private static boolean isProperTableNesting(IdentValue parent, IdentValue child)
    {
        return (parent == IdentValue.TABLE && (child == IdentValue.TABLE_HEADER_GROUP ||
                child == IdentValue.TABLE_ROW_GROUP ||
                child == IdentValue.TABLE_FOOTER_GROUP ||
                child == IdentValue.TABLE_CAPTION))
                || ((parent == IdentValue.TABLE_HEADER_GROUP ||
                parent == IdentValue.TABLE_ROW_GROUP ||
                parent == IdentValue.TABLE_FOOTER_GROUP) &&
                child == IdentValue.TABLE_ROW)
                || (parent == IdentValue.TABLE_ROW && child == IdentValue.TABLE_CELL)
                || (parent == IdentValue.INLINE_TABLE && (child == IdentValue.TABLE_HEADER_GROUP ||
                child == IdentValue.TABLE_ROW_GROUP ||
                child == IdentValue.TABLE_FOOTER_GROUP));

    }

    private static boolean isNestingTableContent(IdentValue display)
    {
        return display == IdentValue.TABLE || display == IdentValue.INLINE_TABLE ||
                display == IdentValue.TABLE_HEADER_GROUP || display == IdentValue.TABLE_ROW_GROUP ||
                display == IdentValue.TABLE_FOOTER_GROUP || display == IdentValue.TABLE_ROW;
    }

    private static boolean isAttrFunction(FSFunction function) 
    {
        if (function.getName().equals("attr")) {
            List<PropertyValue> params = function.getParameters();
            if (params.size() == 1) {
                PropertyValue value = params.get(0);
                return value.getPrimitiveTypeN() == CSSPrimitiveUnit.CSS_IDENT;
            }
        }

        return false;
    }

    public static boolean isElementFunction(FSFunction function) 
    {
        if (function.getName().equals("element")) {
            List<PropertyValue> params = function.getParameters();
            if (params.size() < 1 || params.size() > 2) {
                return false;
            }
            boolean ok = true;
            PropertyValue value1 = params.get(0);
            ok = value1.getPrimitiveTypeN() == CSSPrimitiveUnit.CSS_IDENT;
            if (ok && params.size() == 2) {
                PropertyValue value2 = params.get(1);
                ok = value2.getPrimitiveTypeN() == CSSPrimitiveUnit.CSS_IDENT;
            }

            return ok;
        }

        return false;
    }

    private static CounterFunction makeCounterFunction(FSFunction function, LayoutContext c, CalculatedStyle style)
    {
        if (function.getName().equals("counter")) {
            List<PropertyValue> params = function.getParameters();
            if (params.size() < 1 || params.size() > 2) {
                return null;
            }

            PropertyValue value = params.get(0);
            if (value.getPrimitiveTypeN() != CSSPrimitiveUnit.CSS_IDENT) {
                return null;
            }

            String s = value.getStringValue();
            // counter(page) and counter(pages) are handled separately
            if (s.equals("page") || s.equals("pages")) {
                return null;
            }

            String counter = value.getStringValue();
            IdentValue listStyleType = IdentValue.DECIMAL;
            if (params.size() == 2) {
                value = params.get(1);
                if (value.getPrimitiveTypeN() != CSSPrimitiveUnit.CSS_IDENT) {
                    return null;
                }

                IdentValue identValue = IdentValue.fsValueOf(value.getStringValue());
                if (identValue != null) {
                    value.setIdentValue(identValue);
                    listStyleType = identValue;
                }
            }

            int counterValue = c.getCounterContext(style).getCurrentCounterValue(counter);

            return new CounterFunction(counterValue, listStyleType);
        } else if (function.getName().equals("counters")) {
            List<PropertyValue> params = function.getParameters();
            if (params.size() < 2 || params.size() > 3) {
                return null;
            }

            PropertyValue value = params.get(0);
            if (value.getPrimitiveTypeN() != CSSPrimitiveUnit.CSS_IDENT) {
                return null;
            }

            String counter = value.getStringValue();

            value = params.get(1);
            if (value.getPrimitiveTypeN() != CSSPrimitiveUnit.CSS_STRING) {
                return null;
            }

            String separator = value.getStringValue();

            IdentValue listStyleType = IdentValue.DECIMAL;
            if (params.size() == 3) {
                value = params.get(2);
                if (value.getPrimitiveTypeN() != CSSPrimitiveUnit.CSS_IDENT) {
                    return null;
                }

                IdentValue identValue = IdentValue.fsValueOf(value.getStringValue());
                if (identValue != null) {
                    value.setIdentValue(identValue);
                    listStyleType = identValue;
                }
            }

            List<Integer> counterValues = c.getCounterContext(style).getCurrentCounterValues(counter);

            return new CounterFunction(counterValues, separator, listStyleType);
        } else {
            return null;
        }
    }

    private static String getAttributeValue(FSFunction attrFunc, Element e)
    {
        PropertyValue value = attrFunc.getParameters().get(0);
        return e.getAttribute(value.getStringValue());
    }

    private static List<Styleable> createGeneratedContentList(
            LayoutContext c, Element element, PropertyValue propValue,
            String peName, CalculatedStyle style, int mode, ChildBoxInfo info)
    {
        List<?> values = propValue.getValues();

        if (values == null) {
            // content: normal or content: none
            return Collections.emptyList();
        }

        List<Styleable> result = new ArrayList<Styleable>(values.size());

        for (Object valueObj : values) {
        	FSCancelController.cancelOpportunity(BoxBuilder.class);
        	
            PropertyValue value = (PropertyValue) valueObj;

            ContentFunction contentFunction = null;
            FSFunction function = null;

            String content = null;

            CSSPrimitiveUnit type = value.getPrimitiveTypeN();
            if (type == CSSPrimitiveUnit.CSS_STRING) {
                content = value.getStringValue();
            } else if (value.getPropertyValueType() == PropertyValueImp.VALUE_TYPE_FUNCTION) {
                if (mode == CONTENT_LIST_DOCUMENT && isAttrFunction(value.getFunction())) {
                    content = getAttributeValue(value.getFunction(), element);
                } else {
                    CounterFunction cFunc = null;

                    if (mode == CONTENT_LIST_DOCUMENT) {
                        cFunc = makeCounterFunction(value.getFunction(), c, style);
                    }

                    if (cFunc != null) {
                        //TODO: counter functions may be called with non-ordered list-style-types, e.g. disc
                        content = cFunc.evaluate();
                        contentFunction = null;
                        function = null;
                    } else if (mode == CONTENT_LIST_MARGIN_BOX && isElementFunction(value.getFunction())) {
                        BlockBox target = getRunningBlock(c, value);
                        if (target != null) {
                            result.add(target.copyOf());
                            info.setContainsBlockLevelContent(true);
                        }
                    } else {
                        contentFunction =
                                c.getContentFunctionFactory().lookupFunction(c, value.getFunction()).orElse(null);
                        if (contentFunction != null) {
                            function = value.getFunction();

                            if (contentFunction.isStatic()) {
                                content = contentFunction.calculate(c, function);
                                contentFunction = null;
                                function = null;
                            } else {
                                content = contentFunction.getLayoutReplacementText();
                            }
                        }
                    }
                }
            } else if (type == CSSPrimitiveUnit.CSS_IDENT) {
                FSDerivedValue dv = style.valueByName(CSSName.QUOTES);
                
                if (dv != IdentValue.NONE) {
                    IdentValue ident = value.getIdentValue();
                    
                    if (ident == IdentValue.OPEN_QUOTE) {
                        String[] quotes = style.asStringArray(CSSName.QUOTES);
                        content = quotes[0];
                    } else if (ident == IdentValue.CLOSE_QUOTE) {
                        String[] quotes = style.asStringArray(CSSName.QUOTES);
                        content = quotes[1];
                    }
                }
            }

            if (content != null) {
                InlineBox iB = new InlineBox(content, null);
                iB.setContentFunction(contentFunction);
                iB.setFunction(function);
                iB.setElement(element);
                iB.setPseudoElementOrClass(peName);
                iB.setStartsHere(true);
                iB.setEndsHere(true);

                result.add(iB);
            }
        }

        return result;
    }

    public static BlockBox getRunningBlock(LayoutContext c, PropertyValue value)
    {
        List<PropertyValue> params = value.getFunction().getParameters();
        String ident = params.get(0).getStringValue();
        PageElementPosition position = null;
        
        if (params.size() == 2) {
            position = PageElementPosition.valueOf(
                    params.get(1).getStringValue());
        }
        if (position == null) {
            position = PageElementPosition.FIRST;
        }
        BlockBox target = c.getRootDocumentLayer().getRunningBlock(ident, c.getPage(), position);
        return target;
    }

    private static void insertGeneratedContent(
            LayoutContext c, Element element, CalculatedStyle parentStyle,
            String peName, List<Styleable> children, ChildBoxInfo info)
    {
        CascadedStyle peStyle = c.getCss().getPseudoElementStyle(element, peName);
        
        if (peStyle != null) {
            PropertyDeclaration contentDecl = peStyle.propertyByName(CSSName.CONTENT);
            PropertyDeclaration counterResetDecl = peStyle.propertyByName(CSSName.COUNTER_RESET);
            PropertyDeclaration counterIncrDecl = peStyle.propertyByName(CSSName.COUNTER_INCREMENT);

            CalculatedStyle calculatedStyle = null;
            if (contentDecl != null || counterResetDecl != null || counterIncrDecl != null) {
                calculatedStyle = parentStyle.deriveStyle(peStyle);
                if (calculatedStyle.isDisplayNone()) return;
                if (calculatedStyle.isIdent(CSSName.CONTENT, IdentValue.NONE)) return;
                if (calculatedStyle.isIdent(CSSName.CONTENT, IdentValue.NORMAL) && (peName.equals("before") || peName.equals("after")))
                    return;

                if (calculatedStyle.isTable() || calculatedStyle.isTableRow() || calculatedStyle.isTableSection()) {
                    CascadedStyle newPeStyle =
                        CascadedStyle.createLayoutStyle(peStyle, new PropertyDeclaration[] {
                            CascadedStyle.createLayoutPropertyDeclaration(
                                CSSName.DISPLAY,
                                IdentValue.BLOCK),
                        });
                    calculatedStyle = parentStyle.deriveStyle(newPeStyle);
                }
                c.resolveCounters(calculatedStyle);
            }

            if (contentDecl != null) {
                PropertyValue propValue = contentDecl.getValue();
                children.addAll(createGeneratedContent(c, element, peName, calculatedStyle,
                        propValue, info));
            }
        }
    }

    private static List<Styleable> createGeneratedContent(
            LayoutContext c, Element element, String peName,
            CalculatedStyle style, PropertyValue property, ChildBoxInfo info)
    {
        if (style.isDisplayNone() || style.isIdent(CSSName.DISPLAY, IdentValue.TABLE_COLUMN)
         || style.isIdent(CSSName.DISPLAY, IdentValue.TABLE_COLUMN_GROUP)) {
            return Collections.emptyList();
        }

        List<Styleable> inlineBoxes = createGeneratedContentList(
                c, element, property, peName, style, CONTENT_LIST_DOCUMENT, null);

        if (style.isInline()) {
            for (Styleable styleable : inlineBoxes) {
                InlineBox iB = (InlineBox) styleable;
                iB.setStyle(style);
                iB.applyTextTransform();
            }
            return inlineBoxes;
        } else {
            CalculatedStyle anon = style.createAnonymousStyle(IdentValue.INLINE);
            for (Styleable styleable : inlineBoxes) {
            	FSCancelController.cancelOpportunity(BoxBuilder.class);
            	
                InlineBox iB = (InlineBox) styleable;
                iB.setStyle(anon);
                iB.applyTextTransform();
                iB.setElement(null);
            }

            BlockBox result = createBlockBox(style, info, true);
            result.setStyle(style);
            result.setInlineContent(inlineBoxes);
            result.setElement(element);
            result.setChildrenContentType(BlockBox.CONTENT_INLINE);
            result.setPseudoElementOrClass(peName);

            if (! style.isLayedOutInInlineContext()) {
                info.setContainsBlockLevelContent(true);
            }

            return new ArrayList<Styleable>(Collections.singletonList(result));
        }
    }

    private static List<Styleable> createGeneratedMarginBoxContent(
            LayoutContext c, Element element, PropertyValue property,
            CalculatedStyle style, ChildBoxInfo info)
    {
        List<Styleable> result = createGeneratedContentList(
                c, element, property, null, style, CONTENT_LIST_MARGIN_BOX, info);

        CalculatedStyle anon = style.createAnonymousStyle(IdentValue.INLINE);
        
        for (Styleable s : result) {
        	FSCancelController.cancelOpportunity(BoxBuilder.class);
        	
            if (s instanceof InlineBox) {
                InlineBox iB = (InlineBox) s;
                iB.setElement(null);
                iB.setStyle(anon);
                iB.applyTextTransform();
            }
        }

        return result;
    }

    private static BlockBox createBlockBox(
            CalculatedStyle style, ChildBoxInfo info, boolean generated)
    {
        if (style.isFloated() && !(style.isAbsolute() || style.isFixed())) {
            BlockBox result;
            if (style.isTable() || style.isInlineTable()) {
                result = new TableBox();
            } else {
                result = new BlockBox();
            }
            result.setFloatedBoxData(new FloatedBoxData());
            return result;
        } else if (style.isSpecifiedAsBlock()) {
            return new BlockBox();
        } else if (! generated && (style.isTable() || style.isInlineTable())) {
            return new TableBox();
        } else if (style.isTableCell()) {
            info.setContainsTableContent(true);
            return new TableCellBox();
        } else if (! generated && style.isTableRow()) {
            info.setContainsTableContent(true);
            return new TableRowBox();
        } else if (! generated && style.isTableSection()) {
            info.setContainsTableContent(true);
            return new TableSectionBox();
        } else if (style.isTableCaption()) {
            info.setContainsTableContent(true);
            return new BlockBox();
        } else {
            return new BlockBox();
        }
    }

    private static void addColumns(LayoutContext c, TableBox table, TableColumn parent) 
    {
        SharedContext sharedContext = c.getSharedContext();

        NodeList nl = parent.getElement().getChildNodes();
        int length = nl.getLength();
        boolean haveColumn = false;
        
        for (int i = 0; i < length; i++)
        {
        	FSCancelController.cancelOpportunity(BoxBuilder.class);
        	
        	Node n = nl.item(i);

        	if (!(n instanceof Element))
        		continue;

        	Element el = (Element) n;
        	CalculatedStyle style = sharedContext.getStyle(el);

        	if (!style.isIdent(CSSName.DISPLAY, IdentValue.TABLE_COLUMN))
        		continue;
        	
        	TableColumn column = new TableColumn(el, style);
        	column.setParent(parent);
        	
        	table.addStyleColumn(column);
        	haveColumn = true;
        }
        
        if (!haveColumn)
        	table.addStyleColumn(parent);
    }

    private static void addColumnOrColumnGroup(
            LayoutContext c, TableBox table, Element e, CalculatedStyle style)
    {
        if (style.isIdent(CSSName.DISPLAY, IdentValue.TABLE_COLUMN)) {
            table.addStyleColumn(new TableColumn(e, style));
        } else { /* style.isIdent(CSSName.DISPLAY, IdentValue.TABLE_COLUMN_GROUP) */
            addColumns(c, table, new TableColumn(e, style));
        }
    }

    private static InlineBox createInlineBox(
            String text, Element parent, CalculatedStyle parentStyle, Node node)
    {
        InlineBox result = new InlineBox(text, node);

        if (parentStyle.isInline() && ! (parent.getParentNode() instanceof Document)) {
            result.setStyle(parentStyle);
            result.setElement(parent);
        } else {
            result.setStyle(parentStyle.createAnonymousStyle(IdentValue.INLINE));
        }

        result.applyTextTransform();

        return result;
    }

    private static void createChildren(
            LayoutContext c,
            BlockBox blockParent,
            Element parent,
            List<Styleable> children,
            ChildBoxInfo info,
            boolean inline)
    {
        SharedContext sharedContext = c.getSharedContext();

        CalculatedStyle parentStyle = sharedContext.getStyle(parent);

        insertGeneratedContent(c, parent, parentStyle, "before", children, info);

        Node working = parent.getFirstChild();
        
        boolean needStartText = inline;
        boolean needEndText = inline;
        
        if (working != null) {
            InlineBox previousIB = null;
            do {
                Styleable child = null;
                Node nodeType = working;
                
                if (nodeType instanceof Element) {
                    Element element = (Element) working;
                    CalculatedStyle style = sharedContext.getStyle(element);

                    if (style.isDisplayNone()) {
                        continue;
                    }

                    c.resolveCounters(style);

                    if (style.isIdent(CSSName.DISPLAY, IdentValue.TABLE_COLUMN)
                     || style.isIdent(CSSName.DISPLAY, IdentValue.TABLE_COLUMN_GROUP)) {
                        if ((blockParent != null) &&
                            (blockParent.getStyle().isTable() || blockParent.getStyle().isInlineTable())) {
                            TableBox table = (TableBox) blockParent;
                            addColumnOrColumnGroup(c, table, element, style);
                        }

                        continue;
                    }

                    if (style.isInline()) {
                        if (needStartText) {
                            needStartText = false;
                            InlineBox iB = createInlineBox("", parent, parentStyle, null);
                            iB.setStartsHere(true);
                            iB.setEndsHere(false);
                            children.add(iB);
                            previousIB = iB;
                        }
                        createChildren(c, null, element, children, info, true);
                        if (inline) {
                            if (previousIB != null) {
                                previousIB.setEndsHere(false);
                            }
                            needEndText = true;
                        }
                    } else {
                        child = createBlockBox(style, info, false);
                        child.setStyle(style);
                        child.setElement(element);
                        if (style.isListItem()) {
                            BlockBox block = (BlockBox) child;
                            block.setListCounter(c.getCounterContext(style).getCurrentCounterValue("list-item"));
                        }

                        if (style.isTable() || style.isInlineTable()) {
                            TableBox table = (TableBox) child;
                            table.ensureChildren(c);

                            child = reorderTableContent(c, table);
                        }

                        if (!info.isContainsBlockLevelContent()
                                && !style.isLayedOutInInlineContext()) {
                            info.setContainsBlockLevelContent(true);
                        }

                        BlockBox block = (BlockBox) child;
                        if (block.getStyle().mayHaveFirstLine()) {
                            block.setFirstLineStyle(c.getCss().getPseudoElementStyle(element,
                                    "first-line"));
                        }
                        if (block.getStyle().mayHaveFirstLetter()) {
                            block.setFirstLetterStyle(c.getCss().getPseudoElementStyle(element,
                                    "first-letter"));
                        }
                        //I think we need to do this to evaluate counters correctly
                        block.ensureChildren(c);
                    }
                } else if (nodeType instanceof Text || nodeType instanceof CDATASection) {
                    needStartText = false;
                    needEndText = false;

                    Node textNode = (Node) working;

                    /*
                    StringBuffer text = new StringBuffer(textNode.getData());

                    Node maybeText = textNode;
                    while (true) {
                        maybeText = textNode.getNextSibling();
                        if (maybeText != null) {
                            short maybeNodeType = maybeText.getNodeType();
                            if (maybeNodeType == Node.TEXT_NODE ||
                                    maybeNodeType == Node.CDATA_SECTION_NODE) {
                                textNode = (Text)maybeText;
                                text.append(textNode.getData());
                            } else {
                                break;
                            }
                        } else {
                            break;
                        }
                    }

                    working = textNode;
                    child = createInlineBox(text.toString(), parent, parentStyle, textNode);
                    */

                    child = createInlineBox(textNode instanceof CDATASection ? ((CDATASection) textNode).getTextContent() : ((Text) textNode).getWholeText(), parent, parentStyle, textNode);

                    InlineBox iB = (InlineBox) child;
                    iB.setEndsHere(true);
                    if (previousIB == null) {
                        iB.setStartsHere(true);
                    } else {
                        previousIB.setEndsHere(false);
                    }
                    previousIB = iB;
                }

                if (child != null) {
                    children.add(child);
                }
            } while ((working = working.getNextSibling()) != null);
        }
        if (needStartText || needEndText) {
            InlineBox iB = createInlineBox("", parent, parentStyle, null);
            iB.setStartsHere(needStartText);
            iB.setEndsHere(needEndText);
            children.add(iB);
        }
        insertGeneratedContent(c, parent, parentStyle, "after", children, info);
    }

    private static void insertAnonymousBlocks(
            SharedContext c, Box parent, List<Styleable> children, boolean layoutRunningBlocks) 
    {
        List<Styleable> inline = new ArrayList<Styleable>();
        LinkedList<InlineBox> parents = new LinkedList<InlineBox>();

        List<InlineBox> savedParents = null;

        for (Styleable child : children) 
        {
        	FSCancelController.cancelOpportunity(BoxBuilder.class);
        	
            if (child.getStyle().isLayedOutInInlineContext() &&
             ! (layoutRunningBlocks && child.getStyle().isRunning())) {
                inline.add(child);

                if (child.getStyle().isInline()) {
                    InlineBox iB = (InlineBox) child;
                    if (iB.isStartsHere()) {
                        parents.add(iB);
                    }
                    if (iB.isEndsHere()) {
                        parents.removeLast();
                    }
                }
            } else {
                if (inline.size() > 0) {
                    createAnonymousBlock(c, parent, inline, savedParents);
                    inline = new ArrayList<Styleable>();
                    savedParents = new ArrayList<InlineBox>(parents);
                }
                parent.addChild((Box) child);
            }
        }

        createAnonymousBlock(c, parent, inline, savedParents);
    }

    /**
     * A <code>BlockBox</code> can only contain one of inline or block
     * level content. This function creates an anonymous block around inline content so that
     * a block containing <code>BlockBox</code> preserves this constraint.
     * @param c
     * @param parent
     * @param inline
     * @param savedParents
     */
    private static void createAnonymousBlock(SharedContext c, Box parent, List<Styleable> inline, List<InlineBox> savedParents)
    {
        WhitespaceStripper.stripInlineContent(inline);
        
        if (!inline.isEmpty()) 
        {
            AnonymousBlockBox anon = new AnonymousBlockBox(parent.getElement());
            anon.setStyle(parent.getStyle().createAnonymousStyle(IdentValue.BLOCK));
            anon.setAnonymous(true);
            
            if (savedParents != null && !savedParents.isEmpty()) 
            {
                anon.setOpenInlineBoxes(savedParents);
            }
            
            parent.addChild(anon);
            anon.setChildrenContentType(BlockBox.CONTENT_INLINE);
            anon.setInlineContent(inline);
        }
    }

    private static class ChildBoxInfo
    {
        private boolean _containsBlockLevelContent;
        private boolean _containsTableContent;
        private boolean _layoutRunningBlocks;

        public ChildBoxInfo() {
        }

        public boolean isContainsBlockLevelContent() {
            return _containsBlockLevelContent;
        }

        public void setContainsBlockLevelContent(boolean containsBlockLevelContent) {
            _containsBlockLevelContent = containsBlockLevelContent;
        }

        public boolean isContainsTableContent() {
            return _containsTableContent;
        }

        public void setContainsTableContent(boolean containsTableContent) {
            _containsTableContent = containsTableContent;
        }

        public boolean isLayoutRunningBlocks() {
            return _layoutRunningBlocks;
        }

        public void setLayoutRunningBlocks(boolean layoutRunningBlocks) {
            _layoutRunningBlocks = layoutRunningBlocks;
        }
    }
}
