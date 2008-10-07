package org.obiba.onyx.quartz.core.engine.questionnaire.condition;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;

public abstract class Condition implements Serializable {

  private List<Question> questions;

  public List<Question> getQuestions() {
    return questions != null ? questions : (questions = new ArrayList<Question>());
  }

  public void addQuestion(Question question) {
    if(question != null) {
      getQuestions().add(question);
      question.setCondition(this);
    }
  }

  public abstract boolean isToBeAnswered();

}
