package org.obiba.onyx.jade.core.service;

import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.jade.core.domain.instrument.Instrument;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentComputedOutputParameter;
import org.obiba.onyx.jade.core.domain.run.InstrumentRun;

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

}
