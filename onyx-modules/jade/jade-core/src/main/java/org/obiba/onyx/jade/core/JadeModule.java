package org.obiba.onyx.jade.core;

import org.obiba.onyx.engine.Module;
import org.obiba.onyx.engine.Stage;
import org.obiba.onyx.engine.StageExecution;

public class JadeModule implements Module {

  public String getName() {
    return "jade";
  }

  public StageExecution resume(Stage stage) {
    throw new UnsupportedOperationException("resume");
  }

  public StageExecution startStage(Stage stage) {
    return new JadeStageExecution(stage);
  }

}
