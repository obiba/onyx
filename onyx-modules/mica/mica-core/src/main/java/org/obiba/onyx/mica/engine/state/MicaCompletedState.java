/***********************************************************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 **********************************************************************************************************************/
/**
 * 
 */
package org.obiba.onyx.mica.engine.state;

import java.util.Set;

import org.obiba.onyx.core.domain.participant.InterviewStatus;
import org.obiba.onyx.engine.ActionType;
import org.obiba.onyx.engine.state.TransitionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MicaCompletedState extends AbstractMicaStageState {

  private static final Logger log = LoggerFactory.getLogger(MicaCompletedState.class);

  @Override
  protected void addUserActions(Set<ActionType> types) {
    // Don't allow any action since we complete the interview when we enter this state.
    // If the rule of completing the interview changes (is put somewhere else) we could
    // allow user actions.

    // Another solution is to set the Interview to IN_PROGRESS when we exit this state.
  }

  @Override
  public void onEntry(TransitionEvent event) {
    super.onEntry(event);
    log.debug("Setting interview state to {}", InterviewStatus.COMPLETED);
    super.activeInterviewService.setStatus(InterviewStatus.COMPLETED);
  }

  @Override
  public boolean isCompleted() {
    return true;
  }

  public String getName() {
    return "Completed";
  }

  @Override
  public ActionType getStartingActionType() {
    return ActionType.EXECUTE;
  }

}
