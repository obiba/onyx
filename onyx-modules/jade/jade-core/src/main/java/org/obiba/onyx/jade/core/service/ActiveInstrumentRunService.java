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
import java.util.Map;

import org.obiba.onyx.core.data.ComputingDataSource;
import org.obiba.onyx.core.domain.contraindication.IContraindicatable;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.jade.core.domain.instrument.Instrument;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentOutputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.onyx.jade.core.domain.instrument.validation.IntegrityCheck;
import org.obiba.onyx.jade.core.domain.run.InstrumentRun;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunStatus;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;
import org.obiba.onyx.jade.core.domain.run.Measure;
import org.obiba.onyx.util.data.Data;

public interface ActiveInstrumentRunService extends IContraindicatable {

  /**
   * Create the current {@link InstrumentRun}
   * @param participant
   * @param instrument
   * @return
   */
  public InstrumentRun start(Participant participant, Instrument instrument);

  /**
   * Set the end date to the current {@link InstrumentRun} with its current status.
   */
  public void end();

  /**
   * Get the associated {@link Participant}
   * @return
   */
  public Participant getParticipant();

  /**
   * Get the current {@link InstrumentType}.
   * @return
   */
  public InstrumentType getInstrumentType();

  /**
   * Set the instrument used for performing the measure.
   * @param instrument
   */
  public void setInstrument(Instrument instrument);

  // used by public class OutputParametersStep extends WizardStepPanel {
  public List<InstrumentOutputParameter> getParametersWithWarning();

  /**
   * Sets the current instrument run (ONYX-181).
   * 
   * @param instrumentRun current instrument run
   */
  public void setInstrumentRun(InstrumentRun instrumentRun);

  /**
   * Get the current {@link InstrumentRun}.
   * @return
   */
  public InstrumentRun getInstrumentRun();

  /**
   * Set the {@link InstrumentRunStatus} to the current {@link InstrumentRun}.
   * @param status
   */
  public void setInstrumentRunStatus(InstrumentRunStatus status);

  /**
   * Persist {@link InstrumentRunValue} current {@link InstrumentRun}.
   * @param currentRunValue
   */
  public void update(InstrumentRunValue value);

  /**
   * Compute the values of parameters with a {@link ComputingDataSource} for the current {@link InstrumentRun}.
   */
  public void computeOutputParameters();

  /**
   * Get the {@code InstrumentRunValue} for the named {@code InstrumentParameter} of the current {@code InstrumentRun}.
   * @param code
   * @return null if current instrument run is null
   * @throws IllegalArgumentException if parameter name is not applicable to the {@code Instrument}
   */
  public InstrumentRunValue getInstrumentRunValue(String code);

  /**
   * Get the {@code InstrumentRunValue} for the named {@code InstrumentParameter} of the current {@code InstrumentRun}.
   * @param code
   * @return empty list if none
   * @throws IllegalArgumentException if parameter name is not applicable to the {@code Instrument}
   */
  public List<InstrumentRunValue> getInstrumentRunValues(String code);

  /**
   * Get or create the {@code InstrumentRunValue} for the named {@code InstrumentParameter} of the current {@code
   * InstrumentRun}.
   * @param code
   * @return null if current instrument run is null
   * @throws IllegalArgumentException if parameter name is not applicable to the {@code Instrument}
   */
  public InstrumentRunValue getOrCreateInstrumentRunValue(String code);

  /**
   * Get (or create it if needed) the {@code InstrumentRunValue} for the specified {@code InstrumentParameter} of the
   * current {@code InstrumentRun}.
   * 
   * @param parameter the {@code InstrumentParameter} for which to obtain the value of the current run
   * @return the {@code InstrumentRunValue}
   */
  public InstrumentRunValue getOrCreateInstrumentRunValue(InstrumentParameter parameter);

  /**
   * Sets the InstrumentRunValue for input parameters list (used for readonly input parameters)
   */
  public String updateReadOnlyInputParameterRunValue();

  /**
   * Persists repeatable instrument parameters.
   * @param repeatableData A map of parameter names to {@link Data} values.
   */
  public Measure addMeasure(Map<String, Data> repeatableData);

  /**
   * Get the count of measures (repeatable or not).
   * @return
   */
  public int getCurrentMeasureCount();

  /**
   * Persists both repeatable and non repeatable instrument output parameters.
   * @param values A map of parameter names to {@link Data} values.
   */
  public void addOutputParameterValues(Map<String, Data> values);

  /**
   * Persists and marks as manually captured both repeatable and non repeatable instrument output parameters.
   * @param values A map of parameter names to {@link Data} values.
   */
  public void addManuallyCapturedOutputParameterValues(Map<String, Data> values);

  /**
   * Deletes the selected measure from the current InstrumentRun.
   * 
   * @param measure The measure to delete.
   */
  public void deleteMeasure(Measure measure);

  /**
   * Deletes the provided {@link InstrumentRunValue} from the current {@link InstrumentRun}.
   * @param instrumentRunValue
   */
  public void deleteInstrumentRunValue(InstrumentRunValue instrumentRunValue);

  /**
   * 
   * Updates the skip comment for current instrumentRun
   * 
   * @param comment
   */
  public void setSkipRemainingMeasuresCommentFromInstrumentRun(String comment);

  /**
   * Removes the skip comment for current instrumentRun
   */
  public void removeSkipRemainingMeasuresCommentFromInstrumentRun();

  /**
   * Removes measures marked as invalid from current instrumentRun
   */
  public void removeInvalidMeasuresFromInstrumentRun();

  /**
   * For each output parameter, performs all integrity checks of type <code>ERROR</code>.
   * 
   * @param outputParams output parameters
   * @return list of integrity checks (description) that failed (empty list if none)
   */
  public Map<IntegrityCheck, InstrumentOutputParameter> checkIntegrity(List<InstrumentOutputParameter> outputParams);

}
