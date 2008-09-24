package org.obiba.onyx.jade.core.domain.instrument.validation;

import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameter;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;
import org.obiba.onyx.jade.core.service.InstrumentRunService;

/**
 * Integrity check to verify that an instrument run value is equal
 * to the value of another parameter.
 * 
 * The check fails (returns <code>false</code>) if the values are <i>not</i>
 * equal.
 * 
 * @author cag-dspathis
 *
 */
public class EqualsParameterCheck implements IntegrityCheck {

  private InstrumentRunService instrumentRunService;
    
  private EqualsValueCheck equalsValueCheck;
  
  private InstrumentParameter parameter;
  
  public EqualsParameterCheck() {
    equalsValueCheck = new EqualsValueCheck();  
  }
  
  public void setInstrumentRunService(InstrumentRunService instrumentRunService) {
    this.instrumentRunService = instrumentRunService;
  }
  
  public void setParameter(InstrumentParameter param) {
    this.parameter = param;
  }
  
  public InstrumentParameter getParameter() {
    return this.parameter;
  }
  
  //
  // IntegrityCheck Methods
  //
  
  /**
   * Returns <code>true</code> if the specified instrument run value
   * is equal to the value of the configured other parameter.
   * 
   * @param value instrument run value
   * @return <code>true</code> if instrument run value equals value of 
   *         configured other parameter
   */
  public boolean checkParameterValue(InstrumentRunValue value) {
    // Get the other parameter's value.
    Object paramValue = instrumentRunService.findInstrumentRunValue(
      value.getInstrumentRun().getParticipantInterview(), 
      value.getInstrumentRun().getInstrument().getInstrumentType(), 
      parameter.getName()
    );
   
    // Update the equalsValueCheck accordingly.
    equalsValueCheck.setValue(paramValue);

    return equalsValueCheck.checkParameterValue(value);
  }
}