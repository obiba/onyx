package org.obiba.onyx.webapp.ws;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.PageParameters;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.core.purge.PurgeParticipantDataTasklet;
import org.obiba.onyx.core.service.JobExecutionService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameter;

public class PurgeWebService extends JsonWebServicePage {

  @SpringBean(name = "purgeParticipantDataJob")
  private Job purgeParticipantDataJob;

  @SpringBean
  private JobExecutionService jobExecutionService;

  public PurgeWebService(PageParameters params) {
    super(params, PurgeStatus.class);
  }

  @Override
  protected boolean isAuthorized() {
    return hasRole("SYSTEM_ADMINISTRATOR");
  }

  @Override
  public void doPost(PageParameters params) {
    PurgeStatus status = executePurge();
    setStatus(200);
    setDefaultModel(new Model<PurgeStatus>(status));
  }

  private PurgeStatus executePurge() {
    Map<String, JobParameter> jobParameterMap = new HashMap<String, JobParameter>();
    jobParameterMap.put("date", new JobParameter(new Date()));
    JobExecution jobExecution = jobExecutionService.launchJob(purgeParticipantDataJob, jobParameterMap);
    boolean jobCompleted = jobExecution.getExitStatus().getExitCode().equals("COMPLETED");

    int participantsDeleted = jobExecution.getExecutionContext().containsKey(PurgeParticipantDataTasklet.TOTAL_DELETED)
        ? jobExecution.getExecutionContext().getInt(PurgeParticipantDataTasklet.TOTAL_DELETED)
        : 0;

    return new PurgeStatus(jobExecution.getExitStatus().getExitCode(), participantsDeleted, jobCompleted);
  }

  public static class PurgeStatus implements Serializable {

    String exitCode = "";

    int participantsDeleted = 0;

    boolean jobCompleted = false;

    public PurgeStatus(String exitCode, int participantsDeleted, boolean jobCompleted) {
      this.exitCode = exitCode;
      this.participantsDeleted = participantsDeleted;
      this.jobCompleted = jobCompleted;
    }
  }

}
