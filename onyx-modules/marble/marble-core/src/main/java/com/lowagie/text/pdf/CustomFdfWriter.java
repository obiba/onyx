/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package com.lowagie.text.pdf;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An extension of {@code FdfWriter} to support setting the {@code PdfName.A} entry in a field dictionary. The PDF
 * standard supports {@code A} entries in the dictionary, but iText's FdfWriter does not support it.
 * <p>
 * This class offers an additional method {@link #setFieldAsAction(String, PdfAction)} that allows setting a field's
 * {@code A} entry. The {@code PdfAction} can be constructed using one of the static method of the {@code PdfAction}
 * class.
 * 
 */
public class CustomFdfWriter extends FdfWriter {

  private static final Logger log = LoggerFactory.getLogger(CustomFdfWriter.class);

  @Override
  public void writeTo(OutputStream os) throws IOException {
    MyWrt wrt = new MyWrt(os, this);
    wrt.writeTo();
  }

  public void setFieldAsAction(String field, PdfAction action) {
    log.debug("Setting field {} as an A dictionary entry", field);
    setField(field, action);
  }

  /**
   * Overridden to handle the PdfAction type.
   */
  static class MyWrt extends Wrt {

    MyWrt(OutputStream os, FdfWriter fdf) throws IOException {
      super(os, fdf);
    }

    @Override
    @SuppressWarnings("unchecked")
    PdfArray calculate(HashMap map) throws IOException {
      PdfArray ar = new PdfArray();
      for(Iterator it = map.entrySet().iterator(); it.hasNext();) {
        Map.Entry entry = (Map.Entry) it.next();
        String key = (String) entry.getKey();
        Object v = entry.getValue();
        PdfDictionary dic = new PdfDictionary();
        dic.put(PdfName.T, new PdfString(key, PdfObject.TEXT_UNICODE));
        if(v instanceof HashMap) {
          dic.put(PdfName.KIDS, calculate((HashMap) v));
        } else if(v instanceof PdfAction) {
          // Special case of the A entry.
          dic.put(PdfName.A, (PdfAction) v);
        } else {
          dic.put(PdfName.V, (PdfObject) v);
        }
        ar.add(dic);
      }
      return ar;
    }

  }
}
