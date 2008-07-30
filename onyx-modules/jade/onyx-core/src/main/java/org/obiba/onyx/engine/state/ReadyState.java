package org.obiba.onyx.engine.state;

import org.apache.wicket.Component;
import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.ActionInstance;
import org.obiba.onyx.engine.ActionType;


public abstract class ReadyState extends StageState {
  
  protected ReadyState() {
    addAction(new Action(ActionType.EXECUTE));
    addAction(new Action(ActionType.SKIP));
  }
  
  public void doAction(ActionInstance action) {
    switch(action.getActionType()) {
    case EXECUTE:
      start();
      castEvent(TransitionEvent.START);
      break;

    case SKIP:
      skip();
      castEvent(TransitionEvent.SKIP);
      break;
      
    default:
      break;
    }
    
  }
  
  protected abstract void start();
  
  protected abstract void skip();
  
  public Component getWidget(String id) {
    return null;
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
