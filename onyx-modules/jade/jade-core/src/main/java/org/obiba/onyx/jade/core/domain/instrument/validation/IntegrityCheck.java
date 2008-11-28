/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.core.domain.instrument.validation;

import java.util.Map;

import org.obiba.onyx.core.service.UserSessionService;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameter;
import org.obiba.onyx.jade.core.service.ActiveInstrumentRunService;
import org.obiba.onyx.jade.core.service.InstrumentRunService;
import org.obiba.onyx.util.data.Data;
import org.springframework.context.ApplicationContext;

/**
 * Interface for instrument parameter integrity checks.
 * 
 * @author cag-dspathis
 * 
 */
public interface IntegrityCheck {

  /**
   * Returns the type of check.
   * 
   * @return check type
   */
  public IntegrityCheckType getType(); 

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
   * @param paramData the instrument run value to check
   * @param runService service used to query other parameter values if needed by the check (<code>null</code> if not
   * used)
   * @param activeRunService service used to query the active instrument run (<code>null</code> if not used)
   * @return <code>true</code> if the value passes the check
   */
  public boolean checkParameterValue(Data paramData, InstrumentRunService runService, ActiveInstrumentRunService activeRunService);
    
  /**
   * Returns a (localized) description of the check performed.
   * 
   * If a check's behaviour varies based on features of the measurement context, the 
   * check's description may also be different in different contexts. For example, a 
   * range check that specifies different ranges for male and female participants may
   * return one description when the current participant is male and another when the 
   * current participant is female. 
   * 
   * @param activeRunService service used to query the measurement context
   * @return localized description
   */
  public String getDescription(ActiveInstrumentRunService activeRunService);
  
  public void setApplicationContext(ApplicationContext context);
  
  public void setUserSessionService(UserSessionService userSessionService);
}
