package org.obiba.onyx.core.domain.stage;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.obiba.core.domain.AbstractEntity;
import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.engine.Stage;

@Entity
public class StageExecutionMemento extends AbstractEntity {

  private static final long serialVersionUID = 8309472904104798783L;

  @ManyToOne
  @JoinColumn(name="stage_id")
  private Stage stage;

  @ManyToOne
  @JoinColumn(name="interview_id")
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
