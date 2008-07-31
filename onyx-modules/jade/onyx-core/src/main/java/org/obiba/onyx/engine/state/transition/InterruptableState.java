package org.obiba.onyx.engine.state.transition;

import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.state.TransitionEvent;

public interface InterruptableState {

  public TransitionEvent INTERRUPT = new TransitionEvent("interrupt");

  public void onInterrupt(Action action);

}
