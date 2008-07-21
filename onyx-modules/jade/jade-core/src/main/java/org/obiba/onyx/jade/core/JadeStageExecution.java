package org.obiba.onyx.jade.core;

import org.apache.wicket.Component;
import org.obiba.onyx.engine.StageExecution;
import org.obiba.onyx.jade.core.wicket.panel.JadePanel;

public class JadeStageExecution implements StageExecution {

  private JadeStage stage;

  JadeStageExecution(JadeStage stage) {
    this.stage = stage;
  }

  public Component createStageComponent(String id) {
    return new JadePanel(id, stage.getInstrumentType());
  }

  public void interrupt() {
    throw new UnsupportedOperationException("interrupt");
  }

}
