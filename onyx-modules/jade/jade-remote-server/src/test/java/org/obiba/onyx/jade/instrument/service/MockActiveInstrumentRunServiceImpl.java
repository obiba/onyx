package org.obiba.onyx.jade.instrument.service;

import org.obiba.core.service.impl.PersistenceManagerAwareService;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.domain.user.User;
import org.obiba.onyx.jade.core.domain.instrument.ContraIndication;
import org.obiba.onyx.jade.core.domain.instrument.Instrument;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.onyx.jade.core.domain.run.InstrumentRun;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunStatus;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;
import org.obiba.onyx.jade.core.domain.run.ParticipantInterview;
import org.obiba.onyx.jade.core.service.ActiveInstrumentRunService;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class MockActiveInstrumentRunServiceImpl extends PersistenceManagerAwareService implements ActiveInstrumentRunService {

  private InstrumentRun run = null;
  
  public void computeOutputParameters() {
  }

  public InstrumentRun getInstrumentRun() {
    if (run == null) {
      //System.out.println("++++++++++++++++++++++++ run.count=" + getPersistenceManager().count(InstrumentRun.class));
      run = new InstrumentRun();//getPersistenceManager().get(InstrumentRun.class, 1l);
      run.setUser(new User());
      run.getUser().setLastName("technicien_1");
      run.getUser().setFirstName("tech");
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

  public void reset() {
  }

  public InstrumentRun start(Participant participant, Instrument instrument) {
    return null;
  }

  public InstrumentRunValue getOutputInstrumentRunValue(String parameterName) {
    // TODO Auto-generated method stub
    return null;
  }
  
  public InstrumentRunValue getInputInstrumentRunValue(String parameterName) {
    // TODO Auto-generated method stub
    return null;
  }

  public Instrument getInstrument() {
    // TODO Auto-generated method stub
    return null;
  }

  public InstrumentType getInstrumentType() {
    // TODO Auto-generated method stub
    return null;
  }

  public void setInstrumentType(InstrumentType instrumentType) {
    // TODO Auto-generated method stub
    
  }

  public ContraIndication getContraIndication() {
    // TODO Auto-generated method stub
    return null;
  }

  public void setContraIndication(ContraIndication contraIndication) {
    // TODO Auto-generated method stub
    
  }

  public void update(InstrumentRun currentRun) {
    // TODO Auto-generated method stub
    
  }

  public String getOtherContraIndication() {
    // TODO Auto-generated method stub
    return null;
  }

  public void setOtherContraIndication(String otherContraIndication) {
    // TODO Auto-generated method stub
    
  }

  public InstrumentRunStatus getInstrumentRunStatus() {
    // TODO Auto-generated method stub
    return null;
  }

  public void setInstrumentRunStatus(InstrumentRunStatus status) {
    // TODO Auto-generated method stub
    
  }

  public void update(InstrumentRunValue currentRunValue) {
    // TODO Auto-generated method stub
    
  }

  public void end() {
    // TODO Auto-generated method stub
    
  }

  public InstrumentRunValue getInterpretativeInstrumentRunValue(String parameterName) {
    // TODO Auto-generated method stub
    return null;
  }

}
