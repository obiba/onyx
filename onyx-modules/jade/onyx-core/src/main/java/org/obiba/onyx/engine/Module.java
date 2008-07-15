package org.obiba.onyx.engine;

public interface Module {

  public String getName();

  public StageExecution startStage(Stage stage);

  public StageExecution resume(Stage stage);

}
