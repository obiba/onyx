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
    return (T)value;
  }

  public void setValue(Serializable value) {
    if(value != null) {
      
      switch(type) {
      case BOOLEAN:
        if(!value.getClass().isAssignableFrom(Boolean.class)) throw new IllegalArgumentException("DataType " + type + " expected, " + value.getClass().getName() + " received.");
        break;
        
      case DATE:
        if(!value.getClass().isAssignableFrom(Date.class)) throw new IllegalArgumentException("DataType " + type + " expected, " + value.getClass().getName() + " received.");
        break;
        
      case DECIMAL:
        if(!value.getClass().isAssignableFrom(Double.class) || !value.getClass().isAssignableFrom(Float.class)) throw new IllegalArgumentException("DataType " + type + " expected, " + value.getClass().getName() + " received.");
        
        break;
        
      case INTEGER:
        if(!value.getClass().isAssignableFrom(Long.class) || !value.getClass().isAssignableFrom(Integer.class)) throw new IllegalArgumentException("DataType " + type + " expected, " + value.getClass().getName() + " received.");
        break;
        
      case TEXT:
        if(!value.getClass().isAssignableFrom(String.class)) throw new IllegalArgumentException("DataType " + type + " expected, " + value.getClass().getName() + " received.");
        break;
        
      case DATA:
        if(!value.getClass().isAssignableFrom(byte[].class)) throw new IllegalArgumentException("DataType " + type + " expected, " + value.getClass().getName() + " received.");
        break;

      default:
        break;
      }
    }
    this.value = value;
  }

}
