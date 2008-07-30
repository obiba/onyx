package org.obiba.onyx.engine.state;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Component;
import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.ActionInstance;
import org.obiba.onyx.engine.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StageExecutionContext implements IStageExecution, ITransitionEventSink {

  private static final Logger log = LoggerFactory.getLogger(StageExecutionContext.class);
  
  private Stage stage;

  private Interview interview;

  private IStageExecution stageState;

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

  public void addEdge(StageState source, TransitionEvent event, StageState target) {
    Map<TransitionEvent, IStageExecution> stateEdges = edges.get(source);
    if(stateEdges == null) {
      stateEdges = new HashMap<TransitionEvent, IStageExecution>();
      edges.put(source, stateEdges);
    }
    stateEdges.put(event, target);
    source.setEventSink(this);
    target.setEventSink(this);
  }

  public void castEvent(TransitionEvent event) {
    log.info("castEvent("+event+") from " + stageState.getClass().getSimpleName());
    Map<TransitionEvent, IStageExecution> stateEdges = edges.get(stageState);
    if(stateEdges != null) {
      stageState = stateEdges.get(event);
      for(ITransitionListener listener : transitionListeners) {
        listener.onTransition(this);
      }
    }
    log.info("                 to " + stageState.getClass().getSimpleName());
  }

  public void doAction(ActionInstance action) {
    stageState.doAction(action);
  }

  public List<Action> getActions() {
    return stageState.getActions();
  }

  public Component getWidget(String id) {
    return stageState.getWidget(id);
  }

  public boolean isInteractive() {
    return stageState.isInteractive();
  }

  public boolean isFinal() {
    return stageState.isFinal();
  }

  public boolean isCompleted() {
    return stageState.isCompleted();
  }

  public void restoreFromMemento(Object memento) {
    // TODO Auto-generated method stub

  }

  public Object saveToMemento() {
    // TODO Auto-generated method stub
    return null;
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
    this.stageState = stageState;
  }

}
