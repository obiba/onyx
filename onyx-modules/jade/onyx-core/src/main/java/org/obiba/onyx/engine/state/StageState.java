package org.obiba.onyx.engine.state;

import java.util.ArrayList;
import java.util.List;

import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.ActionInstance;
import org.obiba.onyx.engine.Stage;

/**
 * Base class for Stage states.
 * @author Yannick Marcon
 * 
 */
public abstract class StageState implements IStageExecution {

  private ITransitionEventSink eventSink;

  private Stage stage;

  protected List<Action> actions = new ArrayList<Action>();

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

  protected void addAction(Action action) {
    actions.add(action);
  }

  public List<Action> getActions() {
    return actions;
  }

  public void execute(ActionInstance action) {
  }

  public void interrupt(ActionInstance action) {
  }

  public void skip(ActionInstance action) {
  }

  public void stop(ActionInstance action) {
  }

  public void send() {
  }

}
