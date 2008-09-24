package org.obiba.onyx.engine;

import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.engine.state.IStageExecution;
import org.obiba.onyx.util.data.Data;

/**
 * Specific Stage Dependency condition for Consent step
 * @author acarey
 */
public class ConsentStageDependencyCondition extends StageDependencyCondition {

  private static final long serialVersionUID = 1L;

  private String stageName;

  public ConsentStageDependencyCondition() {
  }

  public ConsentStageDependencyCondition(String name) {
    this.stageName = name;
  }

  /**
   * Returns a Boolean depending on the fact that the step is completed and also on its result
   * Null if not completed
   * True if completed and consented
   * False if completed and not consented 
   */
  @Override
  public Boolean isDependencySatisfied(ActiveInterviewService activeInterviewService) {
    IStageExecution consent = activeInterviewService.getStageExecution(stageName);
    if(consent != null) {
      if(!consent.isCompleted()) return null;
      else {
        Data consentData = consent.getData("Consent");
        if(consentData != null) {
          return (Boolean) consentData.getValue();
        } else
          return false;
      }
    }
    return null;
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
