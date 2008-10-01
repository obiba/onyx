package org.obiba.onyx.wicket.wizard;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.behavior.IBehavior;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class WizardForm extends Form {

  private static final long serialVersionUID = 8829452703870884599L;

  private static final Logger log = LoggerFactory.getLogger(WizardForm.class);

  /**
   * Indicates whether the action was canceled or not. TODO: should probably go in an Action interface or abstract class
   * of some sort
   */
  private boolean canceled = false;

  public WizardForm(String id) {
    super(id);

    setOutputMarkupId(true);
    setMultiPart(true);

    IBehavior buttonBehavior = new WizardButtonBehavior();
    // finish button
    AjaxButton finish = new AjaxButton("finish", this) {

      private static final long serialVersionUID = 0L;

      @Override
      protected void onSubmit(AjaxRequestTarget target, Form form) {
        log.debug("finish.onSubmit");
        if(getFeedbackPanel() != null) target.addComponent(getFeedbackPanel());
        onFinish(target, form);
      }

      @Override
      protected void onError(AjaxRequestTarget target, Form form) {
        log.debug("finish.onError");
        if(getFeedbackPanel() != null) target.addComponent(getFeedbackPanel());
        WizardForm.this.onError(target, form);
      }

    };
    finish.add(new AttributeModifier("value", true, new StringResourceModel("Finish", WizardForm.this, null)));
    finish.add(buttonBehavior);
    finish.setEnabled(false);
    finish.setOutputMarkupId(true);
    add(finish);

    // previous button
    AjaxLink link = new AjaxLink("previousLink") {
      private static final long serialVersionUID = 0L;

      @Override
      public void onClick(AjaxRequestTarget target) {
        if(getFeedbackPanel() != null) target.addComponent(getFeedbackPanel());
        WizardForm.this.gotoPrevious(target);
      }

    };
    link.add(new AttributeModifier("value", true, new StringResourceModel("Previous", WizardForm.this, null)));
    link.setEnabled(false);
    link.setOutputMarkupId(true);
    link.add(buttonBehavior);
    add(link);

    // next button
    AjaxButton button = new AjaxButton("nextLink") {
      private static final long serialVersionUID = 0L;

      @Override
      protected void onSubmit(AjaxRequestTarget target, Form form) {
        log.debug("next.onSubmit");
        if(getFeedbackPanel() != null) target.addComponent(getFeedbackPanel());
        WizardForm.this.gotoNext(target);
      }

      @Override
      protected void onError(AjaxRequestTarget target, Form form) {
        log.debug("next.onError");
        if(getFeedbackPanel() != null) target.addComponent(getFeedbackPanel());
        WizardForm.this.onError(target, form);
      }

    };
    button.add(new AttributeModifier("value", true, new StringResourceModel("Next", WizardForm.this, null)));
    button.setOutputMarkupId(true);
    button.add(buttonBehavior);
    add(button);

    // cancel button
    link = new AjaxLink("cancelLink") {
      private static final long serialVersionUID = 0L;

      @Override
      public void onClick(AjaxRequestTarget target) {
        canceled = true;
        onCancel(target);
      }

    };
    link.add(new AttributeModifier("value", true, new StringResourceModel("Cancel", WizardForm.this, null)));
    add(link);
  }

  /**
   * Called after wizard form submission generates an error (on next or finish click).
   * @param target
   * @param form
   */
  public abstract void onError(AjaxRequestTarget target, Form form);

  /**
   * Called when finish is clicked.
   * @param target
   * @param form
   */
  public abstract void onFinish(AjaxRequestTarget target, Form form);

  /**
   * Called when cancel is clicked.
   * @param target
   */
  public abstract void onCancel(AjaxRequestTarget target);

  public Component getNextLink() {
    return get("nextLink");
  }

  public Component getPreviousLink() {
    return get("previousLink");
  }

  public Component getFinishLink() {
    return get("finish");
  }
  
  public Component getCancelLink() {
    return get("cancelLink");
  }

  protected void gotoNext(AjaxRequestTarget target) {
    WizardStepPanel currentStep = (WizardStepPanel) get("step");
    log.info("gotoNext.currentStep=" + currentStep.getClass().getName());
    currentStep.onStepOutNext(WizardForm.this, target);

    WizardStepPanel next = currentStep.getNextStep();
    if(next != null) {
      currentStep.replaceWith(next);
      next.onStepInNext(this, target);
      next.handleWizardState(this, target);
    }
    target.addComponent(this);
  }

  protected void gotoPrevious(AjaxRequestTarget target) {
    WizardStepPanel currentStep = (WizardStepPanel) get("step");
    log.info("gotoPrevious.currentStep=" + currentStep.getClass().getName());
    currentStep.onStepOutPrevious(WizardForm.this, target);

    WizardStepPanel previous = currentStep.getPreviousStep();
    if(previous != null) {
      currentStep.replaceWith(previous);
      previous.onStepInPrevious(this, target);
      previous.handleWizardState(this, target);
    }
    target.addComponent(this);
  }

  public boolean isCanceled() {
    return canceled;
  }

  public void setCanceled(boolean canceled) {
    this.canceled = canceled;
  }

  public FeedbackPanel getFeedbackPanel() {
    return null;
  }

  public static String getStepId() {
    return "step";
  }
  
  private class WizardButtonBehavior extends AttributeAppender {

    private static final long serialVersionUID = -2793180600410649652L;

    public WizardButtonBehavior() {
      super("class", new Model("disabled"), " ");
    }

    /**
     * Overriden to enable the behaviour if the component is disabled. We want to append the attribute when the
     * component is disabled.
     */
    @Override
    public boolean isEnabled(Component component) {
      return component.isEnabled() == false;
    }
  }
}
