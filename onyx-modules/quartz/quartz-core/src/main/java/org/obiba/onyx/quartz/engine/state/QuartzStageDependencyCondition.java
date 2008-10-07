package org.obiba.onyx.quartz.engine.state;

import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.engine.StageDependencyCondition;
import org.obiba.onyx.engine.state.IStageExecution;

public class QuartzStageDependencyCondition extends StageDependencyCondition {

  private String stageName;

  public QuartzStageDependencyCondition() {
  }

  public QuartzStageDependencyCondition(String name) {
    this.stageName = name;
  }

  @Override
  public Boolean isDependencySatisfied(ActiveInterviewService activeInterviewService) {
    IStageExecution stageExecution = activeInterviewService.getStageExecution(stageName);

    if(!stageExecution.isCompleted()) return null;
    else
      return true;
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
