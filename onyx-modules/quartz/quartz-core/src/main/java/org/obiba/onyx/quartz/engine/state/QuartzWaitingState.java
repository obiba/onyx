/***********************************************************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 **********************************************************************************************************************/
/**
 * State waiting for Quartz module
 * 
 * @author acarey
 * 
 * Coming from states: ready Possible forward states/actions/transitions: valid, notApplicable
 */
package org.obiba.onyx.quartz.engine.state;

import java.util.Set;

import org.obiba.onyx.engine.ActionType;
import org.obiba.onyx.engine.state.StageState;
import org.obiba.onyx.engine.state.TransitionEvent;

public class QuartzWaitingState extends AbstractQuartzStageState {

  public String getName() {
    return StageState.Waiting.toString();
  }

  @Override
  protected void addUserActions(Set<ActionType> types) {
    // No user actions available when waiting
  }

  @Override
  protected boolean wantTransitionEvent(TransitionEvent transitionEvent) {
    if(transitionEvent.equals(TransitionEvent.INVALID)) {
      return false;
    } else {
      return true;
    }
  }

}
