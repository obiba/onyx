package org.obiba.onyx.jade.core.wicket.panel.wizard;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.model.IModel;

public class TestWizardPanel extends WizardPanel {

  private static final long serialVersionUID = 390982191889443136L;

  public TestWizardPanel(String id, IModel instrument) {
    super(id, instrument);
  }
  
  @Override
  public WizardForm getForm(String componentId) {
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

  /**
   * Phase and Def selection (required).
   * @author ymarcon
   * 
   */
  private class StepOnePanel extends WizardStepPanel {

    private static final long serialVersionUID = 1L;

    DropDownChoice defChoices;

    public StepOnePanel(String id, WizardForm form) {
      super(id, form);
      setOutputMarkupId(true);

      add(new Label("title", "Step One"));

      add(new EmptyPanel("panel"));
    }

    @Override
    void handleWizardState(WizardForm form, AjaxRequestTarget target) {
      // No previous step
      Component previous = form.getPreviousLink().setEnabled(false);
      // Enable next only if a DEF has been selected
      if(target != null) {
        target.addComponent(previous);
        target.addComponent(next);
      }
    }

  }

  /**
   * Meta-data file upload (required).
   * @author ymarcon
   * 
   */
  private class StepTwoPanel extends WizardStepPanel {

    private static final long serialVersionUID = -2511672064460152210L;

    public StepTwoPanel(String id, WizardForm form) {
      super(id, form);
      setOutputMarkupId(true);

      add(new Label("title", "Step Two"));

     add(new EmptyPanel("panel"));
    }

    @Override
    void handleWizardState(WizardForm form, AjaxRequestTarget target) {
      form.getPreviousLink().setEnabled(true);
      form.getNextLink().setEnabled(true);
      if(target != null) target.addComponent(form.getNextLink());
    }

    /**
     * Called when file upload is successfully finished.
     * 
     */
    public void onFileSubmit(Form form) {
    }

  }

  /**
   * Progress bar or result of pre-processing ?
   * @author ymarcon
   * 
   */
  private class StepThreePanel extends WizardStepPanel {

    private static final long serialVersionUID = 6617334507631332206L;

    public StepThreePanel(String id, WizardForm form) {
      super(id, form);
      setOutputMarkupId(true);
      add(new Label("title", "Step Three"));

      add(new EmptyPanel("panel"));
    }

    public void onStep(AddForm form, AjaxRequestTarget target) {
    }

    @Override
    void handleWizardState(WizardForm form, AjaxRequestTarget target) {
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
