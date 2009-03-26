/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.engine;

import java.util.Date;
import java.util.List;

/**
 * Determines the start and end time stamps for a {@link Stage} when initialized with an ordered List of {@link Action}s
 * associated with that {@link Stage}.
 */
public class TimeStampAlgorithm {
  private Date startTimeStamp = null;

  private Date endTimeStamp = null;

  private boolean stageWasCompletedOnce = false;

  public TimeStampAlgorithm(List<Action> actions) {

    // Iterate on all actions (in the order they were executed)
    for(Action action : actions) {
      switch(action.getActionType()) {
      case EXECUTE:
        // This is either the first start, a start after interrupt or start after stop (delete)
        // We set sts only if this is not a resume
        // Resume is EXECUTE after an interruption or after completion
        if(startTimeStamp == null) {
          startTimeStamp = action.getDateTime();
        }
        break;
      case INTERRUPT:
        // Interrupt: we set ets only when the stage was not already completed
        if(stageWasCompletedOnce == false) {
          endTimeStamp = action.getDateTime();
        }
        break;
      case COMPLETE:
        // We only want to capture the first time we complete
        if(stageWasCompletedOnce == false) {
          endTimeStamp = action.getDateTime();
        }
        stageWasCompletedOnce = true;
        break;
      case STOP:
        // We're being deleted: erase history.
        startTimeStamp = null;
        endTimeStamp = null;
        stageWasCompletedOnce = false;
        break;
      case SKIP:
        // When skipping both start and end are set
        startTimeStamp = action.getDateTime();
        endTimeStamp = action.getDateTime();
        stageWasCompletedOnce = false;
        break;
      }
    }
  }

  public Date getStartTimeStamp() {
    return startTimeStamp == null ? null : new Date(startTimeStamp.getTime());
  }

  public Date getEndTimeStamp() {
    return endTimeStamp == null ? null : new Date(endTimeStamp.getTime());
  }

  @Override
  public String toString() {
    return "TimeStampAlgorithm sts=[" + startTimeStamp + "] ets=[" + endTimeStamp + "]";
  }
}
