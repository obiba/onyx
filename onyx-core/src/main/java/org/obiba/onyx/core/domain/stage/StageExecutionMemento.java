package org.obiba.onyx.core.domain.stage;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.obiba.core.domain.AbstractEntity;
import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.engine.Action;

@Entity
public class StageExecutionMemento extends AbstractEntity {

  private static final long serialVersionUID = 8309472904104798783L;

  private String stage;

  @ManyToOne
  @JoinColumn(name = "interview_id")
  private Interview interview;
  
  @ManyToOne
  @JoinColumn(name = "action_id")
  private Action action;

  private String state;

  public StageExecutionMemento() {
  }

  public String getStage() {
    return stage;
  }

  public void setStage(String stage) {
    this.stage = stage;
  }

  public Interview getInterview() {
    return interview;
  }

  public void setInterview(Interview interview) {
    this.interview = interview;
  }

  public Action getAction() {
    return action;
  }

  public void setAction(Action action) {
    this.action = action;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

}
