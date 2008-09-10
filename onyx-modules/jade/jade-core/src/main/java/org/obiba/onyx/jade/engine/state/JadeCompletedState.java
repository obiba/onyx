/**
 * 
 */
package org.obiba.onyx.jade.engine.state;

import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.ActionDefinition;
import org.obiba.onyx.engine.ActionDefinitionBuilder;
import org.obiba.onyx.engine.ActionType;
import org.obiba.onyx.engine.state.TransitionEvent;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunRefusalReason;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class JadeCompletedState extends AbstractJadeStageState implements InitializingBean {

  private static final Logger log = LoggerFactory.getLogger(JadeCompletedState.class);

  public void afterPropertiesSet() throws Exception {
    ActionDefinition def = ActionDefinitionBuilder.create(ActionType.STOP, "Cancel").setDescription("You may explain why you are cancelling this stage.").getActionDefinition();
    for (InstrumentRunRefusalReason reason : InstrumentRunRefusalReason.values()) {
      def.addReason(reason.toString());
      if (def.getDefaultReason() == null)
        def.setDefaultReason(reason.toString());
    }
    addAction(def);
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