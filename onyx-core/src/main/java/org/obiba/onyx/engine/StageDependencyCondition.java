package org.obiba.onyx.engine;

import org.obiba.onyx.core.service.ActiveInterviewService;

/**
 * Base class for Stage Dependency Conditions.
 * @author acarey
 * 
 */

public abstract class StageDependencyCondition {

  /**
   * Returns true if dependency is satisfied, false if it is not,
   * null if it's impossible to know whether it's right or wrong (step not done yet)
   * @param activeInterviewService
   * @return
   */
  public abstract Boolean isDependencySatisfied(ActiveInterviewService activeInterviewService);

  /**
   * returns true if this stageDependencyCondition depends on the specified stage 
   * @param stageName
   * @return
   */
  public abstract boolean isDependentOn(String stageName);

}
