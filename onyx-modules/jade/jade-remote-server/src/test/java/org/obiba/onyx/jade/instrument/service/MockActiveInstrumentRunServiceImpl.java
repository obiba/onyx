package org.obiba.onyx.jade.instrument.service;

import org.obiba.core.service.impl.PersistenceManagerAwareService;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.domain.user.User;
import org.obiba.onyx.jade.core.domain.instrument.Instrument;
import org.obiba.onyx.jade.core.domain.run.InstrumentRun;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;
import org.obiba.onyx.jade.core.domain.run.ParticipantInterview;
import org.obiba.onyx.jade.core.service.ActiveInstrumentRunService;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class MockActiveInstrumentRunServiceImpl extends PersistenceManagerAwareService implements ActiveInstrumentRunService {

  private InstrumentRun run = null;
  
  public void cancel() {
  }

  public void complete() {
  }

  public void computeOutputParameters() {
  }

  public void fail() {
  }

  public InstrumentRun getInstrumentRun() {
    if (run == null) {
      //System.out.println("++++++++++++++++++++++++ run.count=" + getPersistenceManager().count(InstrumentRun.class));
      run = new InstrumentRun();//getPersistenceManager().get(InstrumentRun.class, 1l);
      run.setUser(new User());
      run.getUser().setName("technicien_1");
      run.setParticipantInterview(new ParticipantInterview());
      run.getParticipantInterview().setParticipant(new Participant());
      run.getParticipantInterview().getParticipant().setLastName("Dupont");
      run.getParticipantInterview().getParticipant().setFirstName("Laura");
    }
    return run;
  }

  public Participant getParticipant() {
    return null;
  }

  public InstrumentRun refresh() {
    return null;
  }

  public void reset() {
  }

  public InstrumentRun start(Participant participant, Instrument instrument) {
    return null;
  }

  public void validate() {
  }

  public InstrumentRunValue getOutputInstrumentRunValue(String parameterName) {
    // TODO Auto-generated method stub
    return null;
  }
  
  public InstrumentRunValue getInputInstrumentRunValue(String parameterName) {
    // TODO Auto-generated method stub
    return null;
  }

}
