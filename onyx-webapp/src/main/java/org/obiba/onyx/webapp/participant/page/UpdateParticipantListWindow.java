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
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.core.validation.exception.ValidationRuntimeException;
import org.obiba.onyx.core.service.AppointmentManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateParticipantListWindow extends ModalWindow {
  //
  // Constants
  //

  private static final long serialVersionUID = 1L;

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(UpdateParticipantListWindow.class);

  private static final int DEFAULT_INITIAL_HEIGHT = 120;

  private static final int DEFAULT_INITIAL_WIDTH = 400;

  //
  // Instance Variables
  //

  private ConfirmationFragment confirmationFragment;

  private ProgressFragment progressFragment;

  private ResultFragment resultFragment;

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

    setTitle((new ResourceModel("UpdateParticipantList")));
    setInitialHeight(DEFAULT_INITIAL_HEIGHT);
    setInitialWidth(DEFAULT_INITIAL_WIDTH);

    confirmationFragment = new ConfirmationFragment("contentFragment", new ResourceModel("ConfirmParticipantListUpdate"));
    progressFragment = new ProgressFragment("contentFragment", new ResourceModel("ParticipantListUpdateInProgress"));
    resultFragment = new ResultFragment("contentFragment", new ResourceModel("ParticipantsListSuccessfullyUpdated"));

    WebMarkupContainer content = new WebMarkupContainer(getContentId());
    content.setOutputMarkupId(true);
    setContent(content);

    // Initially show confirmation fragment.
    showConfirmation();
  }

  //
  // Methods
  //

  public void showConfirmation() {
    replaceOrAddFragment(confirmationFragment);
  }

  public void showProgress() {
    replaceOrAddFragment(progressFragment);
  }

  public void showResult(boolean updateSucceeded) {
    String messageKey = updateSucceeded ? "ParticipantsListSuccessfullyUpdated" : "ParticipantListUpdateFailed";
    IModel messageModel = new ResourceModel(messageKey, messageKey);
    resultFragment.resultLabel.setModel(messageModel);

    replaceOrAddFragment(resultFragment);
  }

  private void replaceOrAddFragment(Fragment fragment) {
    WebMarkupContainer content = (WebMarkupContainer) get(getContentId());

    Fragment currentFragment = (Fragment) content.get("contentFragment");

    if(currentFragment != null) {
      content.replace(fragment);
    } else {
      content.add(fragment);
    }
  }

  //
  // Inner Classes
  //

  class ConfirmationFragment extends Fragment {

    private static final long serialVersionUID = 1L;

    private Image icon;

    private MultiLineLabel messageLabel;

    private AjaxLink okLink;

    private AjaxLink cancelLink;

    public ConfirmationFragment(String id, IModel messageModel) {
      super(id, "confirmationFragment", UpdateParticipantListWindow.this);

      icon = new Image("confirmIcon");
      add(icon);

      messageLabel = new MultiLineLabel("confirmMessage", messageModel);
      add(messageLabel);

      okLink = createOkLink();
      add(okLink);

      cancelLink = createCancelLink();
      add(cancelLink);
    }

    private AjaxLink createOkLink() {
      return new AjaxLink("ok") {
        private static final long serialVersionUID = 1L;

        public void onClick(AjaxRequestTarget target) {
          // Show the progress fragment.
          showProgress();

          // Register update callback.
          UpdateParticipantListBehavior updateCallback = new UpdateParticipantListBehavior();
          UpdateParticipantListWindow.this.add(updateCallback);
          target.appendJavascript(updateCallback.getJavascript());

          target.addComponent(UpdateParticipantListWindow.this.get("content"));
        }
      };
    }

    private AjaxLink createCancelLink() {
      return new AjaxLink("cancel") {
        private static final long serialVersionUID = 1L;

        public void onClick(AjaxRequestTarget target) {
          UpdateParticipantListWindow.this.close(target);
        }
      };
    }
  }

  class ProgressFragment extends Fragment {

    private static final long serialVersionUID = 1L;

    private Label progressLabel;

    private Image progressImage;

    public ProgressFragment(String id, IModel messageModel) {
      super(id, "progressFragment", UpdateParticipantListWindow.this);

      progressLabel = new Label("progressLabel", messageModel);
      add(progressLabel);

      progressImage = new Image("progressImage");
      add(progressImage);
    }
  }

  class ResultFragment extends Fragment {

    private static final long serialVersionUID = 1L;

    private Label resultLabel;

    private AjaxLink okLink;

    public ResultFragment(String id, IModel messageModel) {
      super(id, "resultFragment", UpdateParticipantListWindow.this);

      resultLabel = new Label("resultLabel", messageModel);
      add(resultLabel);

      okLink = createOkLink();
      add(okLink);
    }

    private AjaxLink createOkLink() {
      return new AjaxLink("ok") {
        private static final long serialVersionUID = 1L;

        public void onClick(AjaxRequestTarget target) {
          UpdateParticipantListWindow.this.close(target);
        }
      };
    }
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
}
