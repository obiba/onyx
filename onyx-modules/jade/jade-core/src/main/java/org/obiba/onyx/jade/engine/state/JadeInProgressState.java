/**
 * 
 */
package org.obiba.onyx.jade.engine.state;

import org.apache.wicket.Component;
import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.state.AbstractStageState;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.onyx.jade.core.wicket.panel.JadePanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JadeInProgressState extends AbstractStageState {

  private static final Logger log = LoggerFactory.getLogger(JadeInProgressState.class);

  private InstrumentType instrumentType;

  public JadeInProgressState(InstrumentType instrumentType) {
    this.instrumentType = instrumentType;
  }

  public Component getWidget(String id) {
    return new JadePanel(id, instrumentType);
  }

  public void onStop(Action action) {
    log.info("Jade Stage {} is stopping", super.getStage().getName());
  }

  public void onComplete(Action action) {
    log.info("Jade Stage {} is completing", super.getStage().getName());
  }

  @Override
  public boolean isInteractive() {
    return true;
  }
}