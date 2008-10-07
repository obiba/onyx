/**
 * State waiting for Quartz module
 * 
 * @author acarey
 * 
 * Coming from states: ready Possible forward states/actions/transitions: valid, notApplicable
 */
package org.obiba.onyx.quartz.engine.state;

import org.obiba.onyx.engine.state.AbstractStageState;
import org.obiba.onyx.engine.state.TransitionEvent;
import org.springframework.beans.factory.InitializingBean;

public class QuartzWaitingState extends AbstractStageState implements InitializingBean {

  public void afterPropertiesSet() throws Exception {
  }

  public String getName() {
    return "Quartz.Waiting";
  }

  @Override
  protected boolean wantTransitionEvent(TransitionEvent transitionEvent) {
    if(transitionEvent.equals(TransitionEvent.INVALID)) return false;
    else
      return true;
  }

}
