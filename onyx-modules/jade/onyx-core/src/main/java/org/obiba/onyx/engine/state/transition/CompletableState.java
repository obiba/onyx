package org.obiba.onyx.engine.state.transition;

import org.obiba.onyx.engine.ActionInstance;
import org.obiba.onyx.engine.state.TransitionEvent;

public interface CompletableState {

  public TransitionEvent COMPLETE = new TransitionEvent("complete");

  public void onComplete(ActionInstance action);

}
