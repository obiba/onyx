/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.core.service.impl;

import java.util.List;

import org.obiba.core.service.impl.PersistenceManagerAwareService;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.jade.core.domain.instrument.Instrument;
import org.obiba.onyx.jade.core.domain.run.InstrumentRun;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunStatus;
import org.obiba.onyx.jade.core.domain.run.ParticipantInterview;
import org.obiba.onyx.jade.core.service.InstrumentRunService;

public abstract class DefaultInstrumentRunServiceImpl extends PersistenceManagerAwareService implements InstrumentRunService {

  private List<InstrumentRun> getInstrumentRuns(Instrument instrument, Participant participant, InstrumentRunStatus status) {
    
    ParticipantInterview interview = new ParticipantInterview();
    interview.setParticipant(participant);
    interview = getPersistenceManager().matchOne(interview);
    InstrumentRun template = new InstrumentRun();
    template.setInstrument(instrument);
    template.setParticipantInterview(interview);
    template.setStatus(status);

    return getPersistenceManager().match(template);
  }

  public List<InstrumentRun> getCompletedInstrumentRuns(Participant participant, Instrument instrument) {
    return getInstrumentRuns(instrument, participant, InstrumentRunStatus.COMPLETED);
  }

}
