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
import java.util.List;
import java.util.Locale;

import org.apache.wicket.Component;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.core.service.UserSessionService;
import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.ActionDefinition;
import org.obiba.onyx.engine.ActionType;
import org.obiba.onyx.engine.Stage;
import org.obiba.onyx.util.data.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Base class for Stage states.
 * @author Yannick Marcon
 * 
 */
public abstract class AbstractStageState implements IStageExecution, ITransitionListener, ApplicationContextAware {

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(AbstractStageState.class);

  private ITransitionEventSink eventSink;

  private Stage stage;

  protected List<ActionDefinition> actionDefinitions = new ArrayList<ActionDefinition>();

  protected List<ActionDefinition> systemActionDefinitions = new ArrayList<ActionDefinition>();

  protected ApplicationContext context;

  protected ActiveInterviewService activeInterviewService;

  protected UserSessionService userSessionService;

  /**
   * The reason the stage is in its current state (i.e., the action that caused the stage to transition to this state).
   */
  private Action reason;

  public void setUserSessionService(UserSessionService userSessionService) {
    this.userSessionService = userSessionService;
  }

  public void setActiveInterviewService(ActiveInterviewService activeInterviewService) {
    this.activeInterviewService = activeInterviewService;
  }

  public void setApplicationContext(ApplicationContext context) {
    this.context = context;
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

  public boolean removeAfterTransition() {
    return false;
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

  public String getMessage() {
    Locale locale = userSessionService.getLocale();
    return context.getMessage(getName(), null, locale);
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

  protected boolean wantTransitionEvent(TransitionEvent transitionEvent) {
    return true;
  }
  
  public ActionType getStartingActionType() {
    return null;
  }
}
