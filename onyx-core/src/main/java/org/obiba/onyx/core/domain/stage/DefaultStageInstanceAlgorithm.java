/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.domain.stage;

import java.util.ArrayList;
import java.util.List;

import org.obiba.onyx.engine.state.StageState;

/**
 * Default algorithm for deriving a list of {@link StageInstance}s from a list of {@link StageTransition}s.
 * 
 * Based on the pseudo-code provided in the software specification:
 * http://wiki.obiba.org/confluence/display/ONYX/Improve+Stage+Duration+recording
 */
public class DefaultStageInstanceAlgorithm implements IStageInstanceAlgorithm {

  public List<StageInstance> getStageInstances(List<StageTransition> stageTransitions) {
    List<StageInstance> stageInstances = new ArrayList<StageInstance>();

    StageInstance stageInstance = null;

    for(StageTransition stageTransition : stageTransitions) {
      if(stageInstance == null) {
        stageInstance = createStageInstance(stageTransition);
        stageInstances.add(stageInstance);
      }

      if(stageTransition.getFromState().equals(StageState.Ready) && stageTransition.getToState().equals(StageState.InProgress)) {
        if(stageInstance.getDuration() == 0) {
          // Previous transitions were just "administrative."
          // StageInstance actually begins with this transition.
          stageInstance.setStartTime(stageTransition.getAction().getDateTime());
          stageInstance.setLastTime(stageTransition.getAction().getDateTime());
        } else {
          // New StageInstance.
          stageInstance = createStageInstance(stageTransition);
          stageInstances.add(stageInstance);
        }

        // Update the stage instance's user to the user who started the stage.
        stageInstance.setUser(stageTransition.getAction().getUser());
      }

      // When leaving the IN_PROGRESS state, increment the StageInstance's duration by
      // the time spent in that state.
      if(stageTransition.getFromState().equals(StageState.InProgress)) {
        long timeInProgressMillis = stageTransition.getAction().getDateTime().getTime() - stageInstance.getLastTime().getTime();
        stageInstance.incrementDuration((int) (timeInProgressMillis / 1000l));
      }

      // When entering the INTERRUPTED state, increment the StageInstance's interruptedCount.
      if(stageTransition.getToState().equals(StageState.Interrupted)) {
        stageInstance.incrementInterruptionCount();
      }

      // Record the last state reached.
      stageInstance.setLastStageState(stageTransition.getToState());
      stageInstance.setLastTime(stageTransition.getAction().getDateTime());
    }

    // Mark the last StageInstance (if there is one) as the LAST one for the given stage.
    if(stageInstance != null) {
      stageInstance.setLast(true);
    }

    return stageInstances;
  }

  private StageInstance createStageInstance(StageTransition stageTransition) {
    StageInstance stageInstance = new StageInstance();

    stageInstance.setInterview(stageTransition.getInterview());
    stageInstance.setStage(stageTransition.getStage());
    stageInstance.setUser(stageTransition.getAction().getUser());
    stageInstance.setStartTime(stageTransition.getAction().getDateTime());
    stageInstance.setLastTime(stageTransition.getAction().getDateTime());
    stageInstance.setDuration(0);

    return stageInstance;
  }
}
