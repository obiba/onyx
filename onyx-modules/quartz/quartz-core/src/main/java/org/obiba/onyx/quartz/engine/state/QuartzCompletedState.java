/***********************************************************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 **********************************************************************************************************************/
/**
 * State complete for Quartz module
 * 
 * @author acarey
 * 
 * Coming from states: inProgress Possible forward states/actions/transitions: notApplicable, cancel, resume On cancel
 * and notApplicable transitions, current questionnaireParticipant is deleted from database (with its dependencies)
 */
package org.obiba.onyx.quartz.engine.state;

import java.util.Set;

import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.ActionType;
import org.obiba.onyx.engine.state.TransitionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuartzCompletedState extends AbstractQuartzStageState {

  private static final Logger log = LoggerFactory.getLogger(QuartzCompletedState.class);

  public String getName() {
    return "Completed";
  }

  @Override
  protected void addUserActions(Set<ActionType> types) {
    types.add(ActionType.STOP);
    types.add(ActionType.EXECUTE);
  }

  @Override
  public void stop(Action action) {
    log.info("Quartz Stage {} is cancelling", super.getStage().getName());
    cancelQuestionnaireParticipant();
    if(areDependenciesCompleted() != null && areDependenciesCompleted()) {
      castEvent(TransitionEvent.CANCEL);
    } else {
      castEvent(TransitionEvent.INVALID);
    }
  }

  @Override
  protected boolean wantTransitionEvent(TransitionEvent transitionEvent) {
    if(transitionEvent.equals(TransitionEvent.NOTAPPLICABLE)) cancelQuestionnaireParticipant();
    return super.wantTransitionEvent(transitionEvent);
  }

  @Override
  public boolean isCompleted() {
    return true;
  }

  @Override
  public ActionType getStartingActionType() {
    return ActionType.EXECUTE;
  }

  @Override
  public void execute(Action action) {
    log.info("Quartz Stage {} is resuming", super.getStage().getName());
    inactivateQuestionnaireParticipant();
    castEvent(TransitionEvent.RESUME);
  }
}
