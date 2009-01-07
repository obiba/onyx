/***********************************************************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 **********************************************************************************************************************/
package org.obiba.onyx.quartz.core.service.impl.hibernate;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.obiba.core.service.impl.hibernate.AssociationCriteria;
import org.obiba.core.service.impl.hibernate.AssociationCriteria.Operation;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.quartz.core.domain.answer.CategoryAnswer;
import org.obiba.onyx.quartz.core.domain.answer.OpenAnswer;
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

  public List<CategoryAnswer> getCategoryAnswers(Participant participant, String questionnaireName, String questionName) {
    QuestionnaireParticipant questionnaireParticipant = getQuestionnaireParticipant(participant, questionnaireName);

    Criteria criteria = AssociationCriteria.create(CategoryAnswer.class, getSession()).add("questionAnswer.questionnaireParticipant", Operation.eq, questionnaireParticipant).add("questionAnswer.questionName", Operation.eq, questionName).add("active", Operation.eq, true).getCriteria();

    return criteria.list();
  }

  public OpenAnswer getOpenAnswer(Participant participant, String questionnaireName, String questionName, String categoryName, String openAnswerName) {
    // TODO Auto-generated method stub
    return null;
  }

  public List<OpenAnswer> getOpenAnswers(Participant participant, String questionnaireName, String questionName, String categoryName) {
    // TODO Auto-generated method stub
    return null;
  }

}
