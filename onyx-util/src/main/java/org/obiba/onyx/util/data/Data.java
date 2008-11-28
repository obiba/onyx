/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.util.data;

import java.io.Serializable;
import java.util.Date;

public class Data implements Serializable, Comparable {

  private static final long serialVersionUID = -2470483891384378865L;

  private Serializable value;

  private DataType type;

  public Data(DataType type) {
    this.type = type;
  }

  public Data(DataType type, Serializable value) {
    this.type = type;
    setValue(value);
  }

  public DataType getType() {
    return type;
  }

  @SuppressWarnings("unchecked")
  public <T> T getValue() {
    return (T) value;
  }

  public void setValue(Serializable value) {
    if(value != null) {
      Class<?> valueClass = value.getClass();

      switch(type) {
      case BOOLEAN:
        if(!valueClass.isAssignableFrom(Boolean.class)) throw new IllegalArgumentException("DataType " + type + " expected, " + valueClass.getName() + " received.");
        break;

      case DATE:
        if(!(value instanceof Date)) throw new IllegalArgumentException("DataType " + type + " expected, " + valueClass.getName() + " received.");
        break;

      case DECIMAL:
        if(!valueClass.isAssignableFrom(Double.class) && !valueClass.isAssignableFrom(Float.class)) throw new IllegalArgumentException("DataType " + type + " expected, " + valueClass.getName() + " received.");
        break;

      case INTEGER:
        if(!valueClass.isAssignableFrom(Long.class) && !valueClass.isAssignableFrom(Integer.class)) throw new IllegalArgumentException("DataType " + type + " expected, " + valueClass.getName() + " received.");
        break;

      case TEXT:
        if(!valueClass.isAssignableFrom(String.class)) throw new IllegalArgumentException("DataType " + type + " expected, " + valueClass.getName() + " received.");
        break;

      case DATA:
        if(!valueClass.isAssignableFrom(byte[].class)) throw new IllegalArgumentException("DataType " + type + " expected, " + valueClass.getName() + " received.");
        break;

      default:
        break;
      }
    }
    this.value = value;
  }

  public String getValueAsString() {
    if(type != DataType.DATA) {
      return value != null ? value.toString() : null;
    }
    // TODO determine how to output a meaningful value for byte arrays
    return "";
  }

  @Override
  public String toString() {
    return "[" + type + ":" + value + "]";
  }

  @Override
  public int hashCode() {
    int hashCode = 0;

    if(value != null) {
      if(type != DataType.DATA) {
        hashCode = value.hashCode();
      } else {
        byte[] bytes = (byte[]) value;
        int byteCount = bytes.length;
        hashCode = byteCount != 0 ? (byteCount + bytes[0] + bytes[byteCount - 1]) % 17 : Integer.MAX_VALUE;
      }
    }

    return hashCode;
  }

  @Override
  public boolean equals(Object otherObj) {
    boolean isEqual = false;

    if(otherObj instanceof Data) {
      Data otherData = (Data) otherObj;

      if(otherData.getType().equals(type)) {
        if(value != null) {
          switch(type) {
          case BOOLEAN:
          case INTEGER:
          case DECIMAL:
          case DATE:
          case TEXT:
            isEqual = value.equals(otherData.getValue());
            break;
          case DATA:
            isEqual = byteArraysEqual((byte[]) value, (byte[]) otherData.getValue());
            break;
          }
        } else {
          isEqual = (otherData.getValue() == null);
        }
      }
    }

    return isEqual;
  }

  private boolean byteArraysEqual(byte[] firstArray, byte[] secondArray) {
    boolean arraysEqual = false;

    if(firstArray != null && secondArray != null) {
      if(firstArray.length == secondArray.length) {
        boolean mismatch = false;

        for(int i = 0; i < firstArray.length; i++) {
          if(firstArray[i] != secondArray[i]) {
            mismatch = true;
            break;
          }
        }

        arraysEqual = !mismatch;
      }
    } else {
      arraysEqual = (firstArray == null && secondArray == null);
    }

    return arraysEqual;
  }

  public int compareTo(Object object) {
    int result = 0;

    if(object instanceof Data) {
      Data data = (Data) object;

      if(data.getType().equals(type)) {
        if(value != null) {
          switch(type) {
          case BOOLEAN:
          case DATE:
          case TEXT:
            result = ((Comparable) value).compareTo(data.getValue());
            break;
          case INTEGER: {
            // compare two Integers or two Longs, can't mix them
            Number numberValue = (Number) value;
            Number dataValue = data.getValue();
            result = Long.valueOf(numberValue.longValue()).compareTo(Long.valueOf(dataValue.longValue()));
          }
            break;
          case DECIMAL: {
            // compare two Floats or two Doubles, can't mix them
            Number numberValue = (Number) value;
            Number dataValue = data.getValue();
            result = Double.valueOf(numberValue.doubleValue()).compareTo(Double.valueOf(dataValue.doubleValue()));
          }
            break;
          case DATA:
            if(byteArraysEqual((byte[]) value, (byte[]) data.getValue())) {
              result = 0;
            } else {
              result = 1;
            }
            break;
          }
        } else {
          result = (data.getValue() == null) ? 0 : 1;
        }
      }
    }

    return result;
  }
}
