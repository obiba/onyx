/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.core.service;

import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.jade.core.domain.run.InstrumentRun;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;

public interface InstrumentRunService {

  /**
   * Get the instrument whatever is its status for participant and instrument type name.
   * @param participant
   * @param instrumentTypeName
   * @return
   */
  public InstrumentRun getInstrumentRun(Participant participant, String instrumentTypeName);

  /**
   * Find the value from the run of the instrument of the given type for given participant.
   * @param participant
   * @param instrumentTypeName
   * @param parameterCode
   * @param measurePosition null if not a repeatable measure or if parameter is not a output
   * @return
   */
  public InstrumentRunValue getInstrumentRunValue(Participant participant, String instrumentTypeName, String parameterCode, Integer measurePosition);

}
