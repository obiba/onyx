package org.obiba.onyx.jade.core.wicket.panel;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.model.IModel;
import org.obiba.onyx.jade.core.wicket.panel.wizard.WizardForm;
import org.obiba.onyx.jade.core.wicket.panel.wizard.WizardPanel;
import org.obiba.onyx.jade.core.wicket.panel.wizard.WizardStepPanel;

public class InstrumentPanel extends WizardPanel {

  private static final long serialVersionUID = 390982191889443136L;

  public InstrumentPanel(String id, IModel instrument) {
    super(id, instrument);
  }

  @Override
  public WizardForm createForm(String componentId) {
    return new AddForm(componentId);
  }

  private class AddForm extends WizardForm {

    private static final long serialVersionUID = 1L;

    public AddForm(String id) {
      super(id);

      WizardStepPanel step1 = new StepOnePanel("step", this);
      WizardStepPanel step2 = new StepTwoPanel("step", this);
      WizardStepPanel step3 = new StepThreePanel("step", this);

      step1.setNextStep(step2);
      step2.setNextStep(step3);
      step2.setPreviousStep(step1);
      step3.setPreviousStep(step2);

      add(step1);

      step1.handleWizardState(this, null);
    }

  }

  private class StepOnePanel extends WizardStepPanel {

    private static final long serialVersionUID = 1L;

    DropDownChoice defChoices;

    public StepOnePanel(String id, WizardForm form) {
      super(id, form);
      setOutputMarkupId(true);

      add(new Label("title", "1: Input Parameters"));

      add(new EmptyPanel("panel"));
    }

    @Override
    public void handleWizardState(WizardForm form, AjaxRequestTarget target) {
      // No previous step
      Component previous = form.getPreviousLink().setEnabled(false);
      if(target != null) {
        target.addComponent(previous);
        target.addComponent(next);
      }
    }

  }

  private class StepTwoPanel extends WizardStepPanel {

    private static final long serialVersionUID = -2511672064460152210L;

    public StepTwoPanel(String id, WizardForm form) {
      super(id, form);
      setOutputMarkupId(true);

      add(new Label("title", "2: Instructions"));

      add(new EmptyPanel("panel"));
    }

    @Override
    public void handleWizardState(WizardForm form, AjaxRequestTarget target) {
      form.getPreviousLink().setEnabled(true);
      form.getNextLink().setEnabled(true);
      if(target != null) target.addComponent(form.getNextLink());
    }

  }

  private class StepThreePanel extends WizardStepPanel {

    private static final long serialVersionUID = 6617334507631332206L;

    public StepThreePanel(String id, WizardForm form) {
      super(id, form);
      setOutputMarkupId(true);
      add(new Label("title", "3: Output Parameters"));

      add(new EmptyPanel("panel"));
    }

    @Override
    public void handleWizardState(WizardForm form, AjaxRequestTarget target) {
      form.getNextLink().setEnabled(false);
      form.getPreviousLink().setEnabled(true);
      form.getFinishLink().setEnabled(true);
      if(target != null) {
        target.addComponent(form.getNextLink());
        target.addComponent(form.getPreviousLink());
        target.addComponent(form.getFinishLink());
      }
    }
  }

}
