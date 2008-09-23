package org.obiba.onyx.engine;

import javax.persistence.Entity;

import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.engine.state.IStageExecution;
import org.obiba.onyx.util.data.Data;

public class ConsentStageDependencyCondition extends StageDependencyCondition {

  private static final long serialVersionUID = 1L;

  private String stageName;

  public ConsentStageDependencyCondition() {
  }

  public ConsentStageDependencyCondition(String name) {
    this.stageName = name;
  }

  @Override
  public Boolean isDependencySatisfied(ActiveInterviewService activeInterviewService) {
    IStageExecution consent = activeInterviewService.getStageExecution(stageName);
    if (consent != null){
      if (!consent.isCompleted()) return null;
      else {
        Data consentData = consent.getData("Consent");
        if (consentData != null) {
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
