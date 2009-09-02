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

import java.io.Serializable;

import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.core.validation.exception.ValidationRuntimeException;
import org.obiba.onyx.core.service.AppointmentManagementService;
import org.obiba.onyx.webapp.participant.panel.UpdateParticipantListPanel;
import org.obiba.onyx.wicket.reusable.Dialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateParticipantListWindow extends Dialog {
  //
  // Constants
  //

  private static final long serialVersionUID = 1L;

  private static final Logger log = LoggerFactory.getLogger(UpdateParticipantListWindow.class);

  private static final int DEFAULT_INITIAL_HEIGHT = 120;

  private static final int DEFAULT_INITIAL_WIDTH = 400;

  //
  // Instance Variables
  //

  private UpdateParticipantListPanel content;

  private AjaxLink updateSubmitLink;

  @SpringBean
  private AppointmentManagementService appointmentManagementService;

  //
  // Constructors
  //

  public void setAppointmentManagementService(AppointmentManagementService appointmentManagementService) {
    this.appointmentManagementService = appointmentManagementService;
  }

  public UpdateParticipantListWindow(String id) {
    super(id);

    setCssClassName("onyx");

    setTitle((new ResourceModel("UpdateParticipantList")));
    setInitialHeight(DEFAULT_INITIAL_HEIGHT);
    setInitialWidth(DEFAULT_INITIAL_WIDTH);

    content = new UpdateParticipantListPanel(getContentId());
    content.setOutputMarkupId(true);
    setContent(content);

    addOption("UpdateAppointments", OptionSide.RIGHT, updateSubmitLink = new AjaxLink("submitUpdate") {

      private static final long serialVersionUID = 1L;

      @Override
      public void onClick(AjaxRequestTarget target) {
        UpdateParticipantListWindow.this.setStatus(Status.OTHER);

        // Show the progress fragment.
        showProgress();

        // Register update callback.
        UpdateParticipantListBehavior updateCallback = new UpdateParticipantListBehavior();
        UpdateParticipantListWindow.this.add(updateCallback);
        target.appendJavascript(updateCallback.getJavascript());

        target.addComponent(UpdateParticipantListWindow.this.get("content"));
      }

    });
    updateSubmitLink.setVisible(false);

    // Initially show confirmation fragment.
    showConfirmation();
  }

  //
  // Methods
  //

  public void showConfirmation() {
    setOptions(Option.CANCEL_OPTION);
    updateSubmitLink.setVisible(true);

    setCloseButtonCallback(new CloseButtonCallback() {
      private static final long serialVersionUID = 1L;

      public boolean onCloseButtonClicked(AjaxRequestTarget target, Status status) {
        return true;
      }
    });

    setWindowClosedCallback(new WindowClosedCallback() {
      private static final long serialVersionUID = 1L;

      public void onClose(AjaxRequestTarget target, Status status) {
        if(status != null && status.equals(Dialog.Status.CANCELLED)) UpdateParticipantListWindow.this.close(target);
      }
    });

    content.showConfirmation();
  }

  public void showProgress() {
    setOptions(null);
    updateSubmitLink.setVisible(false);
    content.showProgress();
  }

  public void showResult(boolean updateSucceeded) {
    setOptions(Option.CLOSE_OPTION);
    content.showResult(updateSucceeded);
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
      boolean updateSucceeded = false;

      try {
        appointmentManagementService.updateAppointments();
        updateSucceeded = true;
      } catch(ValidationRuntimeException e) {
        log.error("Failed to update participants: {}", e.toString());
      }

      return updateSucceeded;
    }
  }

  public UpdateParticipantListPanel getContent() {
    return content;
  }

}
