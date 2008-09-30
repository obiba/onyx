package org.obiba.onyx.jade.core.domain.instrument.validation;

import java.util.Map;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.obiba.onyx.jade.core.domain.instrument.InstrumentInputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentOutputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameter;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;
import org.obiba.onyx.jade.core.service.ActiveInstrumentRunService;
import org.obiba.onyx.jade.core.service.InstrumentRunService;
import org.obiba.onyx.util.data.Data;
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

  private Integer offset;
  
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

  public void setOffset(Integer offset) {
    this.offset = offset;
  }

  public Integer getOffset() {
    return offset;
  }
  
  //
  // IntegrityCheck Methods
  //

  @Override
  public boolean checkParameterValue(Data paramData, InstrumentRunService runService, ActiveInstrumentRunService activeRunService) {
    if (parameter != null && (percent != null || offset != null)) {
      //
      // Get the other parameter's value.
      //
      InstrumentRunValue otherRunValue = null;
      Data otherData = null;
      
      if (parameter instanceof InstrumentInputParameter) {
        otherRunValue = activeRunService.getInputInstrumentRunValue(parameter.getName());
      }
      else if (parameter instanceof InstrumentOutputParameter) {
        otherRunValue = activeRunService.getOutputInstrumentRunValue(parameter.getName());
      }
      
      if (otherRunValue != null)  {
        otherData = otherRunValue.getData();
      }
  
      // Update the rangeCheck accordingly.
      rangeCheck.setTargetParameter(getTargetParameter());
  
      if(getValueType().equals(DataType.INTEGER)) {
        initIntegerRangeCheck(paramData, otherData);
      } else if(getValueType().equals(DataType.DECIMAL)) {
        initDecimalRangeCheck(paramData, otherData);
      } else {
        return false;
      }

      return rangeCheck.checkParameterValue(paramData, null, activeRunService);
    }
    else {
      return true;
    }
  }
  
  @Override
  protected String getDescriptionKey() {
    String descriptionKey = super.getDescriptionKey();
    
    if (percent != null && offset == null) {
      descriptionKey += "_PercentOnly";
    }
    else if (percent == null && offset != null) {
      descriptionKey += "_OffsetOnly";
    }
    
    return descriptionKey;
  }
  
  protected Object[] getDescriptionArgs() {
    // Set the parameter's context and user session service to ensure
    // proper localization.
    parameter.setApplicationContext(context);
    parameter.setUserSessionService(userSessionService);
    
    if (percent != null && offset == null) {
      return new Object[] { getTargetParameter().getDescription(), parameter.getDescription(), percent };
    }
    else if (percent == null && offset != null) {
      return new Object[] { getTargetParameter().getDescription(), parameter.getDescription(), offset, getTargetParameter().getMeasurementUnit() };
    }
    else {
      return new Object[] { getTargetParameter().getDescription(), parameter.getDescription(), percent, offset, getTargetParameter().getMeasurementUnit() };
    }
  }
  
  private void initIntegerRangeCheck(Data checkedData, Data otherData) {
    Long otherValue = otherData.getValue();

    Long minCheckedValue = otherValue;
    Long maxCheckedValue = otherValue;
    
    if (percent != null) {
      double percentValue = percent / 100.0;

      minCheckedValue = new Double(Math.ceil((1.0 - percentValue) * otherValue.longValue())).longValue();
      maxCheckedValue = new Double(Math.floor((1.0 + percentValue) * otherValue.longValue())).longValue();
    }
    
    if (offset != null) {
      minCheckedValue = minCheckedValue - offset;
      maxCheckedValue = maxCheckedValue + offset; 
    }

    if (percent == null && offset == null) {
      minCheckedValue = null;
      maxCheckedValue = null;
    }
    
    rangeCheck.setIntegerMinValueMale(minCheckedValue);
    rangeCheck.setIntegerMaxValueMale(maxCheckedValue);
    rangeCheck.setIntegerMinValueFemale(minCheckedValue);
    rangeCheck.setIntegerMaxValueFemale(maxCheckedValue);
  }

  private void initDecimalRangeCheck(Data checkedData, Data otherData) {
    Double otherValue = otherData.getValue();

    Double minCheckedValue = otherValue.doubleValue();
    Double maxCheckedValue = otherValue.doubleValue();
    
    if (percent != null) {
      double percentValue = percent / 100.0;

      minCheckedValue = (1.0 - percentValue) * otherValue;
      maxCheckedValue = (1.0 + percentValue) * otherValue;
    }
    
    if (offset != null) {
      minCheckedValue = minCheckedValue - offset.longValue();
      maxCheckedValue = maxCheckedValue + offset.longValue(); 
    }
    
    if (percent == null && offset == null) {
      minCheckedValue = null;
      maxCheckedValue = null;
    }
    
    rangeCheck.setDecimalMinValueMale(minCheckedValue);
    rangeCheck.setDecimalMaxValueMale(maxCheckedValue);
    rangeCheck.setDecimalMinValueFemale(minCheckedValue);
    rangeCheck.setDecimalMaxValueFemale(maxCheckedValue);
  }
}