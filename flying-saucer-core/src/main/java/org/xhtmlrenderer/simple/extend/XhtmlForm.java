/*
 * {{{ header & license
 * Copyright (c) 2004, 2005 Torbjoern Gannholm
 * Copyright (c) 2007 Sean Bright
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
package org.xhtmlrenderer.simple.extend;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JRadioButton;

import org.w3c.dom.CDATASection;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.simple.extend.form.FormField;
import org.xhtmlrenderer.simple.extend.form.FormFieldFactory;
import org.xhtmlrenderer.util.XRLog;

/**
 * Represents a form object
 *
 * @author Torbjoern Gannholm
 * @author Sean Bright
 */
public class XhtmlForm {
    private static final String FS_DEFAULT_GROUP = "__fs_default_group_";

    private static int _defaultGroupCount = 1;

    private final UserAgentCallback _userAgentCallback;
    private final Map<Element, FormField> _componentCache;
    private final Map<String, ButtonGroupWrapper> _buttonGroups;
    private final Element _parentFormElement;
    private final FormSubmissionListener _formSubmissionListener;

    public XhtmlForm(final UserAgentCallback uac, final Element e, final FormSubmissionListener fsListener) {
        _userAgentCallback = uac;
        _buttonGroups = new HashMap<String, ButtonGroupWrapper>();
        _componentCache = new LinkedHashMap<Element, FormField>();
        _parentFormElement = e;
        _formSubmissionListener = fsListener;
    }

    public XhtmlForm(final UserAgentCallback uac, final Element e) {
        this(uac, e, new DefaultFormSubmissionListener());
    }

    public UserAgentCallback getUserAgentCallback() {
        return _userAgentCallback;
    }
    
    public void addButtonToGroup(String groupName, final AbstractButton button) {
        if (groupName == null) {
            groupName = createNewDefaultGroupName();
        }

        ButtonGroupWrapper group = _buttonGroups.get(groupName);
        
        if (group == null) {
            group = new ButtonGroupWrapper();

            _buttonGroups.put(groupName, group);
        }

        group.add(button);
    }
    
    private static String createNewDefaultGroupName() {
        return FS_DEFAULT_GROUP + ++_defaultGroupCount;
    }

    private static boolean isFormField(final Element e) {
        final String nodeName = e.getNodeName();
        
        if (nodeName.equals("input") || nodeName.equals("select") || nodeName.equals("textarea")) {
            return true;
        }
        
        return false;
    }

    public FormField addComponent(final Element e, final LayoutContext context, final BlockBox box) {
        FormField field = null;

        if (_componentCache.containsKey(e)) {
            field = _componentCache.get(e);
        } else {
            if (!isFormField(e)) {
                return null;
            }

            field = FormFieldFactory.create(this, context, box);
    
            if (field == null) {
                XRLog.layout("Unknown field type: " + e.getNodeName());

                return null;
            }
            
            _componentCache.put(e, field);
        }

        return field;
    }
    
    public void reset() {
        final Iterator<ButtonGroupWrapper> buttonGroups = _buttonGroups.values().iterator();
        while (buttonGroups.hasNext()) {
            buttonGroups.next().clearSelection();
        }

        final Iterator<FormField> fields = _componentCache.values().iterator();
        while (fields.hasNext()) {
            fields.next().reset();
        }
    }

    public void submit(final JComponent source) {
        // If we don't have a <form> to tell us what to do, don't
        // do anything.
        if (_parentFormElement == null) {
            return;
        }

        final StringBuffer data = new StringBuffer();
        final String action = _parentFormElement.getAttribute("action");
        data.append(action).append("?");
        final Iterator<Map.Entry<Element, FormField>> fields = _componentCache.entrySet().iterator();
        boolean first=true;
        while (fields.hasNext()) {
            final Map.Entry<Element, FormField> entry = fields.next();

            final FormField field = (FormField) entry.getValue();
            
            if (field.includeInSubmission(source)) {
                final String [] dataStrings = field.getFormDataStrings();
                
                for (final String dataString : dataStrings) {
                    if (!first) {
                        data.append('&');
                    }
    
                    data.append(dataString);
                    first=false;
                }
            }
        }
        
        if(_formSubmissionListener !=null) _formSubmissionListener.submit(data.toString());
    }

    public static String collectText(final Element e) {
        final StringBuffer result = new StringBuffer();

        if (e.hasChildNodes()) {
            Node node = e.getFirstChild();
        	do {
                if (node instanceof Text) {
                    final Text text = (Text) node;
                    result.append(text.getTextContent());
                }
                else if (node instanceof CDATASection) {
                	final CDATASection data = (CDATASection) node;
                	result.append(data.getTextContent());
                }
                
            } while ((node = node.getNextSibling()) != null);
        }
        return result.toString().trim();
    }
    
    private static class ButtonGroupWrapper {
        private final ButtonGroup _group;
        private final AbstractButton _dummy;
        
        public ButtonGroupWrapper() {
            _group = new ButtonGroup();
            _dummy = new JRadioButton();

            // We need a dummy button to have the appearance of all of
            // the radio buttons being in an unselected state.
            //
            // From:
            //   http://java.sun.com/j2se/1.5/docs/api/javax/swing/ButtonGroup.html
            //
            // "There is no way to turn a button programmatically to 'off', in
            // order to clear the button group. To give the appearance of 'none
            // selected', add an invisible radio button to the group and then
            // programmatically select that button to turn off all the displayed
            // radio buttons. For example, a normal button with the label 'none'
            // could be wired to select the invisible radio button.
            _group.add(_dummy);
        }
        
        public void add(final AbstractButton b) {
            _group.add(b);
        }

        public void clearSelection() {
            _group.setSelected(_dummy.getModel(), true);
        }
    }
}
