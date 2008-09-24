package org.obiba.onyx.engine;

import org.obiba.onyx.core.service.ActiveInterviewService;

/**
 * Most simple case of Previous Stage Dependency Condition
 * @author acarey
 */
public class PreviousStageDependencyCondition extends StageDependencyCondition {

  private static final long serialVersionUID = 1L;

  private String stageName;

  public PreviousStageDependencyCondition() {
  }

  public PreviousStageDependencyCondition(String name) {
    this.stageName = name;
  }

  /**
   * returns true if stage execution is completed
   * returns null if it's not completed
   */
  @Override
  public Boolean isDependencySatisfied(ActiveInterviewService activeInterviewService) {
    if (!activeInterviewService.getStageExecution(stageName).isCompleted())
      return null;
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
