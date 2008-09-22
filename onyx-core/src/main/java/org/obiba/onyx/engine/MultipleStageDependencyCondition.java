package org.obiba.onyx.engine;

import javax.persistence.Entity;

import org.obiba.onyx.core.service.ActiveInterviewService;

@Entity
public class MultipleStageDependencyCondition extends StageDependencyCondition {

  private static final long serialVersionUID = 1L;

  private Operator operator;

  private StageDependencyCondition leftStageDependencyCondition;

  private StageDependencyCondition rightStageDependencyCondition;

  @Override
  public Boolean isDependencySatisfied(ActiveInterviewService activeInterviewService) {
    Boolean leftResult = leftStageDependencyCondition.isDependencySatisfied(activeInterviewService);
    Boolean rightResult = rightStageDependencyCondition.isDependencySatisfied(activeInterviewService);
    
    if (leftResult != null && rightResult != null){
      if (operator.equals(Operator.AND)) return (leftResult && rightResult);
      else return (leftResult || rightResult);
    } else {
      if (operator.equals(Operator.AND)){
        if (leftResult != null && leftResult == false) return false;
        if (rightResult != null && rightResult == false) return false;
        return null;
      } else {
        if (leftResult != null) return leftResult;
        if (rightResult != null) return rightResult;
        return null;
      }
    }
  }

  @Override
  public boolean isDependentOn(String stageName) {
    return (leftStageDependencyCondition.isDependentOn(stageName) || rightStageDependencyCondition.isDependentOn(stageName));
  }
}
