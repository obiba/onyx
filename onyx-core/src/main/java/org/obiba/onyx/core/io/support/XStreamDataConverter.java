/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.io.support;

import java.io.Serializable;

import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataType;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * Xstream converter for <code>Data</code> objects.
 */
public class XStreamDataConverter implements Converter {
  //
  // Converter Methods
  //

  public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
    Data data = (Data) source;

    writer.startNode("data");
    writer.addAttribute("type", data.getType().name().toLowerCase());
    writer.setValue(data.getValueAsString());
    writer.endNode();
  }

  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
    String typeName = reader.getAttribute("type");

    Data data = new Data(DataType.valueOf(typeName.toUpperCase()));
    data.setValue(convertStringToType(reader.getValue(), data.getType()));

    return data;
  }

  public boolean canConvert(Class type) {
    return type.equals(Data.class);
  }

  //
  // Methods
  //

  private Serializable convertStringToType(String stringValue, DataType type) {
    Serializable value = null;

    switch(type) {
    case TEXT:
      value = stringValue;
      break;
    case INTEGER:
      value = Integer.parseInt(stringValue);
      break;
    case DECIMAL:
      value = Double.parseDouble(stringValue);
      break;
    }

    return value;
  }
}