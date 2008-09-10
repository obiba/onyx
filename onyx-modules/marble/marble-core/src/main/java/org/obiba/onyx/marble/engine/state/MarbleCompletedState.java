/**
 * 
 */
package org.obiba.onyx.marble.engine.state;

import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.ActionDefinitionBuilder;
import org.obiba.onyx.engine.state.TransitionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class MarbleCompletedState extends AbstractMarbleStageState implements InitializingBean {

  private static final Logger log = LoggerFactory.getLogger(MarbleCompletedState.class);

  public void afterPropertiesSet() throws Exception {
    addAction(ActionDefinitionBuilder.CANCEL_ACTION);
  }

  @Override
  public void stop(Action action) {
    super.execute(action);
    log.info("Marble Stage {} is cancelling", super.getStage().getName());
    castEvent(TransitionEvent.CANCEL);
  }

  @Override
  public String getMessage() {
    return "Completed";
  }

  @Override
  public boolean isCompleted() {
    return true;
  }

  @Override
  public String getName() {
    return "Completed";
  }

}