package org.obiba.onyx.quartz.core.wicket.layout.impl;

import org.apache.wicket.model.Model;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.wicket.layout.IQuestionPanelFactory;
import org.obiba.onyx.quartz.core.wicket.layout.QuestionPanel;

public class DefaultQuestionPanelFactory implements IQuestionPanelFactory {

  public QuestionPanel createPanel(String id, Question question) {
    if(question.isMultiple()) {
      return new MultipleChoiceQuestionPanel(id, new Model(question));
    }
    else {
      return new SingleChoiceQuestionPanel(id, new Model(question));
    }
  }

  public String getName() {
    return "quartz." + getClass().getSimpleName();
  }

}
