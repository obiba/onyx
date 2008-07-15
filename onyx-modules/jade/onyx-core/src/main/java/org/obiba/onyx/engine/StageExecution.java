package org.obiba.onyx.engine;

import org.apache.wicket.Component;

public interface StageExecution {

  public void interrupt();

  public Component createStageComponent(String id);

}
