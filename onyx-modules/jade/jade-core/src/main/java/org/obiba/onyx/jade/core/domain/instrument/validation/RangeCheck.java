package org.obiba.onyx.jade.core.domain.instrument.validation;

import java.util.Map;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.obiba.onyx.core.domain.participant.Gender;
import org.obiba.onyx.jade.core.service.ActiveInstrumentRunService;
import org.obiba.onyx.jade.core.service.InstrumentRunService;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataType;

@Entity
@DiscriminatorValue("RangeCheck")
public class RangeCheck extends AbstractIntegrityCheck implements IntegrityCheck {

  private static final long serialVersionUID = 1L;

  private Long integerMinValueMale;

  private Long integerMaxValueMale;

  private Long integerMinValueFemale;
  
  private Long integerMaxValueFemale;
  
  private Double decimalMinValueMale;

  private Double decimalMaxValueMale;

  private Double decimalMinValueFemale;

  private Double decimalMaxValueFemale;
  
  public RangeCheck() {
    super();
  }

  public DataType getValueType() {
    return getTargetParameter().getDataType();
  }

  public void setIntegerMinValueMale(Long value) {
    integerMinValueMale = value;
  }

  public void setIntegerMaxValueMale(Long value) {
    integerMaxValueMale = value;
  }

  public void setIntegerMinValueFemale(Long value) {
    integerMinValueFemale = value;
  }

  public void setIntegerMaxValueFemale(Long value) {
    integerMaxValueFemale = value;
  }
  
  public void setDecimalMinValueMale(Double value) {
    decimalMinValueMale = value;
  }

  public void setDecimalMaxValueMale(Double value) {
    decimalMaxValueMale = value;
  }

  public void setDecimalMinValueFemale(Double value) {
    decimalMinValueFemale = value;
  }

  public void setDecimalMaxValueFemale(Double value) {
    decimalMaxValueFemale = value;
  }
  
  //
  // IntegrityCheck Methods
  //

  @Override
  public boolean checkParameterValue(Data paramData, InstrumentRunService runService, ActiveInstrumentRunService activeRunService) {
    // Get the participant's gender (range is gender-dependent).
    Gender gender = activeRunService.getParticipant().getGender(); 
    
    if(getValueType().equals(DataType.INTEGER)) {
      return checkIntegerParameterValue(paramData, gender);
    } else if(getValueType().equals(DataType.DECIMAL)) {
      return checkDecimalParameterValue(paramData, gender);
    }

    return false;
  }

  @Override
  public Map<String, String> getFeedbackVariables() {
    Map<String, String> variablesMap = super.getFeedbackVariables();
    
    if (getValueType().equals(DataType.INTEGER)) {
      variablesMap.put("minValueMale", (integerMinValueMale != null) ? integerMinValueMale.toString() : "0");
      variablesMap.put("maxValueMale", (integerMaxValueMale != null) ? integerMaxValueMale.toString() : "9999");
      variablesMap.put("minValueFemale", (integerMinValueFemale != null) ? integerMinValueFemale.toString() : "0");
      variablesMap.put("maxValueFemale", (integerMaxValueFemale != null) ? integerMaxValueFemale.toString() : "9999");
    }
    else if (getValueType().equals(DataType.DECIMAL)) {
      variablesMap.put("minValueMale", (decimalMinValueMale != null) ? decimalMinValueMale.toString() : "0.0");
      variablesMap.put("maxValueMale", (decimalMaxValueMale != null) ? decimalMaxValueMale.toString() : "9999.0");
      variablesMap.put("minValueFemale", (decimalMinValueFemale != null) ? decimalMinValueFemale.toString() : "0.0");
      variablesMap.put("maxValueFemale", (decimalMaxValueFemale != null) ? decimalMaxValueFemale.toString() : "9999.0");
    } 
    
    return variablesMap;
  }
  
  private boolean checkIntegerParameterValue(Data paramData, Gender gender) {
    boolean withinRange = true;

    Long minValue = gender.equals(Gender.MALE) ? integerMinValueMale : integerMinValueFemale;
    Long maxValue = gender.equals(Gender.MALE) ? integerMaxValueMale : integerMaxValueFemale;
    
    if(minValue != null) {
      if(minValue.compareTo((Long) paramData.getValue()) > 0) {
        withinRange = false;
      }
    }

    if(withinRange) {
      if(maxValue != null) {
        if(maxValue.compareTo((Long) paramData.getValue()) < 0) {
          withinRange = false;
        }
      }
    }

    return withinRange;
  }

  private boolean checkDecimalParameterValue(Data paramData, Gender gender) {
    boolean withinRange = true;

    Double minValue = gender.equals(Gender.MALE) ? decimalMinValueMale : decimalMinValueFemale;
    Double maxValue = gender.equals(Gender.MALE) ? decimalMaxValueMale : decimalMaxValueFemale;

    if(minValue != null) {
      if(minValue.compareTo((Double) paramData.getValue()) > 0) {
        withinRange = false;
      }
    }

    if(withinRange) {
      if(maxValue != null) {
        if(maxValue.compareTo((Double) paramData.getValue()) < 0) {
          withinRange = false;
        }
      }
    }

    return withinRange;
  }
}