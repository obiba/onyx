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

import java.util.List;

import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.jade.core.domain.run.InstrumentRun;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;
import org.obiba.onyx.jade.core.domain.run.Measure;
import org.obiba.onyx.jade.core.domain.run.MeasureStatus;

public interface InstrumentRunService {

  /**
   * Get the instrument whatever is its status for participant and instrument type name.
   * @param participant
   * @param instrumentTypeName
   * @return
   */
  public InstrumentRun getInstrumentRun(Participant participant, String instrumentTypeName);

  /**
   * Get the List of {@list InstrumentRun}s that match the provided InstrumentRun template.
   * @param instrumentRun The InstrumentRun template to match. Providing null will return all InstrumentRuns.
   * @return A list of matching InstrumentRun values.
   */
  public List<InstrumentRun> getInstrumentRuns(InstrumentRun instrumentRun);

  /**
   * Find the value from the run of the instrument of the given type for given participant.
   * @param participant
   * @param instrumentTypeName
   * @param parameterCode
   * @param measurePosition null if not a repeatable measure or if parameter is not a output
   * @return
   */
  public InstrumentRunValue getInstrumentRunValue(Participant participant, String instrumentTypeName, String parameterCode, Integer measurePosition);

  /**
   * Delete the {@link InstrumentRun} associated with the participant and instrument type name. The members of
   * InstrumentRun ({@link Measure} and {@link InstrumentRunValue}) will also be deleted.
   * @param participant
   * @param instrumentTypeName
   */
  public void deleteInstrumentRun(Participant participant, String instrumentTypeName);

  /**
   * Deletes the InstrumentRuns of a specific Participant.
   * 
   * @param participant The participant
   */
  public void deleteAllInstrumentRuns(Participant participant);

  /**
   * Updates the status of the selected measure.
   * 
   * @param measure The measure to update.
   * @param measure The status.
   */
  public void updateMeasureStatus(Measure measure, MeasureStatus status);

  /**
   * Get the count of {@list InstrumentRun}s that match the provided InstrumentRun template.
   * @param instrumentRun The InstrumentRun template to match.
   * @return The InstrumentRun count.
   */
  public int countInstrumentRuns(InstrumentRun instrumentRun);

}
