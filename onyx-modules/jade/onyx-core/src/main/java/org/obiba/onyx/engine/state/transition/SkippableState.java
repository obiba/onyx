package org.obiba.onyx.engine.state.transition;

import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.state.TransitionEvent;

public interface SkippableState {

  public TransitionEvent SKIP = new TransitionEvent("skip");

  public void onSkip(Action action);

}
