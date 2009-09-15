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

import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.IErrorMessageSource;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidationError;
import org.apache.wicket.validation.IValidator;
import org.obiba.onyx.core.domain.statistics.AppointmentUpdateStats;
import org.obiba.onyx.core.etl.participant.impl.AbstractParticipantReader;
import org.obiba.onyx.wicket.reusable.FeedbackWindow;
import org.obiba.wicket.markup.html.panel.KeyValueDataPanel;

/**
 * 
 */
public class UpdateParticipantListPanel extends Panel {

  private static final long serialVersionUID = 1L;

  private static final String AUTOMATIC_UPLOAD = "Automatic";

  private static final String MANUAL_UPLOAD = "Manual";

  @SpringBean
  private AbstractParticipantReader participantReader;

  //
  // Instance variables
  //

  private FeedbackWindow feedbackWindow;

  private ConfirmationFragment confirmationFragment;

  private ProgressFragment progressFragment;

  private ResultFragment resultFragment;

  //
  // Constructors
  //

  public UpdateParticipantListPanel(String id) {
    super(id);

    add(feedbackWindow = new FeedbackWindow("feedback"));
    feedbackWindow.setOutputMarkupId(true);

    confirmationFragment = new ConfirmationFragment("contentFragment");
    progressFragment = new ProgressFragment("contentFragment", new ResourceModel("ParticipantListUpdateInProgress"));
    resultFragment = new ResultFragment("contentFragment", new ResourceModel("ParticipantsListSuccessfullyUpdated"));

  }

  public void showConfirmation() {
    replaceOrAddFragment(confirmationFragment);
  }

  public void showProgress() {
    replaceOrAddFragment(progressFragment);
  }

  public void showResult(boolean updateSucceeded, AppointmentUpdateStats stats) {
    String messageKey = updateSucceeded ? "ParticipantsListSuccessfullyUpdated" : "ParticipantListUpdateFailed";
    IModel messageModel = new ResourceModel(messageKey, messageKey);
    resultFragment.resultLabel.setDefaultModel(messageModel);

    Model<AppointmentUpdateStats> statsModel = new Model<AppointmentUpdateStats>(stats);
    KeyValueDataPanel kvPanel = new KeyValueDataPanel("stats");
    kvPanel.addRow(new StringResourceModel("File", this, null), new PropertyModel(statsModel, "fileName"));
    kvPanel.addRow(new StringResourceModel("TotalParticipants", this, null), new PropertyModel(statsModel, "totalParticipants"));
    kvPanel.addRow(new StringResourceModel("UpdatedParticipants", this, null), new PropertyModel(statsModel, "updatedParticipants"));
    kvPanel.addRow(new StringResourceModel("CreatedParticipants", this, null), new PropertyModel(statsModel, "addedParticipants"));
    kvPanel.addRow(new StringResourceModel("IgnoredParticipants", this, null), new PropertyModel(statsModel, "ignoredParticipants"));
    kvPanel.addRow(new StringResourceModel("UnreadableParticipants", this, null), new PropertyModel(statsModel, "unreadableParticipants"));

    if(updateSucceeded) {
      kvPanel.setVisible(true);
    } else {
      kvPanel.setVisible(false);
    }
    resultFragment.addOrReplace(kvPanel);

    replaceOrAddFragment(resultFragment);
  }

  private void replaceOrAddFragment(Fragment fragment) {
    Fragment currentFragment = (Fragment) get("contentFragment");

    if(currentFragment != null) {
      replace(fragment);
    } else {
      add(fragment);
    }
  }

  //
  // Inner Classes
  //

  public class ConfirmationFragment extends Fragment {

    private static final long serialVersionUID = 1L;

    private final FileUploadField fileUpload;

    public ConfirmationFragment(String id) {
      super(id, "confirmationFragment", UpdateParticipantListPanel.this);

      RadioGroup radioGroup = new RadioGroup("radioGroup", new Model());
      add(radioGroup);
      setRadioOptions(radioGroup);

      fileUpload = new FileUploadField("fileUpload");
      fileUpload.add(new IValidator<FileUpload>() {
        private static final long serialVersionUID = 1L;

        public void validate(IValidatable validatable) {
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

    private void setRadioOptions(final RadioGroup radioGroup) {

      Radio radio = new Radio("automaticUpload", new Model(AUTOMATIC_UPLOAD));
      radio.setLabel(new StringResourceModel("UpdateParticipantList." + AUTOMATIC_UPLOAD, UpdateParticipantListPanel.this, null));

      radio.add(new AjaxEventBehavior("onchange") {
        private static final long serialVersionUID = 1L;

        @Override
        protected void onEvent(AjaxRequestTarget target) {
          fileUpload.setEnabled(false);
          target.addComponent(fileUpload);
        }

      });

      radioGroup.add(radio);
      radioGroup.add(new Label("alabel", radio.getLabel()).setRenderBodyOnly(true));

      radio = new Radio("manualUpload", new Model(MANUAL_UPLOAD));
      radio.setLabel(new StringResourceModel("UpdateParticipantList." + MANUAL_UPLOAD, UpdateParticipantListPanel.this, null));

      radio.add(new AjaxEventBehavior("onchange") {
        private static final long serialVersionUID = 1L;

        @Override
        protected void onEvent(AjaxRequestTarget target) {
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
        return new StringResourceModel("FileWrongType", UpdateParticipantListPanel.this, null).getString();
      }
    }
  }

  public class ProgressFragment extends Fragment {

    private static final long serialVersionUID = 1L;

    private Label progressLabel;

    private Image progressImage;

    public ProgressFragment(String id, IModel messageModel) {
      super(id, "progressFragment", UpdateParticipantListPanel.this);

      progressLabel = new Label("progressLabel", messageModel);
      add(progressLabel);

      progressImage = new Image("progressImage");
      add(progressImage);
    }
  }

  public class ResultFragment extends Fragment {

    private static final long serialVersionUID = 1L;

    private Label resultLabel;

    public ResultFragment(String id, IModel messageModel) {
      super(id, "resultFragment", UpdateParticipantListPanel.this);

      resultLabel = new Label("resultLabel", messageModel);
      add(resultLabel);

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

  public void displayFeedback(AjaxRequestTarget target) {
    feedbackWindow.setContent(new FeedbackPanel("content"));
    feedbackWindow.show(target);
  }
}
