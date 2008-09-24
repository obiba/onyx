package org.obiba.onyx.jade.core.domain.instrument.validation;

import java.util.Date;

import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;
import org.obiba.onyx.util.data.DataType;

/**
 * Integrity check to verify that an instrument run value is equal
 * to a given (fixed) value.
 * 
 * The check fails (returns <code>false</code>) if the values are <i>not</i>
 * equal.
 * 
 * @author cag-dspathis
 *
 */
public class EqualsValueCheck implements IntegrityCheck {

  private DataType type;
  
  private Boolean booleanValue;

  private Date dateValue;

  private Double decimalValue;

  private Long integerValue;

  private String textValue;

  private byte[] dataValue;
  
  public EqualsValueCheck() {
    super(); 
  }
  
  public void setDataType(DataType type) {
    this.type = type;
  }
  
  public DataType getDataType() {
    return type;
  }
  
  public void setData(Data data) {
    if(data != null) {
      if(data.getType() == getDataType()) {

        switch(getDataType()) {
        case BOOLEAN:
          booleanValue = data.getValue();
          break;

        case DATE:
          dateValue = data.getValue();
          break;

        case DECIMAL:
          decimalValue = data.getValue();
          break;

        case INTEGER:
          integerValue = data.getValue();
          break;

        case TEXT:
          textValue = data.getValue();
          break;

        case DATA:
          dataValue = data.getValue();
          break;
        }
      } else {
        throw new IllegalArgumentException("DataType " + getDataType() + " expected, " + data.getType() + " received.");
      }
    }
  }
 
  public Data getData() {
    Data data = null;

    switch(getDataType()) {
    case BOOLEAN:
      data = DataBuilder.buildBoolean(booleanValue);
      break;

    case DATE:
      data = DataBuilder.buildDate(dateValue);
      break;

    case DECIMAL:
      data = DataBuilder.buildDecimal(decimalValue);
      break;

    case INTEGER:
      data = DataBuilder.buildInteger(integerValue);
      break;

    case TEXT:
      data = DataBuilder.buildText(textValue);
      break;

    case DATA:
      data = new Data(getDataType(), dataValue);
      break;
    }

    return data;
  }
  
  //
  // IntegrityCheck Methods
  //
  
  /**
   * Returns <code>true</code> if the specified instrument run value
   * is equal to the configured value.
   * 
   * @param value instrument run value
   * @return <code>true</code> if instrument run value equals configured value
   */
  public boolean checkParameterValue(InstrumentRunValue value) {
        
    boolean isEqual = false;
    
    if (value != null && value.getData() != null) {
      isEqual = value.getData().equals(getData());
    }
    else {
      isEqual = (getData() == null);
    }
    
    return isEqual;
  }
}