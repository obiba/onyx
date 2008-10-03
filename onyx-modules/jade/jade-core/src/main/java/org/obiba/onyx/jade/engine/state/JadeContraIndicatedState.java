package org.obiba.onyx.jade.engine.state;

import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.ActionDefinition;
import org.obiba.onyx.engine.ActionDefinitionBuilder;
import org.obiba.onyx.engine.ActionType;
import org.obiba.onyx.engine.state.TransitionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class JadeContraIndicatedState extends AbstractJadeStageState implements InitializingBean {

  private static final Logger log = LoggerFactory.getLogger(JadeCompletedState.class);

  public void afterPropertiesSet() throws Exception {
    ActionDefinition def = ActionDefinitionBuilder.create(ActionType.STOP, "Cancel").setDescription("You may explain why you are cancelling this stage.").getActionDefinition();
    addAction(def);
  }

  @Override
  public void stop(Action action) {
    super.execute(action);
    log.info("Jade Stage {} is canceling", super.getStage().getName());
    if(areDependenciesCompleted() != null && areDependenciesCompleted()) {
      castEvent(TransitionEvent.CANCEL);
    } else {
      castEvent(TransitionEvent.INVALID);
    }
  }

  @Override
  public boolean isCompleted() {
    return true;
  }

  public String getName() {
    return "Jade.ContraIndicated";
  }

  @Override
  protected boolean wantTransitionEvent(TransitionEvent transitionEvent) {
    if(transitionEvent.equals(TransitionEvent.CONTRAINDICATED) || transitionEvent.equals(TransitionEvent.VALID) || transitionEvent.equals(TransitionEvent.NOTAPPLICABLE)) return false;
    else
      return true;
  }

  @Override
  public ActionType getStartingActionType() {
    return ActionType.EXECUTE;
  }
}
