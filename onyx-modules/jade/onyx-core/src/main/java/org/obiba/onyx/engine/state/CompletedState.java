package org.obiba.onyx.engine.state;

import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.ActionDefinition;
import org.obiba.onyx.engine.ActionType;
import org.obiba.onyx.engine.state.transition.StoppableState;

public abstract class CompletedState extends StageState implements StoppableState {

  protected CompletedState() {
    addAction(new ActionDefinition(ActionType.STOP));
  }

  @Override
  public void stop(Action action) {
    onStop(action);
    castEvent(STOP);
  }

  public boolean isInteractive() {
    return false;
  }

  public boolean isFinal() {
    return false;
  }

  public boolean isCompleted() {
    return true;
  }

}
