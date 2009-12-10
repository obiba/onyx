/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.engine.state;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.wicket.Component;
import org.obiba.onyx.core.domain.participant.InterviewStatus;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.ActionDefinition;
import org.obiba.onyx.engine.ActionDefinitionConfiguration;
import org.obiba.onyx.engine.ActionType;
import org.obiba.onyx.engine.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.support.DefaultMessageSourceResolvable;

/**
 * Helper class for implementing {@link IStageExecution}.
 */
public abstract class AbstractStageState implements IStageExecution, ITransitionListener, InitializingBean {

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(AbstractStageState.class);

  private ITransitionEventSink eventSink;

  private Stage stage;

  private Set<ActionType> userActions = new LinkedHashSet<ActionType>();

  private Set<ActionType> systemActions = new LinkedHashSet<ActionType>();;

  private ActionDefinitionConfiguration actionDefinitionConfig;

  protected ActiveInterviewService activeInterviewService;

  /**
   * The reason the stage is in its current state (i.e., the action that caused the stage to transition to this state).
   */
  private Action reason;

  public void setActiveInterviewService(ActiveInterviewService activeInterviewService) {
    this.activeInterviewService = activeInterviewService;
  }

  public void setActionDefinitionConfiguration(ActionDefinitionConfiguration actionDefinitionConfig) {
    this.actionDefinitionConfig = actionDefinitionConfig;
  }

  // make sure there is no possibility for getting out of this stage final state.
  public void afterPropertiesSet() throws Exception {
    if(!isFinal()) {
      addUserActions(userActions);
      addSystemActions(systemActions);
    }
  }

  public void setStage(Stage stage) {
    this.stage = stage;
  }

  public Stage getStage() {
    return stage;
  }

  public void onTransition(IStageExecution execution, TransitionEvent event) {
    Boolean var = areDependenciesCompleted();

    StageState currentState = StageState.valueOf(getName());

    // ONYX-936 : Update current stage state only if no data has been collected for that stage, i.e. stage state equals
    // Contraindicated, NotApplicable, Ready, Waiting.
    if(currentState == StageState.Contraindicated || currentState == StageState.NotApplicable || currentState == StageState.Ready || currentState == StageState.Waiting) {
      if(var == null) {
        // ONYX-383 ignore if master stage is interactive and this stage is completed (waiting from a stable state from
        // master stage).
        if((!execution.isInteractive() || !isCompleted()) && wantTransitionEvent(TransitionEvent.INVALID)) {
          castEvent(TransitionEvent.INVALID);
        }
      } else if(var == true && wantTransitionEvent(TransitionEvent.VALID)) {
        castEvent(TransitionEvent.VALID);
      } else if(var == false && wantTransitionEvent(TransitionEvent.NOTAPPLICABLE)) {
        castEvent(TransitionEvent.NOTAPPLICABLE);
      }
    }
  }

  protected Boolean areDependenciesCompleted() {
    if(stage.getStageDependencyCondition() != null) {
      return stage.getStageDependencyCondition().isDependencySatisfied(stage, activeInterviewService);
    }
    return true;
  }

  protected void castEvent(TransitionEvent event) {
    eventSink.castEvent(event);
  }

  public void setEventSink(ITransitionEventSink eventSink) {
    this.eventSink = eventSink;
  }

  public List<ActionDefinition> getActionDefinitions() {
    return buildActionDefinitionList(userActions);
  }

  public ActionDefinition getActionDefinition(ActionType type) {
    ActionDefinition definition = actionDefinitionConfig.getActionDefinition(type, getName(), getStage().getModule(), getStage().getName());
    checkDefinitionForType(definition, type);
    return definition;
  }

  public ActionDefinition getSystemActionDefinition(ActionType type) {
    ActionDefinition definition = actionDefinitionConfig.getActionDefinition(type, getName(), getStage().getModule(), getStage().getName());
    checkDefinitionForType(definition, type);
    return definition;
  }

  public List<ActionDefinition> getSystemActionDefinitions() {
    return buildActionDefinitionList(systemActions);
  }

  // if final stage state, complete the interview
  public void onEntry(TransitionEvent event) {
    if(isFinal()) {
      activeInterviewService.setStatus(InterviewStatus.COMPLETED);
    }
  }

  public void onExit(TransitionEvent event) {
  }

  public void execute(Action action) {
  }

  public void interrupt(Action action) {
  }

  public void skip(Action action) {
  }

  public void start(Action action) {
  }

  public void stop(Action action) {
  }

  public void complete(Action action) {
  }

  public Component getWidget(String id) {
    return null;
  }

  public boolean isCompleted() {
    return false;
  }

  public boolean isFinal() {
    return isCompleted() && stage != null && stage.isInterviewConclusion();
  }

  public boolean isInteractive() {
    return false;
  }

  public MessageSourceResolvable getMessage() {
    // Codes are: <fullname>, State.<name>, <name>
    return new DefaultMessageSourceResolvable(new String[] { getFullName(), "State." + getName(), getName() }, getName());
  }

  public void setReason(Action reason) {
    this.reason = reason;
  }

  public Action getReason() {
    return reason;
  }

  public MessageSourceResolvable getReasonMessage() {
    String reason = (getReason() != null) ? getReason().getEventReason() : null;
    if(reason != null) {
      return new DefaultMessageSourceResolvable(new String[] { getName() + "." + reason, reason }, reason);
    } else {
      return null;
    }
  }

  public ActionType getStartingActionType() {
    return null;
  }

  public Date getEndTime() {
    return null;
  }

  public Date getStartTime() {
    return null;
  }

  abstract protected void addUserActions(Set<ActionType> types);

  protected void addSystemActions(Set<ActionType> types) {

  }

  protected boolean wantTransitionEvent(TransitionEvent transitionEvent) {
    return true;
  }

  protected String getFullName() {
    return getStage().getModule() + "." + getName();
  }

  private List<ActionDefinition> buildActionDefinitionList(Set<ActionType> types) {
    List<ActionDefinition> definitions = new ArrayList<ActionDefinition>(types.size());
    for(ActionType type : types) {
      ActionDefinition definition = actionDefinitionConfig.getActionDefinition(type, getName(), getStage().getModule(), getStage().getName());
      checkDefinitionForType(definition, type);
      definitions.add(definition);
    }
    return definitions;
  }

  private void checkDefinitionForType(ActionDefinition definition, ActionType type) {
    if(definition == null) {
      throw new IllegalStateException("No ActionDefinition associated for ActionType " + type);
    }
  }
}
