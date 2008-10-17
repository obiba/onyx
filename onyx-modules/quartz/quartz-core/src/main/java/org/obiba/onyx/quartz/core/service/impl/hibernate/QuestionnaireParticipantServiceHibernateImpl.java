/***********************************************************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 **********************************************************************************************************************/
package org.obiba.onyx.quartz.core.service.impl.hibernate;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
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

  public QuestionnaireParticipant getQuestionnaireParticipant(Participant participant, String questionnaireName) {
    Criteria criteria = AssociationCriteria.create(QuestionnaireParticipant.class, getSession()).add("questionnaireName", Operation.eq, questionnaireName).add("participant", Operation.eq, participant).getCriteria();

    return (QuestionnaireParticipant) criteria.setMaxResults(1).uniqueResult();
  }

}
