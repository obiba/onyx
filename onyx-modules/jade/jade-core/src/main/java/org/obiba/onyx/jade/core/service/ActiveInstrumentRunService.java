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

import org.obiba.onyx.core.domain.contraindication.Contraindication;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.jade.core.domain.instrument.Instrument;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentComputedOutputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.onyx.jade.core.domain.run.InstrumentRun;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunStatus;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;

public interface ActiveInstrumentRunService {

  /**
   * Returns true if the the underlying instrument has at least one contraindication of the specified type.
   * @param type
   * @return
   */
  public boolean hasContraindications(Contraindication.Type type);

  /**
   * Returns the selected contraindication or null if none is set.
   * @return
   */
  public Contraindication getContraindication();

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
   * Get the current {@link InstrumentType}.
   * @return
   */
  public InstrumentType getInstrumentType();

  public Instrument getInstrument();

  public void setInstrument(Instrument instrument);

  /**
   * Get the current {@link InstrumentRun}.
   * @return
   */
  public InstrumentRun getInstrumentRun();

  /**
   * Create the current {@link InstrumentRun} without persisting it.
   * @param participant
   * @param instrument
   * @return
   */
  public InstrumentRun start(Participant participant, InstrumentType instrument);

  /**
   * Get the associated {@link Participant}
   * @return
   */
  public Participant getParticipant();

  /**
   * Set the end date to the current {@link InstrumentRun} with its current status.
   */
  public void end();

  /**
   * Make sure there is no current {@link InstrumentRun}.
   */
  public void reset();

  /**
   * Persist current {@link InstrumentRun}.
   * @param currentRun
   */
  public void persistRun();

  /**
   * Persist {@link InstrumentRunValue} current {@link InstrumentRun}.
   * @param currentRunValue
   */
  public void update(InstrumentRunValue currentRunValue);

  /**
   * Compute the output parameters values of {@link InstrumentComputedOutputParameter} for the current
   * {@link InstrumentRun}.
   */
  public void computeOutputParameters();

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
  public InstrumentParameter getParameterByVendorName(String vendorName);

  /**
   * Get (or create it if needed) the {@code InstrumentRunValue} for the named {@code InstrumentOutputParameter} of the
   * current {@code InstrumentRun}.
   * @param parameterCode
   * @return null if current instrument run is null
   * @throws IllegalArgumentException if parameter name is not applicable to the {@code Instrument}
   */
  public InstrumentRunValue getOutputInstrumentRunValue(String parameterCode);

  /**
   * Get (or create it if needed) the {@code InstrumentRunValue} for the {@code InstrumentOutputParameter} with the
   * specified {@code vendorName} attribute of the current {@code InstrumentRun}.
   * @param parameterVendorName the {@code vendorName} of the parameter to lookup
   * @return null if current instrument run is null
   * @throws IllegalArgumentException if parameter name is not applicable to the {@code Instrument}
   */
  public InstrumentRunValue getOutputInstrumentRunValueByVendorName(String parameterVendorName);

  /**
   * Get (or create it if needed) the {@code InstrumentRunValue} for the named {@code InstrumentInputParameter} of the
   * current {@code InstrumentRun}.
   * @param parameterCode
   * @return null if current instrument run is null
   * @throws IllegalArgumentException if parameter name is not applicable to the {@code Instrument}
   */
  public InstrumentRunValue getInputInstrumentRunValue(String parameterCode);

  /**
   * Get (or create it if needed) the {@code InstrumentRunValue} for the named {@code InstrumentInterpretativeParameter}
   * of the current {@code InstrumentRun}.
   * @param parameterCode
   * @return null if current instrument run is null
   * @throws IllegalArgumentException if parameter name is not applicable to the {@code Instrument}
   */
  public InstrumentRunValue getInterpretativeInstrumentRunValue(String parameterName);

  /**
   * Get (or create it if needed) the {@code InstrumentRunValue} for the specified {@code InstrumentParameter} of the
   * current {@code InstrumentRun}.
   * 
   * @param parameter the {@code InstrumentParameter} for which to obtain the value of the current run
   * @return the {@code InstrumentRunValue}
   */
  public InstrumentRunValue getInstrumentRunValue(InstrumentParameter parameter);
}
