package org.obiba.onyx.engine.state;

import java.util.ArrayList;
import java.util.List;

import org.obiba.onyx.engine.Action;

/**
 * Base class for Stage states.
 * @author Yannick Marcon
 *
 */
public abstract class StageState implements IStageExecution {

  private ITransitionEventSink eventSink;
  
  protected List<Action> actions = new ArrayList<Action>();

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

}
