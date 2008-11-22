/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.mica.engine.state;

import java.util.Set;

import org.obiba.onyx.engine.ActionType;
import org.obiba.onyx.engine.state.ITransitionListener;
import org.obiba.onyx.engine.state.TransitionEvent;

/**
 */
public class MicaWaitingState extends AbstractMicaStageState implements ITransitionListener {

  public String getName() {
    return "Waiting";
  }

  @Override
  protected void addUserActions(Set<ActionType> types) {
    // No user action possible
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
