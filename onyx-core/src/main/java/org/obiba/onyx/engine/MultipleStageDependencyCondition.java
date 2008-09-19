package org.obiba.onyx.engine;

import org.obiba.onyx.core.service.ActiveInterviewService;

public class MultipleStageDependencyCondition extends StageDependencyCondition {

  private static final long serialVersionUID = 1L;

  private Operator operator;

  @Override
  public Boolean isDependencySatisfied(ActiveInterviewService activeInterviewService) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean isDependentOn(String stageName) {
    return false;
  }

}
