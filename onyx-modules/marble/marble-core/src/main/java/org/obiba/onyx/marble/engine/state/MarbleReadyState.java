/**
 * 
 */
package org.obiba.onyx.marble.engine.state;

import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.ActionDefinitionBuilder;
import org.obiba.onyx.engine.state.TransitionEvent;
import org.obiba.onyx.marble.core.service.ActiveConsentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * Marble Ready State, goes there each time a state is cancelled.
 * 
 * @author Yannick Marcon
 * 
 */
public class MarbleReadyState extends AbstractMarbleStageState implements InitializingBean {

  private static final Logger log = LoggerFactory.getLogger(MarbleReadyState.class);

  private ActiveConsentService activeConsentService;

  public void setActiveConsentService(ActiveConsentService activeConsentService) {
    this.activeConsentService = activeConsentService;
  }

  public void afterPropertiesSet() throws Exception {
    addAction(ActionDefinitionBuilder.START_ACTION);
  }

  @Override
  public void execute(Action action) {
    super.execute(action);
    log.info("Marble Stage {} is starting", super.getStage().getName());
    activeConsentService.setConsent(false);
    castEvent(TransitionEvent.START);
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