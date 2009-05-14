/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.core.service.impl.hibernate;

import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.obiba.core.service.SortingClause;
import org.obiba.core.service.impl.hibernate.AssociationCriteria;
import org.obiba.core.service.impl.hibernate.AssociationCriteria.Operation;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.onyx.jade.core.domain.run.InstrumentRun;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunStatus;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;
import org.obiba.onyx.jade.core.domain.run.Measure;
import org.obiba.onyx.jade.core.service.impl.DefaultInstrumentRunServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class InstrumentRunServiceHibernateImpl extends DefaultInstrumentRunServiceImpl {

  private static final Logger log = LoggerFactory.getLogger(InstrumentRunServiceHibernateImpl.class);

  private SessionFactory factory;

  private Map<String, InstrumentType> instrumentTypes;

  public void setSessionFactory(SessionFactory factory) {
    this.factory = factory;
  }

  public void setInstrumentTypes(Map<String, InstrumentType> instrumentTypes) {
    this.instrumentTypes = instrumentTypes;
  }

  @Transactional(propagation = Propagation.SUPPORTS)
  private Session getSession() {
    return factory.getCurrentSession();
  }

  public InstrumentRun getLastInstrumentRun(Participant participant, InstrumentType instrumentType) {
    if(instrumentType == null) throw new IllegalArgumentException("Cannot retrieve the last instrument run for a null instrument type.");
    InstrumentRun template = new InstrumentRun();
    template.setInstrumentType(instrumentType.getName());
    template.setParticipant(participant);
    List<InstrumentRun> runs = getPersistenceManager().match(template, SortingClause.create("id", false));
    if(runs != null && runs.size() > 0) {
      return runs.get(0);
    }
    return null;
  }

  public InstrumentRun getLastInstrumentRun(Participant participant, String instrumentTypeName) {
    InstrumentType type = instrumentTypes.get(instrumentTypeName);
    return getLastInstrumentRun(participant, type);
  }

  public InstrumentRun getLastCompletedInstrumentRun(Participant participant, InstrumentType instrumentType) {
    if(instrumentType == null) throw new IllegalArgumentException("Cannot retrieve the last completed instrument run for a null instrument type.");

    Criteria criteria = AssociationCriteria.create(InstrumentRun.class, getSession()).add("instrumentType", Operation.eq, instrumentType.getName()).add("participant", Operation.eq, participant).addSortingClauses(new SortingClause("timeEnd", false)).getCriteria();
    criteria.add(Restrictions.or(Restrictions.eq("status", InstrumentRunStatus.COMPLETED), Restrictions.eq("status", InstrumentRunStatus.CONTRA_INDICATED)));

    return (InstrumentRun) criteria.setMaxResults(1).uniqueResult();
  }

  public InstrumentRunValue findInstrumentRunValue(Participant participant, InstrumentType instrumentType, String parameterCode, Integer measurePosition) {
    if(instrumentType == null) throw new IllegalArgumentException("Cannot retrieve the last completed instrument run for a null instrument type.");
    if(parameterCode == null) throw new IllegalArgumentException("Cannot retrieve the last completed instrument run for a null parameter.");

    InstrumentRun run = getLastCompletedInstrumentRun(participant, instrumentType);

    return getInstrumentRunValue(run, participant, instrumentType, parameterCode, measurePosition);
  }

  public InstrumentRunValue findInstrumentRunValueFromLastRun(Participant participant, InstrumentType instrumentType, String parameterCode, Integer measurePosition) {
    if(instrumentType == null) throw new IllegalArgumentException("Cannot retrieve the last instrument run for a null instrument type.");
    if(parameterCode == null) throw new IllegalArgumentException("Cannot retrieve the last instrument run for a null parameter.");

    InstrumentRun run = getLastInstrumentRun(participant, instrumentType);

    return getInstrumentRunValue(run, participant, instrumentType, parameterCode, measurePosition);
  }

  private InstrumentRunValue getInstrumentRunValue(InstrumentRun run, Participant participant, InstrumentType instrumentType, String parameterCode, Integer measurePosition) {
    InstrumentRunValue runValue = null;

    if(run != null) {
      log.debug("Run.id={} Param.code={}", run.getId(), parameterCode);
      AssociationCriteria assoCriteria = AssociationCriteria.create(InstrumentRunValue.class, getSession()).add("instrumentParameter", Operation.eq, parameterCode);
      if(measurePosition == null) {
        assoCriteria.add("instrumentRun", Operation.eq, run);
      } else {
        // find the measure from its position
        Measure measure = new Measure();
        measure.setInstrumentRun(run);

        List<Measure> measures = getPersistenceManager().match(measure);
        if(measurePosition <= measures.size()) {
          measure = measures.get(measurePosition - 1);
          assoCriteria.add("measure", Operation.eq, measure);
        } else {
          log.warn("No run value at: InstrumentType={} Param.code={} Measure.position={}", new Object[] { instrumentType.getName(), parameterCode, measurePosition });
          return null;
        }
      }
      runValue = (InstrumentRunValue) assoCriteria.getCriteria().uniqueResult();
    }

    return runValue;
  }

}
