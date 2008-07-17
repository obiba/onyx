package org.obiba.onyx.jade.core.service.impl;

import java.util.List;

import org.obiba.core.service.impl.PersistenceManagerAwareService;
import org.obiba.onyx.jade.core.domain.instrument.Instrument;
import org.obiba.onyx.jade.core.domain.run.InstrumentRun;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunStatus;
import org.obiba.onyx.jade.core.domain.run.ParticipantInterview;
import org.obiba.onyx.jade.core.service.InstrumentRunService;

public abstract class DefaultInstrumentRunServiceImpl extends PersistenceManagerAwareService implements InstrumentRunService {

  public InstrumentRun createInstrumentRun(Instrument instrument) {
    // TODO Auto-generated method stub
    return null;
  }

  private List<InstrumentRun> getInstrumentRuns(Instrument instrument, ParticipantInterview participantInterview, InstrumentRunStatus status) {
    InstrumentRun template = new InstrumentRun();
    template.setInstrument(instrument);
    template.setParticipantInterview(participantInterview);
    template.setStatus(status);

    return getPersistenceManager().match(template);
  }

  public List<InstrumentRun> getCompletedInstrumentRuns(Instrument instrument, ParticipantInterview participantInterview) {
    return getInstrumentRuns(instrument, participantInterview, InstrumentRunStatus.COMPLETED);
  }

}
