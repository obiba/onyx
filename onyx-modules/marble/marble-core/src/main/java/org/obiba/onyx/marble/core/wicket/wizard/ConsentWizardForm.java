/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.marble.core.wicket.wizard;

import java.util.EnumSet;
import java.util.List;
import java.util.Locale;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.behavior.IBehavior;
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
import org.obiba.onyx.marble.core.service.ActiveConsentService;
import org.obiba.onyx.marble.core.service.ConsentService;
import org.obiba.onyx.marble.domain.consent.Consent;
import org.obiba.onyx.marble.domain.consent.ConsentMode;
import org.obiba.onyx.wicket.StageModel;
import org.obiba.onyx.wicket.action.ActionWindow;
import org.obiba.onyx.wicket.behavior.ButtonDisableBehavior;
import org.obiba.onyx.wicket.reusable.FeedbackWindow;
import org.obiba.onyx.wicket.reusable.WizardAdministrationWindow;
import org.obiba.onyx.wicket.reusable.Dialog.CloseButtonCallback;
import org.obiba.onyx.wicket.reusable.Dialog.Status;
import org.obiba.onyx.wicket.reusable.Dialog.WindowClosedCallback;
import org.obiba.onyx.wicket.wizard.WizardForm;
import org.obiba.onyx.wicket.wizard.WizardStepPanel;

public class ConsentWizardForm extends WizardForm {

  @SpringBean(name = "activeInterviewService")
  private ActiveInterviewService activeInterviewService;

  @SpringBean
  private ActiveConsentService activeConsentService;

  @SpringBean
  private ConsentService consentService;

  private WizardStepPanel consentModeSelectionStep;

  private WizardStepPanel electronicConsentStep;

  private WizardStepPanel consentConfirmationStep;

  private StageModel stageModel;

  private ActionWindow actionWindow;

  private FeedbackWindow feedbackWindow;

  protected WizardAdministrationWindow adminWindow;

  private boolean adminWindowClosed = false;

  public ConsentWizardForm(String id, IModel interviewConsentModel) {
    super(id, interviewConsentModel);

    consentConfirmationStep = new ManualConsentStep(getStepId());
    electronicConsentStep = new ElectronicConsentStep(getStepId(), consentConfirmationStep);
    consentModeSelectionStep = new ConsentModeSelectionStep(getStepId(), electronicConsentStep, consentConfirmationStep);

    WizardStepPanel startStep = setupWizardFlow();

    startStep.onStepInNext(this, null);
    startStep.handleWizardState(this, null);
    add(startStep);

    createModalAdministrationPanel();

    final IBehavior buttonDisableBehavior = new ButtonDisableBehavior();

    // admin button
    AjaxLink link = new AjaxLink("adminLink") {
      private static final long serialVersionUID = 0L;

      @Override
      public void onClick(AjaxRequestTarget target) {
        adminWindow.setInitialWidth(350);
        adminWindow.setInterruptState(false, false, buttonDisableBehavior);
        if(getCancelLink() != null) adminWindow.setCancelState(getCancelLink().isEnabled(), getCancelLink().isVisible(), buttonDisableBehavior);
        adminWindow.setFinishState(getFinishLink().isEnabled(), getFinishLink().isVisible(), buttonDisableBehavior);
        adminWindow.show(target);
      }

    };
    link.add(new AttributeModifier("value", true, new StringResourceModel("Administration", this, null)));
    link.add(new AttributeAppender("class", new Model("ui-corner-all"), " "));
    add(link);
  }

  private WizardStepPanel setupWizardFlow() {
    // get the consent mode variable value
    EnumSet<ConsentMode> supportedConsentMode = consentService.getSupportedConsentModes();
    List<Locale> supportedConsentLocale = consentService.getSupportedConsentLocales();
    WizardStepPanel startStep;

    if(supportedConsentMode.containsAll(EnumSet.allOf(ConsentMode.class)) || supportedConsentMode.contains(ConsentMode.MANUAL) && supportedConsentLocale.size() > 1) {
      startStep = consentModeSelectionStep;
      startStep.setPreviousStep(startStep);
      electronicConsentStep.setPreviousStep(startStep);
    } else {
      consentConfirmationStep.setPreviousStep(null);
      startStep = consentConfirmationStep;
    }

    return startStep;
  }

  @SuppressWarnings("serial")
  private void createModalAdministrationPanel() {
    // Create modal feedback window
    adminWindow = new WizardAdministrationWindow("adminWindow");

    adminWindow.setCancelLink("CancelConsent");
    adminWindow.createFinishLink(this.getRootForm());
    adminWindow.setCloseButtonCallback(new CloseButtonCallback() {

      public boolean onCloseButtonClicked(AjaxRequestTarget target, Status status) {
        if(status.equals(Status.ERROR)) {
          showFeedbackWindow(target);
          return false;
        }
        adminWindowClosed = true;
        return true;
      }

    });

    adminWindow.setWindowClosedCallback(new WindowClosedCallback() {
      public void onClose(AjaxRequestTarget target, Status status) {
        switch(status) {
        case SUCCESS:
          onFinishSubmit(target, ConsentWizardForm.this);
          break;
        case ERROR:
          onFinishError(target, ConsentWizardForm.this);
          break;
        case CLOSED:
          onCancelClick(target);
        }
      }
    });
    adminWindowClosed = false;
    add(adminWindow);
  }

  public ActiveInterviewService getActiveInterviewService() {
    return activeInterviewService;
  }

  public void setActiveInterviewService(ActiveInterviewService activeInterviewService) {
    this.activeInterviewService = activeInterviewService;
  }

  public WizardStepPanel getConsentModeSelectionStep() {
    return consentModeSelectionStep;
  }

  public void setConsentModeSelectionStep(WizardStepPanel consentModeSelectionStep) {
    this.consentModeSelectionStep = consentModeSelectionStep;
  }

  public WizardStepPanel getElectronicConsentStep() {
    return electronicConsentStep;
  }

  public void setElectronicConsentStep(WizardStepPanel electronicConsentStep) {
    this.electronicConsentStep = electronicConsentStep;
  }

  public WizardStepPanel getConsentConfirmationStep() {
    return consentConfirmationStep;
  }

  public void setConsentConfirmationStep(WizardStepPanel consentConfirmationStep) {
    this.consentConfirmationStep = consentConfirmationStep;
  }

  public void setStageModel(StageModel stageModel) {
    this.stageModel = stageModel;
  }

  public void setActionWindow(ActionWindow window) {
    this.actionWindow = window;
  }

  public ActionWindow getActionWindow() {
    return actionWindow;
  }

  public void setFeedbackWindow(FeedbackWindow feedbackWindow) {
    this.feedbackWindow = feedbackWindow;
  }

  @Override
  public FeedbackWindow getFeedbackWindow() {
    return feedbackWindow;
  }

  //
  // WizardForm Methods
  //

  public void onCancel(AjaxRequestTarget target) {
    IStageExecution exec = activeInterviewService.getStageExecution((Stage) stageModel.getObject());
    ActionDefinition actionDef = exec.getActionDefinition(ActionType.STOP);
    if(actionDef != null) {
      actionWindow.show(target, stageModel, actionDef);
    }
  }

  public void onFinish(AjaxRequestTarget target, Form form) {
    Boolean consentIsSubmitted = activeConsentService.isConsentFormSubmitted();
    Boolean consentIsAccepted = activeConsentService.getConsent().isAccepted();
    Boolean consentIsElectronic = activeConsentService.getConsent().getMode() == ConsentMode.ELECTRONIC ? true : false;

    // Consent not submitted, inform the user that the submit button (PDF form) has to be clicked.
    if(!consentIsSubmitted) {
      error(getString("MissingConsentForm"));
      onError(target, form);

      // Invalid electronic consent.
    } else if(consentIsAccepted && consentIsElectronic && !activeConsentService.validateElectronicConsent()) {
      error(getString("InvalidConsentForm"));
      getElectronicConsentStep().setNextStep(null);
      gotoNext(target);
      this.changeWizardFormStyle("wizard-consent");
      onError(target, form);

      // Valid electronic consent, refused electronic consent, or manual consent.
    } else {
      IStageExecution exec = activeInterviewService.getStageExecution((Stage) stageModel.getObject());
      ActionDefinition actionDef = exec.getSystemActionDefinition(ActionType.COMPLETE);

      // Delete previous consent (if exist) for that interview
      Consent existingConsent = consentService.getConsent(activeInterviewService.getInterview());
      if(existingConsent != null) {
        consentService.deletePreviousConsent(activeInterviewService.getInterview());
      }

      // Save the consent
      consentService.saveConsent(activeConsentService.getConsent());

      if(actionDef != null) {
        actionWindow.show(target, stageModel, actionDef);
      }
    }

    target.appendJavascript("Resizer.resizeWizard();");
    target.appendJavascript("Resizer.resizeConsentFrame();");
  }

  public void onError(AjaxRequestTarget target, Form form) {
    showFeedbackWindow(target);
  }
}
