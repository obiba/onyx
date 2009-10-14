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

import java.util.ArrayList;
import java.util.List;

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
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.core.service.UserSessionService;
import org.obiba.onyx.engine.ActionDefinition;
import org.obiba.onyx.engine.ActionType;
import org.obiba.onyx.engine.Stage;
import org.obiba.onyx.engine.state.IStageExecution;
import org.obiba.onyx.jade.core.domain.instrument.Instrument;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentInputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentOutputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameterCaptureMethod;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.onyx.jade.core.domain.instrument.InterpretativeParameter;
import org.obiba.onyx.jade.core.domain.run.InstrumentRun;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;
import org.obiba.onyx.jade.core.domain.run.Measure;
import org.obiba.onyx.jade.core.service.ActiveInstrumentRunService;
import org.obiba.onyx.jade.core.service.InstrumentRunService;
import org.obiba.onyx.jade.core.service.InstrumentService;
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

public class InstrumentWizardForm extends WizardForm {

  private static final long serialVersionUID = 1L;

  private static final Logger log = LoggerFactory.getLogger(InstrumentWizardForm.class);

  @SpringBean
  private EntityQueryService queryService;

  @SpringBean(name = "activeInterviewService")
  private ActiveInterviewService activeInterviewService;

  @SpringBean
  private ActiveInstrumentRunService activeInstrumentRunService;

  @SpringBean
  private InstrumentRunService instrumentRunService;

  @SpringBean
  private InstrumentService instrumentService;

  @SpringBean
  private UserSessionService userSessionService;

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

  private boolean adminWindowClosed = false;

  private boolean instrumentSelected = false;

  public InstrumentWizardForm(String id, IModel instrumentTypeModel) {
    super(id, instrumentTypeModel);

    // Add Interrupt button.
    add(createInterrupt());

    InstrumentType type = (InstrumentType) instrumentTypeModel.getObject();
    log.debug("instrumentType={}", type.getName());

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

    createModalAdministrationPanel();

    final IBehavior buttonDisableBehavior = new ButtonDisableBehavior();

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

        ConfirmationDialog confirmationDialog = ((ReusableDialogProvider) getPage()).getConfirmationDialog();
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

  private List<Contraindication> getContraindications(Contraindication.Type type) {
    List<Contraindication> contraindications = new ArrayList<Contraindication>();

    for(Contraindication contraindication : ((InstrumentType) getModelObject()).getContraindications()) {
      if(contraindication.getType().equals(type)) {
        contraindications.add(contraindication);
      }
    }

    return contraindications;
  }

  private boolean hasContraindications(Contraindication.Type type) {
    return !getContraindications(type).isEmpty();
  }

  public WizardStepPanel setUpWizardFlow(WizardStepPanel startStepWhenResuming) {
    boolean resuming = startStepWhenResuming != null;
    WizardStepPanel startStep = startStepWhenResuming;
    WizardStepPanel lastStep = null;

    List<Instrument> activeInstrumentsForCurrentWorkstation = getActiveInstrumentsForCurrentWorkstation();
    log.debug("instruments.count={}", activeInstrumentsForCurrentWorkstation.size());
    if(activeInstrumentsForCurrentWorkstation.size() == 0 || activeInstrumentsForCurrentWorkstation.size() > 1) {
      // Either found no instruments or too many instruments of the correct type.
      // In both cases we will prompt the user to enter the instrument they will be using for this measure.
      if(startStep == null || startStep.equals(instrumentSelectionStep)) {
        startStep = instrumentSelectionStep;
        lastStep = startStep;
      } else {
        if(lastStep != null) {
          lastStep.setNextStep(instrumentSelectionStep);
        }
        instrumentSelectionStep.setPreviousStep(lastStep);
        lastStep = instrumentSelectionStep;
      }
      instrumentSelected = true;
    } else {
      // A single instrument of the correct type is associated with this workstation.
      if(resuming) {
        activeInstrumentRunService.setInstrument(activeInstrumentsForCurrentWorkstation.get(0));
        log.debug("Resuming an InstrumentRun with the instrument type [" + activeInstrumentsForCurrentWorkstation.get(0).getType() + "] and barcode [" + activeInstrumentsForCurrentWorkstation.get(0).getBarcode() + "].");
      } else {
        if(!instrumentSelected) {
          activeInstrumentRunService.start(activeInterviewService.getParticipant(), activeInstrumentsForCurrentWorkstation.get(0));
          log.debug("Starting a new InstrumentRun with the instrument type [" + activeInstrumentsForCurrentWorkstation.get(0).getType() + "] and barcode [" + activeInstrumentsForCurrentWorkstation.get(0).getBarcode() + "].");
          instrumentSelected = true;
        }
      }
    }

    // are there observed contra-indications to display ?
    if(hasContraindications(Contraindication.Type.OBSERVED)) {
      if(startStep == null || startStep.equals(observedContraIndicationStep)) {
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
    if(hasContraindications(Contraindication.Type.ASKED)) {
      if(startStep == null || startStep.equals(askedContraIndicationStep)) {
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

    // are there input parameters with input source that requires user provisioning ?
    // or interpretative questions
    InstrumentType instrumentType = (InstrumentType) getModelObject();
    log.debug("instrumentInterpretativeParameters.count={}", instrumentType.getInterpretativeParameters().size());
    log.debug("instrumentInputParameters.count={}", instrumentType.getInputParameters(false));
    if(instrumentType.hasInterpretativeParameter() || instrumentType.hasInputParameter(false)) {
      if(startStep == null || startStep.equals(inputParametersStep)) {
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
      if(startStep == null || startStep.equals(instrumentLaunchStep)) {
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
    if(getOutputParametersOriginallyMarkedForManualCapture().size() > 0) {
      if(startStep == null || startStep.equals(outputParametersStep)) {
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

  private List<Instrument> getActiveInstrumentsForCurrentWorkstation() {
    List<Instrument> activeInstrumentsForCurrentWorkstation = new ArrayList<Instrument>();
    InstrumentType instrumentType = (InstrumentType) getModelObject();
    List<Instrument> activeInstruments = instrumentService.getActiveInstruments(instrumentType);
    for(Instrument instrument : activeInstruments) {
      if(instrument.getWorkstation() != null && instrument.getWorkstation().equals(userSessionService.getWorkstation())) {
        activeInstrumentsForCurrentWorkstation.add(instrument);
      }
    }
    return activeInstrumentsForCurrentWorkstation;
  }

  /**
   * Determines the step to be displayed when resuming the stage.
   * @return The step to resume to.
   */
  private WizardStepPanel getStepWhenResuming() {
    WizardStepPanel resumingStartStep = null;

    List<Instrument> activeInstrumentsForCurrentWorkstation = getActiveInstrumentsForCurrentWorkstation();
    if(activeInstrumentsForCurrentWorkstation.size() == 0 || activeInstrumentsForCurrentWorkstation.size() > 1) {
      // The user must select an instrument for the measure.
      return null;
    }

    InstrumentType instrumentType = activeInstrumentRunService.getInstrumentType();
    if(hasSomeOrAllOutputParameterValues()) {
      log.info("Has all output values.");
      if(getOutputParametersOriginallyMarkedForManualCapture().size() > 0) {
        log.info("Resume at outputParametersStep.");
        resumingStartStep = outputParametersStep;
      } else {
        log.info("Resume at instrumentLaunchStep.");
        resumingStartStep = instrumentLaunchStep;
      }
    } else {
      if(instrumentType.hasInterpretativeParameter() || instrumentType.hasInputParameter(false)) {
        if(hasSomeOrAllInterpretiveParameterValues()) {
          log.info("Resume at inputParametersStep.");
          resumingStartStep = inputParametersStep;
        } else {
          if(activeInstrumentRunService.hasContraindications(Contraindication.Type.OBSERVED)) {
            log.info("Resume at observedContraIndicationStep.");
            resumingStartStep = observedContraIndicationStep;
          } else if(activeInstrumentRunService.hasContraindications(Contraindication.Type.ASKED)) {
            log.info("Resume at askedContraIndicationStep.");
            resumingStartStep = askedContraIndicationStep;
          } else {
            log.info("Resume at inputParametersStep.");
            resumingStartStep = inputParametersStep;
          }
        }
      } else {
        if(activeInstrumentRunService.hasContraindications(Contraindication.Type.OBSERVED)) {
          log.info("Resume at observedContraIndicationStep.");
          resumingStartStep = observedContraIndicationStep;
        } else if(activeInstrumentRunService.hasContraindications(Contraindication.Type.ASKED)) {
          log.info("Resume at askedContraIndicationStep.");
          resumingStartStep = askedContraIndicationStep;
        } else {
          if(getOutputParametersOriginallyMarkedForManualCapture().size() > 0) {
            log.info("Resume at outputParametersStep.");
            resumingStartStep = outputParametersStep;
          } else {
            log.info("Resume at instrumentLaunchStep.");
            resumingStartStep = instrumentLaunchStep;
          }
        }
      }
    }
    log.info("Set resumingStartStep to [{}].", resumingStartStep);

    return resumingStartStep;
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
    WizardStepPanel startStepWhenResuming = null;

    InstrumentRun previousInstrumentRun = instrumentRunService.getInstrumentRun(activeInterviewService.getParticipant(), ((InstrumentType) getModelObject()).getName());
    if(resuming && previousInstrumentRun != null) {
      activeInstrumentRunService.setInstrumentRun(instrumentRunService.getInstrumentRun(activeInterviewService.getParticipant(), ((InstrumentType) getModelObject()).getName()));
      startStepWhenResuming = getStepWhenResuming();
      instrumentSelected = true;
    }

    WizardStepPanel startStep = setUpWizardFlow(startStepWhenResuming);

    add(startStep);
    startStep.onStepInNext(this, null);
    startStep.handleWizardState(this, null);
  }

  /**
   * Returns true if at least one output parameter has a value.
   * @return True if we have at least one output parameter value;
   */
  private boolean hasSomeOrAllOutputParameterValues() {
    InstrumentType instrumentType = activeInstrumentRunService.getInstrumentType();
    if(instrumentType.isRepeatable()) {
      return haveSomeOrAllRepeatingOutputParameterValues();
    } else {
      return haveSomeOrAllNonRepeatableOutputParameterValues();
    }
  }

  private boolean haveSomeOrAllNonRepeatableOutputParameterValues() {
    InstrumentType instrumentType = activeInstrumentRunService.getInstrumentType();
    InstrumentRun instrumentRun = activeInstrumentRunService.getInstrumentRun();
    for(InstrumentOutputParameter instrumentOutputParameter : instrumentType.getOutputParameters()) {
      InstrumentRunValue instrumentRunValue = instrumentRun.getInstrumentRunValue(instrumentOutputParameter);
      if(instrumentRunValue != null) {
        if(instrumentRunValue.getData(instrumentOutputParameter.getDataType()).getValue() != null) return true;
      }
    }
    return false; // No values found
  }

  private boolean haveSomeOrAllRepeatingOutputParameterValues() {
    InstrumentType instrumentType = activeInstrumentRunService.getInstrumentType();
    InstrumentRun instrumentRun = activeInstrumentRunService.getInstrumentRun();
    Participant participant = activeInstrumentRunService.getParticipant();
    if(instrumentType.getExpectedMeasureCount(participant) == 0) return false; // No values
    for(Measure measure : instrumentRun.getMeasures()) {
      InstrumentRun measureInstrumentRun = measure.getInstrumentRun();
      for(InstrumentOutputParameter instrumentOutputParameter : instrumentType.getOutputParameters()) {
        InstrumentRunValue instrumentRunValue = measureInstrumentRun.getInstrumentRunValue(instrumentOutputParameter);
        if(instrumentRunValue != null) {
          if(instrumentRunValue.getData(instrumentOutputParameter.getDataType()).getValue() != null) return true;
        }
      }
    }
    return false; // No values found
  }

  /**
   * Returns true if at least one interpretive parameter has a value.
   * @return True if we have at least one interpretive parameter value.
   */
  private boolean hasSomeOrAllInterpretiveParameterValues() {
    InstrumentType instrumentType = activeInstrumentRunService.getInstrumentType();
    InstrumentRun instrumentRun = activeInstrumentRunService.getInstrumentRun();
    for(InterpretativeParameter interpretiveParameter : instrumentType.getInterpretativeParameters()) {
      InstrumentRunValue instrumentRunValue = instrumentRun.getInstrumentRunValue(interpretiveParameter);
      if(instrumentRunValue != null) {
        if(instrumentRunValue.getData(interpretiveParameter.getDataType()).getValue() != null) return true;
      }
    }
    for(InstrumentInputParameter instrumentInputParameter : instrumentType.getInputParameters(false)) {
      InstrumentRunValue instrumentRunValue = instrumentRun.getInstrumentRunValue(instrumentInputParameter);
      if(instrumentRunValue != null) {
        if(instrumentRunValue.getData(instrumentInputParameter.getDataType()).getValue() != null) return true;
      }
    }
    return false;
  }

  private List<InstrumentOutputParameter> getOutputParametersOriginallyMarkedForManualCapture() {
    List<InstrumentOutputParameter> result = new ArrayList<InstrumentOutputParameter>();
    InstrumentType instrumentType = (InstrumentType) getModelObject();
    List<InstrumentOutputParameter> outputParams = instrumentType.getOutputParameters(InstrumentParameterCaptureMethod.MANUAL);

    for(InstrumentOutputParameter param : outputParams) {
      if(!param.isManualCaptureAllowed()) {
        result.add(param);
      }
    }
    return result;
  }

}