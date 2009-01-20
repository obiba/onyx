/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.marble.engine.state;

import java.util.Arrays;
import java.util.Set;

import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.ActionType;
import org.obiba.onyx.engine.state.TransitionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MarbleCompletedState extends AbstractMarbleStageState {

  private static final Logger log = LoggerFactory.getLogger(MarbleCompletedState.class);

  public String getName() {
    return "Completed";
  }

  @Override
  protected void addUserActions(Set<ActionType> types) {
    types.add(ActionType.STOP);
    log.info("Marble Completed state has these actions {}", Arrays.toString(types.toArray()));
  }

  @Override
  public void stop(Action action) {
    super.execute(action);
    log.info("Marble Stage {} is cancelling", super.getStage().getName());
    castEvent(TransitionEvent.CANCEL);
    consentService.deletePreviousConsent(activeInterviewService.getInterview());
  }

  @Override
  public boolean isCompleted() {
    return true;
  }

  @Override
  public ActionType getStartingActionType() {
    return ActionType.EXECUTE;
  }

}
