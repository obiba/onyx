/**
 * State notApplicable for Quartz module
 * 
 * @author acarey
 * 
 * Coming from states: waiting, ready, skipped, completed
 */
package org.obiba.onyx.quartz.engine.state;

import org.obiba.onyx.engine.state.AbstractStageState;
import org.obiba.onyx.engine.state.TransitionEvent;
import org.springframework.beans.factory.InitializingBean;

public class QuartzNotApplicableState extends AbstractStageState implements InitializingBean {

  public void afterPropertiesSet() throws Exception {
  }
  
  public String getName() {
    return "Quartz.NotApplicable";
  }

  @Override
  public boolean isCompleted() {
    return true;
  }
  
  @Override
  protected boolean wantTransitionEvent(TransitionEvent transitionEvent) {
    if(transitionEvent.equals(TransitionEvent.NOTAPPLICABLE)) return false;
    else
      return true;
  }
}
