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
import org.obiba.onyx.engine.variable.export.OnyxDataExport;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameter;

public class ExportWebService extends JsonWebServicePage {

  @SpringBean
  private OnyxDataExport onyxDataExport;

  public ExportWebService(PageParameters params) {
    super(params, ExportStatus.class);
  }

  @Override
  protected boolean isAuthorized() {
    return hasRole("PARTICIPANT_MANAGER");
  }

  @Override
  public void doPost(PageParameters params) {
    ExportStatus status = executeExport();
    setStatus(200);
    setDefaultModel(new Model<ExportStatus>(status));
  }

  private ExportStatus executeExport() {
    try {
      onyxDataExport.exportInterviews();
    } catch(Exception e) {
      throw new RuntimeException("Data export failed", e);
    }
    return new ExportStatus(onyxDataExport.getOutputRootDirectory().getAbsolutePath());
  }

  public static class ExportStatus implements Serializable {

    String destination = "";

    public ExportStatus(String destination) {
      this.destination = destination;
    }
  }

}
