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
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.onyx.jade.core.domain.run.InstrumentRun;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunStatus;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;

public interface InstrumentRunService {

  /**
   * Get the last instrument whatever is its status for participant and instrument type.
   * @param participant
   * @param instrumentType
   * @return
   */
  public InstrumentRun getLastInstrumentRun(Participant participant, InstrumentType instrumentType);

  /**
   * Get the last instrument whatever is its status for participant and instrument type name.
   * @param participant
   * @param instrumentTypeName
   * @return
   */
  public InstrumentRun getLastInstrumentRun(Participant participant, String instrumentTypeName);

  /**
   * Get the last completed run for participant and instrument type.
   * @param participant
   * @param instrumentType
   * @return
   */
  public InstrumentRun getLastCompletedInstrumentRun(Participant participant, InstrumentType instrumentType);

  /**
   * Set the end date, and make sure it is not done twice.
   * @param run
   */
  public void end(InstrumentRun run);

  /**
   * Find the value from the last completed run of the instrument of the given type for given participant.
   * @param participant
   * @param instrumentType
   * @param parameterCode
   * @return
   */
  public InstrumentRunValue findInstrumentRunValue(Participant participant, InstrumentType instrumentType, String parameterCode);

  /**
   * Update the {@link InstrumentRun} with the given {@link InstrumentRunStatus}.
   * @param run
   * @param status
   */
  public void setInstrumentRunStatus(InstrumentRun run, InstrumentRunStatus status);
}
