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
 * Ruby READY state.
 */
public class RubyReadyState extends AbstractRubyStageState {
  //
  // Constants
  //

  private static final Logger log = LoggerFactory.getLogger(RubyReadyState.class);

  //
  // AbstratRubyStageState Methods
  //

  public String getName() {
    return "Waiting";
  }

  @Override
  protected void addUserActions(Set<ActionType> types) {
    types.add(ActionType.EXECUTE);
    types.add(ActionType.SKIP);
  }

  @Override
  public void execute(Action action) {
    super.execute(action);
    log.info("Ruby Stage {} is starting", super.getStage().getName());
    castEvent(TransitionEvent.START);
  }

  @Override
  public void skip(Action action) {
    super.skip(action);
    log.info("Ruby Stage {} is skipping", super.getStage().getName());
    castEvent(TransitionEvent.SKIP);
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
