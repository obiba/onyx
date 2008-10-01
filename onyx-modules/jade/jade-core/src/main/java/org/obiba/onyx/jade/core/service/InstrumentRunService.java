package org.obiba.onyx.jade.core.service;

import java.util.List;

import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.jade.core.domain.instrument.Instrument;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.onyx.jade.core.domain.run.InstrumentRun;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunStatus;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;

public interface InstrumentRunService {

  /**
   * Get the completed instrument runs for given participant.
   * @param participant
   * @param instrument
   * @return
   */
  public List<InstrumentRun> getCompletedInstrumentRuns(Participant participant, Instrument instrument);

  /**
   * Get the last instrument whatever is its status for participant and instrument type.
   * @param participant
   * @param instrumentType
   * @return
   */
  public InstrumentRun getLastInstrumentRun(Participant participant, InstrumentType instrumentType);

  /**
   * Get the last completed run for participant and instrument type.
   * @param participant
   * @param instrumentType
   * @return
   */
  public InstrumentRun getLastCompletedInstrumentRun(Participant participant, InstrumentType instrumentType);

  /**
   * Find the value from the last completed run of the instrument of the given type for given participant.
   * @param participant
   * @param instrumentType
   * @param parameterName
   * @return
   */
  public InstrumentRunValue findInstrumentRunValue(Participant participant, InstrumentType instrumentType, String parameterName);

  /**
   * Update the {@link InstrumentRun} with the given {@link InstrumentRunStatus}.
   * @param run
   * @param status
   */
  public void setInstrumentRunStatus(InstrumentRun run, InstrumentRunStatus status);
}
