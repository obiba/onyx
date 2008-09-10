package org.obiba.onyx.jade.engine.state;

import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.ActionDefinition;
import org.obiba.onyx.engine.ActionDefinitionBuilder;
import org.obiba.onyx.engine.ActionType;
import org.obiba.onyx.engine.state.ITransitionListener;
import org.obiba.onyx.engine.state.TransitionEvent;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunRefusalReason;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * Jade Waiting State, goes there if a dependency is not satisfied.
 * @author Yannick Marcon
 * 
 */
public class JadeWaitingState extends AbstractJadeStageState implements InitializingBean, ITransitionListener {

  private static final Logger log = LoggerFactory.getLogger(JadeWaitingState.class);

  public void afterPropertiesSet() throws Exception {
    ActionDefinition def = ActionDefinitionBuilder.create(ActionType.SKIP, "Skip").setDescription("You may explain why this stage is skipped.").getActionDefinition();
    for(InstrumentRunRefusalReason reason : InstrumentRunRefusalReason.values()) {
      def.addReason(reason.toString());
      if(def.getDefaultReason() == null) def.setDefaultReason(reason.toString());
    }
    addAction(def);
  }

  @Override
  public void skip(Action action) {
    super.skip(action);
    log.info("Jade Stage {} is skipping", super.getStage().getName());
    castEvent(TransitionEvent.SKIP);
  }

  @Override
  public String getMessage() {
    return "Waiting";
  }

  @Override
  public String getName() {
    return "Waiting";
  }

}