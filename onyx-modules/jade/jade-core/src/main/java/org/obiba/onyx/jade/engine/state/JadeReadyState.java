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
import org.obiba.onyx.engine.state.TransitionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Jade Ready State, goes there each time a state is cancelled.
 */
public class JadeReadyState extends AbstractJadeStageState {

  private static final Logger log = LoggerFactory.getLogger(JadeReadyState.class);

  @Override
  protected void addUserActions(Set<ActionType> types) {
    types.add(ActionType.EXECUTE);
    types.add(ActionType.SKIP);
  }

  @Override
  public void execute(Action action) {
    super.execute(action);
    log.info("Jade Stage {} is starting", super.getStage().getName());
    castEvent(TransitionEvent.START);
  }

  @Override
  public void skip(Action action) {
    super.skip(action);
    log.info("Jade Stage {} is skipping", super.getStage().getName());
    castEvent(TransitionEvent.SKIP);
  }

  public String getName() {
    return "Ready";
  }

  @Override
  protected boolean wantTransitionEvent(TransitionEvent transitionEvent) {
    if(transitionEvent.equals(TransitionEvent.VALID)) {
      return false;
    } else {
      return true;
    }
  }
}
