package org.obiba.onyx.util.data;

import java.io.Serializable;
import java.util.Date;

public class Data implements Serializable {

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

}
