package org.obiba.onyx.engine;

import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.core.domain.participant.InterviewStatus;

/**
 * Stages execution context may want to be informed when the englobing interview changes its status.
 * @author Yannick Marcon
 *
 */
public interface IInterviewListener {

  /**
   * Called when given interview goes to status completed.
   * @see InterviewStatus
   * @param interview
   */
  public void onInterviewCompleted(Interview interview);
  
}
