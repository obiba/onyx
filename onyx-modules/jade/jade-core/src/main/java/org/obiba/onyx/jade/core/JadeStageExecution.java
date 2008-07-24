package org.obiba.onyx.jade.core;

import org.apache.wicket.Component;
import org.obiba.onyx.engine.Stage;
import org.obiba.onyx.engine.StageExecution;
import org.obiba.onyx.engine.StageExecutionStatus;
import org.obiba.onyx.jade.core.wicket.panel.JadePanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JadeStageExecution implements StageExecution {

  private static final Logger log = LoggerFactory.getLogger(JadeStageExecution.class);
  
  private JadeStage stage;
  
  private StageExecutionStatus status = StageExecutionStatus.READY;

  public JadeStageExecution() {
  }

  JadeStageExecution(JadeStage stage) {
    this.stage = stage;
  }

  public Component createStageComponent(String id) {
    return new JadePanel(id, stage.getInstrumentType());
  }

  public void interrupt() {
    status = StageExecutionStatus.INTERRUPTED;
  }
  
  public void resume() {
    status = StageExecutionStatus.PENDING;
  }

  public void start(Stage stage) {
    log.info("start("+stage+")");
    this.stage = (JadeStage)stage;
    status = StageExecutionStatus.PENDING;
  }

  public void stop() {
    log.info("stop");
    this.stage = null;
    status = StageExecutionStatus.READY;
  }

  public void initialize() {
    log.info("initialize");
  }

  public void shutdown() {
    log.info("shutdown");
  }

  public StageExecutionStatus getStatus() {
    return status;
  }
  
  public JadeStage getStage() {
    return stage;
  }
  
}
