package org.obiba.onyx.jade.core.domain.instrument.validation;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.obiba.onyx.jade.core.service.ActiveInstrumentRunService;
import org.obiba.onyx.jade.core.service.InstrumentRunService;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataType;

@Entity
@DiscriminatorValue("RangeCheck")
public class RangeCheck extends AbstractIntegrityCheck implements IntegrityCheck {

  private static final long serialVersionUID = 1L;

  private Long integerMinValue;

  private Long integerMaxValue;

  private Double decimalMinValue;

  private Double decimalMaxValue;

  public RangeCheck() {
    super();
  }

  public DataType getValueType() {
    return getTargetParameter().getDataType();
  }

  public void setIntegerMinValue(Long value) {
    integerMinValue = value;
  }

  public void setIntegerMaxValue(Long value) {
    integerMaxValue = value;
  }

  public void setDecimalMinValue(Double value) {
    decimalMinValue = value;
  }

  public void setDecimalMaxValue(Double value) {
    decimalMaxValue = value;
  }

  //
  // IntegrityCheck Methods
  //

  @Override
  public boolean checkParameterValue(Data paramData, InstrumentRunService runService, ActiveInstrumentRunService activeRunService) {
    if(getValueType().equals(DataType.INTEGER)) {
      return checkIntegerParameterValue(paramData);
    } else if(getValueType().equals(DataType.DECIMAL)) {
      return checkDecimalParameterValue(paramData);
    }

    return false;
  }

  private boolean checkIntegerParameterValue(Data paramData) {
    boolean withinRange = true;

    if(integerMinValue != null) {
      if(integerMinValue.compareTo((Long) paramData.getValue()) > 0) {
        withinRange = false;
      }
    }

    if(withinRange) {
      if(integerMaxValue != null) {
        if(integerMaxValue.compareTo((Long) paramData.getValue()) < 0) {
          withinRange = false;
        }
      }
    }

    return withinRange;
  }

  private boolean checkDecimalParameterValue(Data paramData) {
    boolean withinRange = true;

    if(decimalMinValue != null) {
      if(decimalMinValue.compareTo((Double) paramData.getValue()) > 0) {
        withinRange = false;
      }
    }

    if(withinRange) {
      if(decimalMaxValue != null) {
        if(decimalMaxValue.compareTo((Double) paramData.getValue()) < 0) {
          withinRange = false;
        }
      }
    }

    return withinRange;
  }
}