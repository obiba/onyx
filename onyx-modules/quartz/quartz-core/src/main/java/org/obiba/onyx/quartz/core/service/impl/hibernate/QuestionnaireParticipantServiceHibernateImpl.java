package org.obiba.onyx.quartz.core.service.impl.hibernate;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.obiba.core.service.SortingClause;
import org.obiba.core.service.impl.hibernate.AssociationCriteria;
import org.obiba.core.service.impl.hibernate.AssociationCriteria.Operation;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.quartz.core.domain.answer.QuestionnaireParticipant;
import org.obiba.onyx.quartz.core.service.impl.DefaultQuestionnaireParticipantServiceImpl;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class QuestionnaireParticipantServiceHibernateImpl extends DefaultQuestionnaireParticipantServiceImpl {

  private SessionFactory factory;

  public void setSessionFactory(SessionFactory factory) {
    this.factory = factory;
  }

  private Session getSession() {
    return factory.getCurrentSession();
  }

  public QuestionnaireParticipant getLastQuestionnaireParticipant(Participant participant, String questionnaireName) {
    Criteria criteria = AssociationCriteria.create(QuestionnaireParticipant.class, getSession()).add("questionnaireName", Operation.eq, questionnaireName).add("participant", Operation.eq, participant).getCriteria();

    return (QuestionnaireParticipant) criteria.setMaxResults(1).uniqueResult();
  }

}
