package org.obiba.onyx.core.service;

import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.Stage;
import org.obiba.onyx.engine.state.IStageExecution;

public interface ActiveInterviewService {
  
  public Participant getCurrentParticipant();
  
  public IStageExecution getStageExecution(Stage stage);
  
  /**
   * Persist and perform action on the stage.
   * @param stage
   * @param action
   */
  public void doAction(Stage stage, Action action);
  
}
