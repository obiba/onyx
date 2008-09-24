package org.obiba.onyx.jade.core.domain.instrument.validation;

import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;

/**
 * Interface for instrument parameter integrity checks.
 * 
 * @author cag-dspathis
 *
 */
public interface IntegrityCheck {
  /**
   * Checks the specified instrument run value.
   * 
   * The nature of the "check" is specific to implementing classes.
   *  
   * @param value the instrument run value to check
   * @return <code>true</code> if the value passes the check
   */
  public boolean checkParameterValue(InstrumentRunValue value);
}
