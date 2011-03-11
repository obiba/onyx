/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.magma;

import java.io.Serializable;

import org.obiba.magma.MagmaDate;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSequence;
import org.obiba.magma.ValueType;
import org.obiba.magma.type.BinaryType;
import org.obiba.magma.type.DateTimeType;
import org.obiba.magma.type.DateType;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataType;

/**
 * Static utility methods to provide conversion between the (Onyx) {@link Data} class and the (Magma) {@link Value}
 * class.
 */
public class DataValueConverter {

  /**
   * Convert from an (Onyx) {@link Data} class to a (Magma) {@link Value} class.
   * @param data Onyx Data
   * @return Magma Value
   */
  public static Value dataToValue(Data data) {

    ValueType valueType;
    switch(data.getType()) {
    case DATE:
      valueType = DateTimeType.get();
      break;
    case DATA:
      valueType = BinaryType.get();
      break;
    default:
      // Otherwise, the names are equivalent
      valueType = ValueType.Factory.forName(data.getType().name());
      break;
    }
    return valueType.valueOf(data.getValue());
  }

  /**
   * Convert from a (Magma) {@link Value} class to an (Onyx) {@link Data} class.
   * @param value Magma Value
   * @return Onyx Data
   * @throws UnsupportedOperationException thrown when an attempt is made to convert a {@link ValueSequence} object to a
   * Data object
   */
  public static Data valueToData(Value value) {
    if(value.isNull()) {
      return null;
    }

    if(value.isSequence()) {
      return new Data(DataType.TEXT, value.toString());
    }

    ValueType valueType = value.getValueType();
    String dataTypeName = valueType.getName();
    Serializable data = (Serializable) value.getValue();
    if(valueType == BinaryType.get()) {
      dataTypeName = DataType.DATA.name();
    } else if(valueType == DateTimeType.get()) {
      dataTypeName = DataType.DATE.name();
    } else if(valueType == DateType.get()) {
      dataTypeName = DataType.DATE.name();
      data = ((MagmaDate) value.getValue()).asDate();
    }

    return new Data(DataType.valueOf(dataTypeName.toUpperCase()), data);
  }
}
