package org.obiba.onyx.quartz.core.service.impl.hibernate;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.obiba.core.service.impl.hibernate.AssociationCriteria;
import org.obiba.core.service.impl.hibernate.AssociationCriteria.Operation;
import org.obiba.onyx.quartz.core.domain.answer.CategoryAnswer;
import org.obiba.onyx.quartz.core.domain.answer.OpenAnswer;
import org.obiba.onyx.quartz.core.domain.answer.QuestionAnswer;
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

  @Transactional(readOnly = true)
  public CategoryAnswer findAnswer(QuestionCategory questionCategory) {
    return findAnswer(questionCategory.getQuestion(), questionCategory);
  }

  @Transactional(readOnly = true)
  public CategoryAnswer findAnswer(Question question, QuestionCategory questionCategory) {
    return findAnswer(question, questionCategory.getCategory());
  }

  @Transactional(readOnly = true)
  public CategoryAnswer findAnswer(Question question, Category category) {
    Criteria criteria = createQuestionnaireParticipantCriteria(CategoryAnswer.class, "questionAnswer").add("categoryName", Operation.eq, category.getName()).add("questionAnswer.questionName", Operation.eq, question.getName()).getCriteria();
    return (CategoryAnswer) criteria.uniqueResult();
  }

  @Transactional(readOnly = true)
  public CategoryAnswer findAnswer(String questionnaireName, String questionName, String categoryName) {
    Criteria criteria = createQuestionnaireParticipantCriteria(CategoryAnswer.class, "questionAnswer").add("categoryName", Operation.eq, categoryName).add("questionAnswer.questionName", Operation.eq, questionName).getCriteria();
    return (CategoryAnswer) criteria.uniqueResult();
  }

  @SuppressWarnings("unchecked")
  @Transactional(readOnly = true)
  public List<CategoryAnswer> findAnswers(Question question) {
    Criteria criteria = createQuestionnaireParticipantCriteria(CategoryAnswer.class, "questionAnswer").add("questionAnswer.questionName", Operation.eq, question.getName()).getCriteria();
    return criteria.list();
  }

  @SuppressWarnings("unchecked")
  @Transactional(readOnly = true)
  public List<CategoryAnswer> findActiveAnswers(Question question) {
    Criteria criteria = createQuestionnaireParticipantCriteria(CategoryAnswer.class, "questionAnswer").add("questionAnswer.questionName", Operation.eq, question.getName()).add("active", Operation.eq, true).getCriteria();
    return criteria.list();
  }

  @SuppressWarnings("unchecked")
  @Transactional(readOnly = true)
  public List<CategoryAnswer> findActiveAnswers(String questionnaireName, String questionName) {
    Criteria criteria = createQuestionnaireParticipantCriteria(CategoryAnswer.class, "questionAnswer").add("questionAnswer.questionName", Operation.eq, questionName).add("active", Operation.eq, true).getCriteria();
    return criteria.list();
  }

  @Transactional(readOnly = true)
  public OpenAnswer findOpenAnswer(QuestionCategory questionCategory, OpenAnswerDefinition openAnswerDefinition) {
    return findOpenAnswer(questionCategory.getQuestion(), questionCategory.getCategory(), openAnswerDefinition);
  }

  @Transactional(readOnly = true)
  public OpenAnswer findOpenAnswer(Question question, Category category, OpenAnswerDefinition openAnswerDefinition) {
    Criteria criteria = createQuestionnaireParticipantCriteria(OpenAnswer.class, "categoryAnswer.questionAnswer").add("categoryAnswer.questionAnswer.questionName", Operation.eq, question.getName()).add("categoryAnswer.categoryName", Operation.eq, category.getName()).add("openAnswerDefinitionName", Operation.eq, openAnswerDefinition.getName()).getCriteria();
    return (OpenAnswer) criteria.uniqueResult();
  }

  @Transactional(readOnly = true)
  public OpenAnswer findOpenAnswer(String questionnaireName, String questionName, String categoryName, String openAnswerDefinitionName) {
    Criteria criteria = createQuestionnaireParticipantCriteria(OpenAnswer.class, "categoryAnswer.questionAnswer").add("categoryAnswer.questionAnswer.questionName", Operation.eq, questionName).add("categoryAnswer.categoryName", Operation.eq, categoryName).add("openAnswerDefinitionName", Operation.eq, openAnswerDefinitionName).getCriteria();
    return (OpenAnswer) criteria.uniqueResult();
  }

  @SuppressWarnings("unchecked")
  @Transactional(readOnly = true)
  public List<OpenAnswer> findOpenAnswers(Question question, Category category) {
    Criteria criteria = createQuestionnaireParticipantCriteria(OpenAnswer.class, "categoryAnswer.questionAnswer").add("categoryAnswer.questionAnswer.questionName", Operation.eq, question.getName()).add("categoryAnswer.categoryName", Operation.eq, category.getName()).getCriteria();
    return criteria.list();
  }

  @Override
  protected QuestionAnswer findAnswer(Question question) {
    Criteria criteria = createQuestionnaireParticipantCriteria(QuestionAnswer.class, null).add("questionName", Operation.eq, question.getName()).getCriteria();
    return (QuestionAnswer) criteria.uniqueResult();
  }

  private AssociationCriteria createQuestionnaireParticipantCriteria(Class<?> entityType, String prefix) {
    String pref = (prefix != null ? prefix + "." : "");
    return AssociationCriteria.create(entityType, getSession()).add(pref + "questionnaireParticipant.questionnaireName", Operation.eq, getQuestionnaire().getName()).add(pref + "questionnaireParticipant.participant", Operation.eq, activeInterviewService.getParticipant());
  }

}
