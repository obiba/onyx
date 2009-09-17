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
 */
public interface ActiveInterviewService {

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
   * Returns the {@code User} that is administrating this interview.
   * @return the {@code User} administrating this interview.
   */
  public User getOperator();

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
   * Returns the {@code Stage} instance that is currently {@code interactive} or null if no such stage exists.
   * @return the {@code Stage} instance for the current interactive stage or null if no stage is currently interactive.
   * @See {@link IStageExecution#isInteractive()}
   */
  public Stage getInteractiveStage();

  /**
   * Persist and perform action on the stage, in the interview of current participant.
   * @param stage
   * @param action
   */
  public void doAction(Stage stage, Action action);

  /**
   * Returns the action currently being performed (by the invoking thread).
   * 
   * @return the action currently being performed (<code>null</code> if none)
   */
  public Action getCurrentAction();

  public void updateAction(Action action);

  /**
   * Set the status of the current interview.
   * @param status
   */
  public void setStatus(InterviewStatus status);

  /**
   * Return a list of all the actions associated with the current interview that contain comments. Actions are returned
   * in descending order based on the time stamp of the action.
   * @return A list of actions that contain comments.
   */
  public List<Action> getInterviewComments();

  /**
   * Return a complete list of actions associated with the current interview. Actions are returned in descending order
   * based on the time stamp of the action.
   * @return A list of all actions.
   */
  public List<Action> getInterviewActions();

  /**
   * Return a list of actions associated with the current interview that belong to the given {@code stage}. Actions are
   * returned in descending order based on the time stamp of the action.
   * @param stageName Only return actions that belong to this stage.
   * @return A list of all actions.
   */
  public List<Action> getInterviewActions(String stageName);

  /**
   * Get the {@link Action} associated to interview status.
   * @return
   */
  public Action getStatusAction();

}
