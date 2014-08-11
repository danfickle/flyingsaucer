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
package com.github.neoflyingsaucer.pdfout.form;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.render.RenderingContext;
import com.github.neoflyingsaucer.pdfout.PdfOutputDevice;
import com.github.pdfstream.Page;
import com.github.pdfstream.PdfFormElement;


public class SelectFormField extends AbstractFormField {
    private static final String FIELD_TYPE = "Select";
    
    private static final int EMPTY_SPACE_COUNT = 10;
    private static final int EXTRA_SPACE_COUNT = 4;
    
    private final List<Option> _options;

    private final int _baseline;
    
    public SelectFormField(final LayoutContext c, final BlockBox box, final int cssWidth, final int cssHeight) 
    {
        _options = readOptions(box.getElement());
        initDimensions(c, box, cssWidth, cssHeight);
        
        final float fontSize = box.getStyle().getFSFont(c).getSize2D();

        // FIXME: findbugs possible loss of precision, cf. int / (float)2
        _baseline = (int)(getHeight() / 2 + (fontSize * 0.3f));
    }
    
    private int getSelectedIndex() 
    {
        for (int i = 0; i < _options.size(); i++)
        {
        	if (_options.get(i).isSelected())
        		return i;
        }
        
        return 0;
    }
    
    private String[][] getPDFOptions() 
    {
        final String[][] result = new String[_options.size()][];
        
        for (int i = 0; i < _options.size(); i++)
        {
        	result[i] = new String[] { _options.get(i).getValue(), _options.get(i).getLabel() };
        }
        
        return result;
    }
    
    private int calcDefaultWidth(final LayoutContext c, final BlockBox box) 
    {
        if (_options.size() == 0)
        {
            return c.getTextRenderer().getWidth(
                    c.getFontContext(),
                    box.getStyle().getFSFont(c),
                    spaces(EMPTY_SPACE_COUNT));
        }
        else
        {
            int maxWidth = 0;
            for (final Option option : _options) {
                final String result = option.getLabel() + spaces(EXTRA_SPACE_COUNT);
                
                final int width = c.getTextRenderer().getWidth(
                        c.getFontContext(),
                        box.getStyle().getFSFont(c),
                        result);
                
                if (width > maxWidth) {
                    maxWidth = width;
                }
            }
            
            return maxWidth;
        }
    }
    
    private List<Option> readOptions(final Element e) 
    {
        final List<Option> result = new ArrayList<Option>();
        
        Node n = e.getFirstChild();
        
        while (n != null) 
        {
            if (n instanceof Element && "option".equalsIgnoreCase(n.getNodeName())) 
            {
                final Element optionElem = (Element) n;
                final String label = collectText(optionElem);
                String value;

                if (!optionElem.hasAttribute("value")) {
                    value = label;
                } else {
                    value = optionElem.getAttribute("value");
                }
                
                if (label != null) {
                    final Option option = new Option();
                    option.setLabel(label);
                    option.setValue(value);
                    if (isSelected(optionElem)) {
                        option.setSelected(true);
                    }
                    result.add(option);
                }
            }
            
            n = n.getNextSibling();
        }
        
        return result;
    }
    
    private String collectText(final Element e) 
    {
        final StringBuilder result = new StringBuilder();
        
        Node n = e.getFirstChild();

        while (n != null) 
        {
        	if (n instanceof Text)
            	result.append(((Text) n).getWholeText());

            n = n.getNextSibling();
        }
        
        return result.length() > 0 ? result.toString() : null;
    }

    protected void initDimensions(final LayoutContext c, final BlockBox box, final int cssWidth, final int cssHeight) 
    {
        if (cssWidth != -1) {
            setWidth(cssWidth);
        } else {
            setWidth(calcDefaultWidth(c, box));
        }

        if (cssHeight != -1) {
            setHeight(cssHeight);
        } else {
            setHeight((int) (box.getStyle().getLineHeight(c) * getSize(box.getElement())));
        }
    } 
    
    private int getSize(final Element elem) 
    {
        int result = 1;
        try {
            final String v = elem.hasAttribute("size") ? elem.getAttribute("size").trim() : "";

            if (!v.isEmpty()) {
                final int i = Integer.parseInt(v);
                if (i > 1) {
                    result = i;
                }
            }
        } catch (final NumberFormatException e) {
            // ignore
        }
        
        return result;
    }
    
    protected boolean isMultiple(final Element e) {
        return e.hasAttribute("multiple");
    }
    
    protected String getFieldType() {
        return FIELD_TYPE;
    }

    public void paint(final RenderingContext c, final PdfOutputDevice outputDevice, final BlockBox box) 
    {
        final Page cb = outputDevice.getCurrentPage();
        final String[][] options = getPDFOptions();
        final int selectedIndex = getSelectedIndex();
        final Rectangle2D targetArea = outputDevice.createLocalTargetArea(c, box, false);
        final PdfFormElement field = new PdfFormElement();
        final String fieldName = getFieldName(outputDevice, box.getElement());
        /*
         * Comment out for now.  We need to draw an appropriate appearance for
         * this to work correctly.
         */
        /*
        if (isMultiple(box.getElement())) {
            field = PdfFormField.createList(writer, options, selectedIndex);  
        } else {
            field = PdfFormField.createCombo(writer, false, options, selectedIndex);    
        }
        */
        field.setClass("Ch");
        field.setOptions(options, selectedIndex);
        field.setRectangle((float) targetArea.getMinX(), (float) targetArea.getMinY(), (float) targetArea.getMaxX(), (float) targetArea.getMaxY());
        
        //field.setWidget(, PdfAnnotation.HIGHLIGHT_INVERT);
        field.setPartialName(fieldName.replace('.', '_'));
        field.setExportName(fieldName);
        
        createAppearance(c, outputDevice, box, field);

        if (isReadOnly(box.getElement()))
            field.setBitfield((1 << 18) | PdfFormElement.BF_READONLY);
        else
        	field.setBitfield(1 << 18);
        
        //field.setDefaultAppearanceString("0 1 0 rg 0 0 1 RG");
        
        /*
        if (isMultiple(box.getElement())) {
            field.setFieldFlags(PdfFormField.FF_MULTISELECT);
        }
        */
        //field.publishAppearanceStreams(cb.getPdf());
        cb.addFormField(field);
    }
    
    private void createAppearance(
            final RenderingContext c, final PdfOutputDevice outputDevice, 
            final BlockBox box, final PdfFormElement field) 
    {
//        final PdfFont font = (PdfFont) box.getStyle().getFSFont(c);
//        
//        final float width = outputDevice.getDeviceLength(getWidth());
//        final float height = outputDevice.getDeviceLength(getHeight());
//        final float fontSize = outputDevice.getDeviceLength(font.getSize2D());
//        
//        final PdfAppearanceStream tp = new PdfAppearanceStream("", width, height);
//        tp.setFontAndSize(font.getFontDescription().getFont(), fontSize);
//        
//        final FSColor color = box.getStyle().getColor();
//        setFillColor(tp, color);
        
        //field.setDefaultAppearanceString();
    }    

    public int getBaseline() {
        return _baseline;
    }

    public boolean hasBaseline() {
        return true;
    }
    
    private static final class Option {
        private String _value;
        private String _label;
        private boolean _selected;
        
        public String getValue() {
            return _value;
        }
        
        public void setValue(final String value) {
            _value = value;
        }
        
        public String getLabel() {
            return _label;
        }
        
        public void setLabel(final String label) {
            _label = label;
        }
        
        public boolean isSelected() {
            return _selected;
        }
        
        public void setSelected(final boolean selected) {
            _selected = selected;
        }
    }
}
