package org.obiba.onyx.engine;

import org.obiba.onyx.core.service.ActiveInterviewService;

public class InverseStageDependencyCondition extends StageDependencyCondition {

  private static final long serialVersionUID = 1L;

  private StageDependencyCondition stageDependencyCondition;

  @Override
  public Boolean isDependencySatisfied(ActiveInterviewService activeInterviewService) {
    if (stageDependencyCondition.isDependencySatisfied(activeInterviewService) == null)
      return false;
    return (!(stageDependencyCondition.isDependencySatisfied(activeInterviewService)));
  }

  @Override
  public boolean isDependentOn(String stageName) {
    return stageDependencyCondition.isDependentOn(stageName);
  }

  public void setStageDependencyCondition(StageDependencyCondition stageDependencyCondition) {
    this.stageDependencyCondition = stageDependencyCondition;
  }
}