package org.obiba.onyx.jade.core;

import org.apache.wicket.Component;
import org.obiba.onyx.engine.Stage;
import org.obiba.onyx.engine.StageExecution;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.onyx.jade.core.wicket.panel.JadePanel;

public class JadeStageExecution implements StageExecution {

  private Stage stage;

  JadeStageExecution(Stage stage) {
    this.stage = stage;
  }

  public Component createStageComponent(String id) {
    return new JadePanel(id, (InstrumentType)stage);
  }

  public void interrupt() {
  }

}
