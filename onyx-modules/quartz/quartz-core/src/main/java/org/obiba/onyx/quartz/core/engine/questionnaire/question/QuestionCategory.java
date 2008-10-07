package org.obiba.onyx.quartz.core.engine.questionnaire.question;

import java.io.Serializable;

public class QuestionCategory implements Serializable {

  private static final long serialVersionUID = 5244745063169629959L;

  private String name;

  private Question question;

  private Category codeAnswer;

  private Boolean selected;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Question getQuestion() {
    return question;
  }

  public void setQuestion(Question question) {
    this.question = question;
  }

  public Category getCodeAnswer() {
    return codeAnswer;
  }

  public void setCodeAnswer(Category codeAnswer) {
    this.codeAnswer = codeAnswer;
  }

  public Boolean getSelected() {
    return selected;
  }

  public void setSelected(Boolean selected) {
    this.selected = selected;
  }

}
