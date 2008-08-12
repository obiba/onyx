package org.obiba.onyx.jade.core.service;

import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.jade.core.domain.instrument.Instrument;
import org.obiba.onyx.jade.core.domain.run.InstrumentRun;

public interface ActiveInstrumentRunService {

  public InstrumentRun start(Participant participant, Instrument instrument);
  
  public InstrumentRun getInstrumentRun();
  
  public Participant getParticipant();
  
  public void complete();

  public void cancel();

  public void fail();
  
}
