package org.obiba.onyx.jade.core.domain.instrument.validation;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameter;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;
import org.obiba.onyx.jade.core.service.InstrumentRunService;
import org.obiba.onyx.util.data.DataType;

@Entity
@DiscriminatorValue("ParameterSpreadCheck")
public class ParameterSpreadCheck extends AbstractIntegrityCheck implements IntegrityCheck {

  private static final long serialVersionUID = 1L;

  @Transient
  private RangeCheck rangeCheck;

  @ManyToOne
  private InstrumentParameter parameter;
  
  private Integer percent;
  
  public ParameterSpreadCheck() {
    rangeCheck = new RangeCheck();
  }

  public void setParameter(InstrumentParameter param) {
    this.parameter = param;
  }

  public InstrumentParameter getParameter() {
    return parameter;
  }
  
  public DataType getValueType() {
    return getTargetParameter().getDataType();
  }
  
  public void setPercent(Integer percent) {
    this.percent = percent;
  }
  
  public Integer getPercent() {
    return percent;
  }
  
  //
  // IntegrityCheck Methods
  //
  
  @Override
  public boolean checkParameterValue(InstrumentRunValue runValue, InstrumentRunService runService) {
    // Get the other parameter's value.
    InstrumentRunValue paramValue = runService.findInstrumentRunValue(runValue.getInstrumentRun().getParticipantInterview(), runValue.getInstrumentRun().getInstrument().getInstrumentType(), parameter.getName());

    // Update the rangeCheck accordingly.
    rangeCheck.setTargetParameter(getTargetParameter());
    
    if (getValueType().equals(DataType.INTEGER)) {
      initIntegerRangeCheck(runValue, paramValue);
    }
    else if (getValueType().equals(DataType.DECIMAL)){
      initDecimalRangeCheck(runValue, paramValue);
    }
    else {
      return false;
    }

    return rangeCheck.checkParameterValue(runValue, null);
  }
  
  private void initIntegerRangeCheck(InstrumentRunValue checkedRunValue, InstrumentRunValue otherRunValue) {
    Long otherValue = otherRunValue.getData().getValue();
    
    Long minCheckedValue = Double.valueOf(Math.ceil((1.0 - percent)*otherValue.longValue())).longValue(); 
    Long maxCheckedValue = Double.valueOf(Math.floor((1.0 + percent)*otherValue.longValue())).longValue();
    
    rangeCheck.setIntegerMinValue(minCheckedValue);
    rangeCheck.setIntegerMaxValue(maxCheckedValue);
  }
  
  private void initDecimalRangeCheck(InstrumentRunValue checkedRunValue, InstrumentRunValue otherRunValue) {
    Double otherValue = otherRunValue.getData().getValue();
    
    Double minCheckedValue = (1.0 - percent)*otherValue; 
    Double maxCheckedValue = (1.0 + percent)*otherValue;
    
    rangeCheck.setDecimalMinValue(minCheckedValue);
    rangeCheck.setDecimalMaxValue(maxCheckedValue);
  }
}