package org.obiba.onyx.mica.engine.state;

import org.obiba.onyx.engine.state.AbstractStageState;
import org.obiba.onyx.engine.state.TransitionEvent;
import org.springframework.beans.factory.InitializingBean;

public class MicaNotApplicableState extends AbstractStageState implements InitializingBean {

  public void afterPropertiesSet() throws Exception {
    // TODO Auto-generated method stub
  }

  @Override
  public boolean isCompleted() {
    return true;
  }

  public String getName() {
    return "Mica.NotApplicable";
  }

  @Override
  protected boolean wantTransitionEvent(TransitionEvent transitionEvent) {
    if(transitionEvent.equals(TransitionEvent.NOTAPPLICABLE)) return false;
    else
      return true;
  }
}
