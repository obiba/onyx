/**
 * State inProgress for Quartz module
 * 
 * @author acarey
 * 
 * Coming from states: ready
 * Possible forward states/actions/transitions: cancel, complete, interrupt
 * On cancel transition, current questionnaireParticipant is deleted from database (with its dependencies)
 */
package org.obiba.onyx.quartz.engine.state;

import org.apache.wicket.Component;
import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.ActionDefinition;
import org.obiba.onyx.engine.ActionDefinitionBuilder;
import org.obiba.onyx.engine.ActionType;
import org.obiba.onyx.engine.state.AbstractStageState;
import org.obiba.onyx.engine.state.TransitionEvent;
import org.obiba.onyx.quartz.core.domain.answer.QuestionnaireParticipant;
import org.obiba.onyx.quartz.core.service.QuestionnaireParticipantService;
import org.obiba.onyx.quartz.core.wicket.QuartzPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class QuartzInProgressState extends AbstractStageState implements InitializingBean {

  private static final Logger log = LoggerFactory.getLogger(QuartzInProgressState.class);

  private QuestionnaireParticipantService questionnaireParticipantService;

  public void setQuestionnaireParticipantService(QuestionnaireParticipantService questionnaireParticipantService) {
    this.questionnaireParticipantService = questionnaireParticipantService;
  }

  public void afterPropertiesSet() throws Exception {
    ActionDefinition def = ActionDefinitionBuilder.create(ActionType.STOP, "Cancel").setDescription("You may explain why you are cancelling this stage.").getActionDefinition();
    /*
     * for(QuestionnaireParticipantRefusalReason reason : QuestionnaireParticipantRefusalReason.values()) {
     * def.addReason(reason.toString()); if(def.getDefaultReason() == null) def.setDefaultReason(reason.toString()); }
     */
    addAction(def);
    def = ActionDefinitionBuilder.create(ActionType.INTERRUPT, "Interrupt").getActionDefinition();
    addAction(def);
    addSystemAction(ActionDefinitionBuilder.COMPLETE_ACTION);
  }

  public String getName() {
    return "Quartz.InProgress";
  }

  public Component getWidget(String id) {
    return new QuartzPanel(id, getStage());
  }
  
  private void cancelQuestionnaireParticipant() {
    QuestionnaireParticipant questionnaireParticipant = questionnaireParticipantService.getLastQuestionnaireParticipant(activeInterviewService.getParticipant(), getStage().getName());
    questionnaireParticipantService.deleteQuestionnaireParticipant(questionnaireParticipant.getId());
  }

  @Override
  public void stop(Action action) {
    log.info("Quartz Stage {} is stopping", super.getStage().getName());
    cancelQuestionnaireParticipant();
    if(areDependenciesCompleted() != null && areDependenciesCompleted()) {
      castEvent(TransitionEvent.CANCEL);
    } else {
      castEvent(TransitionEvent.INVALID);
    }
  }

  @Override
  public void complete(Action action) {
    log.info("Quartz Stage {} is completing", super.getStage().getName());
    castEvent(TransitionEvent.COMPLETE);
  }

  @Override
  public boolean isInteractive() {
    return true;
  }

}
