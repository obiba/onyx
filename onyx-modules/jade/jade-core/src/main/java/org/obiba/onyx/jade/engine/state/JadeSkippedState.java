/**
 * 
 */
package org.obiba.onyx.jade.engine.state;

import java.util.Locale;

import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.ActionDefinitionBuilder;
import org.obiba.onyx.engine.ActionType;
import org.obiba.onyx.engine.state.AbstractStageState;
import org.obiba.onyx.engine.state.IStageExecution;
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
    addAction(ActionDefinitionBuilder.CANCEL_SKIPPED_ACTION);
  }

  @Override
  public void stop(Action action) {
    super.execute(action);
    log.info("Jade Stage {} is cancelling", super.getStage().getName());
    if (areDependenciesCompleted() != null && areDependenciesCompleted()) {
      castEvent(TransitionEvent.CANCEL);
    } else {
      castEvent(TransitionEvent.INVALID);
    }
  }

  @Override
  public void onTransition(IStageExecution execution, TransitionEvent event) {
    // do nothing when skipped
    if (event.equals(TransitionEvent.CONTRAINDICATED)) super.onTransition(execution, event);
  }

  @Override
  public String getMessage() {
    Locale locale = userSessionService.getLocale();

    String state = getName();
    String reason = (getReason() != null) ? getReason().getEventReason() : null;

    String message = context.getMessage(state, null, locale);

    if (reason != null) {
      message += " (" + context.getMessage(reason, null, locale) + ")";
    }

    return message;
  }

  @Override
  public boolean isCompleted() {
    return true;
  }
  
  public String getName() {
    return "Jade.Skipped";
  }

  @Override
  public ActionType getStartingActionType() {
    return ActionType.SKIP;
  }
  
}