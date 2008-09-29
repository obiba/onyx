package org.obiba.onyx.jade.core.domain.instrument.validation;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import org.apache.wicket.Application;
import org.apache.wicket.spring.SpringWebApplication;
import org.obiba.onyx.core.service.UserSessionService;
import org.obiba.onyx.jade.core.service.ActiveInstrumentRunService;
import org.obiba.onyx.jade.core.service.InstrumentRunService;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;
import org.obiba.onyx.util.data.DataType;
import org.springframework.context.ApplicationContext;

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
      if(data.getType().equals(getValueType())) {

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
  public boolean checkParameterValue(Data paramData, InstrumentRunService runService, ActiveInstrumentRunService activeRunService) {

    boolean isEqual = false;

    if(paramData != null) {
      isEqual = paramData.equals(getData());
    } else {
      isEqual = (getData() == null);
    }

    return isEqual;
  }
  
  protected Object[] getDescriptionArgs() {
    return new Object[] { getTargetParameter().getDescription(), getData().getValue() };
  }
}