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

import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.springframework.scheduling.quartz.CronTriggerBean;

/**
 * Factory for creating a job triggers.
 */
public class JobTriggerFactory {
  //
  // Static Methods
  //

  public static Trigger newTrigger(JobDetail jobDetail, String cronExpression) throws Exception {
    if(jobDetail != null && cronExpression != null && cronExpression.trim().length() != 0) {
      CronTriggerBean cronTrigger = new CronTriggerBean();
      cronTrigger.setJobDetail(jobDetail);
      cronTrigger.setJobGroup(jobDetail.getGroup());
      cronTrigger.setJobName(jobDetail.getJobDataMap().getString(OnyxJobDetailDelegate.JOB_NAME));
      cronTrigger.setCronExpression(cronExpression);
      return cronTrigger;
    } else {
      return new NullTrigger();
    }
  }
}
