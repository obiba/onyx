package org.obiba.onyx.mica.engine.state;

import java.util.Set;

import org.obiba.onyx.engine.ActionType;
import org.obiba.onyx.engine.state.AbstractStageState;
import org.obiba.onyx.engine.state.TransitionEvent;

public class MicaNotApplicableState extends AbstractStageState {

  public String getName() {
    return "NotApplicable";
  }

  @Override
  public boolean isCompleted() {
    return true;
  }

  @Override
  protected void addUserActions(Set<ActionType> types) {
    // No user action possible
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
