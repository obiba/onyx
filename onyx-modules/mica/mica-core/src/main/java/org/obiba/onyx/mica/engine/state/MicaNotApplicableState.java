package org.obiba.onyx.mica.engine.state;

import org.obiba.onyx.engine.state.AbstractStageState;
import org.obiba.onyx.engine.state.TransitionEvent;

public class MicaNotApplicableState extends AbstractStageState {

  @Override
  public boolean isCompleted() {
    return true;
  }

  public String getName() {
    return "NotApplicable";
  }

  @Override
  protected boolean wantTransitionEvent(TransitionEvent transitionEvent) {
    if(transitionEvent.equals(TransitionEvent.NOTAPPLICABLE)) {
      return false;
    } else {
      return true;
    }
  }
}
