/**
 * 
 */
package org.obiba.onyx.jade.engine.state;

import org.apache.wicket.Component;
import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.ActionDefinition;
import org.obiba.onyx.engine.ActionDefinitionBuilder;
import org.obiba.onyx.engine.ActionType;
import org.obiba.onyx.engine.state.TransitionEvent;
import org.obiba.onyx.jade.core.domain.run.InstrumentRun;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunRefusalReason;
import org.obiba.onyx.jade.core.service.ActiveInstrumentRunService;
import org.obiba.onyx.jade.core.wicket.JadePanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class JadeInProgressState extends AbstractJadeStageState implements InitializingBean {

  private static final Logger log = LoggerFactory.getLogger(JadeInProgressState.class);

  private ActiveInstrumentRunService activeInstrumentRunService;

  public void setActiveInstrumentRunService(ActiveInstrumentRunService activeInstrumentRunService) {
    this.activeInstrumentRunService = activeInstrumentRunService;
  }

  public void afterPropertiesSet() throws Exception {
    ActionDefinition def = ActionDefinitionBuilder.create(ActionType.STOP, "Cancel").setDescription("You may explain why you are cancelling this stage.").getActionDefinition();
    for(InstrumentRunRefusalReason reason : InstrumentRunRefusalReason.values()) {
      def.addReason(reason.toString());
      if(def.getDefaultReason() == null) def.setDefaultReason(reason.toString());
    }
    addAction(def);
    addSystemAction(ActionDefinitionBuilder.COMPLETE_ACTION);
  }

  public Component getWidget(String id) {
    return new JadePanel(id, getStage());
  }

  @Override
  public void stop(Action action) {
    log.info("Jade Stage {} is stopping", super.getStage().getName());
    // Invalidate current instrument run
    InstrumentRun run = activeInstrumentRunService.getInstrumentRun();
    if(run != null) {
      activeInstrumentRunService.cancel();
      activeInstrumentRunService.reset();
    }
    if(areDependenciesCompleted()) {
      castEvent(TransitionEvent.CANCEL);
    } else {
      castEvent(TransitionEvent.INVALID);
    }
  }

  @Override
  public void complete(Action action) {
    log.info("Jade Stage {} is completing", super.getStage().getName());
    // Finish current instrument run
    InstrumentRun run = activeInstrumentRunService.getInstrumentRun();
    if(run != null) {
      activeInstrumentRunService.complete();
      activeInstrumentRunService.reset();
    }
    castEvent(TransitionEvent.COMPLETE);
  }

  @Override
  public boolean isInteractive() {
    return true;
  }

  @Override
  public String getMessage() {
    return "In Progress";
  }

  @Override
  public String getName() {
    return "InProgress";
  }

}