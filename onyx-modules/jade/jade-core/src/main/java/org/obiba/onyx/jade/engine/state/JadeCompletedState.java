/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
/**
 * 
 */
package org.obiba.onyx.jade.engine.state;

import java.util.Set;

import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.ActionType;
import org.obiba.onyx.engine.state.StageState;
import org.obiba.onyx.engine.state.TransitionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JadeCompletedState extends AbstractJadeStageState {

  private static final Logger log = LoggerFactory.getLogger(JadeCompletedState.class);

  public String getName() {
    return StageState.Completed.toString();
  }

  @Override
  protected void addUserActions(Set<ActionType> types) {
    types.add(ActionType.STOP);
  }

  @Override
  public void stop(Action action) {
    super.execute(action);
    log.debug("Jade Stage {} is cancelling", super.getStage().getName());
    cancelInstrumentRuns();
    if(areDependenciesCompleted() != null && areDependenciesCompleted()) {
      castEvent(TransitionEvent.CANCEL);
    } else {
      castEvent(TransitionEvent.INVALID);
    }
  }

  @Override
  protected boolean wantTransitionEvent(TransitionEvent transitionEvent) {
    // We don't need this event since we're already done. This can happen when our stage depends on multiple stages but
    // not exclusively.
    if(transitionEvent == TransitionEvent.VALID) {
      return false;
    }
    if(transitionEvent.equals(TransitionEvent.NOTAPPLICABLE) || transitionEvent.equals(TransitionEvent.INVALID)) {
      cancelInstrumentRuns();
    }
    return super.wantTransitionEvent(transitionEvent);
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
