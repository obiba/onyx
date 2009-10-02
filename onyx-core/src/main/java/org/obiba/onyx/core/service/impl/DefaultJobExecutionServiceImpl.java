/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.service.impl;

import java.util.Map;

import org.obiba.onyx.core.service.JobExecutionService;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;

public class DefaultJobExecutionServiceImpl implements JobExecutionService {

  private JobLauncher jobLauncher;

  public ExitStatus launchJob(Job job, Map<String, JobParameter> parameters) {

    try {
      JobExecution jobExecution = jobLauncher.run(job, new JobParameters(parameters));
      return jobExecution.getExitStatus();
    } catch(JobExecutionAlreadyRunningException e) {
      throw new RuntimeException("This job is currently running", e);
    } catch(JobInstanceAlreadyCompleteException e) {
      throw new RuntimeException("This was already run.  Maybe you need to change the input parameters?");
    } catch(JobRestartException e) {
      throw new RuntimeException("Unspecified restart exception", e);
    }
  }

  public void setJobLauncher(JobLauncher jobLauncher) {
    this.jobLauncher = jobLauncher;
  }

}
