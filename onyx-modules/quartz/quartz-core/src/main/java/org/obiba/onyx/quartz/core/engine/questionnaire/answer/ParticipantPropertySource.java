package org.obiba.onyx.quartz.core.engine.questionnaire.answer;

import org.obiba.onyx.util.data.Data;

public class ParticipantPropertySource extends AnswerSource {

  private static final long serialVersionUID = 5625713001098059689L;

  private String property;

  public ParticipantPropertySource() {
  }

  public String getProperty() {
    return property;
  }

  public void setProperty(String property) {
    this.property = property;
  }

  public Data getData() {
    // TODO Auto-generated method stub
    return null;
  }

}
