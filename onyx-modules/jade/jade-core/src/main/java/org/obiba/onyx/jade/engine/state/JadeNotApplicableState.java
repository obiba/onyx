package org.obiba.onyx.jade.engine.state;

import org.obiba.onyx.engine.state.TransitionEvent;
import org.springframework.beans.factory.InitializingBean;

public class JadeNotApplicableState extends AbstractJadeStageState implements InitializingBean {

  public void afterPropertiesSet() throws Exception {
    // TODO Auto-generated method stub

  }

  public String getName() {
    return "Jade.NotApplicable";
  }

  @Override
  protected boolean wantTransitionEvent(TransitionEvent transitionEvent) {
    if(transitionEvent.equals(TransitionEvent.NOTAPPLICABLE)) return false;
    else
      return true;
  }
}
