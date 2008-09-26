package org.obiba.onyx.jade.core.domain.instrument.validation;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameter;
import org.obiba.onyx.jade.core.domain.run.InstrumentRun;
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
    // Get the other parameter's value.
    InstrumentRun instrumentRun = activeRunService.getInstrumentRun();
    InstrumentRunValue paramValue = runService.findInstrumentRunValue(instrumentRun.getParticipantInterview(), instrumentRun.getInstrument().getInstrumentType(), parameter.getName());

    // Update the equalsValueCheck accordingly.
    equalsValueCheck.setTargetParameter(getTargetParameter());
    equalsValueCheck.setData(paramValue.getData());

    return equalsValueCheck.checkParameterValue(paramData, null, null);
  }
}