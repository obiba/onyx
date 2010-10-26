/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.webapp.administration.panel;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.core.service.JobExecutionService;
import org.obiba.onyx.wicket.model.OnyxDataPurgeModel;
import org.obiba.onyx.wicket.reusable.Dialog;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameter;

public class PurgeDialog extends Dialog {

  private static final long serialVersionUID = 1L;

  private static final int DEFAULT_INITIAL_HEIGHT = 13;

  private static final int DEFAULT_INITIAL_WIDTH = 31;

  @SpringBean(name = "purgeParticipantDataJob")
  private Job purgeParticipantDataJob;

  @SpringBean
  private JobExecutionService jobExecutionService;

  private PurgeDialogPanel content;

  private AjaxLink<?> purgeSubmitLink;

  private int participantsDeleted;

  @SuppressWarnings("rawtypes")
  public PurgeDialog(String id) {
    super(id);
    setTitle((new ResourceModel("PurgeParticipants")));
    setHeightUnit("em");
    setWidthUnit("em");
    setInitialHeight(DEFAULT_INITIAL_HEIGHT);
    setInitialWidth(DEFAULT_INITIAL_WIDTH);
    setType(Type.PLAIN);

    super.setDefaultModel(new OnyxDataPurgeModel());

    // Set submit button
    addOption("PurgeParticipants", OptionSide.RIGHT, purgeSubmitLink = new AjaxLink("submitPurge") {

      private static final long serialVersionUID = 1L;

      @Override
      public void onClick(AjaxRequestTarget target) {
        // Show the progress fragment.
        showProgress();

        // Register update callback.
        PurgeBehavior updateCallback = new PurgeBehavior();
        PurgeDialog.this.add(updateCallback);
        target.appendJavascript(updateCallback.getJavascript());

        target.addComponent(PurgeDialog.this.get("content"));
      }

      @Override
      public boolean isVisible() {
        return getPurgeModel().hasInterviewsToPurge();
      }

    });

    content = new PurgeDialogPanel(getContentId(), getPurgeModel());
    content.add(new AttributeModifier("class", true, new Model<String>("obiba-content purge-panel-content")));
    content.setOutputMarkupId(true);
    setContent(content);

    // Initially show confirmation fragment.
    showConfirmation();
  }

  //
  // Methods
  //

  public void showConfirmation() {
    setOptions(Option.CANCEL_OPTION);
    purgeSubmitLink.setVisible(true);

    setCloseButtonCallback(new CloseButtonCallback() {
      private static final long serialVersionUID = 1L;

      public boolean onCloseButtonClicked(AjaxRequestTarget target, Status status) {
        return true;
      }
    });

    setWindowClosedCallback(new WindowClosedCallback() {
      private static final long serialVersionUID = 1L;

      public void onClose(AjaxRequestTarget target, Status status) {
        if(status != null && status.equals(Dialog.Status.CANCELLED)) PurgeDialog.this.close(target);
      }
    });

    content.showConfirmation();
  }

  public void showProgress() {
    setOptions(null);
    purgeSubmitLink.setVisible(false);
    content.showProgress();
  }

  public void showResult(boolean purgeSucceeded) {
    setOptions(Option.CLOSE_OPTION);
    content.showResult(purgeSucceeded, participantsDeleted);
  }

  private OnyxDataPurgeModel getPurgeModel() {
    return (OnyxDataPurgeModel) super.getDefaultModel();
  }

  private class PurgeBehavior extends AbstractDefaultAjaxBehavior {
    private static final long serialVersionUID = 1L;

    public void respond(AjaxRequestTarget target) {

      boolean purgeSucceeded = executePurge();

      // Show the result fragment.
      PurgeDialog.this.showResult(purgeSucceeded);

      target.addComponent(PurgeDialog.this.get("content"));
    }

    public String getJavascript() {
      return this.getCallbackScript().toString();
    }

    private boolean executePurge() {
      Map<String, JobParameter> jobParameterMap = new HashMap<String, JobParameter>();
      jobParameterMap.put("date", new JobParameter(new Date()));
      JobExecution jobExecution = jobExecutionService.launchJob(purgeParticipantDataJob, jobParameterMap);
      boolean jobCompleted = jobExecution.getExitStatus().getExitCode().equals("COMPLETED");

      participantsDeleted = (jobCompleted) ? jobExecution.getExecutionContext().getInt("totalDeleted") : 0;
      return jobCompleted;
    }
  }
}
