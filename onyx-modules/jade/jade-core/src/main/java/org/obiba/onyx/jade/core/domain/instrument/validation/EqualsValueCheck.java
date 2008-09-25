package org.obiba.onyx.jade.core.domain.instrument.validation;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;
import org.obiba.onyx.jade.core.service.InstrumentRunService;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;
import org.obiba.onyx.util.data.DataType;

/**
 * Integrity check to verify that an instrument run value is equal to a given (fixed) value.
 * 
 * The check fails (returns <code>false</code>) if the values are <i>not</i> equal.
 * 
 * @author cag-dspathis
 * 
 */
@Entity
@DiscriminatorValue("EqualsValueCheck")
public class EqualsValueCheck extends AbstractIntegrityCheck implements IntegrityCheck {

  private static final long serialVersionUID = 1L;

  private Boolean booleanValue;

  private Long integerValue;

  private Double decimalValue;

  private String textValue;

  public EqualsValueCheck() {
    super();
  }

  public DataType getValueType() {
    return getTargetParameter().getDataType();
  }

  public void setBooleanValue(boolean value) {
    booleanValue = Boolean.valueOf(value);
  }

  public void setIntegerValue(long value) {
    integerValue = Long.valueOf(value);
  }

  public void setDecimalValue(double value) {
    decimalValue = Double.valueOf(value);
  }

  public void setTextValue(String value) {
    textValue = value;
  }

  public void setData(Data data) {
    if(data != null) {
      if(data.getType() == getValueType()) {

        switch(getValueType()) {
        case BOOLEAN:
          booleanValue = data.getValue();
          break;

        case INTEGER:
          integerValue = data.getValue();
          break;

        case DECIMAL:
          decimalValue = data.getValue();
          break;

        case TEXT:
          textValue = data.getValue();
          break;
        }
      } else {
        throw new IllegalArgumentException("DataType " + getValueType() + " expected, " + data.getType() + " received.");
      }
    }
  }

  public Data getData() {
    Data data = null;

    switch(getValueType()) {
    case BOOLEAN:
      data = DataBuilder.buildBoolean(booleanValue);
      break;

    case INTEGER:
      data = DataBuilder.buildInteger(integerValue);
      break;

    case DECIMAL:
      data = DataBuilder.buildDecimal(decimalValue);
      break;

    case TEXT:
      data = DataBuilder.buildText(textValue);
      break;
    }

    return data;
  }

  //
  // IntegrityCheck Methods
  //

  /**
   * Returns <code>true</code> if the specified instrument run value is equal to the configured value.
   * 
   * @param runValue instrument run value
   * @param runService instrument run service (not used by this check)
   * @return <code>true</code> if instrument run value equals configured value
   */
  public boolean checkParameterValue(InstrumentRunValue runValue, InstrumentRunService runService) {

    boolean isEqual = false;

    if(runValue != null && runValue.getData() != null) {
      isEqual = runValue.getData().equals(getData());
    } else {
      isEqual = (getData() == null);
    }

    return isEqual;
  }
}