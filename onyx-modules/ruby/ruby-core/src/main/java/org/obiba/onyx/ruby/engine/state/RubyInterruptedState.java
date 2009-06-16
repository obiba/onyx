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
import org.obiba.onyx.engine.state.TransitionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Ruby INTERRUPTED state.
 */
public class RubyInterruptedState extends AbstractRubyStageState {
  //
  // Constants
  //

  private static final Logger log = LoggerFactory.getLogger(RubyInterruptedState.class);

  //
  // AbstractRubyStageState Methods
  //

  public String getName() {
    return "Interrupted";
  }

  @Override
  protected void addUserActions(Set<ActionType> types) {
    types.add(ActionType.STOP);
    types.add(ActionType.EXECUTE);
  }

  @Override
  public void stop(Action action) {
    log.debug("Ruby Stage {} is canceling", super.getStage().getName());

    activeTubeRegistrationService.deleteParticipantTubeRegistration();

    if(areDependenciesCompleted() != null && areDependenciesCompleted()) {
      castEvent(TransitionEvent.CANCEL);
    } else {
      castEvent(TransitionEvent.INVALID);
    }
  }

  @Override
  public void execute(Action action) {
    log.debug("Ruby Stage {} is resuming", super.getStage().getName());
    castEvent(TransitionEvent.RESUME);
  }

  public boolean wantTransitionEvent(TransitionEvent event) {
    // ONYX-428
    if(event.equals(TransitionEvent.VALID)) {
      return false;
    }
    return super.wantTransitionEvent(event);
  }
}
