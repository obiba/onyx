package org.obiba.onyx.engine;

import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.engine.state.IStageExecution;

public interface Module {

  public String getName();

  public IStageExecution createStageExecution(Interview interview, Stage stage);

  public void initialize();

  public void shutdown();

}
