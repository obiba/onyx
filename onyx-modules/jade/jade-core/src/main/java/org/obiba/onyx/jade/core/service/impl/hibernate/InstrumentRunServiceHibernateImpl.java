package org.obiba.onyx.jade.core.service.impl.hibernate;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.obiba.core.service.SortingClause;
import org.obiba.core.service.impl.hibernate.AssociationCriteria;
import org.obiba.core.service.impl.hibernate.AssociationCriteria.Operation;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.onyx.jade.core.domain.run.InstrumentRun;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunStatus;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;
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

  public InstrumentRun getLastInstrumentRun(ParticipantInterview participantInterview, InstrumentType instrumentType) {
    return (InstrumentRun) AssociationCriteria.create(InstrumentRun.class, getSession()).add("instrument.instrumentType", Operation.eq, instrumentType).add("participantInterview", Operation.eq, participantInterview).addSortingClauses(new SortingClause("timeEnd", false)).getCriteria().uniqueResult();
  }

  public InstrumentRun getLastCompletedInstrumentRun(ParticipantInterview participantInterview, InstrumentType instrumentType) {
    Criteria criteria = AssociationCriteria.create(InstrumentRun.class, getSession()).add("instrument.instrumentType", Operation.eq, instrumentType).add("participantInterview", Operation.eq, participantInterview).addSortingClauses(new SortingClause("timeEnd", false)).getCriteria();
    criteria.add(Restrictions.or(Restrictions.eq("status", InstrumentRunStatus.COMPLETED),Restrictions.eq("status", InstrumentRunStatus.CONTRA_INDICATED)));
    
    return (InstrumentRun) criteria.setMaxResults(1).uniqueResult();
  }

  public InstrumentRunValue findInstrumentRunValue(ParticipantInterview participantInterview, InstrumentType instrumentType, String parameterName) {
    InstrumentRunValue runValue = null;
    InstrumentRun run = getLastCompletedInstrumentRun(participantInterview, instrumentType);

    if(run != null) {
      runValue = (InstrumentRunValue) AssociationCriteria.create(InstrumentRunValue.class, getSession()).add("instrumentRun", Operation.eq, run).add("instrumentParameter.name", Operation.eq, parameterName).getCriteria().uniqueResult();
    }
    return runValue;
  }

}
