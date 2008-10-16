/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.service;

import java.util.List;

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
   * Get the stage execution object for given stage name, in the interview of current participant.
   * @param stageName
   * @return
   */
  public IStageExecution getStageExecution(String stageName);
  
  /**
   * Persist and perform action on the stage, in the interview of current participant.
   * @param stage
   * @param action
   */
  public void doAction(Stage stage, Action action, User user);
  
  /**
   * Set the status of the current interview.
   * @param status
   */
  public void setStatus(InterviewStatus status);
  
  /**
   * Get a list of all the comments for the current interview.
   * 
   * @return  The list of comment
   */
  public List<Action> getInterviewComments();
  
  /**
   * Get the {@link Action} associated to interview status.
   * @return
   */
  public Action getStatusAction();
  
}
