package org.obiba.onyx.jade.core.service;

import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.jade.core.domain.instrument.Instrument;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentComputedOutputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentOutputParameter;
import org.obiba.onyx.jade.core.domain.run.InstrumentRun;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;

public interface ActiveInstrumentRunService {

  /**
   * Create the current {@link InstrumentRun} without persisting it.
   * @param participant
   * @param instrument
   * @return
   */
  public InstrumentRun start(Participant participant, Instrument instrument);

  /**
   * Get the current {@link InstrumentRun}.
   * @return
   */
  public InstrumentRun getInstrumentRun();

  /**
   * Get the associated {@link Participant}
   * @return
   */
  public Participant getParticipant();

  /**
   * Complete the current {@link InstrumentRun}.
   */
  public void complete();

  /**
   * Cancel the current {@link InstrumentRun}.
   */
  public void cancel();

  /**
   * Set the current {@link InstrumentRun} as being in error.
   */
  public void fail();

  /**
   * Make sure there is no current {@link InstrumentRun}.
   */
  public void reset();

  /**
   * Read currently persisted {@link InstrumentRun}.
   */
  public InstrumentRun refresh();

  /**
   * Persist current {@link InstrumentRun}.
   */
  public void validate();

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
