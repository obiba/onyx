package org.obiba.onyx.engine.state;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Component;
import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.ActionDefinition;
import org.obiba.onyx.engine.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StageExecutionContext implements IStageExecution, ITransitionEventSink {

  private static final Logger log = LoggerFactory.getLogger(StageExecutionContext.class);

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

  public void addTransitionListener(ITransitionListener listener) {
    transitionListeners.add(listener);
  }

  public void removeTransitionListener(ITransitionListener listener) {
    transitionListeners.remove(listener);
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
    log.info("castEvent(" + event + ") from " + currentState.getClass().getSimpleName());
    Map<TransitionEvent, IStageExecution> stateEdges = edges.get(currentState);
    if(stateEdges != null) {
      currentState = stateEdges.get(event);
      for(ITransitionListener listener : transitionListeners) {
        listener.onTransition(this);
      }
    }
    log.info("                 to " + currentState.getClass().getSimpleName());
  }

  public List<ActionDefinition> getActions() {
    return currentState.getActions();
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

}
