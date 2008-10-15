package org.obiba.onyx.quartz.core.wicket.layout;

import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;

public interface IQuestionPanelFactory {

  public QuestionPanel createPanel(String id, Question question);
  
}
