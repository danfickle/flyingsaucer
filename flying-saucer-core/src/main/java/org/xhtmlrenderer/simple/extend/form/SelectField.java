/*
 * {{{ header & license
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
package org.xhtmlrenderer.simple.extend.form;

import java.awt.Component;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jsoup.select.Elements;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.simple.extend.XhtmlForm;
import org.xhtmlrenderer.util.GeneralUtil;
import org.xhtmlrenderer.util.NodeHelper;

class SelectField extends FormField {
  public SelectField(final Element e, final XhtmlForm form,
      final LayoutContext context, final BlockBox box) {
    super(e, form, context, box);
  }

  public JComponent create() {
    final List<NameValuePair> optionList = createList();

    // Either a select list or a drop down/combobox
    if (shouldRenderAsList()) {
      final JList<?> select = new JList<>(optionList.toArray());
      applyComponentStyle(select);

      select.setCellRenderer(new CellRenderer());
      select.addListSelectionListener(new HeadingItemListener());

      if (hasAttribute("multiple")
          && getAttribute("multiple").equalsIgnoreCase("true")) {
        select.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
      } else {
        select.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      }

      int size = 0;

      if (hasAttribute("size")) {
        size = GeneralUtil.parseIntRelaxed(getAttribute("size"));
      }

      if (size == 0) {
        // Default to the number of items in the list or 20 - whichever is less
        select.setVisibleRowCount(Math.min(select.getModel().getSize(), 20));
      } else {
        select.setVisibleRowCount(size);
      }

      return new JScrollPane(select);
    } else {
      final JComboBox<?> select = new JComboBox<>(optionList.toArray());
      applyComponentStyle(select);

      select.setEditable(false);
      select.setRenderer(new CellRenderer());
      select.addItemListener(new HeadingItemListener());

      return select;
    }
  }

  protected FormFieldState loadOriginalState() {
    final ArrayList<Integer> list = new ArrayList<Integer>();

    final NodeList options = getElement().getElementsByTagName("option");

    for (int i = 0; i < options.getLength(); i++) {
      final Element option = (Element) options.item(i);

      if (option.hasAttribute("selected")
          && option.getAttribute("selected").equalsIgnoreCase("selected")) {
        list.add(new Integer(i));
      }
    }

    return FormFieldState.fromList(list);
  }

  protected void applyOriginalState() {
    if (shouldRenderAsList()) {
      @SuppressWarnings("unchecked")
      final JList<NameValuePair> select = (JList<NameValuePair>) ((JScrollPane) getComponent())
          .getViewport().getView();

      select.setSelectedIndices(getOriginalState().getSelectedIndices());
    } else {
      @SuppressWarnings("unchecked")
      final JComboBox<NameValuePair> select = (JComboBox<NameValuePair>) getComponent();

      // This looks strange, but basically since this is a single select, and
      // someone might have put selected="selected" on more than a single option
      // I believe that the correct play here is to select the _last_ option
      // with
      // that attribute.
      final int[] indices = getOriginalState().getSelectedIndices();

      if (indices.length == 0) {
        select.setSelectedIndex(0);
      } else {
        select.setSelectedIndex(indices[indices.length - 1]);
      }
    }
  }

  protected String[] getFieldValues() {
    if (shouldRenderAsList()) {
      @SuppressWarnings("unchecked")
      final JList<NameValuePair> select = (JList<NameValuePair>) ((JScrollPane) getComponent())
          .getViewport().getView();

      final List<NameValuePair> selectedValues = select.getSelectedValuesList();
      final String[] submitValues = new String[selectedValues.size()];

      for (int i = 0; i < selectedValues.size(); i++) {
        final NameValuePair pair = (NameValuePair) selectedValues.get(i);
        if (pair.getValue() != null)
          submitValues[i] = pair.getValue();
      }

      return submitValues;
    } else {
      @SuppressWarnings("unchecked")
      final JComboBox<NameValuePair> select = (JComboBox<NameValuePair>) getComponent();

      final NameValuePair selectedValue = (NameValuePair) select
          .getSelectedItem();

      if (selectedValue != null) {
        if (selectedValue.getValue() != null)
          return new String[] { selectedValue.getValue() };
      }
    }

    return new String[] {};
  }

  private List<NameValuePair> createList() {
    final List<NameValuePair> list = new ArrayList<NameValuePair>();
    addChildren(list, getElement(), 0);
    return list;
  }

  private void addChildren(final List<NameValuePair> list, final Element e,
      final int indent) {
    for (final Node childNode : NodeHelper.getChildrenAsNodes(e)) {

      if (childNode instanceof Element) {
        final Element child = (Element) childNode;

        if ("option".equals(child.getNodeName())) {
          // option tag, add it
          final String optionText = XhtmlForm.collectText(child);
          String optionValue = optionText;

          if (child.hasAttribute("value")) {
            optionValue = child.getAttribute("value");
          }

          list.add(new NameValuePair(optionText, optionValue, indent));

        } else if ("optgroup".equals(child.getNodeName())) {
          // optgroup tag, append heading and indent children
          final String titleText = child.getAttribute("label");
          list.add(new NameValuePair(titleText, null, indent));
          addChildren(list, child, indent + 1);
        }
      }
    }
  }

  private boolean shouldRenderAsList() {
    boolean result = false;

    if (hasAttribute("multiple")
        && getAttribute("multiple").equalsIgnoreCase("true")) {
      result = true;
    } else if (hasAttribute("size")) {
      final int size = GeneralUtil.parseIntRelaxed(getAttribute("size"));

      if (size > 0) {
        result = true;
      }
    }

    return result;
  }

  /**
   * Provides a simple container for name/value data, such as that used by the
   * &lt;option&gt; elements in a &lt;select&gt; list.
   * <p>
   * When the value is {@code null}, this pair is used as a heading and should
   * not be selected by itself.
   * <p>
   * The indent property was added to support indentation of items as children
   * below headings.
   */
  private static class NameValuePair {
    private final String _name;
    private final String _value;
    private final int _indent;

    public NameValuePair(final String name, final String value, final int indent) {
      _name = name;
      _value = value;
      _indent = indent;
    }

    public String getName() {
      return _name;
    }

    public String getValue() {
      return _value;
    }

    public int getIndent() {
      return _indent;
    }

    public String toString() {
      String txt = getName();
      for (int i = 0; i < getIndent(); i++) {
        txt = "    " + txt;
      }
      return txt;
    }
  }

  /**
   * Renderer for ordinary items and headings in a List.
   */
  @SuppressWarnings("serial")
  private static class CellRenderer extends DefaultListCellRenderer {
    public Component getListCellRendererComponent(final JList<?> list,
        final Object value, final int index, final boolean isSelected,
        final boolean cellHasFocus) {
      final NameValuePair pair = (NameValuePair) value;

      if (pair != null && pair.getValue() == null) {
        // render as heading as such
        super.getListCellRendererComponent(list, value, index, false, false);
        final Font fold = getFont();
        final Font fnew = new Font(fold.getName(), Font.BOLD | Font.ITALIC,
            fold.getSize());
        setFont(fnew);
      } else {
        // other items as usuall
        super.getListCellRendererComponent(list, value, index, isSelected,
            cellHasFocus);
      }

      return this;
    }
  }

  /**
   * Helper class that makes headings inside a list unselectable
   * <p>
   * This is an {@linkplain ItemListener} for comboboxes, and a
   * {@linkplain ListSelectionListener} for lists.
   */
  private static class HeadingItemListener implements ItemListener,
      ListSelectionListener {

    private Object oldSelection = null;
    private int[] oldSelections = new int[0];

    public void itemStateChanged(final ItemEvent e) {
      if (e.getStateChange() != ItemEvent.SELECTED)
        return;
      // only for comboboxes
      if (!(e.getSource() instanceof JComboBox))
        return;
      @SuppressWarnings("unchecked")
      final JComboBox<NameValuePair> combo = (JComboBox<NameValuePair>) e
          .getSource();

      if (((NameValuePair) e.getItem()).getValue() == null) {
        // header selected: revert to old selection
        combo.setSelectedItem(oldSelection);
      } else {
        // store old selection
        oldSelection = e.getItem();
      }
    }

    public void valueChanged(final ListSelectionEvent e) {
      // only for lists
      if (!(e.getSource() instanceof JList))
        return;
      @SuppressWarnings("unchecked")
      final JList<NameValuePair> list = (JList<NameValuePair>) e.getSource();
      final ListModel<NameValuePair> model = list.getModel();

      // deselect all headings
      for (int i = e.getFirstIndex(); i <= e.getLastIndex(); i++) {
        if (!list.isSelectedIndex(i))
          continue;
        final NameValuePair pair = (NameValuePair) model.getElementAt(i);
        if (pair != null && pair.getValue() == null) {
          // We have a heading, remove it. As this handler is called
          // as a result of the resulting removal and we do process
          // the events while the value is adjusting, we don't need
          // to process any other headings here.
          // BUT if there'll be no selection anymore because by selecting
          // this one the old selection was cleared, restore the old
          // selection.
          if (list.getSelectedIndices().length == 1)
            list.setSelectedIndices(oldSelections);
          else
            list.removeSelectionInterval(i, i);
          return;
        }
      }

      // if final selection: store it
      if (!e.getValueIsAdjusting())
        oldSelections = list.getSelectedIndices();
    }
  }
}
