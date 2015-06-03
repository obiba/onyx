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

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.obiba.core.service.SortingClause;
import org.obiba.core.service.impl.PersistenceManagerAwareService;
import org.obiba.onyx.core.domain.statistics.AppointmentUpdateLog;
import org.obiba.onyx.core.domain.statistics.AppointmentUpdateStats;
import org.obiba.onyx.core.service.AppointmentManagementService;
import org.obiba.onyx.core.service.JobExecutionService;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class DefaultAppointmentManagementServiceImpl extends PersistenceManagerAwareService
    implements AppointmentManagementService, ResourceLoaderAware {

  private final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");

  private JobExplorer jobExplorer;

  private Job job;

  private JobExecutionService jobExecutionService;

  private String inputDirectory;

  private String outputDirectory;

  private File inputDir;

  private File outputDir;

  private ResourceLoader resourceLoader;

  public void initialize() {
    if(inputDirectory == null || inputDirectory.isEmpty())
      throw new IllegalArgumentException("DefaultAppointmentManagementServiceImpl: InputDirectory should not be null");

    try {
      setInputDir(resourceLoader.getResource(inputDirectory).getFile());
      if(!getInputDir().exists()) {
        getInputDir().mkdirs();
      }
      if(!getInputDir().isDirectory()) {
        throw new IllegalArgumentException(
            "DefaultAppointmentManagementServiceImpl: InputDirectory " + getInputDir().getAbsolutePath() +
                " is not a directory");
      }

      if(outputDirectory != null && !outputDirectory.isEmpty()) {
        setOutputDir(resourceLoader.getResource(outputDirectory).getFile());
        if(!getOutputDir().exists()) {
          getOutputDir().mkdirs();
        }
        if(!getOutputDir().isDirectory()) {
          throw new IllegalArgumentException(
              "DefaultAppointmentManagementServiceImpl: OutputDirectory " + getOutputDir().getAbsolutePath() +
                  " is not a directory");
        }
      }
    } catch(IOException ex) {
      throw new RuntimeException("DefaultAppointmentManagementServiceImpl: Failed to access directory - " + ex);
    }
  }

  // Do not use transaction here as Spring Batch will never use only one transaction for a whole job (unless the job
  // has a single step): Spring Batch handles transactions at the step level. A tasklet is transactional?Spring Batch
  // creates and commits a transaction for each chunk in a chunk-oriented step.
  @Transactional(propagation = Propagation.NOT_SUPPORTED)
  synchronized public ExitStatus updateAppointments() {

    Map<String, JobParameter> jobParameterMap = new HashMap<String, JobParameter>();
    jobParameterMap.put("date", new JobParameter(new Date()));
    JobExecution jobExecution = jobExecutionService.launchJob(job, jobParameterMap);

    return jobExecution.getExitStatus();

  }

  @Override
  public void saveAppointmentUpdateStats(AppointmentUpdateStats appointmentUpdateStats) {
    getPersistenceManager().save(appointmentUpdateStats);
  }

  @Override
  public AppointmentUpdateStats getLastAppointmentUpdateStats() {
    return getPersistenceManager().list(AppointmentUpdateStats.class, new SortingClause("date", false)).get(0);
  }

  @Override
  public List<AppointmentUpdateLog> getLogListForDate(Date date) {
    List<AppointmentUpdateLog> logList = new ArrayList<AppointmentUpdateLog>();
    List<JobInstance> jobsList = jobExplorer.getJobInstances(job.getName(), 0, 10);

    JobExecution jobExecution = null;

    // note: do not care about time zones as every thing is local
    String dateStr = DATE_FORMAT.format(date);
    for(JobInstance jobInstance : jobsList) {
      List<JobExecution> jobExecutions = jobExplorer.getJobExecutions(jobInstance);
      for(JobExecution jobExec : jobExecutions) {
        Date jobDate = jobExec.getJobParameters().getDate("date");
        String jobDateStr = DATE_FORMAT.format(jobDate);
        if(jobDateStr.equals(dateStr)) {
          jobExecution = jobExec;
          break;
        }
      }
      if(jobExecution != null) {
        break;
      }
    }

    if(jobExecution == null) return null;

    for(StepExecution stepExec : jobExecution.getStepExecutions()) {
      StepExecution stepExecution = jobExplorer.getStepExecution(jobExecution.getId(), stepExec.getId());
      if(stepExecution.getExecutionContext().get("logList") != null) {
        logList.addAll((List<AppointmentUpdateLog>) (stepExecution.getExecutionContext().get("logList")));
      }
    }

    return logList;
  }

  public void setInputDirectory(String inputDirectory) {
    this.inputDirectory = inputDirectory;
  }

  public void setOutputDirectory(String outputDirectory) {
    this.outputDirectory = outputDirectory;
  }

  @Override
  public void setResourceLoader(ResourceLoader resourceLoader) {
    this.resourceLoader = resourceLoader;
  }

  public File getInputDir() {
    return inputDir;
  }

  public void setInputDir(File inputDir) {
    this.inputDir = inputDir;
  }

  public void setOutputDir(File outputDir) {
    this.outputDir = outputDir;
  }

  public File getOutputDir() {
    return outputDir;
  }

  public void setJob(Job job) {
    this.job = job;
  }

  public void setJobExplorer(JobExplorer jobExplorer) {
    this.jobExplorer = jobExplorer;
  }

  public void setJobExecutionService(JobExecutionService jobExecutionService) {
    this.jobExecutionService = jobExecutionService;
  }

}
