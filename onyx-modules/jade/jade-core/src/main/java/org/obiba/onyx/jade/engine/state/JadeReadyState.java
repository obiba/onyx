/**
 * 
 */
package org.obiba.onyx.jade.engine.state;

import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.state.ReadyState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JadeReadyState extends ReadyState {

  private static final Logger log = LoggerFactory.getLogger(JadeReadyState.class);

  public void onExecute(Action action) {
    log.info("Jade Stage {} is starting", super.getStage().getName());
  }

  public void onSkip(Action action) {
    log.info("Jade Stage {} is skipping", super.getStage().getName());
  }

}