/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.ruby.engine.state;

import java.util.Set;

import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.ActionType;
import org.obiba.onyx.engine.state.StageState;
import org.obiba.onyx.engine.state.TransitionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Ruby COMPLETED state.
 */
public class RubyCompletedState extends AbstractRubyStageState {
  //
  // Constants
  //

  private static final Logger log = LoggerFactory.getLogger(RubyCompletedState.class);

  //
  // AbstractRubyStageState Methods
  //

  public String getName() {
    return StageState.Completed.toString();
  }

  @Override
  protected void addUserActions(Set<ActionType> types) {
    types.add(ActionType.STOP);
  }

  @Override
  public void stop(Action action) {
    log.debug("Ruby Stage {} is cancelling", super.getStage().getName());

    if(areDependenciesCompleted() != null && areDependenciesCompleted()) {
      castEvent(TransitionEvent.CANCEL);
    } else {
      castEvent(TransitionEvent.INVALID);
    }
  }

  @Override
  public boolean isCompleted() {
    return true;
  }

  @Override
  public ActionType getStartingActionType() {
    return ActionType.EXECUTE;
  }

  @Override
  public void onExit(TransitionEvent event) {
    // ONYX-366: Delete participant tube registration whenever Ruby leaves the COMPLETED state.
    deleteParticipantTubeRegistration();
  }
}
