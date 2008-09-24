package org.obiba.onyx.jade.core.domain.instrument.validation;

import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;

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

  private Object value;
  
  public EqualsValueCheck() {
    super();  
  }
  
  public void setValue(Object value) {
    this.value = value;  
  }
  
  public Object getValue() {
    return this.value;
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
    
    if (value != null && value.getValue() != null) {
      isEqual = value.getValue().equals(getValue());
    }
    else {
      isEqual = (getValue() == null);
    }
    
    return isEqual;
  }
}