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
import com.github.neoflyingsaucer.pdfout.PdfOutputDevice;
import com.github.pdfstream.Page;
import com.github.pdfstream.PdfAppearanceStream;
import com.github.pdfstream.PdfFormElement;
import org.w3c.dom.Element;
import org.xhtmlrenderer.css.parser.FSRGBColor;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.render.RenderingContext;

public class CheckboxFormField extends AbstractFormField
{
  private static final String FIELD_TYPE = "Checkbox";

  public CheckboxFormField(final LayoutContext c, final BlockBox box, final int cssWidth, final int cssHeight)
  {
    initDimensions(c, box, cssWidth, cssHeight);
  }

  protected String getFieldType()
  {
    return FIELD_TYPE;
  }

  public void paint(final RenderingContext c, final PdfOutputDevice outputDevice, final BlockBox box)
  {
    final Page cb = outputDevice.getCurrentPage();
    final Element elm = box.getElement();
    final Rectangle2D targetArea = outputDevice.createLocalTargetArea(c, box, false);
    final String onValue = getValue(elm);
    final PdfFormElement field = new PdfFormElement();
    		
    field.setRectangle((float) targetArea.getMinX(), (float) targetArea.getMinY(), (float) targetArea.getMaxX(), (float) targetArea.getMaxY());
    field.setPartialName(getFieldName(outputDevice, elm).replace('.', '_'));
    field.setExportName(getFieldName(outputDevice, elm));
    field.setExportValue(onValue);
    field.setClass("Btn");
    field.setDefaultState(isChecked(elm) ? "Yes" : "Off");

    if (isReadOnly(elm))
    	field.setBitfield(PdfFormElement.BF_READONLY);
    
    createAppearances(field, (float) targetArea.getWidth(), (float) targetArea.getHeight());
    
    field.publishAppearanceStreams(cb.getPdf());
    cb.addFormField(field);
  }

  private void createAppearances(final PdfFormElement field,
          final float width, final float height) 
  {
      final PdfAppearanceStream tpOff = new PdfAppearanceStream("Off", width, height);
      final PdfAppearanceStream tpOn = new PdfAppearanceStream("Yes", width, height);     
      
      rectPath(tpOff, width, height);
      rectPath(tpOn, width, height);

      setFillColor(tpOn, new FSRGBColor(255, 255, 255));
      setFillColor(tpOff, new FSRGBColor(255, 255, 255));
      
      // Our white background.
      tpOff.fill();
      tpOn.fill();
            
      rectPath(tpOff, width, height);
      rectPath(tpOn, width, height);
      
      setStrokeColor(tpOff, new FSRGBColor(0, 0, 0));
      setStrokeColor(tpOn, new FSRGBColor(0, 0, 0));

      // Black border.
      tpOff.stroke();
      tpOn.stroke();
      
      // Our tick.
      tpOn.moveTo(0, height / 2f);
      tpOn.lineTo(width / 2f, 0);
      tpOn.lineTo(width, height); 
      tpOn.stroke();

      field.addApearanceStream(tpOff);
      field.addApearanceStream(tpOn);
  }
  
  private void rectPath(PdfAppearanceStream strm, float width, float height)
  {
      strm.moveTo(0, 0);
      strm.lineTo(width, 0);
      strm.lineTo(width, height);
      strm.lineTo(0, height);
      strm.lineTo(0, 0);
      strm.closePath();
  }
  
  public int getBaseline()
  {
    return 0;
  }

  public boolean hasBaseline()
  {
    return false;
  }
}
