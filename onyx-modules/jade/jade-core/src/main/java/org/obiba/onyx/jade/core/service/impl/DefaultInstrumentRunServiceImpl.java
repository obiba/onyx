package org.obiba.onyx.jade.core.service.impl;

import java.util.Date;
import java.util.List;

import org.obiba.core.service.impl.PersistenceManagerAwareService;
import org.obiba.onyx.core.domain.Participant;
import org.obiba.onyx.jade.core.domain.instrument.Instrument;
import org.obiba.onyx.jade.core.domain.run.InstrumentRun;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunStatus;
import org.obiba.onyx.jade.core.domain.run.ParticipantInterview;
import org.obiba.onyx.jade.core.service.InstrumentRunService;

public abstract class DefaultInstrumentRunServiceImpl extends PersistenceManagerAwareService implements InstrumentRunService {

  public InstrumentRun createInstrumentRun(ParticipantInterview participantInterview, Instrument instrument) {
    InstrumentRun run = new InstrumentRun();
    run.setParticipantInterview(participantInterview);
    run.setInstrument(instrument);
    run.setStatus(InstrumentRunStatus.IN_PROGRESS);
    run.setTimeStart(new Date());

    return getPersistenceManager().save(run);
  }

  public void cancelInstrumentRun(InstrumentRun instrumentRun) {
    endInstrumentRun(instrumentRun, InstrumentRunStatus.CANCELED);
  }

  public void failInstrumentRun(InstrumentRun instrumentRun) {
    endInstrumentRun(instrumentRun, InstrumentRunStatus.IN_ERROR);
  }

  public void completeInstrumentRun(InstrumentRun instrumentRun) {
    endInstrumentRun(instrumentRun, InstrumentRunStatus.COMPLETED);
  }

  private void endInstrumentRun(InstrumentRun instrumentRun, InstrumentRunStatus status) {
    instrumentRun.setStatus(status);
    instrumentRun.setTimeEnd(new Date());

    getPersistenceManager().save(instrumentRun);
  }

  public ParticipantInterview getParticipantInterview(Participant participant) {
    ParticipantInterview template = new ParticipantInterview(participant);
    ParticipantInterview interview = getPersistenceManager().matchOne(template);
    
    if (interview == null) {
      interview = getPersistenceManager().save(template);
    }
    
    return interview;
  }

  private List<InstrumentRun> getInstrumentRuns(Instrument instrument, ParticipantInterview participantInterview, InstrumentRunStatus status) {
    InstrumentRun template = new InstrumentRun();
    template.setInstrument(instrument);
    template.setParticipantInterview(participantInterview);
    template.setStatus(status);

    return getPersistenceManager().match(template);
  }

  public List<InstrumentRun> getCompletedInstrumentRuns(ParticipantInterview participantInterview, Instrument instrument) {
    return getInstrumentRuns(instrument, participantInterview, InstrumentRunStatus.COMPLETED);
  }

}
