/**
 * 
 */
package org.obiba.onyx.jade.engine.state;

import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.ActionDefinition;
import org.obiba.onyx.engine.ActionDefinitionBuilder;
import org.obiba.onyx.engine.ActionType;
import org.obiba.onyx.engine.state.AbstractStageState;
import org.obiba.onyx.engine.state.TransitionEvent;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunRefusalReason;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunStatus;
import org.obiba.onyx.jade.core.service.InstrumentRunService;
import org.obiba.onyx.jade.core.service.InstrumentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class JadeCompletedState extends AbstractStageState implements InitializingBean {

  private static final Logger log = LoggerFactory.getLogger(JadeCompletedState.class);

  private InstrumentRunService instrumentRunService;
  
  private InstrumentService instrumentService;
  
  public void setInstrumentRunService(InstrumentRunService instrumentRunService) {
    this.instrumentRunService = instrumentRunService;
  }

  public void setInstrumentService(InstrumentService instrumentService) {
    this.instrumentService = instrumentService;
  }

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
    cancelInstrumentRun();
    if (areDependenciesCompleted() != null && areDependenciesCompleted()) {
      castEvent(TransitionEvent.CANCEL);
    } else {
      castEvent(TransitionEvent.INVALID);
    }
  }
  
  @Override
  protected boolean wantTransitionEvent(TransitionEvent transitionEvent) {
    if(transitionEvent.equals(TransitionEvent.NOTAPPLICABLE)) {
      cancelInstrumentRun();
    }
    return super.wantTransitionEvent(transitionEvent);
  }

  private void cancelInstrumentRun() {
    InstrumentType instrumentType = instrumentService.getInstrumentType(getStage().getName());
    Participant participant = activeInterviewService.getParticipant();
    instrumentRunService.setInstrumentRunStatus(instrumentRunService.getLastCompletedInstrumentRun(participant, instrumentType), InstrumentRunStatus.CANCELED);
  }
  
  @Override
  public boolean isCompleted() {
    return true;
  }

  public String getName() {
    return "Jade.Completed";
  }

  @Override
  public ActionType getStartingActionType() {
    return ActionType.EXECUTE;
  }
}