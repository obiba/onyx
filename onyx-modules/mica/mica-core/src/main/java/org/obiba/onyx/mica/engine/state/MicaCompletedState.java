/***********************************************************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 **********************************************************************************************************************/
/**
 * 
 */
package org.obiba.onyx.mica.engine.state;

import java.util.Set;

import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.ActionType;
import org.obiba.onyx.engine.state.TransitionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MicaCompletedState extends AbstractMicaStageState {

  private static final Logger log = LoggerFactory.getLogger(MicaCompletedState.class);

  // public void afterPropertiesSet() throws Exception {
  // addAction(ActionDefinitionBuilder.CANCEL_ACTION);
  // }

  @Override
  protected void addUserActions(Set<ActionType> types) {
    types.add(ActionType.STOP);
  }

  @Override
  public void stop(Action action) {
    super.execute(action);
    log.info("Mica Stage {} is cancelling", super.getStage().getName());
    castEvent(TransitionEvent.CANCEL);
  }

  @Override
  public boolean isCompleted() {
    return true;
  }

  public String getName() {
    return "Completed";
  }

  @Override
  public ActionType getStartingActionType() {
    return ActionType.EXECUTE;
  }

}
