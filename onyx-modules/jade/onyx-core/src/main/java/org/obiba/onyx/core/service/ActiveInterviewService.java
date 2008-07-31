package org.obiba.onyx.core.service;

import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.engine.Stage;
import org.obiba.onyx.engine.state.IStageExecution;

public interface ActiveInterviewService {
  
  public Participant getCurrentParticipant();
  
  public IStageExecution getStageExecution(Stage stage);
  
}
