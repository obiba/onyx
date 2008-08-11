package org.obiba.onyx.jade.core.wicket.wizard;

import org.apache.wicket.model.IModel;
import org.obiba.onyx.jade.core.domain.instrument.Instrument;
import org.obiba.onyx.wicket.wizard.WizardForm;
import org.obiba.onyx.wicket.wizard.WizardStepPanel;

public abstract class InstrumentWizardForm extends WizardForm {

  private Instrument instrument = null;

  WizardStepPanel instrumentSelectionStep;

  WizardStepPanel inputParametersStep;

  WizardStepPanel instructionsStep;

  WizardStepPanel outputParametersStep;

  public InstrumentWizardForm(String id, IModel instrumentTypeModel) {
    super(id);

    instrumentSelectionStep = new InstrumentSelectionStep("step", this, instrumentTypeModel);
    inputParametersStep = new InputParametersStep("step");
    instructionsStep = new InstructionsStep("step");
    outputParametersStep = new OutputParametersStep("step");

    // step0.setNextStep(step1);
    // step1.setNextStep(step2);
    // step1.setPreviousStep(step0);
    // step2.setNextStep(step3);
    // step2.setPreviousStep(step1);
    // step3.setPreviousStep(step2);

    add(instrumentSelectionStep);
    instrumentSelectionStep.handleWizardState(this, null);
  }

  public Instrument getInstrument() {
    return instrument;
  }

  public void setInstrument(Instrument instrument) {
    this.instrument = instrument;
  }

  public WizardStepPanel getInstrumentSelectionStep() {
    return instrumentSelectionStep;
  }

  public WizardStepPanel getInputParametersStep() {
    return inputParametersStep;
  }

  public WizardStepPanel getInstructionsStep() {
    return instructionsStep;
  }

  public WizardStepPanel getOutputParametersStep() {
    return outputParametersStep;
  }

}
