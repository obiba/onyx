/**
 * 
 */
package org.obiba.onyx.mica.engine.state;

import org.apache.wicket.Component;
import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.ActionDefinitionBuilder;
import org.obiba.onyx.engine.state.TransitionEvent;
import org.obiba.onyx.mica.core.wicket.MicaPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class MicaInProgressState extends AbstractMicaStageState implements InitializingBean {

  private static final Logger log = LoggerFactory.getLogger(MicaInProgressState.class);

  public void afterPropertiesSet() throws Exception {
    addAction(ActionDefinitionBuilder.CANCEL_ACTION);
    addSystemAction(ActionDefinitionBuilder.COMPLETE_ACTION);
  }

  public Component getWidget(String id) {
    return new MicaPanel(id, getStage());
  }

  @Override
  public void complete(Action action) {
    log.info("Mica Stage {} is completing", super.getStage().getName());
    // Finish current conclusion
    getActiveConclusionService().validate();
    castEvent(TransitionEvent.COMPLETE);
  }

  @Override
  public void stop(Action action) {
    log.info("Mica Stage {} is canceling", super.getStage().getName());
    castEvent(TransitionEvent.CANCEL);
  }

  @Override
  public boolean isInteractive() {
    return true;
  }

  public String getName() {
    return "Mica.InProgress";
  }

}