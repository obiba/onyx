/**
 * 
 */
package org.obiba.onyx.jade.engine.state;

import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.ActionDefinition;
import org.obiba.onyx.engine.ActionType;
import org.obiba.onyx.engine.state.AbstractStageState;
import org.obiba.onyx.engine.state.IStageExecution;
import org.obiba.onyx.engine.state.TransitionEvent;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunRefusalReason;
import org.obiba.onyx.util.data.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * Jade Ready State, goes there each time a state is cancelled.
 * @author Yannick Marcon
 *
 */
public class JadeReadyState extends AbstractJadeStageState implements InitializingBean {

  private static final Logger log = LoggerFactory.getLogger(JadeReadyState.class);
  
  public void afterPropertiesSet() throws Exception {
    addAction(ActionDefinition.START_ACTION);
    ActionDefinition def = new ActionDefinition(ActionType.SKIP, "Skip", "Please explain why this stage is skipped.");
    for (InstrumentRunRefusalReason reason : InstrumentRunRefusalReason.values()) {
      def.addReason(reason.toString());
      if (def.getDefaultReason() == null)
        def.setDefaultReason(reason.toString());
    }
    addAction(def);
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
    castEvent(TransitionEvent.SKIP);
  }

  @Override
  public String getMessage() {
    return "Ready";
  }

  @Override
  public String getName() {
    return "Ready";
  }
}