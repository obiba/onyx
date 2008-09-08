/**
 * 
 */
package org.obiba.onyx.marble.engine.state;

import org.apache.wicket.Component;
import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.ActionDefinition;
import org.obiba.onyx.engine.state.TransitionEvent;
import org.obiba.onyx.marble.core.wicket.MarblePanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class MarbleInProgressState extends AbstractMarbleStageState implements InitializingBean {

  private static final Logger log = LoggerFactory.getLogger(MarbleInProgressState.class);
  
  public void afterPropertiesSet() throws Exception {
    addAction(ActionDefinition.CANCEL_ACTION);
    addSystemAction(ActionDefinition.COMPLETE_ACTION);
  }

  public Component getWidget(String id) {
    return new MarblePanel(id, getStage());
  }

  @Override
  public void complete(Action action) {
    log.info("Marble Stage {} is completing", super.getStage().getName());
    // Finish current consent
    getActiveConsentService().validate();
    castEvent(TransitionEvent.COMPLETE);
  }

  @Override
  public void stop(Action action) {
    log.info("Marble Stage {} is canceling", super.getStage().getName());
    castEvent(TransitionEvent.CANCEL);
  }
  
  @Override
  public boolean isInteractive() {
    return true;
  }

  @Override
  public String getMessage() {
    return "In Progress";
  }

  @Override
  public String getName() {
    return "InProgress";
  }

}