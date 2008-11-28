/***********************************************************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 **********************************************************************************************************************/
/**
 * State skipped for Quartz module
 * 
 * @author acarey
 * 
 * Coming from states: ready Possible forward states/actions/transitions: cancel, notApplicable
 */
package org.obiba.onyx.quartz.engine.state;

import java.util.Set;

import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.ActionType;
import org.obiba.onyx.engine.state.IStageExecution;
import org.obiba.onyx.engine.state.TransitionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuartzSkippedState extends AbstractQuartzStageState {

  private static final Logger log = LoggerFactory.getLogger(QuartzSkippedState.class);

  public String getName() {
    return "Skipped";
  }

  @Override
  protected void addUserActions(Set<ActionType> types) {
    types.add(ActionType.STOP);
  }

  @Override
  public void stop(Action action) {
    log.info("Quartz Stage {} is cancelling", super.getStage().getName());
    if(areDependenciesCompleted() != null && areDependenciesCompleted()) {
      castEvent(TransitionEvent.CANCEL);
    } else {
      castEvent(TransitionEvent.INVALID);
    }
  }

  @Override
  public void onTransition(IStageExecution execution, TransitionEvent event) {
    // case not applicable transition
    Boolean var = areDependenciesCompleted();
    if(var != null && var == false) castEvent(TransitionEvent.NOTAPPLICABLE);
  }

  @Override
  public boolean isCompleted() {
    return true;
  }

  @Override
  public ActionType getStartingActionType() {
    return ActionType.SKIP;
  }
}
