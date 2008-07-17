package org.obiba.onyx.jade.core.service.impl.hibernate;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.obiba.core.service.SortingClause;
import org.obiba.core.service.impl.hibernate.AssociationCriteria;
import org.obiba.core.service.impl.hibernate.AssociationCriteria.Operation;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.onyx.jade.core.domain.run.InstrumentRun;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunStatus;
import org.obiba.onyx.jade.core.domain.run.ParticipantInterview;
import org.obiba.onyx.jade.core.service.impl.DefaultInstrumentRunServiceImpl;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class InstrumentRunServiceHibernateImpl extends DefaultInstrumentRunServiceImpl {

  private SessionFactory factory;

  public void setSessionFactory(SessionFactory factory) {
    this.factory = factory;
  }

  private Session getSession() {
    return factory.getCurrentSession();
  }

  public InstrumentRun getLastCompletedInstrumentRun(InstrumentType instrumentType, ParticipantInterview participantInterview) {
    return (InstrumentRun) AssociationCriteria.create(InstrumentRun.class, getSession()).add("instrument.instrumentType", Operation.eq, instrumentType).add("participantInterview", Operation.eq, participantInterview).add("status", Operation.eq, InstrumentRunStatus.COMPLETED).addSortingClauses(new SortingClause("timeComplete", false)).getCriteria().uniqueResult();
  }

}
