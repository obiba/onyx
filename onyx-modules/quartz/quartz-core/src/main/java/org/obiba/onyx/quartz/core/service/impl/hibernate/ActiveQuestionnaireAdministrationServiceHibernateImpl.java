package org.obiba.onyx.quartz.core.service.impl.hibernate;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.obiba.core.service.impl.hibernate.AssociationCriteria;
import org.obiba.core.service.impl.hibernate.AssociationCriteria.Operation;
import org.obiba.onyx.quartz.core.domain.answer.CategoryAnswer;
import org.obiba.onyx.quartz.core.domain.answer.OpenAnswer;
import org.obiba.onyx.quartz.core.domain.answer.QuestionnaireParticipant;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
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
    return findAnswer(question, questionCategory.getCategory());
  }

  public CategoryAnswer findAnswer(Question question, Category category) {
    Criteria criteria = AssociationCriteria.create(CategoryAnswer.class, getSession()).add("questionAnswer.questionnaireParticipant", Operation.eq, getQuestionnaireParticipant()).add("categoryName", Operation.eq, category.getName()).add("questionAnswer.questionName", Operation.eq, question.getName()).getCriteria();
    return (CategoryAnswer) criteria.uniqueResult();
  }

  public CategoryAnswer findAnswer(String questionnaireName, String questionName, String categoryName) {
    Criteria criteria = AssociationCriteria.create(CategoryAnswer.class, getSession()).add("questionAnswer.questionnaireParticipant.participant", Operation.eq, getQuestionnaireParticipant().getParticipant()).add("questionAnswer.questionnaireParticipant.questionnaireName", Operation.eq, questionnaireName).add("categoryName", Operation.eq, categoryName).add("questionAnswer.questionName", Operation.eq, questionName).getCriteria();
    return (CategoryAnswer) criteria.uniqueResult();
  }

  @SuppressWarnings("unchecked")
  public List<CategoryAnswer> findAnswers(Question question) {
    Criteria criteria = AssociationCriteria.create(CategoryAnswer.class, getSession()).add("questionAnswer.questionnaireParticipant", Operation.eq, getQuestionnaireParticipant()).add("questionAnswer.questionName", Operation.eq, question.getName()).getCriteria();
    return criteria.list();
  }

  @SuppressWarnings("unchecked")
  public List<CategoryAnswer> findActiveAnswers(Question question) {
    Criteria criteria = AssociationCriteria.create(CategoryAnswer.class, getSession()).add("questionAnswer.questionnaireParticipant", Operation.eq, getQuestionnaireParticipant()).add("questionAnswer.questionName", Operation.eq, question.getName()).add("active", Operation.eq, true).getCriteria();
    return criteria.list();
  }

  @SuppressWarnings("unchecked")
  public List<CategoryAnswer> findActiveAnswers(String questionnaireName, String questionName) {
    Criteria criteria = AssociationCriteria.create(CategoryAnswer.class, getSession()).add("questionAnswer.questionnaireParticipant.participant", Operation.eq, getQuestionnaireParticipant().getParticipant()).add("questionAnswer.questionnaireParticipant.questionnaireName", Operation.eq, questionnaireName).add("questionAnswer.questionName", Operation.eq, questionName).add("active", Operation.eq, true).getCriteria();
    return criteria.list();
  }

  public OpenAnswer findOpenAnswer(QuestionCategory questionCategory, OpenAnswerDefinition openAnswerDefinition) {
    return findOpenAnswer(questionCategory.getQuestion(), questionCategory.getCategory(), openAnswerDefinition);
  }

  public OpenAnswer findOpenAnswer(Question question, Category category, OpenAnswerDefinition openAnswerDefinition) {
    Criteria criteria = AssociationCriteria.create(OpenAnswer.class, getSession()).add("categoryAnswer.questionAnswer.questionnaireParticipant", Operation.eq, getQuestionnaireParticipant()).add("categoryAnswer.questionAnswer.questionName", Operation.eq, question.getName()).add("categoryAnswer.categoryName", Operation.eq, category.getName()).add("openAnswerDefinitionName", Operation.eq, openAnswerDefinition.getName()).getCriteria();
    return (OpenAnswer) criteria.uniqueResult();
  }

  public OpenAnswer findOpenAnswer(String questionnaireName, String questionName, String categoryName, String openAnswerDefinitionName) {
    QuestionnaireParticipant questionnaireParticipant = getQuestionnaireParticipant();
    Criteria criteria = AssociationCriteria.create(OpenAnswer.class, getSession()).add("categoryAnswer.questionAnswer.questionnaireParticipant.participant", Operation.eq, questionnaireParticipant.getParticipant()).add("categoryAnswer.questionAnswer.questionnaireParticipant.questionnaireName", Operation.eq, questionnaireName).add("categoryAnswer.questionAnswer.questionName", Operation.eq, questionName).add("categoryAnswer.categoryName", Operation.eq, categoryName).add("openAnswerDefinitionName", Operation.eq, openAnswerDefinitionName).getCriteria();
    return (OpenAnswer) criteria.uniqueResult();
  }

}
