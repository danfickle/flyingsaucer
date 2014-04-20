/*
 * {{{ header & license
 * Copyright (c) 2007 Vianney le Clément
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
package org.xhtmlrenderer.simple.xhtml.controls;

import static org.xhtmlrenderer.util.GeneralUtil.ciEquals;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xhtmlrenderer.simple.xhtml.XhtmlForm;
import org.xhtmlrenderer.util.NodeHelper;

public class SelectControl extends AbstractControl {

    private final int _size;
    private final boolean _multiple;
    private List<String> _values;

    private String _initialValue;
    private String[] _initialValues;

    private final Map<String, String> _options;

    public SelectControl(final XhtmlForm form, final Element e) {
        super(form, e);

        _size = getIntAttribute(e, "size", 1);
        _multiple = e.getAttribute("multiple").length() != 0;
        if (_multiple) {
            _values = new ArrayList<String>();
        }
        super.setValue(null);
        setSuccessful(false);

        _options = new LinkedHashMap<String, String>();
        traverseOptions(e, "");

        if (_multiple) {
            _initialValues = getMultipleValues();
            if (_initialValues.length > 0) {
                setSuccessful(true);
            }
        } else {
            _initialValue = getValue();
            if (_initialValue != null) {
                setSuccessful(true);
            }
        }
    }

    private void traverseOptions(final Element e, final String prefix) {
        final NodeList children = e.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (NodeHelper.isElement(children.item(i))) {
                final Element child = (Element) children.item(i);
                if (ciEquals(child.getNodeName(), "optgroup")) {
                    traverseOptions(child, prefix + child.getAttribute("label")
                            + " ");
                } else if (ciEquals(child.getNodeName(), "option")) {
                    String value = child.getAttribute("value");
                    String label = child.getAttribute("label");
                    final String content = collectText(child);
                    if (value.length() == 0) {
                        value = content;
                    }
                    if (label.length() == 0) {
                        label = content;
                    } else {
                        label = prefix.concat(label);
                    }
                    _options.put(value, label);
                    if (child.getAttribute("selected").length() != 0) {
                        if (isMultiple()) {
                            if (!_values.contains(value)) {
                                _values.add(value);
                            }
                        } else {
                            setValue(value);
                        }
                    }
                }
            }
        }
    }

    public int getSize() {
        return _size;
    }

    public boolean isMultiple() {
        return _multiple;
    }

    public Map<String, String> getOptions() {
        return new LinkedHashMap<String, String>(_options);
    }

    public void setValue(final String value) {
        if (!isMultiple()) {
            if (_options.containsKey(value)) {
                super.setValue(value);
                setSuccessful(true);
            } else {
                setSuccessful(false);
                super.setValue(null);
            }
        }
    }

    public String[] getMultipleValues() {
        if (isMultiple()) {
            return _values.toArray(new String[_values.size()]);
        } else {
            return null;
        }
    }

    public void setMultipleValues(final String[] values) {
        if (isMultiple()) {
            _values.clear();
            for (int i = 0; i < values.length; i++) {
                if (_options.get(values[i]) != null
                        && !_values.contains(values[i])) {
                    _values.add(values[i]);
                }
            }
            if (_values.isEmpty()) {
                setSuccessful(false);
            } else {
                setSuccessful(true);
            }
            fireChanged();
        }
    }

    public void reset() {
        if (isMultiple()) {
            setMultipleValues(_initialValues);
        } else {
            setValue(_initialValue);
        }
    }

}
