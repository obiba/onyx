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

import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.core.domain.contraindication.Contraindication;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.jade.core.domain.instrument.Instrument;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameterCaptureMethod;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentStatus;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.onyx.jade.core.service.ActiveInstrumentRunService;
import org.obiba.onyx.jade.core.service.InstrumentService;
import org.obiba.onyx.wicket.wizard.WizardForm;
import org.obiba.onyx.wicket.wizard.WizardStepPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class InstrumentWizardForm extends WizardForm {

  private static final long serialVersionUID = 1L;

  private static final Logger log = LoggerFactory.getLogger(InstrumentWizardForm.class);

  @SpringBean
  private EntityQueryService queryService;

  @SpringBean(name = "activeInterviewService")
  private ActiveInterviewService activeInterviewService;

  @SpringBean
  private ActiveInstrumentRunService activeInstrumentRunService;

  @SpringBean
  private InstrumentService instrumentService;

  private WizardStepPanel instrumentSelectionStep;

  private WizardStepPanel observedContraIndicationStep;

  private WizardStepPanel askedContraIndicationStep;

  private WizardStepPanel inputParametersStep;

  private WizardStepPanel instrumentLaunchStep;

  private WizardStepPanel outputParametersStep;

  private WizardStepPanel conclusionStep;

  private WizardStepPanel warningStep;

  public InstrumentWizardForm(String id, IModel instrumentTypeModel) {
    super(id);

    InstrumentType type = (InstrumentType) instrumentTypeModel.getObject();
    log.debug("instrumentType={}", type.getName());
    // ONYX-181: Set the current InstrumentRun on the ActiveInstrumentRunService. This particular
    // instance of the service may not have had its start method called, in which case it will have
    // a null InstrumentRun.
    activeInstrumentRunService.start(activeInterviewService.getParticipant(), type);

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
    instrumentTemplate.setInstrumentType(instrumentType);
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

    // are there input parameters with input source that requires user provisionning ?
    // or interpretative questions
    log.debug("instrumentInterpretativeParameters.count={}", activeInstrumentRunService.getInterpretativeParameters().size());
    log.debug("instrumentInputParameters.count={}", activeInstrumentRunService.getInputParameters(false));
    if(activeInstrumentRunService.hasInterpretativeParameter() || activeInstrumentRunService.hasInputParameter(false)) {
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
    log.debug("instrument.isInteractive={}", instrumentService.isInteractiveInstrument(instrumentType));
    if(instrumentService.isInteractiveInstrument(instrumentType)) {
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
    log.debug("instrumentOutputParameters.MANUAL.count={}", activeInstrumentRunService.getOutputParameters(InstrumentParameterCaptureMethod.MANUAL).size());
    if(activeInstrumentRunService.hasOutputParameter(InstrumentParameterCaptureMethod.MANUAL)) {
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

}
