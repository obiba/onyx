package org.obiba.onyx.core.domain.stage;

import javax.persistence.Entity;

import org.obiba.core.domain.AbstractEntity;
import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.engine.Stage;

@Entity
public class StageExecutionMemento extends AbstractEntity {

  private static final long serialVersionUID = 8309472904104798783L;

  private Stage stage;

  private Interview interview;
  
  private String state;

  public StageExecutionMemento() {
  }

  public Stage getStage() {
    return stage;
  }

  public void setStage(Stage stage) {
    this.stage = stage;
  }

  public Interview getInterview() {
    return interview;
  }

  public void setInterview(Interview interview) {
    this.interview = interview;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

}
