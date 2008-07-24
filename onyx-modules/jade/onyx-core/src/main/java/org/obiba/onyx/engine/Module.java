package org.obiba.onyx.engine;

import java.util.Collection;

public interface Module {

  public String getName();

  public void initialize();
  
  public void shutdown();
  
  public StageExecution start(Stage stage);

  public StageExecution resume(Stage stage);
  
  public StageExecution getCurrentStageExecution();
  
  public Collection<Stage> getStages();
  
  public boolean isCompleted(Stage stage);

}
