package org.obiba.onyx.jade.core.wicket.wizard;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.PropertyModel;
import org.obiba.onyx.jade.core.wicket.instrument.InstructionsPanel;
import org.obiba.onyx.wicket.wizard.WizardForm;
import org.obiba.onyx.wicket.wizard.WizardStepPanel;

public class InstructionsStep extends WizardStepPanel {

  private static final long serialVersionUID = -2511672064460152210L;

  public InstructionsStep(String id) {
    super(id);
    setOutputMarkupId(true);

    add(new Label("title", "Instructions"));
  }

  @Override
  public void handleWizardState(WizardForm form, AjaxRequestTarget target) {
    WizardStepPanel nextStep = ((InstrumentWizardForm) form).getOutputParametersStep();
    setNextStep(nextStep);
    nextStep.setPreviousStep(this);
    
    form.getPreviousLink().setEnabled(true);
    form.getNextLink().setEnabled(false);
    form.getFinishLink().setEnabled(false);
    if(target != null) {
      target.addComponent(form.getPreviousLink());
      target.addComponent(form.getNextLink());
    }
  }
  
  @Override
  public void onStepIn(final WizardForm form, AjaxRequestTarget target) {
    setContent(target, new InstructionsPanel(getContentId(), new PropertyModel(form, "instrument")) {

      @Override
      public void onInstrumentLaunch() {
        ((InstrumentWizardForm) form).getNextLink().setEnabled(true);
      }
      
    });
  }

}
