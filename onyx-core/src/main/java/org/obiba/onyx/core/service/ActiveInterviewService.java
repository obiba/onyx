package org.obiba.onyx.core.service;

import java.util.Date;

import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.core.domain.participant.InterviewStatus;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.domain.user.User;
import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.Stage;
import org.obiba.onyx.engine.state.IStageExecution;

/**
 * Session active participant's interview service.
 * @author Yannick Marcon
 *
 */
public interface ActiveInterviewService {

  /**
   * Set the current participant to deal with.
   * @param participant
   */
  public void setParticipant(Participant participant);
  
  /**
   * Get the current participant.
   * @return null if none set
   */
  public Participant getParticipant();
  
  /**
   * Get current participant's interview.
   * @return null if no participant set
   */
  public Interview getInterview();
  
  /**
   * Set the operator for the current interview.
   * @param operator
   */
  public Interview setInterviewOperator(User operator);
  
  /**
   * Get the stage execution object for given stage, in the interview of current participant.
   * @param stage
   * @return
   */
  public IStageExecution getStageExecution(Stage stage);
  
  /**
   * Persist and perform action on the stage, in the interview of current participant.
   * @param stage
   * @param action
   */
  public void doAction(Stage stage, Action action);
  
  public void setStatus(InterviewStatus status, Date stopDate);
  
}
