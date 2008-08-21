/**
 * 
 */
package org.obiba.onyx.jade.engine.state;

import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.ActionDefinition;
import org.obiba.onyx.engine.state.AbstractStageState;
import org.obiba.onyx.engine.state.TransitionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * Jade Skipped State.
 * @author Yannick Marcon
 *
 */
public class JadeSkippedState extends AbstractStageState implements InitializingBean {

  private static final Logger log = LoggerFactory.getLogger(JadeSkippedState.class);

  public void afterPropertiesSet() throws Exception {
    addAction(ActionDefinition.CANCEL_ACTION);
    addAction(ActionDefinition.COMMENT_ACTION);
  }

  @Override
  public void stop(Action action) {
    super.execute(action);
    log.info("Jade Stage {} is cancelling", super.getStage().getName());
    if(areDependenciesCompleted()) {
      castEvent(TransitionEvent.CANCEL);
    } else {
      castEvent(TransitionEvent.INVALID);
    }
  }
  
  protected void onDependencyTransition() {
    // do nothing when skipped
  }

  @Override
  public String getMessage() {
    return "Skipped";
  }

}