package org.obiba.onyx.engine;

import org.apache.wicket.Component;

public interface StageExecution {

  public void initialize();
  
  public void shutdown();
  
  public void interrupt();

  public void start();
  
  public void stop();
  
  public Component createStageComponent(String id);

}
