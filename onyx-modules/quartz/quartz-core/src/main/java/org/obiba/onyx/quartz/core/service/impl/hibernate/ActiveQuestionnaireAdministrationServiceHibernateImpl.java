package org.obiba.onyx.quartz.core.service.impl.hibernate;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.obiba.core.service.impl.hibernate.AssociationCriteria;
import org.obiba.core.service.impl.hibernate.AssociationCriteria.Operation;
import org.obiba.onyx.quartz.core.domain.answer.CategoryAnswer;
import org.obiba.onyx.quartz.core.domain.answer.OpenAnswer;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.service.impl.DefaultActiveQuestionnaireAdministrationServiceImpl;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class ActiveQuestionnaireAdministrationServiceHibernateImpl extends DefaultActiveQuestionnaireAdministrationServiceImpl {

  private SessionFactory factory;

  public void setSessionFactory(SessionFactory factory) {
    this.factory = factory;
  }

  private Session getSession() {
    return factory.getCurrentSession();
  }

  public CategoryAnswer findAnswer(QuestionCategory questionCategory) {
    return findAnswer(questionCategory.getQuestion(), questionCategory);
  }

  public CategoryAnswer findAnswer(Question question, QuestionCategory questionCategory) {
    Criteria criteria = AssociationCriteria.create(CategoryAnswer.class, getSession()).add("questionAnswer.questionnaireParticipant", Operation.eq, getQuestionnaireParticipant()).add("categoryName", Operation.eq, questionCategory.getCategory().getName()).add("questionAnswer.questionName", Operation.eq, question.getName()).getCriteria();
    return (CategoryAnswer) criteria.uniqueResult();
  }

  @SuppressWarnings("unchecked")
  public List<CategoryAnswer> findAnswers(Question question) {
    Criteria criteria = AssociationCriteria.create(CategoryAnswer.class, getSession()).add("questionAnswer.questionnaireParticipant", Operation.eq, getQuestionnaireParticipant()).add("questionAnswer.questionName", Operation.eq, question.getName()).getCriteria();
    return criteria.list();
  }

  public OpenAnswer findOpenAnswer(QuestionCategory questionCategory, String openAnswerDefinitionName) {
    Criteria criteria = AssociationCriteria.create(OpenAnswer.class, getSession()).add("categoryAnswer.categoryName", Operation.eq, questionCategory.getCategory().getName()).add("openAnswerDefinitionName", Operation.eq, openAnswerDefinitionName).getCriteria();
    return (OpenAnswer) criteria.uniqueResult();
  }

}
