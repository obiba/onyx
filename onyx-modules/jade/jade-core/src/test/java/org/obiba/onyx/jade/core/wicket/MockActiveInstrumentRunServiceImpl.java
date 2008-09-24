package org.obiba.onyx.jade.core.wicket;

import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.jade.core.domain.instrument.ContraIndication;
import org.obiba.onyx.jade.core.domain.instrument.Instrument;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.onyx.jade.core.domain.run.InstrumentRun;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunStatus;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;
import org.obiba.onyx.jade.core.service.ActiveInstrumentRunService;

public class MockActiveInstrumentRunServiceImpl implements ActiveInstrumentRunService {

  public void cancel() {
    // TODO Auto-generated method stub
    
  }

  public void complete() {
    // TODO Auto-generated method stub
    
  }

  public void computeOutputParameters() {
    // TODO Auto-generated method stub
    
  }

  public void fail() {
    // TODO Auto-generated method stub
    
  }

  public ContraIndication getContraIndication() {
    // TODO Auto-generated method stub
    return null;
  }

  public InstrumentRunValue getInputInstrumentRunValue(String parameterName) {
    // TODO Auto-generated method stub
    return null;
  }

  public Instrument getInstrument() {
    Instrument instrument = new Instrument();
    instrument.setName("dummy");
    instrument.setBarcode("1");
    return instrument;
  }

  public InstrumentRun getInstrumentRun() {
    // TODO Auto-generated method stub
    return null;
  }

  public InstrumentType getInstrumentType() {
    // TODO Auto-generated method stub
    return null;
  }

  public InstrumentRunValue getOutputInstrumentRunValue(String parameterName) {
    // TODO Auto-generated method stub
    return null;
  }

  public Participant getParticipant() {
    // TODO Auto-generated method stub
    return null;
  }

  public void reset() {
    // TODO Auto-generated method stub
    
  }

  public void setContraIndication(ContraIndication contraIndication) {
    // TODO Auto-generated method stub
    
  }

  public void setInstrumentType(InstrumentType instrumentType) {
    // TODO Auto-generated method stub
    
  }

  public InstrumentRun start(Participant participant, Instrument instrument) {
    // TODO Auto-generated method stub
    return null;
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

  

}
