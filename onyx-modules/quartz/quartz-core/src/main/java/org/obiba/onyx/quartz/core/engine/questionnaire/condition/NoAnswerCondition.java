package org.obiba.onyx.quartz.core.engine.questionnaire.condition;

public class NoAnswerCondition extends Condition {

  private static final long serialVersionUID = -7934445960755750180L;

  private Condition condition;

  public Condition getCondition() {
    return condition;
  }

  public void setCondition(Condition condition) {
    this.condition = condition;
  }

  public boolean isToBeAnswered() {
    return !condition.isToBeAnswered();
  }

}
