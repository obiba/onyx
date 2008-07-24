package org.obiba.onyx.jade.core;

import org.apache.wicket.Component;
import org.obiba.onyx.engine.StageExecution;
import org.obiba.onyx.jade.core.wicket.panel.JadePanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JadeStageExecution implements StageExecution {

  private static final Logger log = LoggerFactory.getLogger(JadeStageExecution.class);
  
  private JadeStage stage;

  public JadeStageExecution() {
  }

  JadeStageExecution(JadeStage stage) {
    this.stage = stage;
  }

  public Component createStageComponent(String id) {
    return new JadePanel(id, stage.getInstrumentType());
  }

  public void interrupt() {
    throw new UnsupportedOperationException("interrupt");
  }

  public void start() {
    log.info("start");
  }

  public void stop() {
    log.info("stop");
  }

  public void initialize() {
    log.info("initialize");
  }

  public void shutdown() {
    log.info("shutdown");
  } 

  public void setStage(JadeStage stage) {
    this.stage = stage;
  }
}
