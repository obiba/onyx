package org.obiba.onyx.jade.core.service;

import java.util.List;

import org.obiba.onyx.jade.core.domain.instrument.Instrument;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.onyx.jade.core.domain.run.InstrumentRun;
import org.obiba.onyx.jade.core.domain.run.ParticipantInterview;

public interface InstrumentRunService {
  
  /**
   * Create an instrument run in the scope of participant interview.
   * @param participantInterview
   * @param instrument
   * @return
   */
  public InstrumentRun createInstrumentRun(ParticipantInterview participantInterview, Instrument instrument);
  
  public void completeInstrumentRun(InstrumentRun instrumentRun);
  
  public void cancelInstrumentRun(InstrumentRun instrumentRun);
  
  public void failInstrumentRun(InstrumentRun instrumentRun);
  
  public List<InstrumentRun> getCompletedInstrumentRuns(Instrument instrument, ParticipantInterview participantInterview);
  
  public InstrumentRun getLastCompletedInstrumentRun(InstrumentType instrumentType, ParticipantInterview participantInterview);
  
  
}
