package org.obiba.onyx.engine.state;

import java.util.ArrayList;
import java.util.List;

import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.ActionDefinition;
import org.obiba.onyx.engine.Stage;

/**
 * Base class for Stage states.
 * @author Yannick Marcon
 * 
 */
public abstract class StageState implements IStageExecution {

  private ITransitionEventSink eventSink;

  private Stage stage;

  protected List<ActionDefinition> actions = new ArrayList<ActionDefinition>();

  public void setStage(Stage stage) {
    this.stage = stage;
  }

  public Stage getStage() {
    return stage;
  }

  protected void castEvent(TransitionEvent event) {
    eventSink.castEvent(event);
  }

  public void setEventSink(ITransitionEventSink eventSink) {
    this.eventSink = eventSink;
  }

  protected void addAction(ActionDefinition action) {
    actions.add(action);
  }

  public List<ActionDefinition> getActions() {
    return actions;
  }

  public void execute(Action action) {
  }

  public void interrupt(Action action) {
  }

  public void skip(Action action) {
  }

  public void stop(Action action) {
  }

  public void send() {
  }

}
