package org.obiba.onyx.engine.state.transition;

import org.obiba.onyx.engine.ActionInstance;
import org.obiba.onyx.engine.state.TransitionEvent;

public interface ExecutableState {

  public TransitionEvent EXECUTE = new TransitionEvent("execute");

  public void onExecute(ActionInstance action);

}
