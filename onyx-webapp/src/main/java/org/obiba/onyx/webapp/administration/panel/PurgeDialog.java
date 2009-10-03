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
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.core.service.JobExecutionService;
import org.obiba.onyx.wicket.reusable.Dialog;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameter;

public class PurgeDialog extends Dialog {

  private static final long serialVersionUID = 1L;

  private static final int DEFAULT_INITIAL_HEIGHT = 120;

  private static final int DEFAULT_INITIAL_WIDTH = 350;

  @SpringBean(name = "purgeParticipantDataJob")
  private Job purgeParticipantDataJob;

  @SpringBean
  private JobExecutionService jobExecutionService;

  private PurgeDialogPanel content;

  private AjaxLink purgeSubmitLink;

  public PurgeDialog(String id) {
    super(id);
    setTitle((new ResourceModel("PurgeParticipants")));
    setInitialHeight(DEFAULT_INITIAL_HEIGHT);
    setInitialWidth(DEFAULT_INITIAL_WIDTH);
    setType(Type.PLAIN);

    // Set submit button
    addOption("PurgeParticipants", OptionSide.RIGHT, purgeSubmitLink = new AjaxLink("submitPurge") {

      private static final long serialVersionUID = 1L;

      @Override
      public void onClick(AjaxRequestTarget target) {
        // Show the progress fragment.
        showProgress();
        target.addComponent(PurgeDialog.this.get("content"));
      }
    });

    content = new PurgeDialogPanel(getContentId());
    content.add(new AttributeModifier("class", true, new Model("obiba-content purge-panel-content")));
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

        if(status != null && status.equals(Dialog.Status.SUCCESS)) {
          Map<String, JobParameter> jobParameterMap = new HashMap<String, JobParameter>();
          jobParameterMap.put("date", new JobParameter(new Date()));
          jobExecutionService.launchJob(purgeParticipantDataJob, jobParameterMap);
        }

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
    content.showResult(purgeSucceeded);
  }
}
