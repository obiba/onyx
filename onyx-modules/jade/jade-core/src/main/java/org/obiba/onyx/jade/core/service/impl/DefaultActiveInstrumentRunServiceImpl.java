package org.obiba.onyx.jade.core.service.impl;

import java.util.Date;

import org.obiba.core.service.impl.PersistenceManagerAwareService;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.jade.core.domain.instrument.Instrument;
import org.obiba.onyx.jade.core.domain.run.InstrumentRun;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunStatus;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;
import org.obiba.onyx.jade.core.domain.run.ParticipantInterview;
import org.obiba.onyx.jade.core.service.ActiveInstrumentRunService;

public class DefaultActiveInstrumentRunServiceImpl extends PersistenceManagerAwareService implements ActiveInstrumentRunService {

  private InstrumentRun currentRun = null;

  public InstrumentRun start(Participant participant, Instrument instrument) {
    if(currentRun != null) {
      if(currentRun.getStatus().equals(InstrumentRunStatus.IN_PROGRESS)) {
        cancel();
      }
      currentRun = null;
    }

    ParticipantInterview participantInterviewTemplate = new ParticipantInterview();
    participantInterviewTemplate.setParticipant(participant);
    ParticipantInterview participantInterview = getPersistenceManager().matchOne(participantInterviewTemplate);
    if(participantInterview == null) {
      participantInterview = getPersistenceManager().save(participantInterviewTemplate);
    }

    currentRun = new InstrumentRun();
    currentRun.setParticipantInterview(participantInterview);
    currentRun.setInstrument(instrument);
    currentRun.setStatus(InstrumentRunStatus.IN_PROGRESS);
    currentRun.setTimeStart(new Date());

    return currentRun;
  }

  public void cancel() {
    end(InstrumentRunStatus.CANCELED);
  }

  public void fail() {
    end(InstrumentRunStatus.IN_ERROR);
  }

  public void complete() {
    end(InstrumentRunStatus.COMPLETED);
  }

  private void end(InstrumentRunStatus status) {
    if(currentRun == null) return;

    currentRun.setStatus(status);
    currentRun.setTimeEnd(new Date());

    getPersistenceManager().save(currentRun);

    for(InstrumentRunValue value : currentRun.getInstrumentRunValues()) {
      getPersistenceManager().save(value);
    }
  }

  public InstrumentRun getInstrumentRun() {
    return currentRun;
  }

  public Participant getParticipant() {
    if(currentRun == null) return null;

    return currentRun.getParticipantInterview().getParticipant();
  }

  public void reset() {
    currentRun = null;
  }

  public void validate() {
    if(currentRun == null) return;
    
    getPersistenceManager().save(currentRun);

    for(InstrumentRunValue value : currentRun.getInstrumentRunValues()) {
      getPersistenceManager().save(value);
    }
  }

}
