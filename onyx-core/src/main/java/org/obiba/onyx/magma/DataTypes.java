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

import org.obiba.magma.ValueType;
import org.obiba.magma.type.BinaryType;
import org.obiba.magma.type.BooleanType;
import org.obiba.magma.type.DateTimeType;
import org.obiba.magma.type.DecimalType;
import org.obiba.magma.type.IntegerType;
import org.obiba.magma.type.TextType;
import org.obiba.onyx.util.data.DataType;

public class DataTypes {

  public static ValueType valueTypeFor(DataType dataType) {
    if(dataType == null) throw new NullPointerException("dataType cannot be null.");
    switch(dataType) {
    case BOOLEAN:
      return BooleanType.get();
    case DATE:
      return DateTimeType.get();
    case DATA:
      return BinaryType.get();
    case DECIMAL:
      return DecimalType.get();
    case INTEGER:
      return IntegerType.get();
    case TEXT:
      return TextType.get();
    }
    throw new IllegalArgumentException("No ValueType for DataType '" + dataType + "'");
  }

}
