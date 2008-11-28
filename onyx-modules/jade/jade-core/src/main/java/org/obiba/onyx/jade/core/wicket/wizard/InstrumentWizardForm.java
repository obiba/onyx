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
import org.obiba.onyx.jade.core.domain.instrument.InstrumentOutputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameterCaptureMethod;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentStatus;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.onyx.jade.core.domain.instrument.InterpretativeParameter;
import org.obiba.onyx.jade.core.service.ActiveInstrumentRunService;
import org.obiba.onyx.jade.core.service.InstrumentService;
import org.obiba.onyx.wicket.wizard.WizardForm;
import org.obiba.onyx.wicket.wizard.WizardStepPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class InstrumentWizardForm extends WizardForm {

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

    activeInstrumentRunService.setInstrumentType((InstrumentType) instrumentTypeModel.getObject());

    WizardStepPanel startStep = null;

    instrumentSelectionStep = new InstrumentSelectionStep(getStepId());
    observedContraIndicationStep = new ObservedContraIndicationStep(getStepId());
    askedContraIndicationStep = new AskedContraIndicationStep(getStepId());
    inputParametersStep = new InputParametersStep(getStepId());
    instrumentLaunchStep = new InstrumentLaunchStep(getStepId());
    conclusionStep = new ConclusionStep(getStepId());
    warningStep = new WarningsStep(getStepId());
    outputParametersStep = new OutputParametersStep(getStepId(), conclusionStep, warningStep);

    warningStep.setNextStep(conclusionStep);
    warningStep.setPreviousStep(outputParametersStep);

    // do we need to select the instrument ?
    Instrument template = new Instrument();
    template.setInstrumentType((InstrumentType) instrumentTypeModel.getObject());
    template.setStatus(InstrumentStatus.ACTIVE);
    if(queryService.count(template) > 1) {
      activeInstrumentRunService.reset();
      startStep = instrumentSelectionStep;
    } else {
      // pre selected instrument
      activeInstrumentRunService.start(activeInterviewService.getParticipant(), queryService.matchOne(template));
      startStep = setUpWizardFlow();
    }

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
        lastStep.setNextStep(observedContraIndicationStep);
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
        lastStep.setNextStep(askedContraIndicationStep);
        askedContraIndicationStep.setPreviousStep(lastStep);
        lastStep = askedContraIndicationStep;
      }
    } else {
      log.debug("No contraindications of type ASKED. Skipping step.");
    }

    // are there input parameters with input source that requires user provisionning ?
    // or interpretative questions
    Instrument instrument = activeInstrumentRunService.getInstrument();
    InterpretativeParameter template = new InterpretativeParameter();
    template.setInstrument(instrument);
    log.info("instrumentInterpretativeParameters.count={}", queryService.count(template));
    log.info("instrumentInputParameters.count={}", instrumentService.countInstrumentInputParameter(instrument, false));
    if(queryService.count(template) > 0 || instrumentService.countInstrumentInputParameter(instrument, false) > 0) {
      if(startStep == null) {
        startStep = inputParametersStep;
        lastStep = startStep;
      } else {
        lastStep.setNextStep(inputParametersStep);
        inputParametersStep.setPreviousStep(lastStep);
        lastStep = inputParametersStep;
      }
    }

    // are there output parameters that are to be captured automatically from instrument (i.e. requires instrument
    // launch) ?
    log.info("instrument.isInteractive={}", instrumentService.isInteractiveInstrument(instrument));
    if(instrumentService.isInteractiveInstrument(instrument)) {
      if(startStep == null) {
        startStep = instrumentLaunchStep;
        lastStep = startStep;
      } else {
        lastStep.setNextStep(instrumentLaunchStep);
        instrumentLaunchStep.setPreviousStep(lastStep);
        lastStep = instrumentLaunchStep;
      }
    }

    // are there output parameters that are to be captured manually from instrument ?
    InstrumentOutputParameter opTemplate = new InstrumentOutputParameter();
    opTemplate.setInstrument(instrument);
    opTemplate.setCaptureMethod(InstrumentParameterCaptureMethod.MANUAL);
    log.info("instrumentOutputParameters.MANUAL.count={}", queryService.count(opTemplate));
    if(queryService.count(opTemplate) > 0) {
      if(startStep == null) {
        startStep = outputParametersStep;
        lastStep = startStep;
      } else {
        lastStep.setNextStep(outputParametersStep);
        outputParametersStep.setPreviousStep(lastStep);
        lastStep = outputParametersStep;
      }
    }

    // validation: final step
    if(startStep == null) {
      startStep = conclusionStep;
      lastStep = startStep;
    } else {
      lastStep.setNextStep(conclusionStep);
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
