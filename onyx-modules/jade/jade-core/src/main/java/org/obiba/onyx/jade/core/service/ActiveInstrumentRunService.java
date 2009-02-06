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

import org.obiba.onyx.core.domain.contraindication.IContraindicatable;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.jade.core.domain.instrument.Instrument;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentComputedOutputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentInputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentOutputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameterCaptureMethod;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.onyx.jade.core.domain.instrument.InterpretativeParameter;
import org.obiba.onyx.jade.core.domain.instrument.ParticipantInteractionType;
import org.obiba.onyx.jade.core.domain.run.InstrumentRun;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunStatus;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;

public interface ActiveInstrumentRunService extends IContraindicatable {

  /**
   * Create the current {@link InstrumentRun} without persisting it.
   * @param participant
   * @param instrumentType
   * @return
   */
  public InstrumentRun start(Participant participant, InstrumentType type);

  /**
   * Make sure there is no current {@link InstrumentRun}.
   */
  public void reset();

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

  public void setInstrument(Instrument instrument);

  public Instrument getInstrument();

  /**
   * Returns the instance of {@InstrumentParameter} for the specified {@code code} or null if none exist.
   * @param code
   * @return
   */
  public InstrumentParameter getParameterByCode(String code);

  /**
   * Returns the instance of {@InstrumentParameter} for the specified {@code vendorName} or null if none exist.
   * @param vendorName
   * @return
   */
  public InstrumentParameter getParameterByVendorName(String name);

  public boolean hasInterpretativeParameter(ParticipantInteractionType type);

  public List<InterpretativeParameter> getInterpretativeParameters(ParticipantInteractionType type);

  public boolean hasInterpretativeParameter();

  public List<InterpretativeParameter> getInterpretativeParameters();

  public boolean hasInputParameter(boolean readOnly);

  public List<InstrumentInputParameter> getInputParameters(boolean readOnly);

  public boolean hasInputParameter(InstrumentParameterCaptureMethod captureMethod);

  public List<InstrumentInputParameter> getInputParameters(InstrumentParameterCaptureMethod captureMethod);

  public boolean hasInputParameter();

  public List<InstrumentInputParameter> getInputParameters();

  public boolean hasOutputParameter(InstrumentParameterCaptureMethod captureMethod);

  public List<InstrumentOutputParameter> getOutputParameters(InstrumentParameterCaptureMethod captureMethod);

  public boolean hasOutputParameter(boolean automatic);

  public List<InstrumentOutputParameter> getOutputParameters(boolean automatic);

  public boolean hasOutputParameter();

  public List<InstrumentOutputParameter> getOutputParameters();

  public boolean hasParameterWithWarning();

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
   * Persist current {@link InstrumentRun}.
   * @param currentRun
   */
  public void persistRun();

  /**
   * Set the {@link InstrumentRunStatus} to the current {@link InstrumentRun}.
   * @param status
   */
  public void setInstrumentRunStatus(InstrumentRunStatus status);

  /**
   * Get the {@link InstrumentRunStatus} for the current {@link InstrumentRun}.
   * @return
   */
  public InstrumentRunStatus getInstrumentRunStatus();

  /**
   * Persist {@link InstrumentRunValue} current {@link InstrumentRun}.
   * @param currentRunValue
   */
  public void update(InstrumentRunValue value);

  /**
   * Compute the output parameters values of {@link InstrumentComputedOutputParameter} for the current
   * {@link InstrumentRun}.
   */
  public void computeOutputParameters();

  /**
   * Get (or create it if needed) the {@code InstrumentRunValue} for the named {@code InstrumentOutputParameter} of the
   * current {@code InstrumentRun}.
   * @param parameterCode
   * @return null if current instrument run is null
   * @throws IllegalArgumentException if parameter name is not applicable to the {@code Instrument}
   */
  public InstrumentRunValue getOutputInstrumentRunValue(String code);

  /**
   * Get (or create it if needed) the {@code InstrumentRunValue} for the {@code InstrumentOutputParameter} with the
   * specified {@code vendorName} attribute of the current {@code InstrumentRun}.
   * @param parameterVendorName the {@code vendorName} of the parameter to lookup
   * @return null if current instrument run is null
   * @throws IllegalArgumentException if parameter name is not applicable to the {@code Instrument}
   */
  public InstrumentRunValue getOutputInstrumentRunValueByVendorName(String name);

  /**
   * Get (or create it if needed) the {@code InstrumentRunValue} for the named {@code InstrumentInputParameter} of the
   * current {@code InstrumentRun}.
   * @param parameterCode
   * @return null if current instrument run is null
   * @throws IllegalArgumentException if parameter name is not applicable to the {@code Instrument}
   */
  public InstrumentRunValue getInputInstrumentRunValue(String code);

  /**
   * Get (or create it if needed) the {@code InstrumentRunValue} for the named {@code InstrumentInterpretativeParameter}
   * of the current {@code InstrumentRun}.
   * @param parameterCode
   * @return null if current instrument run is null
   * @throws IllegalArgumentException if parameter name is not applicable to the {@code Instrument}
   */
  public InstrumentRunValue getInterpretativeInstrumentRunValue(String code);

  /**
   * Get (or create it if needed) the {@code InstrumentRunValue} for the specified {@code InstrumentParameter} of the
   * current {@code InstrumentRun}.
   * 
   * @param parameter the {@code InstrumentParameter} for which to obtain the value of the current run
   * @return the {@code InstrumentRunValue}
   */
  public InstrumentRunValue getInstrumentRunValue(InstrumentParameter parameter);

  /**
   * Sets the InstrumentRunValue for input parameters list (used for readonly input parameters)
   * @param inputDataSourceVisitor
   * @param activeInterviewService
   * @param instrumentInputParameters
   * @return
   */
  public String updateReadOnlyInputParameterRunValue();
}
