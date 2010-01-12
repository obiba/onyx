/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.wicket.wizard;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.behavior.IBehavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.engine.ActionDefinition;
import org.obiba.onyx.engine.ActionType;
import org.obiba.onyx.engine.Stage;
import org.obiba.onyx.engine.state.IStageExecution;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;
import org.obiba.onyx.quartz.core.wicket.layout.impl.simplified.ProgressBarPanel;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireModel;
import org.obiba.onyx.wicket.StageModel;
import org.obiba.onyx.wicket.action.ActionWindow;
import org.obiba.onyx.wicket.behavior.ButtonDisableBehavior;
import org.obiba.onyx.wicket.reusable.ConfirmationDialog;
import org.obiba.onyx.wicket.reusable.FeedbackWindow;
import org.obiba.onyx.wicket.reusable.ReusableDialogProvider;
import org.obiba.onyx.wicket.reusable.WizardAdministrationWindow;
import org.obiba.onyx.wicket.reusable.ConfirmationDialog.OnYesCallback;
import org.obiba.onyx.wicket.reusable.Dialog.CloseButtonCallback;
import org.obiba.onyx.wicket.reusable.Dialog.Status;
import org.obiba.onyx.wicket.reusable.Dialog.WindowClosedCallback;
import org.obiba.onyx.wicket.wizard.WizardForm;
import org.obiba.onyx.wicket.wizard.WizardStepPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * WizardForm for the questionnaire Contains Language Selection Step, Conclusion Step and Interrupt link
 */
public class QuestionnaireWizardForm extends WizardForm {

  private static final Logger log = LoggerFactory.getLogger(QuestionnaireWizardForm.class);

  //
  // Constants
  //

  private static final long serialVersionUID = 1L;

  //
  // Instance Variables
  //

  @SpringBean(name = "activeInterviewService")
  private ActiveInterviewService activeInterviewService;

  @SpringBean
  private ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationService;

  private WizardStepPanel languageSelectionStep;

  private WizardStepPanel conclusionStep;

  private WizardStepPanel confirmResumeStep;

  private StageModel stageModel;

  private ActionWindow actionWindow;

  private FeedbackWindow feedbackWindow;

  private boolean resuming;

  protected boolean modalFeedback;

  protected WizardAdministrationWindow adminWindow;

  protected ProgressBarPanel progressBar;

  private boolean adminWindowClosed = false;

  //
  // Constructors
  //

  public QuestionnaireWizardForm(String id, IModel questionnaireModel) {
    super(id, questionnaireModel);

    // Add Interrupt button.
    add(createInterrupt());

    // Language selection step.
    languageSelectionStep = new LanguageSelectionStep(getStepId());

    // Conclusion step.
    conclusionStep = new ConclusionStep(getStepId());

    progressBar = new ProgressBarPanel("progressBar");
    progressBar.setVisible(false);
    add(progressBar);

    createModalAdministrationPanel();

    final IBehavior buttonDisableBehavior = new ButtonDisableBehavior();

    // admin button
    AjaxLink link = new AjaxLink("adminLink") {
      private static final long serialVersionUID = 0L;

      @Override
      public void onClick(AjaxRequestTarget target) {
        adminWindow.setInterruptState(getInterruptLink().isEnabled());
        if(getCancelLink() != null) adminWindow.setCancelState(getCancelLink().isEnabled());
        adminWindow.show(target);
      }

    };
    link.add(new AttributeModifier("value", true, new StringResourceModel("Administration", this, null)));
    link.add(new AttributeAppender("class", new Model("ui-corner-all"), " "));
    add(link);
  }

  @SuppressWarnings("serial")
  private void createModalAdministrationPanel() {
    // Create modal feedback window
    adminWindow = new WizardAdministrationWindow("adminWindow");

    AjaxLink cancelLink = new AjaxLink("cancelStage") {

      private static final long serialVersionUID = 1L;

      @Override
      public void onClick(AjaxRequestTarget target) {
        Label label = new Label("content", new StringResourceModel("ConfirmCancellationOfQuestionnaire", this, null));
        label.add(new AttributeModifier("class", true, new Model("confirmation-dialog-content")));

        ConfirmationDialog confirmationDialog = ((ReusableDialogProvider) getPage()).getConfirmationDialog();
        confirmationDialog.setContent(label);
        confirmationDialog.setTitle(new StringResourceModel("ConfirmCancellationOfQuestionnaireTitle", this, null));
        confirmationDialog.setYesButtonCallback(new OnYesCallback() {

          private static final long serialVersionUID = -6691702933562884991L;

          public void onYesButtonClicked(AjaxRequestTarget target) {
            adminWindow.setStatus(Status.CLOSED);
            if(adminWindow.getCloseButtonCallback() == null || (adminWindow.getCloseButtonCallback() != null && adminWindow.getCloseButtonCallback().onCloseButtonClicked(target, adminWindow.getStatus()))) adminWindow.close(target);
          }

        });
        confirmationDialog.show(target);
      }
    };

    adminWindow.setCancelLink("CancelQuestionnaire", cancelLink);

    adminWindow.setCloseButtonCallback(new CloseButtonCallback() {

      public boolean onCloseButtonClicked(AjaxRequestTarget target, Status status) {
        adminWindowClosed = true;
        return true;
      }

    });

    adminWindow.setWindowClosedCallback(new WindowClosedCallback() {
      public void onClose(AjaxRequestTarget target, Status status) {
        switch(status) {
        case OTHER:
          onInterrupt(target);
          break;
        case SUCCESS:
          onFinishSubmit(target, QuestionnaireWizardForm.this);
          break;
        case ERROR:
          onFinishError(target, QuestionnaireWizardForm.this);
          break;
        case CLOSED:
          onCancelClick(target);
        }
      }
    });
    adminWindowClosed = false;
    add(adminWindow);
  }

  //
  // WizardForm Methods
  //

  public void onInterrupt(AjaxRequestTarget target) {
    IStageExecution exec = activeInterviewService.getStageExecution((Stage) stageModel.getObject());
    ActionDefinition actionDef = exec.getActionDefinition(ActionType.INTERRUPT);

    if(actionDef != null) {
      actionWindow.show(target, stageModel, actionDef);
    }
  }

  public void onCancel(AjaxRequestTarget target) {
    IStageExecution exec = activeInterviewService.getStageExecution((Stage) stageModel.getObject());
    ActionDefinition actionDef = exec.getActionDefinition(ActionType.STOP);
    if(actionDef != null) {
      actionWindow.show(target, stageModel, actionDef);
    }
  }

  public void onFinish(AjaxRequestTarget target, Form form) {
    IStageExecution exec = activeInterviewService.getStageExecution((Stage) stageModel.getObject());
    ActionDefinition actionDef = exec.getSystemActionDefinition(ActionType.COMPLETE);
    if(actionDef != null) {
      actionWindow.show(target, stageModel, actionDef);
    }
  }

  public void onError(AjaxRequestTarget target, Form form) {
    log.info("onError={}", Session.get().getFeedbackMessages().iterator().next());
    showFeedbackWindow(target);
  }

  @Override
  public FeedbackWindow getFeedbackWindow() {
    return feedbackWindow;
  }

  //
  // Methods
  //

  public void initStartStep(boolean resuming) {
    this.resuming = resuming;

    WizardStepPanel startStep = null;

    if(resuming) {
      confirmResumeStep = new ConfirmResumeStep(getStepId(), getModel());
      startStep = confirmResumeStep;
    } else {
      startStep = languageSelectionStep;
    }

    add(startStep);
    startStep.onStepInNext(this, null);
    startStep.handleWizardState(this, null);
  }

  public void setStageModel(StageModel stageModel) {
    this.stageModel = stageModel;
  }

  public void setActionWindow(ActionWindow window) {
    this.actionWindow = window;
  }

  public void setFeedbackWindow(FeedbackWindow feedbackWindow) {
    this.feedbackWindow = feedbackWindow;
  }

  private AjaxLink createInterrupt() {
    AjaxLink link = new AjaxLink("interrupt") {
      private static final long serialVersionUID = 0L;

      @Override
      public void onClick(AjaxRequestTarget target) {
        onInterrupt(target);
      }

    };
    link.add(new AttributeModifier("value", true, new StringResourceModel("Interrupt", QuestionnaireWizardForm.this, null)));

    return link;
  }

  public Component getInterruptLink() {
    return get("interrupt");
  }

  /**
   * Returns the first page step.
   * 
   * @return first page step (or <code>null</code> if the questionnaire has no pages)
   */
  public WizardStepPanel getFirstPageStep() {
    Page startPage = activeQuestionnaireAdministrationService.startPage();
    return new PageStepPanel(getStepId(), new QuestionnaireModel(startPage));
  }

  /**
   * Returns the last page step.
   * 
   * @return last page step (or <code>null</code> if the questionnaire has no pages)
   */
  public WizardStepPanel getLastPageStep() {
    Page lastPage = activeQuestionnaireAdministrationService.lastPage();

    PageStepPanel pageStepPanel = new PageStepPanel(getStepId(), new QuestionnaireModel(lastPage));

    if(resuming && activeQuestionnaireAdministrationService.isOnStartPage()) {
      pageStepPanel.setPreviousEnabled(false);
    }

    return pageStepPanel;
  }

  /**
   * Returns the previous step.
   * 
   * If the current page step is the first page step, returns the language selection step. Otherwise, the previous page
   * step is returned.
   * 
   * @return previous step
   */
  public WizardStepPanel getPreviousStep() {
    Page previousPage = activeQuestionnaireAdministrationService.previousPage();

    if(previousPage != null) {
      PageStepPanel pageStepPanel = new PageStepPanel(getStepId(), new QuestionnaireModel(previousPage));

      if(resuming && activeQuestionnaireAdministrationService.isOnStartPage()) {
        pageStepPanel.setPreviousEnabled(false);
      }

      return pageStepPanel;
    } else {
      return (resuming ? null : languageSelectionStep);
    }
  }

  /**
   * Returns the next step.
   * 
   * If the current page step is the last page step, returns the conclusion step. Otherwise, the next page step is
   * returned.
   * 
   * @return next step
   */
  public WizardStepPanel getNextStep() {
    Page nextPage = activeQuestionnaireAdministrationService.nextPage();

    if(nextPage != null) {
      return new PageStepPanel(getStepId(), new QuestionnaireModel(nextPage));
    } else {
      return conclusionStep;
    }
  }

  public WizardStepPanel getResumeStep() {
    Page resumePage = activeQuestionnaireAdministrationService.resumePage();

    if(resumePage != null) {
      PageStepPanel pageStepPanel = new PageStepPanel(getStepId(), new QuestionnaireModel(resumePage));

      if(resuming && activeQuestionnaireAdministrationService.isOnStartPage()) {
        pageStepPanel.setPreviousEnabled(false);
      }

      return pageStepPanel;
    } else {
      return conclusionStep;
    }
  }

  public boolean hasModalFeedback() {
    return modalFeedback;
  }

  public void setModalFeedback(boolean modalFeedback) {
    this.modalFeedback = modalFeedback;
  }

  /**
   * Updates the progress bar to display the percentage completed for this Questionnaire Wizard.
   * 
   * @param form
   */
  public void updateProgressBar(WizardForm form) {

    // Calculate the percentage
    int progressPercentage = calculatePercentageCompleted();

    // Update the progress bar to display the percentage
    ProgressBarPanel progressBar = ((QuestionnaireWizardForm) form).getProgressBar();
    progressBar.setProgressPercentage(progressPercentage);

    // Make sure the progress bar is visible
    progressBar.setVisible(true);
  }

  /**
   * Calculates the percentage completed for this Questionnaire Wizard.
   * 
   * @return The percentage completed.
   */
  private int calculatePercentageCompleted() {

    int pageTotalCount = activeQuestionnaireAdministrationService.getQuestionnaire().getPages().size();
    log.debug("Total number of pages {}", pageTotalCount);

    int lastCompletedPageNo = activeQuestionnaireAdministrationService.getCurrentPageNumber() - 1;
    log.debug("Last completed page number {}", lastCompletedPageNo);

    int progressPercentage = (int) (((float) lastCompletedPageNo / (float) pageTotalCount) * 100);
    log.debug("Progress percentage {}", progressPercentage);

    return progressPercentage;
  }

  public ProgressBarPanel getProgressBar() {
    return progressBar;
  }

  public void setAdminWindowClosed(boolean adminWindowClosed) {
    this.adminWindowClosed = adminWindowClosed;
  }

}
