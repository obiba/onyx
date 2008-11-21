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
import java.util.List;

import org.apache.wicket.Component;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.ActionDefinition;
import org.obiba.onyx.engine.ActionType;
import org.obiba.onyx.engine.Stage;
import org.obiba.onyx.util.data.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.support.DefaultMessageSourceResolvable;

/**
 * Helper class for implementing {@link IStageExecution}.
 */
public abstract class AbstractStageState implements IStageExecution, ITransitionListener {

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(AbstractStageState.class);

  private ITransitionEventSink eventSink;

  private Stage stage;

  protected List<ActionDefinition> actionDefinitions = new ArrayList<ActionDefinition>();

  protected List<ActionDefinition> systemActionDefinitions = new ArrayList<ActionDefinition>();

  protected ActiveInterviewService activeInterviewService;

  /**
   * The reason the stage is in its current state (i.e., the action that caused the stage to transition to this state).
   */
  private Action reason;

  public void setActiveInterviewService(ActiveInterviewService activeInterviewService) {
    this.activeInterviewService = activeInterviewService;
  }

  public void setStage(Stage stage) {
    this.stage = stage;
  }

  public Stage getStage() {
    return stage;
  }

  public void onTransition(IStageExecution execution, TransitionEvent event) {
    Boolean var = areDependenciesCompleted();

    if(var == null) {
      if(wantTransitionEvent(TransitionEvent.INVALID)) castEvent(TransitionEvent.INVALID);
    } else if(var == true && wantTransitionEvent(TransitionEvent.VALID)) castEvent(TransitionEvent.VALID);
    else if(var == false && wantTransitionEvent(TransitionEvent.NOTAPPLICABLE)) castEvent(TransitionEvent.NOTAPPLICABLE);
  }

  protected Boolean areDependenciesCompleted() {
    if(stage.getStageDependencyCondition() != null) return stage.getStageDependencyCondition().isDependencySatisfied(activeInterviewService);
    return true;
  }

  protected void castEvent(TransitionEvent event) {
    eventSink.castEvent(event);
  }

  public void setEventSink(ITransitionEventSink eventSink) {
    this.eventSink = eventSink;
  }

  protected void addAction(ActionDefinition action) {
    actionDefinitions.add(action);
  }

  public List<ActionDefinition> getActionDefinitions() {
    return actionDefinitions;
  }

  public ActionDefinition getActionDefinition(ActionType type) {
    for(ActionDefinition def : actionDefinitions) {
      if(def.getType().equals(type)) return def;
    }
    return null;
  }

  public ActionDefinition getSystemActionDefinition(ActionType type) {
    for(ActionDefinition def : systemActionDefinitions) {
      if(def.getType().equals(type)) return def;
    }
    return null;
  }

  protected void addSystemAction(ActionDefinition action) {
    systemActionDefinitions.add(action);
  }

  public List<ActionDefinition> getSystemActionDefinitions() {
    return systemActionDefinitions;
  }

  public void onEntry(TransitionEvent event) {
  }

  public void onExit(TransitionEvent event) {
  }

  public void execute(Action action) {
  }

  public void interrupt(Action action) {
  }

  public void skip(Action action) {
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
    return false;
  }

  public boolean isInteractive() {
    return false;
  }

  public MessageSourceResolvable getMessage() {
    // Codes are: <fullname>, State.<name>, <name>
    return new DefaultMessageSourceResolvable(new String[] { getFullName(), "State." + getName(), getName() }, getName());
  }

  public Data getData(String key) {
    return null;
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

  protected boolean wantTransitionEvent(TransitionEvent transitionEvent) {
    return true;
  }

  protected String getFullName() {
    return getStage().getModule() + "." + getName();
  }
}
