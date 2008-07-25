package org.obiba.onyx.engine;

import org.apache.wicket.Component;

public interface StageExecution {

  public void init();
  
  public void cleanup();
  
  public void interrupt();
  
  public void resume();

  public void start(Stage stage);
  
  public void stop();
  
  public StageExecutionStatus getStatus();
  
  public Stage getStage();
  
  public Component createStageComponent(String id);

}
