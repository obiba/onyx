package org.obiba.onyx.engine.state;

import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.ActionDefinition;
import org.obiba.onyx.engine.ActionType;
import org.obiba.onyx.engine.state.transition.CompletableState;
import org.obiba.onyx.engine.state.transition.StoppableState;

public abstract class InProgressState extends StageState implements CompletableState, StoppableState {

  protected InProgressState() {
    addAction(new ActionDefinition(ActionType.STOP));
  }

  @Override
  public void stop(Action action) {
    onStop(action);
    castEvent(STOP);
  }

  public void complete() {
    onComplete(null);
    castEvent(COMPLETE);
  }

  public boolean isInteractive() {
    return true;
  }

  public boolean isFinal() {
    return false;
  }

  public boolean isCompleted() {
    return false;
  }

}
