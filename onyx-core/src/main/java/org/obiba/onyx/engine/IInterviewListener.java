/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
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
