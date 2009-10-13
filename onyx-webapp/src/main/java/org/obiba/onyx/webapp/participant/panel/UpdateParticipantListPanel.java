/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.webapp.participant.panel;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.IErrorMessageSource;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidationError;
import org.apache.wicket.validation.IValidator;
import org.obiba.onyx.core.domain.statistics.AppointmentUpdateLog;
import org.obiba.onyx.core.domain.statistics.AppointmentUpdateStats;
import org.obiba.onyx.core.etl.participant.impl.AbstractParticipantReader;
import org.obiba.onyx.core.service.AppointmentManagementService;
import org.obiba.onyx.wicket.reusable.Dialog;
import org.obiba.onyx.wicket.reusable.FeedbackWindow;
import org.obiba.onyx.wicket.reusable.Dialog.Option;
import org.obiba.wicket.markup.html.panel.KeyValueDataPanel;

/**
 * 
 */
public class UpdateParticipantListPanel extends Panel {

  private static final long serialVersionUID = 1L;

  private static final String AUTOMATIC_UPLOAD = "Automatic";

  private static final String MANUAL_UPLOAD = "Manual";

  @SpringBean
  private AppointmentManagementService appointmentManagementService;

  @SpringBean
  private AbstractParticipantReader participantReader;

  //
  // Instance variables
  //

  private Dialog updateAppointmentLogWindow;

  private FeedbackWindow feedbackWindow;

  private ConfirmationFragment confirmationFragment;

  private ProgressFragment progressFragment;

  private ResultFragment resultFragment;

  private NotificationFragment notificationFragment;

  private FeedbackFragment feedbackFragment;

  //
  // Constructors
  //

  public UpdateParticipantListPanel(String id) {
    super(id);

    addUpdateAppointmentLogWindow();

    feedbackFragment = new FeedbackFragment("feedbackFragment");
    feedbackFragment.setOutputMarkupId(true);
    add(feedbackFragment);

    confirmationFragment = new ConfirmationFragment("contentFragment");
    progressFragment = new ProgressFragment("contentFragment", new ResourceModel("ParticipantListUpdateInProgress"));
    resultFragment = new ResultFragment("contentFragment", new ResourceModel("ParticipantsListSuccessfullyUpdated"));
    notificationFragment = new NotificationFragment("contentFragment");
  }

  public void showConfirmation() {
    replaceOrAddFragment(confirmationFragment);
  }

  public void showProgress() {
    feedbackFragment.refreshFeedback();
    replaceOrAddFragment(progressFragment);
  }

  public void showNotification(String message) {
    notificationFragment.setMessage(new ResourceModel(message));
    replaceOrAddFragment(notificationFragment);
  }

  public void showResult(boolean updateSucceeded, AppointmentUpdateStats stats) {
    String messageKey = updateSucceeded ? "ParticipantsListSuccessfullyUpdated" : "ParticipantListUpdateFailed";
    IModel<String> messageModel = new ResourceModel(messageKey, messageKey);
    resultFragment.resultLabel.setDefaultModel(messageModel);

    Model<AppointmentUpdateStats> statsModel = new Model<AppointmentUpdateStats>(stats);
    KeyValueDataPanel kvPanel = new KeyValueDataPanel("stats");
    kvPanel.addRow(new ResourceModel("File"), new PropertyModel<AppointmentUpdateStats>(statsModel, "fileName"));
    kvPanel.addRow(new ResourceModel("TotalParticipants"), new PropertyModel<AppointmentUpdateStats>(statsModel, "totalParticipants"));
    kvPanel.addRow(new ResourceModel("UpdatedParticipants"), new PropertyModel<AppointmentUpdateStats>(statsModel, "updatedParticipants"));
    kvPanel.addRow(new ResourceModel("CreatedParticipants"), new PropertyModel<AppointmentUpdateStats>(statsModel, "addedParticipants"));
    kvPanel.addRow(new ResourceModel("IgnoredParticipants"), new PropertyModel<AppointmentUpdateStats>(statsModel, "ignoredParticipants"));
    kvPanel.addRow(new ResourceModel("UnreadableParticipants"), new PropertyModel<AppointmentUpdateStats>(statsModel, "unreadableParticipants"));

    if(updateSucceeded) {
      kvPanel.setVisible(true);
    } else {
      kvPanel.setVisible(false);
    }
    resultFragment.setAppointmentUpdateStats(stats);
    resultFragment.addOrReplace(kvPanel);

    replaceOrAddFragment(resultFragment);
  }

  private void replaceOrAddFragment(Fragment fragment) {
    Fragment currentFragment = (Fragment) get(fragment.getId());

    if(currentFragment != null) {
      replace(fragment);
    } else {
      add(fragment);
    }
  }

  private void addUpdateAppointmentLogWindow() {
    updateAppointmentLogWindow = new Dialog("updateAppointmentLogWindow");
    updateAppointmentLogWindow.setTitle(new ResourceModel("Log"));
    updateAppointmentLogWindow.setOptions(Option.CLOSE_OPTION);
    updateAppointmentLogWindow.setInitialHeight(400);
    updateAppointmentLogWindow.setInitialWidth(700);
    updateAppointmentLogWindow.setOutputMarkupId(true);
    add(updateAppointmentLogWindow);
  }

  //
  // Inner Classes
  //

  public class ConfirmationFragment extends Fragment {

    private static final long serialVersionUID = 1L;

    private final FileUploadField fileUpload;

    public ConfirmationFragment(String id) {
      super(id, "confirmationFragment", UpdateParticipantListPanel.this);

      RadioGroup<Serializable> radioGroup = new RadioGroup<Serializable>("radioGroup", new Model<Serializable>());
      add(radioGroup);
      setRadioOptions(radioGroup);

      fileUpload = new FileUploadField("fileUpload");
      fileUpload.add(new IValidator<FileUpload>() {
        private static final long serialVersionUID = 1L;

        public void validate(IValidatable<FileUpload> validatable) {
          FileUpload input = (FileUpload) validatable.getValue();
          if(!input.getClientFileName().endsWith(participantReader.getFilePattern())) {
            validatable.error(new FileTypeError());
          }
        }
      });

      fileUpload.setRequired(true);
      fileUpload.setEnabled(false);
      fileUpload.setOutputMarkupId(true);
      add(fileUpload);
    }

    private void setRadioOptions(final RadioGroup<Serializable> radioGroup) {

      Radio<Serializable> radio = new Radio<Serializable>("automaticUpload", new Model<Serializable>(AUTOMATIC_UPLOAD));
      radio.setLabel(new ResourceModel("UpdateParticipantList." + AUTOMATIC_UPLOAD));

      radio.add(new AjaxEventBehavior("onchange") {
        private static final long serialVersionUID = 1L;

        @Override
        protected void onEvent(AjaxRequestTarget target) {
          feedbackFragment.refreshFeedback();
          target.addComponent(feedbackFragment);
          fileUpload.setEnabled(false);
          target.addComponent(fileUpload);
        }

      });

      radioGroup.add(radio);
      radioGroup.add(new Label("alabel", radio.getLabel()).setRenderBodyOnly(true));

      radio = new Radio<Serializable>("manualUpload", new Model<Serializable>(MANUAL_UPLOAD));
      radio.setLabel(new ResourceModel("UpdateParticipantList." + MANUAL_UPLOAD));

      radio.add(new AjaxEventBehavior("onchange") {
        private static final long serialVersionUID = 1L;

        @Override
        protected void onEvent(AjaxRequestTarget target) {
          feedbackFragment.refreshFeedback();
          target.addComponent(feedbackFragment);
          fileUpload.setEnabled(true);
          target.addComponent(fileUpload);
        }

      });

      radioGroup.add(radio);
      radioGroup.add(new Label("mlabel", radio.getLabel()).setRenderBodyOnly(true));

      radioGroup.setRequired(true);

    }

    @SuppressWarnings("serial")
    private class FileTypeError implements IValidationError, Serializable {

      public String getErrorMessage(IErrorMessageSource messageSource) {
        return new ResourceModel("FileWrongType").toString();
      }
    }
  }

  public class ProgressFragment extends Fragment {

    private static final long serialVersionUID = 1L;

    private Label progressLabel;

    private Image progressImage;

    public ProgressFragment(String id, IModel<String> messageModel) {
      super(id, "progressFragment", UpdateParticipantListPanel.this);

      progressLabel = new Label("progressLabel", messageModel);
      add(progressLabel);

      progressImage = new Image("progressImage");
      add(progressImage);
    }
  }

  public class NotificationFragment extends Fragment {

    private static final long serialVersionUID = 1L;

    private Label messageLabel;

    public NotificationFragment(String id) {
      super(id, "notificationFragment", UpdateParticipantListPanel.this);
    }

    public void setMessage(IModel<String> messageModel) {
      messageLabel = new Label("messageLabel", messageModel);
      addOrReplace(messageLabel);
    }
  }

  public class ResultFragment extends Fragment {

    private static final long serialVersionUID = 1L;

    private Label resultLabel;

    private AppointmentUpdateStats appointmentUpdateStats;

    public ResultFragment(String id, IModel<String> messageModel) {
      super(id, "resultFragment", UpdateParticipantListPanel.this);

      resultLabel = new Label("resultLabel", messageModel);
      add(resultLabel);

    }

    public AppointmentUpdateStats getAppointmentUpdateStats() {
      return appointmentUpdateStats;
    }

    public void setAppointmentUpdateStats(AppointmentUpdateStats appointmentUpdateStats) {
      this.appointmentUpdateStats = appointmentUpdateStats;
    }

  }

  public void setParticipantReader(AbstractParticipantReader participantReader) {
    this.participantReader = participantReader;
  }

  public FeedbackWindow getFeedbackWindow() {
    return feedbackWindow;
  }

  public void setFeedbackWindow(FeedbackWindow feedbackWindow) {
    this.feedbackWindow = feedbackWindow;
  }

  public void setAppointmentManagementService(AppointmentManagementService appointmentManagementService) {
    this.appointmentManagementService = appointmentManagementService;
  }

  public void displayFeedback(AjaxRequestTarget target) {
    feedbackFragment.refreshFeedback();
    replaceOrAddFragment(feedbackFragment);
    target.addComponent(feedbackFragment);
  }

  @SuppressWarnings("unchecked")
  public void displayDetails(AjaxRequestTarget target, Date date) {
    List<AppointmentUpdateLog> apointmentUpdateLogs = appointmentManagementService.getLogListForDate(date);

    AppointmentUpdateLogPanel appointmentUpdateLogPanel = new AppointmentUpdateLogPanel("content", apointmentUpdateLogs);
    appointmentUpdateLogPanel.add(new AttributeModifier("class", true, new Model("obiba-content appointment-update-log-panel-content")));
    updateAppointmentLogWindow.setContent(appointmentUpdateLogPanel);
    updateAppointmentLogWindow.show(target);
  }

  private class FeedbackFragment extends Fragment {
    private static final long serialVersionUID = 1L;

    private RepeatingView repeater;

    private Image errorImage;

    public FeedbackFragment(String id) {
      super(id, "feedbackFragment", UpdateParticipantListPanel.this);

      errorImage = new Image("errorImage");
      add(errorImage);

      repeater = new RepeatingView("message");
      refreshFeedback();
    }

    public void refreshFeedback() {
      repeater.removeAll();
      FeedbackPanel feedbackPanel = new FeedbackPanel("content");
      List<FeedbackMessage> messages = feedbackPanel.getFeedbackMessagesModel().getObject();

      if(messages.size() == 0) {
        errorImage.setVisible(false);
      } else {
        errorImage.setVisible(true);
      }

      for(FeedbackMessage message : messages) {
        repeater.add(new Label(repeater.newChildId(), new Model(message.getMessage().toString() + "\n")));
      }
      addOrReplace(repeater);
    }
  }

}
