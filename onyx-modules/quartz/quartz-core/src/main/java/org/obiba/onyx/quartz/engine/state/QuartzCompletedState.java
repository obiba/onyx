/**
 * State complete for Quartz module
 * 
 * @author acarey
 * 
 * Coming from states: inProgress
 * Possible forward states/actions/transitions: notApplicable, cancel, resume
 * On cancel and notApplicable transitions, current questionnaireParticipant is deleted from database (with its dependencies)
 */
package org.obiba.onyx.quartz.engine.state;

import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.ActionDefinition;
import org.obiba.onyx.engine.ActionDefinitionBuilder;
import org.obiba.onyx.engine.ActionType;
import org.obiba.onyx.engine.state.AbstractStageState;
import org.obiba.onyx.engine.state.TransitionEvent;
import org.obiba.onyx.quartz.core.domain.answer.QuestionnaireParticipant;
import org.obiba.onyx.quartz.core.service.QuestionnaireParticipantService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class QuartzCompletedState extends AbstractStageState implements InitializingBean {

  private static final Logger log = LoggerFactory.getLogger(QuartzCompletedState.class);

  private QuestionnaireParticipantService questionnaireParticipantService;

  public void setQuestionnaireParticipantService(QuestionnaireParticipantService questionnaireParticipantService) {
    this.questionnaireParticipantService = questionnaireParticipantService;
  }

  public void afterPropertiesSet() throws Exception {
    addAction(ActionDefinitionBuilder.create(ActionType.STOP, "Cancel").setDescription("You may explain why you are cancelling this stage.").getActionDefinition());
    addAction(ActionDefinitionBuilder.create(ActionType.EXECUTE, "Resume").getActionDefinition());
  }

  public String getName() {
    return "Quartz.Completed";
  }

  private void cancelQuestionnaireParticipant() {
    QuestionnaireParticipant questionnaireParticipant = questionnaireParticipantService.getLastQuestionnaireParticipant(activeInterviewService.getParticipant(), super.getStage().getName());
    questionnaireParticipantService.deleteQuestionnaireParticipant(questionnaireParticipant.getId());
  }

  @Override
  public void stop(Action action) {
    super.execute(action);
    log.info("Quartz Stage {} is cancelling", super.getStage().getName());
    cancelQuestionnaireParticipant();
    if(areDependenciesCompleted() != null && areDependenciesCompleted()) castEvent(TransitionEvent.CANCEL);
    else
      castEvent(TransitionEvent.INVALID);
  }

  @Override
  protected boolean wantTransitionEvent(TransitionEvent transitionEvent) {
    if(transitionEvent.equals(TransitionEvent.NOTAPPLICABLE)) cancelQuestionnaireParticipant();
    return super.wantTransitionEvent(transitionEvent);
  }

  @Override
  public boolean isCompleted() {
    return true;
  }

  @Override
  public ActionType getStartingActionType() {
    return ActionType.EXECUTE;
  }
}
