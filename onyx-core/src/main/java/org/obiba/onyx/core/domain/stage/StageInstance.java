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

import java.util.Date;

import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.core.domain.user.User;
import org.obiba.onyx.engine.state.StageState;

/**
 * A <code>StageInstance</code> is a summary of a single "run" of a <code>Stage</code>, where a "run" is a sequence
 * of transition events beginning from the READY state.
 */
public class StageInstance implements Comparable<StageInstance> {
  //
  // Instance Variables
  //

  private String stage;

  private Interview interview;

  private User user;

  private Date startTime;

  private Date lastTime;

  private StageState lastState;

  private int duration;

  private int interruptionCount;

  private boolean last;

  //
  // Comparable Methods
  //

  public int compareTo(StageInstance o) {
    return startTime.compareTo(o.getStartTime());
  }

  //
  // Methods
  //

  public String getStage() {
    return stage;
  }

  public void setStage(String stage) {
    this.stage = stage;
  }

  public Interview getInterview() {
    return interview;
  }

  public void setInterview(Interview interview) {
    this.interview = interview;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public Date getStartTime() {
    return startTime;
  }

  public void setStartTime(Date startTime) {
    this.startTime = startTime;
  }

  public Date getLastTime() {
    return lastTime;
  }

  public void setLastTime(Date lastTime) {
    this.lastTime = lastTime;
  }

  public StageState getLastState() {
    return lastState;
  }

  public void setLastStageState(StageState lastState) {
    this.lastState = lastState;
  }

  public int getDuration() {
    return duration;
  }

  public void setDuration(int duration) {
    this.duration = duration;
  }

  public void incrementDuration(int seconds) {
    duration += seconds;
  }

  public int getInterruptionCount() {
    return interruptionCount;
  }

  public void incrementInterruptionCount() {
    interruptionCount++;
  }

  public boolean isLast() {
    return last;
  }

  public void setLast(boolean last) {
    this.last = last;
  }
}
