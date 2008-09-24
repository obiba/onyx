package org.obiba.onyx.jade.core.service;

import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.jade.core.domain.instrument.ContraIndication;
import org.obiba.onyx.jade.core.domain.instrument.Instrument;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentComputedOutputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentOutputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.onyx.jade.core.domain.run.InstrumentRun;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunStatus;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;

public interface ActiveInstrumentRunService {

  /**
   * Set the {@link ContraIndication} associated the current {@link InstrumentRun}.
   * @param contraIndication
   */
  public void setContraIndication(ContraIndication contraIndication);

  /**
   * Get the {@link ContraIndication} associated the current {@link InstrumentRun}.
   */
  public ContraIndication getContraIndication();

  /**
   * Set the other contra-indication text to the current {@link InstrumentRun}.
   * @param otherContraIndication
   */
  public void setOtherContraIndication(String otherContraIndication);

  /**
   * Get the other contra-indication text for the current {@link InstrumentRun}.
   * @return
   */
  public String getOtherContraIndication();

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
   * Set the current {@link InstrumentType}.
   * @return
   */
  public void setInstrumentType(InstrumentType instrumentType);

  /**
   * Get the current {@link InstrumentType}.
   * @return
   */
  public InstrumentType getInstrumentType();

  /**
   * Get the current {@link InstrumentRun}.
   * @return
   */
  public InstrumentRun getInstrumentRun();

  /**
   * Get the {@link Instrument} associated to the current {@link InstrumentRun}.
   * @return
   */
  public Instrument getInstrument();

  /**
   * Create the current {@link InstrumentRun} without persisting it.
   * @param participant
   * @param instrument
   * @return
   */
  public InstrumentRun start(Participant participant, Instrument instrument);

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
  public void update(InstrumentRun currentRun);

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
   * Get (or create it if needed) the {@link InstrumentRunValue} for the named {@link InstrumentOutputParameter} of the
   * current {@link InstrumentRun}.
   * @param parameterName
   * @return null if current instrument run is null
   * @throws IllegalArgumentException if parameter name is not applicable to the {@link Instrument}
   */
  public InstrumentRunValue getOutputInstrumentRunValue(String parameterName);

  /**
   * Get (or create it if needed) the {@link InstrumentRunValue} for the named {@link InstrumentInputParameter} of the
   * current {@link InstrumentRun}.
   * @param parameterName
   * @return null if current instrument run is null
   * @throws IllegalArgumentException if parameter name is not applicable to the {@link Instrument}
   */
  public InstrumentRunValue getInputInstrumentRunValue(String parameterName);

}
