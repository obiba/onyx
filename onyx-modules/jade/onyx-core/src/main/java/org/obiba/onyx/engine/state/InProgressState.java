package org.obiba.onyx.engine.state;

import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.ActionInstance;
import org.obiba.onyx.engine.ActionType;

public abstract class InProgressState extends StageState {

  protected InProgressState() {
    addAction(new Action(ActionType.CANCEL));
    addAction(new Action(ActionType.PAUSE));
  }

  public void doAction(ActionInstance action) {
    switch(action.getActionType()) {
    case PAUSE:
      interrupt();
      castEvent(TransitionEvent.INTERRUPT);
      break;

    case CANCEL:
      cancel();
      castEvent(TransitionEvent.CANCEL);
      break;
      
    default:
      break;
    }
  }

  protected abstract void interrupt();
  
  protected abstract void cancel();
  
  protected void complete() {
    castEvent(TransitionEvent.COMPLETE);
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
