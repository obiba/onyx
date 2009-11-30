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

import org.obiba.magma.Value;
import org.obiba.magma.ValueSequence;
import org.obiba.magma.ValueType;
import org.obiba.magma.type.BinaryType;
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
    String dataTypeName = data.getType().name();
    if(dataTypeName.equalsIgnoreCase(DataType.DATA.name())) dataTypeName = BinaryType.get().getName();

    return ValueType.Factory.newValue(ValueType.Factory.forName(dataTypeName), (Serializable) data.getValue());
  }

  /**
   * Convert from a (Magma) {@link Value} class to an (Onyx) {@link Data} class.
   * @param value Magma Value
   * @return Onyx Data
   * @throws UnsupportedOperationException thrown when an attempt is made to convert a {@link ValueSequence} object to a
   * Data object
   */
  public static Data valueToData(Value value) {
    if(value.isSequence()) throw new UnsupportedOperationException("Cannot convert a ValueSequence object to a Data object.");

    String dataTypeName = value.getValueType().getName();
    if(dataTypeName.equalsIgnoreCase(BinaryType.get().getName())) dataTypeName = DataType.DATA.name();

    return new Data(DataType.valueOf(dataTypeName.toUpperCase()), (Serializable) value.getValue());
  }
}
