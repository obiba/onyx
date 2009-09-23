/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.engine.state;

import org.obiba.onyx.core.domain.stage.StageTransition;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.engine.Action;

/**
 * A transition listener that logs (i.e., persists) transitions.
 */
public class LoggingTransitionListener implements ITransitionListener {
  //
  // Instance Variables
  //

  private ActiveInterviewService activeInterviewService;

  //
  // ITransitionListener Methods
  //

  public void onTransition(IStageExecution execution, StageState fromState, TransitionEvent event) {
    if(execution instanceof StageExecutionContext) {
      Action currentAction = activeInterviewService.getCurrentAction();

      if(currentAction != null) {
        StageTransition stageTransition = new StageTransition();
        stageTransition.setStage(((StageExecutionContext) execution).getStage().getName());
        stageTransition.setInterview(currentAction.getInterview());
        stageTransition.setFromState(fromState);
        stageTransition.setToState(StageState.valueOf(execution.getName()));
        stageTransition.setEvent(event.getName());

        currentAction.addStageTransition(stageTransition);
        activeInterviewService.updateAction(currentAction);
      }
    }
  }

  //
  // Methods
  //

  public void setActiveInterviewService(ActiveInterviewService activeInterviewService) {
    this.activeInterviewService = activeInterviewService;
  }
}
