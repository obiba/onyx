package org.obiba.onyx.jade.core.domain.instrument.validation;

import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;
import org.obiba.onyx.util.data.Data;

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

  private Data value;
  
  public EqualsValueCheck() {
    super(); 
  }
  
  public void setValue(Data value) {
    this.value = value;  
  }
  
  public Data getValue() {
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
    
    if (value != null && value.getData() != null) {
      isEqual = value.getData().equals(getValue());
    }
    else {
      isEqual = (getValue() == null);
    }
    
    return isEqual;
  }
}