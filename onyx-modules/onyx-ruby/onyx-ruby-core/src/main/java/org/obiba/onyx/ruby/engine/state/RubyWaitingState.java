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

import org.obiba.onyx.engine.ActionType;
import org.obiba.onyx.engine.state.StageState;
import org.obiba.onyx.engine.state.TransitionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Ruby WAITING state.
 */
public class RubyWaitingState extends AbstractRubyStageState {
  //
  // Constants
  //

  private static final Logger log = LoggerFactory.getLogger(RubyWaitingState.class);

  //
  // AbstractRubyStageState Methods
  //

  public String getName() {
    return StageState.Waiting.toString();
  }

  @Override
  protected void addUserActions(Set<ActionType> types) {
    // No possible user actions
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
