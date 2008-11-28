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

import java.util.Set;

import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.ActionType;
import org.obiba.onyx.engine.state.TransitionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Marble Ready State
 */
public class MarbleReadyState extends AbstractMarbleStageState {

  private static final Logger log = LoggerFactory.getLogger(MarbleReadyState.class);

  @Override
  protected void addUserActions(Set<ActionType> types) {
    types.add(ActionType.EXECUTE);
  }

  @Override
  public void execute(Action action) {
    super.execute(action);
    log.info("Marble Stage {} is starting", super.getStage().getName());
    castEvent(TransitionEvent.START);
  }

  public String getName() {
    return "Ready";
  }

}
