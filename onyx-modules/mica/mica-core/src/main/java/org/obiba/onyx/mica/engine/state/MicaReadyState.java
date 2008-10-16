/**
 * 
 */
package org.obiba.onyx.mica.engine.state;

import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.ActionDefinitionBuilder;
import org.obiba.onyx.engine.state.TransitionEvent;
import org.obiba.onyx.mica.core.service.ActiveConclusionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * Mica Ready State, goes there each time a state is cancelled.
 * 
 * @author Meryam Belhiah
 * 
 */
public class MicaReadyState extends AbstractMicaStageState implements InitializingBean {

  private static final Logger log = LoggerFactory.getLogger(MicaReadyState.class);

  private ActiveConclusionService activeConclusionService;

  public void setActiveConsentService(ActiveConclusionService activeConsentService) {
    this.activeConclusionService = activeConsentService;
  }

  public void afterPropertiesSet() throws Exception {
    addAction(ActionDefinitionBuilder.START_ACTION);
  }

  @Override
  public void execute(Action action) {
    super.execute(action);
    log.info("Mica Stage {} is starting", super.getStage().getName());
    activeConclusionService.setConclusion(false);
    castEvent(TransitionEvent.START);
  }

  public String getName() {
    return "Mica.Ready";
  }

}