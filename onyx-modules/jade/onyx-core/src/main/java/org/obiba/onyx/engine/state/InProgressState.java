package org.obiba.onyx.engine.state;

import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.ActionInstance;
import org.obiba.onyx.engine.ActionType;
import org.obiba.onyx.engine.state.transition.StoppableState;
import org.obiba.onyx.engine.state.transition.CompletableState;

public abstract class InProgressState extends StageState implements CompletableState, StoppableState {

  protected InProgressState() {
    addAction(new Action(ActionType.STOP));
  }

  @Override
  public void stop(ActionInstance action) {
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
