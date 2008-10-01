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

/**
 * Integrity check to verify that an instrument run value is equal to the value of another parameter.
 * 
 * The check fails (returns <code>false</code>) if the values are <i>not</i> equal.
 * 
 * @author cag-dspathis
 * 
 */
@Entity
@DiscriminatorValue("EqualsParameterCheck")
public class EqualsParameterCheck extends AbstractIntegrityCheck implements IntegrityCheck {

  private static final long serialVersionUID = 1L;

  @Transient
  private EqualsValueCheck equalsValueCheck;

  @ManyToOne
  private InstrumentParameter parameter;

  public EqualsParameterCheck() {
    equalsValueCheck = new EqualsValueCheck();
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
   * Returns <code>true</code> if the specified instrument run value is equal to the value of the configured other
   * parameter.
   * 
   * @param runValue instrument run value
   * @param runService instrument run service
   * @return <code>true</code> if instrument run value equals value of configured other parameter
   */
  public boolean checkParameterValue(Data paramData, InstrumentRunService runService, ActiveInstrumentRunService activeRunService) {
    // If the other parameter has not been specified, there is nothing to check!
    if (parameter == null) {
      return true;
    }
    
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

    // Update the equalsValueCheck accordingly.
    equalsValueCheck.setTargetParameter(getTargetParameter());
    equalsValueCheck.setData(otherData);

    return equalsValueCheck.checkParameterValue(paramData, null, null);
  }
  
  protected Object[] getDescriptionArgs(ActiveInstrumentRunService activeRunService) {
    // Set the parameter's context and user session service to ensure
    // proper localization.
    parameter.setApplicationContext(context);
    parameter.setUserSessionService(userSessionService);
    
    return new Object[] { getTargetParameter().getDescription(), parameter.getDescription() };
  }
}