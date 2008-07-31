package org.obiba.onyx.engine.state;

import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.ActionDefinition;
import org.obiba.onyx.engine.ActionType;
import org.obiba.onyx.engine.state.transition.ExecutableState;
import org.obiba.onyx.engine.state.transition.StoppableState;

public abstract class InterruptedState extends StageState implements ExecutableState, StoppableState {

  protected InterruptedState() {
    addAction(new ActionDefinition(ActionType.EXECUTE));
    addAction(new ActionDefinition(ActionType.STOP));
  }

  @Override
  public void stop(Action action) {
    onStop(action);
    castEvent(STOP);
  }

  @Override
  public void execute(Action action) {
    onStop(action);
    castEvent(EXECUTE);
  }

  public boolean isInteractive() {
    return false;
  }

  public boolean isFinal() {
    return false;
  }

  public boolean isCompleted() {
    return false;
  }

}
