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

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.obiba.core.service.SortingClause;
import org.obiba.core.service.impl.hibernate.AssociationCriteria;
import org.obiba.core.service.impl.hibernate.AssociationCriteria.Operation;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.jade.core.domain.run.InstrumentRun;
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

  public void setSessionFactory(SessionFactory factory) {
    this.factory = factory;
  }

  @Transactional(propagation = Propagation.SUPPORTS)
  private Session getSession() {
    return factory.getCurrentSession();
  }

  public InstrumentRunValue getInstrumentRunValue(Participant participant, String instrumentTypeName, String parameterCode, Integer measurePosition) {
    InstrumentRun instrumentRun = getInstrumentRun(participant, instrumentTypeName);
    if(parameterCode == null) throw new IllegalArgumentException("The parameterCode must not be null.");
    if(measurePosition != null && measurePosition < 1) throw new IllegalArgumentException("The measurePosition [" + measurePosition + "] must be a positive integer.");
    InstrumentRunValue runValue = null;

    if(instrumentRun != null) {
      log.debug("Run.id={} Param.code={}", instrumentRun.getId(), parameterCode);
      AssociationCriteria assoCriteria = AssociationCriteria.create(InstrumentRunValue.class, getSession()).add("instrumentParameter", Operation.eq, parameterCode);
      if(measurePosition == null) {
        assoCriteria.add("instrumentRun", Operation.eq, instrumentRun);
      } else {
        // find the measure from its position
        Measure measure = new Measure();
        measure.setInstrumentRun(instrumentRun);

        List<Measure> measures = getPersistenceManager().match(measure, SortingClause.create("time", false));
        if(measurePosition <= measures.size()) {
          measure = measures.get(measurePosition - 1);
          assoCriteria.add("measure", Operation.eq, measure);
        } else {
          log.warn("No run value at: InstrumentType={} Param.code={} Measure.position={}", new Object[] { instrumentTypeName, parameterCode, measurePosition });
          return null;
        }
      }
      runValue = (InstrumentRunValue) assoCriteria.getCriteria().uniqueResult();
    }

    return runValue;
  }
}
