package org.obiba.onyx.quartz.core.service.impl;

import java.util.Locale;

import org.obiba.core.service.impl.PersistenceManagerAwareService;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.quartz.core.domain.answer.CategoryAnswer;
import org.obiba.onyx.quartz.core.domain.answer.QuestionAnswer;
import org.obiba.onyx.quartz.core.domain.answer.QuestionnaireParticipant;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class DefaultActiveQuestionnaireAdministrationServiceImpl extends PersistenceManagerAwareService implements ActiveQuestionnaireAdministrationService {

  private Questionnaire currentQuestionnaire;

  private QuestionnaireParticipant currentQuestionnaireParticipant;

  public Questionnaire getQuestionnaire() {
    return currentQuestionnaire;
  }

  public void setQuestionnaire(Questionnaire questionnaire) {
    this.currentQuestionnaire = questionnaire;
    this.currentQuestionnaireParticipant = null;
  }

  private QuestionnaireParticipant getQuestionnaireParticipant() {
    return (getPersistenceManager().refresh(currentQuestionnaireParticipant));
  }

  public Locale getLanguage() {
    if(currentQuestionnaireParticipant == null) return null;
    return currentQuestionnaireParticipant.getLocale();
  }

  public QuestionnaireParticipant start(Participant participant, Locale language) {

    if(currentQuestionnaireParticipant != null) throw new IllegalArgumentException("Invalid questionnaireParticipant for specified questionnaire");

    QuestionnaireParticipant questionnaireParticipantTemplate = new QuestionnaireParticipant();
    questionnaireParticipantTemplate.setParticipant(participant);
    questionnaireParticipantTemplate.setQuestionnaireName(currentQuestionnaire.getName());
    questionnaireParticipantTemplate.setQuestionnaireVersion(currentQuestionnaire.getVersion());
    questionnaireParticipantTemplate.setLocale(language);

    currentQuestionnaireParticipant = getPersistenceManager().save(questionnaireParticipantTemplate);

    return currentQuestionnaireParticipant;
  }

  public QuestionAnswer answerQuestion(Question question, CategoryAnswer categoryAnswer) {
    // TODO Auto-generated method stub
    return null;
  }

  public QuestionAnswer findQuestionAnswer(Question question) {
    // TODO Auto-generated method stub
    return null;
  }

  public Page getCurrentPage() {
    // TODO Auto-generated method stub
    return null;
  }

  public Page getNextPage() {
    // TODO Auto-generated method stub
    return null;
  }

}
