package org.obiba.onyx.engine;

import org.obiba.onyx.core.service.ActiveInterviewService;

public abstract class StageDependencyCondition {

  public abstract Boolean isDependencySatisfied(ActiveInterviewService activeInterviewService);

  public abstract boolean isDependentOn(String stageName);

}
