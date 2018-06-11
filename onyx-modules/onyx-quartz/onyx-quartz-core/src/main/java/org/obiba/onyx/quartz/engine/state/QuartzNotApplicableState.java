/***********************************************************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 **********************************************************************************************************************/
/**
 * State notApplicable for Quartz module
 * 
 * @author acarey
 * 
 * Coming from states: waiting, ready, skipped, completed
 */
package org.obiba.onyx.quartz.engine.state;

import java.util.Set;

import org.obiba.onyx.engine.ActionType;
import org.obiba.onyx.engine.state.StageState;
import org.obiba.onyx.engine.state.TransitionEvent;

public class QuartzNotApplicableState extends AbstractQuartzStageState {

  public String getName() {
    return StageState.NotApplicable.toString();
  }

  @Override
  protected void addUserActions(Set<ActionType> types) {
    // No user actions available when waiting
  }

  @Override
  public boolean isCompleted() {
    return true;
  }

  @Override
  protected boolean wantTransitionEvent(TransitionEvent transitionEvent) {
    if(transitionEvent.equals(TransitionEvent.NOTAPPLICABLE)) {
      return false;
    } else {
      return true;
    }
  }
}
