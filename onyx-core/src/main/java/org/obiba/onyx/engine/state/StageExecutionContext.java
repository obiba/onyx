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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Component;
import org.obiba.core.domain.IEntity;
import org.obiba.core.service.impl.PersistenceManagerAwareService;
import org.obiba.onyx.core.domain.IMemento;
import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.core.domain.stage.StageExecutionMemento;
import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.ActionDefinition;
import org.obiba.onyx.engine.ActionType;
import org.obiba.onyx.engine.ModuleRegistry;
import org.obiba.onyx.engine.Stage;
import org.obiba.onyx.util.data.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSourceResolvable;

/**
 * The stage execution context is the entry point of stage state machines. It holds the current state, and performs the
 * {@link TransitionEvent} based state transitions. As a {@link ITransitionSource} it will inform the
 * {@link ITransitionListener} about the transition event. All the method calls defined by {@link IStageExecution} will
 * be forwarded to the {@link IStageExecution} holding the current state.
 * <p>
 * State Machine Design Pattern http://dotnet.zcu.cz/NET_2006/Papers_2006/short/B31-full.pdf
 * @author Yannick Marcon
 * 
 */
public class StageExecutionContext extends PersistenceManagerAwareService implements IStageExecution, ITransitionEventSink, IMemento, ITransitionSource, ITransitionListener {

  private static final Logger log = LoggerFactory.getLogger(StageExecutionContext.class);

  private ModuleRegistry moduleRegistry;

  private Stage stage;

  private Interview interview;

  private IStageExecution currentState;

  private Map<IStageExecution, Map<TransitionEvent, IStageExecution>> edges = new HashMap<IStageExecution, Map<TransitionEvent, IStageExecution>>();

  private List<ITransitionListener> transitionListeners = new ArrayList<ITransitionListener>();

  public StageExecutionContext() {
  }

  public StageExecutionContext(Interview interview, Stage stage) {
    this.interview = interview;
    this.stage = stage;
  }

  public void setModuleRegistry(ModuleRegistry moduleRegistry) {
    this.moduleRegistry = moduleRegistry;
  }

  public void addTransitionListener(ITransitionListener listener) {
    transitionListeners.add(listener);
  }

  public void removeTransitionListener(ITransitionListener listener) {
    transitionListeners.remove(listener);
  }

  public void removeAllTransitionListener() {
    transitionListeners.clear();
  }

  public void addEdge(AbstractStageState source, TransitionEvent event, AbstractStageState target) {
    Map<TransitionEvent, IStageExecution> stateEdges = edges.get(source);
    if(stateEdges == null) {
      stateEdges = new HashMap<TransitionEvent, IStageExecution>();
      edges.put(source, stateEdges);
    }
    stateEdges.put(event, target);
    source.setEventSink(this);
    source.setStage(stage);
    target.setEventSink(this);
    target.setStage(stage);
  }

  public void castEvent(TransitionEvent event) {
    log.info("castEvent({}) from stage '{}' in state '{}'", new Object[] { event, stage.getName(), currentState.getClass().getSimpleName() });
    Map<TransitionEvent, IStageExecution> stateEdges = edges.get(currentState);
    if(stateEdges != null) {
      IStageExecution newState = stateEdges.get(event);
      if(newState == null) {
        log.error("Stage '{}' in state '{}' received event '{}' that has no edge to any other state. Either the state machine is missing edges (to determine what the new state should be) or the event should have never been received by the current state.", new Object[] { stage.getName(), currentState.getClass().getSimpleName(), event });
        throw new IllegalStateException("No destination state for stage '" + stage.getName() + "' in state '" + currentState.getClass().getSimpleName() + "' receiving event '" + event + "'.");
      }

      onExit(event);

      currentState = newState;
      // Re-initialise the reason why transition event occurred (there may be no action at all).
      currentState.setReason(null);

      onEntry(event);

      log.debug("transitionListeners.size=" + transitionListeners.size());
      for(ITransitionListener listener : transitionListeners) {
        listener.onTransition(this, event);
      }
    }
    log.info("castEvent({}) from stage {} now in state {}", new Object[] { event, stage.getName(), currentState.getClass().getSimpleName() });

    saveState();
  }

  private void saveState() {
    StageExecutionMemento template = new StageExecutionMemento();
    template.setStage(stage.getName());
    template.setInterview(interview);
    StageExecutionMemento memento = (StageExecutionMemento) saveToMemento(getPersistenceManager().matchOne(template));
    getPersistenceManager().save(memento);
  }

  public List<ActionDefinition> getActionDefinitions() {
    return currentState.getActionDefinitions();
  }

  public List<ActionDefinition> getSystemActionDefinitions() {
    return currentState.getSystemActionDefinitions();
  }

  public ActionDefinition getActionDefinition(ActionType type) {
    return currentState.getActionDefinition(type);
  }

  public ActionDefinition getSystemActionDefinition(ActionType type) {
    return currentState.getSystemActionDefinition(type);
  }

  public void onEntry(TransitionEvent event) {
    currentState.onEntry(event);
  }

  public void onExit(TransitionEvent event) {
    currentState.onExit(event);
  }

  public void execute(Action action) {
    currentState.execute(action);
  }

  public void interrupt(Action action) {
    currentState.interrupt(action);
  }

  public void skip(Action action) {
    currentState.skip(action);
  }

  public void stop(Action action) {
    currentState.stop(action);
  }

  public void complete(Action action) {
    currentState.complete(action);
  }

  public Component getWidget(String id) {
    return currentState.getWidget(id);
  }

  public boolean isInteractive() {
    return currentState.isInteractive();
  }

  public boolean isFinal() {
    return currentState.isFinal();
  }

  public boolean isCompleted() {
    return currentState.isCompleted();
  }

  public MessageSourceResolvable getMessage() {
    return currentState.getMessage();
  }

  public Data getData(String key) {
    Data data = currentState.getData(key);
    log.info(getName() + ":" + currentState.getName() + ".data." + key + "=" + data);
    return data;
  }

  public Stage getStage() {
    return stage;
  }

  public void setStage(Stage stage) {
    this.stage = stage;
  }

  public Interview getInterview() {
    return interview;
  }

  public void setInterview(Interview interview) {
    this.interview = interview;
  }

  public void setInitialState(IStageExecution stageState) {
    this.currentState = stageState;
  }

  public void onTransition(IStageExecution execution, TransitionEvent event) {
    log.info("Stage {} in state {} receiving onTransition({}, {})", new Object[] { stage.getName(), currentState.getClass().getSimpleName(), execution.getName(), event });
    if(currentState instanceof ITransitionListener) {
      ((ITransitionListener) currentState).onTransition(execution, event);
    }
  }

  public String getName() {
    // do not expose current state name
    return stage.getModule() + ":" + stage.getName();
  }

  public void setReason(Action reason) {
    currentState.setReason(reason);
    // persist the action
    saveState();
  }

  public Action getReason() {
    return currentState.getReason();
  }

  public MessageSourceResolvable getReasonMessage() {
    return currentState.getReasonMessage();
  }

  public void restoreFromMemento(IEntity memento) {
    if(memento instanceof StageExecutionMemento) {
      StageExecutionMemento stageMemento = (StageExecutionMemento) memento;
      this.interview = stageMemento.getInterview();
      this.stage = this.moduleRegistry.getStage(stageMemento.getStage());
      for(IStageExecution exec : edges.keySet()) {
        if(exec.getClass().getSimpleName().equals(stageMemento.getState())) {
          this.currentState = exec;
        }
      }
      this.currentState.setReason(stageMemento.getAction());
    }
  }

  public IEntity saveToMemento(IEntity memento) {
    StageExecutionMemento myMemento = null;
    if(memento == null) {
      myMemento = new StageExecutionMemento();
      myMemento.setInterview(interview);
      myMemento.setStage(stage.getName());
    } else if(memento instanceof StageExecutionMemento) myMemento = (StageExecutionMemento) memento;
    else
      throw new IllegalArgumentException("StageExecutionMemento is expected.");

    myMemento.setState(currentState.getClass().getSimpleName());
    myMemento.setAction(currentState.getReason());

    return myMemento;
  }

  public ActionType getStartingActionType() {
    return currentState.getStartingActionType();
  }

  public Date getStartTime() {
    // Find the last action of type EXECUTE or SKIP for this stage
    Action lastExecuteOrSkipAction = null;
    for(Action action : getStageActionList()) {
      if(action.getActionType() == ActionType.EXECUTE || action.getActionType() == ActionType.SKIP) {
        lastExecuteOrSkipAction = action;
      }
    }

    return (lastExecuteOrSkipAction != null) ? lastExecuteOrSkipAction.getDateTime() : null;
  }

  public Date getEndTime() {
    if(isCompleted() == false) {
      return null;
    }
    // Return the last action's time
    List<Action> actions = getStageActionList();
    if(actions == null || actions.size() == 0) {
      throw new IllegalStateException("Stage " + stage.getName() + " is in a completed state but has no associated action.");
    }
    return actions.get(actions.size() - 1).getDateTime();
  }

  private List<Action> getStageActionList() {
    Action template = new Action();
    template.setInterview(interview);
    template.setStage(stage.getName());

    return getPersistenceManager().match(template);

  }
}
