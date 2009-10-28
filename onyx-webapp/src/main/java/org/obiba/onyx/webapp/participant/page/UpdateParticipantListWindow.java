/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.webapp.participant.page;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.lang.Bytes;
import org.obiba.core.validation.exception.ValidationRuntimeException;
import org.obiba.onyx.core.etl.participant.impl.AbstractParticipantReader;
import org.obiba.onyx.core.service.AppointmentManagementService;
import org.obiba.onyx.webapp.participant.panel.UpdateParticipantListPanel;
import org.obiba.onyx.wicket.reusable.Dialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;

public class UpdateParticipantListWindow extends Dialog {
  //
  // Constants
  //

  private static final long serialVersionUID = 1L;

  private static final Logger log = LoggerFactory.getLogger(UpdateParticipantListWindow.class);

  private static final int DEFAULT_INITIAL_HEIGHT = 150;

  private static final int DEFAULT_INITIAL_WIDTH = 450;

  //
  // Instance Variables
  //

  private UpdateParticipantListPanel content;

  private AjaxButton updateSubmitLink;

  private AjaxLink<Object> detailsLink;

  @SpringBean
  private AppointmentManagementService appointmentManagementService;

  @SpringBean
  private AbstractParticipantReader participantReader;

  //
  // Constructors
  //

  public UpdateParticipantListWindow(String id) {
    super(id);

    setCssClassName("onyx");

    setTitle((new ResourceModel("UpdateParticipantList")));
    setInitialHeight(DEFAULT_INITIAL_HEIGHT);
    setInitialWidth(DEFAULT_INITIAL_WIDTH);

    content = new UpdateParticipantListPanel(getContentId());
    content.add(new AttributeModifier("class", true, new Model<String>("obiba-content updateParticipantList-panel-content")));
    content.setOutputMarkupId(true);
    setContent(content);

    // Set submit button
    addSubmitOption("UpdateAppointments", OptionSide.RIGHT, updateSubmitLink = new AjaxButton("submitUpdate", this.getForm()) {

      private static final long serialVersionUID = 1L;

      @Override
      protected void onSubmit(AjaxRequestTarget target, Form form) {
        UpdateParticipantListWindow.this.setStatus(Status.SUCCESS);
        FileUploadField upload = (FileUploadField) UpdateParticipantListWindow.this.getWindowContent().get("contentFragment:fileUpload");
        boolean isUpdateAvailable = true;

        if(upload != null && upload.getFileUpload() != null) {
          uploadFileForProcessing(upload.getFileUpload());
        } else {
          try {
            isUpdateAvailable = (participantReader.getInputDirectory().getFile().listFiles(participantReader.getFilter()).length > 0);
          } catch(IOException e) {
            throw new RuntimeException(e);
          }
        }

        if(!isUpdateAvailable) {
          showNotification("UpdateParticipantList.NoFileAvailable");
        } else {
          // Show the progress fragment.
          showProgress();

          // Register update callback.
          UpdateParticipantListBehavior updateCallback = new UpdateParticipantListBehavior();
          UpdateParticipantListWindow.this.add(updateCallback);
          target.appendJavascript(updateCallback.getJavascript());
        }
        target.addComponent(UpdateParticipantListWindow.this.get("content"));
      }

      @Override
      protected void onError(AjaxRequestTarget target, Form form) {
        UpdateParticipantListWindow.this.setStatus(Status.ERROR);
        UpdateParticipantListWindow.this.getWindowContent().displayFeedback(target);
      }
    });
    updateSubmitLink.setVisible(false);

    // Set details button
    addOption("Details", OptionSide.RIGHT, detailsLink = new AjaxLink<Object>("details") {

      private static final long serialVersionUID = 1L;

      @Override
      public void onClick(AjaxRequestTarget target) {
        UpdateParticipantListPanel.ResultFragment resultFragment = (UpdateParticipantListPanel.ResultFragment) UpdateParticipantListWindow.this.getWindowContent().get("contentFragment");
        if(resultFragment.getAppointmentUpdateStats() != null) UpdateParticipantListWindow.this.getWindowContent().displayDetails(target, resultFragment.getAppointmentUpdateStats().getDate());

      }
    }, "details");
    detailsLink.setVisible(false);

    // Initially show confirmation fragment.
    showConfirmation();
  }

  //
  // Methods
  //

  public void showConfirmation() {
    setOptions(Option.CANCEL_OPTION);
    updateSubmitLink.setVisible(true);
    detailsLink.setVisible(false);

    getForm().setMaxSize(Bytes.megabytes(2));
    getForm().setMultiPart(true);

    content.showConfirmation();
  }

  public void showProgress() {
    setOptions(null);
    updateSubmitLink.setVisible(false);
    content.showProgress();
  }

  public void showNotification(String message) {
    setOptions(Option.CLOSE_OPTION);
    updateSubmitLink.setVisible(false);
    content.showNotification(message);
  }

  public void showResult(boolean updateSucceeded) {
    setOptions(Option.CLOSE_OPTION);
    detailsLink.setVisible(updateSucceeded);
    content.showResult(updateSucceeded, (updateSucceeded) ? appointmentManagementService.getLastAppointmentUpdateStats() : null);
  }

  private class UpdateParticipantListBehavior extends AbstractDefaultAjaxBehavior implements Serializable {
    private static final long serialVersionUID = 1L;

    public void respond(AjaxRequestTarget target) {
      // Perform update.
      boolean updateSucceeded = updateParticipants();

      // Show the result fragment.
      UpdateParticipantListWindow.this.showResult(updateSucceeded);

      target.addComponent(UpdateParticipantListWindow.this.get("content"));
    }

    public String getJavascript() {
      return this.getCallbackScript().toString();
    }

    private boolean updateParticipants() {
      ExitStatus exitStatus = ExitStatus.UNKNOWN;

      try {
        exitStatus = appointmentManagementService.updateAppointments();
      } catch(ValidationRuntimeException e) {
        log.error("Failed to update participants: {}", e.toString());
      }
      return (exitStatus.getExitCode().equals("COMPLETED"));
    }
  }

  private void uploadFileForProcessing(FileUpload upload) {
    try {
      SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddhhmmss");
      String fileName = formatter.format(new Date()) + "_" + upload.getClientFileName();
      File inputFile = new File(participantReader.getInputDirectory().getFile(), fileName);
      inputFile.createNewFile();
      upload.writeTo(inputFile);
    } catch(IOException e) {
      throw new RuntimeException(e);
    }

  }

  public UpdateParticipantListPanel getWindowContent() {
    return content;
  }

  public void setParticipantReader(AbstractParticipantReader participantReader) {
    this.participantReader = participantReader;
  }

  public void setAppointmentManagementService(AppointmentManagementService appointmentManagementService) {
    this.appointmentManagementService = appointmentManagementService;
  }

}
