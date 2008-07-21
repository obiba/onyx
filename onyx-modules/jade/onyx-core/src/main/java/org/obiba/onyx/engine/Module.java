package org.obiba.onyx.engine;

import java.util.List;

public interface Module {

  public String getName();

  public StageExecution startStage(Stage stage);

  public StageExecution resume(Stage stage);
  
  public List<Stage> getStages();

}
