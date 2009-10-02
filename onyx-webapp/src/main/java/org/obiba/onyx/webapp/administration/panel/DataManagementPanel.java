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
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.value.ValueMap;
import org.obiba.onyx.core.service.JobExecutionService;
import org.obiba.onyx.engine.variable.export.OnyxDataExport;
import org.obiba.onyx.wicket.reusable.ConfirmationDialog;
import org.obiba.onyx.wicket.reusable.ConfirmationDialog.OnYesCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameter;

public class DataManagementPanel extends Panel {

  private static final Logger log = LoggerFactory.getLogger(DataManagementPanel.class);

  private static final long serialVersionUID = 1L;

  @SpringBean
  private OnyxDataExport onyxDataExport;

  @SpringBean(name = "purgeParticipantDataJob")
  private Job purgeParticipantDataJob;

  @SpringBean
  private JobExecutionService jobExecutionService;

  private ConfirmationDialog confirmationDialog;

  public DataManagementPanel(String id) {
    super(id);

    createExportDialog();

    AjaxLink exportLink = new AjaxLink("export") {
      private static final long serialVersionUID = 1L;

      @Override
      public void onClick(AjaxRequestTarget target) {
        Label label = new Label("content", new StringResourceModel("ConfirmExportMessage", new Model(new ValueMap("directory=" + onyxDataExport.getOutputRootDirectory().getAbsolutePath()))));
        label.add(new AttributeModifier("class", true, new Model("long-confirmation-dialog-content")));
        confirmationDialog.setContent(label);

        confirmationDialog.setYesButtonCallback(new OnYesCallback() {
          private static final long serialVersionUID = 1L;

          public void onYesButtonClicked(AjaxRequestTarget target) {
            try {
              onyxDataExport.exportInterviews();
            } catch(Exception e) {
              log.error("Error on data export.", e);
            }
          }
        });
        confirmationDialog.show(target);
      }

    };

    add(exportLink);

    AjaxLink purgeLink = new AjaxLink("purge") {
      private static final long serialVersionUID = 1L;

      @Override
      public void onClick(AjaxRequestTarget target) {
        Map<String, JobParameter> jobParameterMap = new HashMap<String, JobParameter>();
        jobParameterMap.put("date", new JobParameter(new Date()));

        jobExecutionService.launchJob(purgeParticipantDataJob, jobParameterMap);
      }

    };

    add(purgeLink);

  }

  private void createExportDialog() {
    confirmationDialog = new ConfirmationDialog("confirmExportModalWindow");
    confirmationDialog.setTitle(new ResourceModel("ConfirmExport"));
    confirmationDialog.setInitialHeight(130);
    add(confirmationDialog);

  }
}
