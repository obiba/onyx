package org.obiba.onyx.jade.core.wicket.wizard;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.obiba.onyx.jade.core.wicket.instrument.panel.InstrumentParameterPanel;
import org.obiba.onyx.wicket.wizard.WizardForm;
import org.obiba.onyx.wicket.wizard.WizardStepPanel;

public class InputParametersStep extends WizardStepPanel {

  private static final long serialVersionUID = 1L;

  DropDownChoice defChoices;

  public InputParametersStep(String id) {
    super(id);
    setOutputMarkupId(true);

    add(new Label(getTitleId(), "2: Input Parameters"));

  }

  @Override
  public void handleWizardState(WizardForm form, AjaxRequestTarget target) {
    WizardStepPanel nextStep = ((InstrumentWizardForm) form).getInstructionsStep();
    setNextStep(nextStep);
    nextStep.setPreviousStep(this);

    // No previous step
    form.getPreviousLink().setEnabled(true);
    form.getNextLink().setEnabled(true);
    form.getFinishLink().setEnabled(false);
    if(target != null) {
      target.addComponent(form.getPreviousLink());
      target.addComponent(form.getNextLink());
    }
  }

  @Override
  public void onStep(WizardForm form, AjaxRequestTarget target) {
    IModel instrumentModel = new PropertyModel(form, "instrument");

    setContent(target, new InstrumentParameterPanel(getContentId(), instrumentModel, ((InstrumentWizardForm)form).getFeedbackPanel()));
  }

}