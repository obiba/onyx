package org.obiba.onyx.jade.core.service;

import java.util.List;

import org.obiba.onyx.jade.core.domain.instrument.Instrument;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.onyx.jade.core.domain.run.InstrumentRun;
import org.obiba.onyx.jade.core.domain.run.ParticipantInterview;

public interface InstrumentRunService {
  
  public InstrumentRun createInstrumentRun(Instrument instrument);
  
  public List<InstrumentRun> getCompletedInstrumentRuns(Instrument instrument, ParticipantInterview participantInterview);
  
  public InstrumentRun getLastCompletedInstrumentRun(InstrumentType instrumentType, ParticipantInterview participantInterview);
  
  
}
