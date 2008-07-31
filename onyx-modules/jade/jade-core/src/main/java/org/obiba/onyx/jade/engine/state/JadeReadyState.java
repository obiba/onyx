/**
 * 
 */
package org.obiba.onyx.jade.engine.state;

import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.ActionDefinition;
import org.obiba.onyx.engine.ActionType;
import org.obiba.onyx.engine.state.AbstractStageState;
import org.obiba.onyx.engine.state.TransitionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JadeReadyState extends AbstractStageState {

  private static final Logger log = LoggerFactory.getLogger(JadeReadyState.class);

  public JadeReadyState() {
    addAction(new ActionDefinition(ActionType.EXECUTE));
    addAction(new ActionDefinition(ActionType.SKIP));
  }

  @Override
  public void execute(Action action) {
    super.execute(action);
    log.info("Jade Stage {} is starting", super.getStage().getName());
    castEvent(TransitionEvent.START);
  }

  @Override
  public void skip(Action action) {
    super.skip(action);
    log.info("Jade Stage {} is skipping", super.getStage().getName());
    // castEvent(TransitionEvent.SKIP);
  }

}