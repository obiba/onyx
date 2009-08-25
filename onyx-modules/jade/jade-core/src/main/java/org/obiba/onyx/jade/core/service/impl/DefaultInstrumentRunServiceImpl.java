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
import java.util.Map;

import org.obiba.core.service.SortingClause;
import org.obiba.core.service.impl.PersistenceManagerAwareService;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.onyx.jade.core.domain.run.InstrumentRun;
import org.obiba.onyx.jade.core.domain.run.Measure;
import org.obiba.onyx.jade.core.domain.run.MeasureStatus;
import org.obiba.onyx.jade.core.service.InstrumentRunService;

public abstract class DefaultInstrumentRunServiceImpl extends PersistenceManagerAwareService implements InstrumentRunService {

  private Map<String, InstrumentType> instrumentTypes;

  public void setInstrumentTypes(Map<String, InstrumentType> instrumentTypes) {
    this.instrumentTypes = instrumentTypes;
  }

  public InstrumentRun getInstrumentRun(Participant participant, String instrumentTypeName) {
    if(participant == null) throw new IllegalArgumentException("The participant must not be null.");
    if(instrumentTypeName == null) throw new IllegalArgumentException("The instrumentTypeName must not be null.");
    InstrumentType instrumentType = instrumentTypes.get(instrumentTypeName);
    if(instrumentType == null) throw new IllegalArgumentException("Cannot retrieve instrument run for a null instrument type. InstrumentTypeName was [" + instrumentTypeName + "].");
    InstrumentRun template = new InstrumentRun();
    template.setInstrumentType(instrumentType.getName());
    template.setParticipant(participant);
    List<InstrumentRun> runs = getPersistenceManager().match(template, SortingClause.create("id", false));
    if(runs != null && runs.size() > 1) throw new IllegalStateException("Too many InstrumentRun objects for Participant [" + participant.getFullName() + "]. Expected [1] but found [" + runs.size() + "].");
    if(runs != null && runs.size() == 1) return runs.get(0);
    return null;
  }

  public void deleteInstrumentRun(Participant participant, String instrumentTypeName) {
    InstrumentRun instrumentRun = getInstrumentRun(participant, instrumentTypeName);
    if(instrumentRun != null) {
      getPersistenceManager().delete(instrumentRun);
    }
  }

  public void updateMeasureStatus(Measure measure, MeasureStatus status) {
    measure.setStatus(status);
    getPersistenceManager().save(measure);
  }

}
