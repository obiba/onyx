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

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.impl.triggers.SimpleTriggerImpl;

/**
 * A trigger that never fires (for disabling schedulable jobs).
 */
public class NullTrigger extends SimpleTriggerImpl {

  private static final long serialVersionUID = -6749825472910930069L;

  @Override
  public CompletedExecutionInstruction executionComplete(JobExecutionContext context, JobExecutionException result) {
    return CompletedExecutionInstruction.NOOP;
  }
}
