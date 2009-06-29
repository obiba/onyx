/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.core.wicket.wizard;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
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
import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.core.domain.contraindication.Contraindication;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.engine.ActionDefinition;
import org.obiba.onyx.engine.ActionType;
import org.obiba.onyx.engine.Stage;
import org.obiba.onyx.engine.state.IStageExecution;
import org.obiba.onyx.jade.core.domain.instrument.Instrument;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameterCaptureMethod;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentStatus;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.onyx.jade.core.service.ActiveInstrumentRunService;
import org.obiba.onyx.wicket.StageModel;
import org.obiba.onyx.wicket.action.ActionWindow;
import org.obiba.onyx.wicket.reusable.ConfirmationDialog;
import org.obiba.onyx.wicket.reusable.ConfirmationDialogProvider;
import org.obiba.onyx.wicket.reusable.FeedbackWindow;
import org.obiba.onyx.wicket.reusable.WizardAdministrationWindow;
import org.obiba.onyx.wicket.reusable.ConfirmationDialog.OnYesCallback;
import org.obiba.onyx.wicket.reusable.Dialog.CloseButtonCallback;
import org.obiba.onyx.wicket.reusable.Dialog.Status;
import org.obiba.onyx.wicket.reusable.Dialog.WindowClosedCallback;
import org.obiba.onyx.wicket.wizard.WizardForm;
import org.obiba.onyx.wicket.wizard.WizardStepPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InstrumentWizardForm extends WizardForm {

  private static final long serialVersionUID = 1L;

  private static final Logger log = LoggerFactory.getLogger(InstrumentWizardForm.class);

  @SpringBean
  private EntityQueryService queryService;

  @SpringBean(name = "activeInterviewService")
  private ActiveInterviewService activeInterviewService;

  @SpringBean
  private ActiveInstrumentRunService activeInstrumentRunService;

  private WizardStepPanel instrumentSelectionStep;

  private WizardStepPanel observedContraIndicationStep;

  private WizardStepPanel askedContraIndicationStep;

  private WizardStepPanel inputParametersStep;

  private WizardStepPanel instrumentLaunchStep;

  private WizardStepPanel outputParametersStep;

  private WizardStepPanel conclusionStep;

  private WizardStepPanel warningStep;

  private ActionWindow actionWindow;

  private StageModel stageModel;

  private FeedbackWindow feedbackWindow;

  protected WizardAdministrationWindow adminWindow;

  private boolean resuming;

  private boolean adminWindowClosed = false;

  public InstrumentWizardForm(String id, IModel instrumentTypeModel) {
    super(id, instrumentTypeModel);

    // Add Interrupt button.
    add(createInterrupt());

    InstrumentType type = (InstrumentType) instrumentTypeModel.getObject();
    log.debug("instrumentType={}", type.getName());

    WizardStepPanel startStep = null;

    observedContraIndicationStep = new ObservedContraIndicationStep(getStepId());
    askedContraIndicationStep = new AskedContraIndicationStep(getStepId());
    instrumentSelectionStep = new InstrumentSelectionStep(getStepId(), instrumentTypeModel);
    inputParametersStep = new InputParametersStep(getStepId());
    instrumentLaunchStep = new InstrumentLaunchStep(getStepId());
    conclusionStep = new ConclusionStep(getStepId());
    warningStep = new WarningsStep(getStepId());
    outputParametersStep = new OutputParametersStep(getStepId(), conclusionStep, warningStep);

    warningStep.setNextStep(conclusionStep);
    warningStep.setPreviousStep(outputParametersStep);

    startStep = setUpWizardFlow();
    add(startStep);
    startStep.onStepInNext(this, null);
    startStep.handleWizardState(this, null);

    createModalAdministrationPanel();

    final IBehavior buttonDisableBehavior = new WizardButtonDisableBehavior();

    // admin button
    AjaxLink link = new AjaxLink("adminLink") {
      private static final long serialVersionUID = 0L;

      @Override
      public void onClick(AjaxRequestTarget target) {
        adminWindow.setInterruptState(getInterruptLink().isEnabled(), getInterruptLink().isVisible(), buttonDisableBehavior);
        if(getCancelLink() != null) adminWindow.setCancelState(getCancelLink().isEnabled(), getCancelLink().isVisible(), buttonDisableBehavior);
        adminWindow.setFinishState(getFinishLink().isEnabled(), getFinishLink().isVisible(), buttonDisableBehavior);
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
        Label label = new Label("content", new StringResourceModel("ConfirmCancellationOfMeasurement", this, null));
        label.add(new AttributeModifier("class", true, new Model("confirmation-dialog-content")));

        ConfirmationDialog confirmationDialog = ((ConfirmationDialogProvider) getPage()).getConfirmationDialog();
        confirmationDialog.setContent(label);
        confirmationDialog.setTitle(new StringResourceModel("ConfirmCancellationOfMeasurementTitle", this, null));
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

    adminWindow.setCancelLink("CancelMeasurement", cancelLink);

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
          onFinishSubmit(target, InstrumentWizardForm.this);
          break;
        case ERROR:
          onFinishError(target, InstrumentWizardForm.this);
          break;
        case CLOSED:
          onCancelClick(target);
        }
      }
    });
    adminWindowClosed = false;
    add(adminWindow);
  }

  private AjaxLink createInterrupt() {
    AjaxLink link = new AjaxLink("interrupt") {
      private static final long serialVersionUID = 0L;

      @Override
      public void onClick(AjaxRequestTarget target) {
        onInterrupt(target);
      }

    };
    link.add(new AttributeModifier("value", true, new StringResourceModel("Interrupt", InstrumentWizardForm.this, null)));

    return link;
  }

  public Component getInterruptLink() {
    return get("interrupt");
  }

  public void onInterrupt(AjaxRequestTarget target) {
    IStageExecution exec = activeInterviewService.getStageExecution((Stage) stageModel.getObject());
    ActionDefinition actionDef = exec.getActionDefinition(ActionType.INTERRUPT);

    if(actionDef != null) {
      actionWindow.show(target, stageModel, actionDef);
    }
  }

  public WizardStepPanel setUpWizardFlow() {
    WizardStepPanel startStep = null;
    WizardStepPanel lastStep = null;

    // are there observed contra-indications to display ?
    if(activeInstrumentRunService.hasContraindications(Contraindication.Type.OBSERVED)) {
      if(startStep == null) {
        startStep = observedContraIndicationStep;
        lastStep = startStep;
      } else {
        if(lastStep != null) {
          lastStep.setNextStep(observedContraIndicationStep);
        }
        observedContraIndicationStep.setPreviousStep(lastStep);
        lastStep = observedContraIndicationStep;
      }
    } else {
      log.debug("No contraindications of type OBSERVED. Skipping step.");
    }

    // are there asked contra-indications to display ?
    if(activeInstrumentRunService.hasContraindications(Contraindication.Type.ASKED)) {
      if(startStep == null) {
        startStep = askedContraIndicationStep;
        lastStep = startStep;
      } else {
        if(lastStep != null) {
          lastStep.setNextStep(askedContraIndicationStep);
        }
        askedContraIndicationStep.setPreviousStep(lastStep);
        lastStep = askedContraIndicationStep;
      }
    } else {
      log.debug("No contraindications of type ASKED. Skipping step.");
    }

    InstrumentType instrumentType = activeInstrumentRunService.getInstrumentType();
    // do we need to select the instrument ?
    Instrument instrumentTemplate = new Instrument();
    instrumentTemplate.setType(instrumentType.getName());
    instrumentTemplate.setStatus(InstrumentStatus.ACTIVE);
    log.debug("instruments.count={}", queryService.count(instrumentTemplate));
    if(queryService.count(instrumentTemplate) > 1) {
      if(startStep == null) {
        startStep = instrumentSelectionStep;
        lastStep = startStep;
      } else {
        if(lastStep != null) {
          lastStep.setNextStep(instrumentSelectionStep);
        }
        instrumentSelectionStep.setPreviousStep(lastStep);
        lastStep = instrumentSelectionStep;
      }
    } else {
      // pre selected instrument
      activeInstrumentRunService.setInstrument(queryService.matchOne(instrumentTemplate));
    }

    // are there input parameters with input source that requires user provisioning ?
    // or interpretative questions
    log.debug("instrumentInterpretativeParameters.count={}", instrumentType.getInterpretativeParameters().size());
    log.debug("instrumentInputParameters.count={}", instrumentType.getInputParameters(false));
    if(instrumentType.hasInterpretativeParameter() || instrumentType.hasInputParameter(false)) {
      if(startStep == null) {
        startStep = inputParametersStep;
        lastStep = startStep;
      } else {
        if(lastStep != null) {
          lastStep.setNextStep(inputParametersStep);
        }
        inputParametersStep.setPreviousStep(lastStep);
        lastStep = inputParametersStep;
      }
    }

    // are there output parameters that are to be captured automatically from instrument (i.e. requires instrument
    // launch) ?
    log.debug("instrument.isInteractive={}", instrumentType.isInteractive());
    if(instrumentType.isInteractive()) {
      if(startStep == null) {
        startStep = instrumentLaunchStep;
        lastStep = startStep;
      } else {
        if(lastStep != null) {
          lastStep.setNextStep(instrumentLaunchStep);
        }
        instrumentLaunchStep.setPreviousStep(lastStep);
        lastStep = instrumentLaunchStep;
      }
    }

    // are there output parameters that are to be captured manually from instrument ?
    log.debug("instrumentOutputParameters.MANUAL.count={}", instrumentType.getOutputParameters(InstrumentParameterCaptureMethod.MANUAL).size());
    if(instrumentType.hasOutputParameter(InstrumentParameterCaptureMethod.MANUAL)) {
      if(startStep == null) {
        startStep = outputParametersStep;
        lastStep = startStep;
      } else {
        if(lastStep != null) {
          lastStep.setNextStep(outputParametersStep);
        }
        outputParametersStep.setPreviousStep(lastStep);
        lastStep = outputParametersStep;
      }
    }

    // validation: final step
    if(startStep == null) {
      startStep = conclusionStep;
      lastStep = startStep;
    } else {
      if(lastStep != null) {
        lastStep.setNextStep(conclusionStep);
      }
      conclusionStep.setPreviousStep(lastStep);
      lastStep = conclusionStep;
    }

    return startStep;
  }

  public WizardStepPanel getInstrumentSelectionStep() {
    return instrumentSelectionStep;
  }

  public WizardStepPanel getObservedContraIndicationStep() {
    return observedContraIndicationStep;
  }

  public WizardStepPanel getAskedContraIndicationStep() {
    return askedContraIndicationStep;
  }

  public WizardStepPanel getInputParametersStep() {
    return inputParametersStep;
  }

  public WizardStepPanel getInstrumentLaunchStep() {
    return instrumentLaunchStep;
  }

  public WizardStepPanel getOutputParametersStep() {
    return outputParametersStep;
  }

  public WizardStepPanel getConclusionStep() {
    return conclusionStep;
  }

  public void setActionWindow(ActionWindow window) {
    this.actionWindow = window;
  }

  public ActionWindow getActionWindow() {
    return actionWindow;
  }

  public void setStageModel(StageModel stageModel) {
    this.stageModel = stageModel;
  }

  public void setFeedbackWindow(FeedbackWindow feedbackWindow) {
    this.feedbackWindow = feedbackWindow;
  }

  @Override
  public void onFinish(AjaxRequestTarget target, Form form) {
    IStageExecution exec = activeInterviewService.getStageExecution((Stage) stageModel.getObject());
    ActionDefinition actionDef = exec.getSystemActionDefinition(ActionType.COMPLETE);
    if(actionDef != null) {
      actionWindow.show(target, stageModel, actionDef);
    }
  }

  @Override
  public void onCancel(AjaxRequestTarget target) {
    IStageExecution exec = activeInterviewService.getStageExecution((Stage) stageModel.getObject());
    ActionDefinition actionDef = exec.getActionDefinition(ActionType.STOP);
    if(actionDef != null) {
      actionWindow.show(target, stageModel, actionDef);
    }
  }

  @Override
  public void onError(AjaxRequestTarget target, Form form) {
    showFeedbackWindow(target);
  }

  @Override
  public FeedbackWindow getFeedbackWindow() {
    return feedbackWindow;
  }

  public void initStartStep(boolean resuming) {
    this.resuming = resuming;

    WizardStepPanel startStep = setUpWizardFlow();

    add(startStep);
    startStep.onStepInNext(this, null);
    startStep.handleWizardState(this, null);
  }

}