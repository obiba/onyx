package org.obiba.onyx.mica.engine.state;

import org.obiba.onyx.engine.state.ITransitionListener;
import org.obiba.onyx.engine.state.TransitionEvent;
import org.springframework.beans.factory.InitializingBean;

/**
 * Jade Waiting State, goes there if a dependency is not satisfied.
 * @author Meryam Belhiah
 * 
 */
public class MicaWaitingState extends AbstractMicaStageState implements InitializingBean, ITransitionListener {

  public void afterPropertiesSet() throws Exception {
  }

  public String getName() {
    return "Mica.Waiting";
  }

  @Override
  protected boolean wantTransitionEvent(TransitionEvent transitionEvent) {
    if(transitionEvent.equals(TransitionEvent.INVALID)) return false;
    else
      return true;
  }

}