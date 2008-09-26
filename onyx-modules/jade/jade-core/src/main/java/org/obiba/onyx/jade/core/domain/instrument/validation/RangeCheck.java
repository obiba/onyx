package org.obiba.onyx.jade.core.domain.instrument.validation;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;
import org.obiba.onyx.jade.core.service.InstrumentRunService;
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
  public boolean checkParameterValue(InstrumentRunValue runValue, InstrumentRunService runService) {
    if (getValueType().equals(DataType.INTEGER)) {
      return checkIntegerParameterValue(runValue); 
    }
    else if (getValueType().equals(DataType.DECIMAL)) {
      return checkDecimalParameterValue(runValue);
    }
    
    return false;
  }
  
  private boolean checkIntegerParameterValue(InstrumentRunValue runValue) {
    boolean withinRange = true;
    
    if (integerMinValue != null) {
      if (integerMinValue.compareTo((Long)runValue.getData().getValue()) > 0) {
        withinRange = false;
      }
    }
    
    if (withinRange) {
      if (integerMaxValue != null) {
        if (integerMaxValue.compareTo((Long)runValue.getData().getValue()) < 0) {
          withinRange = false;
        }  
      }
    }
    
    return withinRange;
  }
  
  private boolean checkDecimalParameterValue(InstrumentRunValue runValue) {
    boolean withinRange = true;
    
    if (decimalMinValue != null) {
      if (decimalMinValue.compareTo((Double)runValue.getData().getValue()) > 0) {
        withinRange = false;
      }      
    }
        
    if (withinRange) {
      if (decimalMaxValue != null) {
        if (decimalMaxValue.compareTo((Double)runValue.getData().getValue()) < 0) {
          withinRange = false;
        }  
      }
    }
    
    return withinRange;   
  }
}
