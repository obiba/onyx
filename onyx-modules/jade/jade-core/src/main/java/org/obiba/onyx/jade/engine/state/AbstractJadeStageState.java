package org.obiba.onyx.jade.engine.state;

import org.obiba.onyx.engine.state.AbstractStageState;
import org.obiba.onyx.engine.state.IStageExecution;
import org.obiba.onyx.util.data.Data;

public abstract class AbstractJadeStageState extends AbstractStageState {

  @Override
  protected boolean areDependenciesCompleted() {
    boolean completed = super.areDependenciesCompleted();
    if(completed) {
      IStageExecution consent = getDependsOnStageExecutions("marble:CON");
      if(consent != null) {
        Data consentData = consent.getData("Consent");
        if(consentData != null) {
          Boolean value = consentData.getValue();
          return value;
        } else
          return false;
      }
    }
    return completed;
  }

}
