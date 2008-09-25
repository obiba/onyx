package org.obiba.onyx.jade.core.domain.instrument.validation;

import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameter;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;
import org.obiba.onyx.jade.core.service.InstrumentRunService;

/**
 * Interface for instrument parameter integrity checks.
 * 
 * @author cag-dspathis
 * 
 */
public interface IntegrityCheck {

  /**
   * Sets the parameter to which this check is applied.
   * 
   * @param targetParameter parameter to which this check is applied
   */
  public void setTargetParameter(InstrumentParameter targetParameter);

  /**
   * Returns the parameter to which this check is applied.
   * 
   * @return parameter to which this check is applied
   */
  public InstrumentParameter getTargetParameter();

  /**
   * Checks the specified instrument run value.
   * 
   * The nature of the "check" is specific to implementing classes.
   * 
   * @param runValue the instrument run value to check
   * @param runService service used to query other parameter values if needed by the 
   *        check (<code>null</code> if not used)
   * @return <code>true</code> if the value passes the check
   */
  public boolean checkParameterValue(InstrumentRunValue runValue, InstrumentRunService runService);
}
