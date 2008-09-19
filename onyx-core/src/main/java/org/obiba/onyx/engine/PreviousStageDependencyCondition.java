package org.obiba.onyx.engine;

import javax.persistence.Entity;

import org.obiba.onyx.core.service.ActiveInterviewService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity
public class PreviousStageDependencyCondition extends StageDependencyCondition {

  private static final long serialVersionUID = 1L;

  private static final Logger log = LoggerFactory.getLogger(PreviousStageDependencyCondition.class);

  private String stageName;

  public PreviousStageDependencyCondition() {
  }

  public PreviousStageDependencyCondition(String name) {
    this.stageName = name;
  }

  @Override
  public Boolean isDependencySatisfied(ActiveInterviewService activeInterviewService) {
    return (activeInterviewService.getStageExecution(stageName).isCompleted());
  }

  @Override
  public boolean isDependentOn(String stageName) {
    return this.stageName.equals(stageName);
  }

  public String getStageName() {
    return stageName;
  }

  public void setStageName(String stageName) {
    this.stageName = stageName;
  }
}
