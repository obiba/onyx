package org.obiba.onyx.quartz.core.service;

import java.util.Locale;

import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.quartz.core.domain.answer.CategoryAnswer;
import org.obiba.onyx.quartz.core.domain.answer.QuestionAnswer;
import org.obiba.onyx.quartz.core.domain.answer.QuestionnaireParticipant;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;

public interface ActiveQuestionnaireAdministrationService {

  public Page getCurrentPage();
  
  public Page getStartPage();
  
  public Page getPreviousPage();
  
  public Page getNextPage();

  public Locale getLanguage();
  
  public Questionnaire getQuestionnaire();
  
  //public QuestionnaireParticipant getQuestionnaireParticipant();

  public QuestionAnswer findQuestionAnswer(Question question);
  
  public QuestionAnswer answerQuestion(Question question, CategoryAnswer categoryAnswer);
  
  public void setQuestionnaire(Questionnaire questionnaire);
  
  public QuestionnaireParticipant start(Participant participant, Locale language);
  
  public void setDefaultLanguage(Locale language);
  
}
