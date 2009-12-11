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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obiba.core.util.StringUtil;
import org.quartz.JobExecutionContext;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.JobLocator;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * Used to execute a Spring Batch defined job with the Quartz scheduler.
 */
public class OnyxJobDetailDelegate extends QuartzJobBean {

  /**
   * Special key in job data map for the name of a job to run.
   */
  static final String JOB_NAME = "jobName";

  private static Log log = LogFactory.getLog(OnyxJobDetailDelegate.class);

  private JobLocator jobLocator;

  private JobOperator jobOperator;

  /**
   * Public setter for the {@link JobLocator}.
   * @param jobLocator the {@link JobLocator} to set
   */
  public void setJobLocator(JobLocator jobLocator) {
    this.jobLocator = jobLocator;
  }

  /**
   * Public setter for the {@link JobOperator}.
   * @param jobOperator the {@link JobOperator} to set
   */
  public void setJobOperator(JobOperator jobOperator) {
    this.jobOperator = jobOperator;
  }

  @SuppressWarnings("unchecked")
  protected void executeInternal(JobExecutionContext context) {
    Map<String, Object> jobDataMap = context.getMergedJobDataMap();
    String jobName = (String) jobDataMap.get(JOB_NAME);
    log.info("Quartz trigger firing with Spring Batch jobName=" + jobName);

    try {
      Job job = jobLocator.getJob(jobName);
      if(job.getJobParametersIncrementer() != null) {
        jobOperator.startNextInstance(jobName);
      } else {
        JobParameters jobParameters = getJobParametersFromJobMap(jobDataMap);
        jobOperator.start(jobName, toKeyValueCommaDelimitedString(jobParameters));
      }
    } catch(Exception e) {
      log.error("Could not execute job.", e);
    }
  }

  /*
   * Copy parameters that are of the correct type over to {@link JobParameters}, ignoring jobName.
   * 
   * @return a {@link JobParameters} instance
   */
  private JobParameters getJobParametersFromJobMap(Map<String, Object> jobDataMap) {

    JobParametersBuilder builder = new JobParametersBuilder();

    for(Entry<String, Object> entry : jobDataMap.entrySet()) {
      String key = entry.getKey();
      Object value = entry.getValue();
      if(value instanceof String && !key.equals(JOB_NAME)) {
        builder.addString(key, (String) value);
      } else if(value instanceof Float || value instanceof Double) {
        builder.addDouble(key, ((Number) value).doubleValue());
      } else if(value instanceof Integer || value instanceof Long) {
        builder.addLong(key, ((Number) value).longValue());
      } else if(value instanceof Date) {
        builder.addDate(key, (Date) value);
      } else {
        log.debug("JobDataMap contains values which are not job parameters (ignoring).");
      }
    }

    return builder.toJobParameters();

  }

  private String toKeyValueCommaDelimitedString(JobParameters jobParameters) {
    if(jobParameters.getParameters().size() != 0) {
      List<String> keyValuePairs = new ArrayList<String>();
      for(Map.Entry<String, JobParameter> entry : jobParameters.getParameters().entrySet()) {
        keyValuePairs.add(entry.getKey() + '=' + entry.getValue());
      }
      return StringUtil.collectionToString(keyValuePairs);
    }
    return null;
  }
}
