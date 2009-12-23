/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.batch;

import java.util.Date;

import org.quartz.Calendar;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Trigger;

/**
 * A trigger that never fires (for disabling schedulable jobs).
 */
public class NullTrigger extends Trigger {
  //
  // Constants
  //

  private static final long serialVersionUID = 1L;

  //
  // Trigger Methods
  //

  @Override
  public Date computeFirstFireTime(Calendar arg0) {
    return null;
  }

  @Override
  public int executionComplete(JobExecutionContext arg0, JobExecutionException arg1) {
    return INSTRUCTION_NOOP;
  }

  @Override
  public Date getEndTime() {
    return null;
  }

  @Override
  public Date getFinalFireTime() {
    return null;
  }

  @Override
  public Date getFireTimeAfter(Date arg0) {
    return null;
  }

  @Override
  public Date getNextFireTime() {
    return null;
  }

  @Override
  public Date getPreviousFireTime() {
    return null;
  }

  @Override
  public Date getStartTime() {
    return null;
  }

  @Override
  public boolean mayFireAgain() {
    return false;
  }

  @Override
  public void setEndTime(Date arg0) {
    // no-op
  }

  @Override
  public void setStartTime(Date arg0) {
    // no-op
  }

  @Override
  public void triggered(Calendar arg0) {
    // no-op
  }

  @Override
  public void updateAfterMisfire(Calendar arg0) {
    // no-op
  }

  @Override
  public void updateWithNewCalendar(Calendar arg0, long arg1) {
    // no-op
  }

  @Override
  protected boolean validateMisfireInstruction(int arg0) {
    return false;
  }

}
