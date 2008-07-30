package org.obiba.onyx.engine.state;

import org.obiba.onyx.engine.ActionInstance;

public abstract class SentState extends StageState {

  public void doAction(ActionInstance action) {
    
  }

  public boolean isInteractive() {
    return true;
  }
  
  public boolean isFinal() {
    return true;
  }
  
  public boolean isCompleted() {
    return true;
  }

}
