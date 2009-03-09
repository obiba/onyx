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

import java.util.Map;

import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.domain.user.User;
import org.obiba.onyx.engine.state.StageExecutionContext;

/**
 * Manages interview concurrency: allows obtaining locks on an interview making it impossible for another session to
 * obtain the same lock.
 */
public interface InterviewManager {

  /**
   * Returns the {@code Participant} instance being interviewed in the current session.
   * 
   * @return the {@code Participant} being interviewed in the current session.
   */
  public Participant getInterviewedParticipant();

  /**
   * Returns a {@code Map} of {@code StageExecutionContext} instances for stages of the current interview. The keys are
   * stage names and the values are its associated {@code StageExecutionContext} instance.
   * @return a {@code Map} of {@code StageExecutionContext}
   */
  public Map<String, StageExecutionContext> getStageContexts();

  /**
   * Returns {@code true} if the specified {@code Participant} interview is available for locking. Returns {@code false}
   * otherwise.
   * <p>
   * This method allows checking that a call to {@code #obtainInterview(Participant)} would succeed for a given
   * participant (ignoring concurrency issues).
   * 
   * @param participant the {@code Participant} for which to test the interview availability.
   * @return {@code true} if the participant's interview is available for locking by this session.
   */
  public boolean isInterviewAvailable(Participant participant);

  /**
   * Returns the {@code User} instance associated with a {@code Participant}. This method returns the {@code User} that
   * holds the lock for the specified interview.
   * 
   * @param participant the participant instance to check
   * @return the {@code User} instance holding the interview lock or null if no lock is currently held for the specified
   * participant.
   */
  public User getInterviewer(Participant participant);

  /**
   * Locks the specified interview and binds it to the current user's session.
   * <p>
   * If a participant does not yet have an interview, this method will create one.
   * 
   * @param participant the {@code Participant} instance for which to lock the interview.
   * @return the participant's {@code Interview} instance (which may have been created).
   */
  public Interview obtainInterview(Participant participant);

  /**
   * Release a lock that the current user session is holding. If the current user session has no lock, this method has
   * no effect.
   */
  public void releaseInterview();

  /**
   * Release a lock that the specified user session is holding. This method can be used to release locks outside of a
   * request-cycle when the current user session cannot be determined.
   * @param sessionId the unique ID of the session for which to release locks.
   */
  public void releaseInterview(String sessionId);

  /**
   * Release all locks for a given {@code Participant} and obtain a lock to the interview for the current user session.
   * 
   * @param participant the {@code Participant} instance to lock
   * @return the locked {@code Interview} instance.
   */
  public Interview overrideInterview(Participant participant);

}
