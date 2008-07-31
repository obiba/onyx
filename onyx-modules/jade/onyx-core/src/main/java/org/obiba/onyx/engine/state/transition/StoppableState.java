package org.obiba.onyx.engine.state.transition;

import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.state.TransitionEvent;

public interface StoppableState {

  public TransitionEvent STOP = new TransitionEvent("stop");

  public void onStop(Action action);

}
