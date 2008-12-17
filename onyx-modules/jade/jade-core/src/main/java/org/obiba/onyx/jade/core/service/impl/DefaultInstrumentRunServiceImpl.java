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
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.onyx.jade.core.domain.run.InstrumentRun;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunStatus;
import org.obiba.onyx.jade.core.service.InstrumentRunService;

public abstract class DefaultInstrumentRunServiceImpl extends PersistenceManagerAwareService implements InstrumentRunService {

  private List<InstrumentRun> getInstrumentRuns(InstrumentType instrument, Participant participant, InstrumentRunStatus status) {

    InstrumentRun template = new InstrumentRun();
    template.setInstrumentType(instrument);
    template.setParticipant(participant);
    template.setStatus(status);

    return getPersistenceManager().match(template);
  }

  public List<InstrumentRun> getCompletedInstrumentRuns(Participant participant, InstrumentType instrumentType) {
    return getInstrumentRuns(instrumentType, participant, InstrumentRunStatus.COMPLETED);
  }

}
