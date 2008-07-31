package org.obiba.onyx.engine.state;

import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.ActionInstance;
import org.obiba.onyx.engine.ActionType;

public abstract class CompletedState extends StageState {

  protected CompletedState() {
    addAction(new Action(ActionType.STOP));
    addAction(new Action(ActionType.EXECUTE));
  }

  public void doAction(ActionInstance action) {
    switch(action.getActionType()) {
    case EXECUTE:
      resume();
      castEvent(TransitionEvent.RESUME);
      break;

    case STOP:
      cancel();
      castEvent(TransitionEvent.CANCEL);
      break;

    default:
      break;
    }
  }

  protected abstract void resume();

  protected abstract void cancel();
  
  protected void send() {
    castEvent(TransitionEvent.SEND);
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
