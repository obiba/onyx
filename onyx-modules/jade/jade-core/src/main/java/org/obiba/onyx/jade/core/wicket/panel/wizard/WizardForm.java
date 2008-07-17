package org.obiba.onyx.jade.core.wicket.panel.wizard;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.ajax.calldecorator.AjaxCallDecorator;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.behavior.IBehavior;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WizardForm extends Form {

  private static final long serialVersionUID = 8829452703870884599L;

  private static final Logger log = LoggerFactory.getLogger(WizardForm.class);

  /**
   * Indicates whether the action was canceled or not. TODO: should probably go
   * in an Action interface or abstract class of some sort
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
      protected IAjaxCallDecorator getAjaxCallDecorator() {
        return new AjaxCallDecorator() {

          private static final long serialVersionUID = -6689010365115709867L;

          @Override
          public CharSequence decorateScript(CharSequence script) {
            return "jQuery('#" + WizardForm.this.getMarkupId() + " input').addClass('disabled').attr('disabled','disabled');" + script;
          }
        };

      }

      @Override
      protected void onSubmit(AjaxRequestTarget target, Form form) {
        // TODO
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
        WizardForm.this.gotoPrevious(target);
      }

    };
    link.add(new AttributeModifier("value", true, new StringResourceModel("wizard.previous", WizardForm.this, null)));
    link.setEnabled(false);
    link.setOutputMarkupId(true);
    link.add(buttonBehavior);
    add(link);

    // next button
    link = new AjaxLink("nextLink") {
      private static final long serialVersionUID = 0L;

      @Override
      public void onClick(AjaxRequestTarget target) {
        WizardForm.this.gotoNext(target);
      }

    };
    link.add(new AttributeModifier("value", true, new StringResourceModel("wizard.next", WizardForm.this, null)));
    link.setOutputMarkupId(true);
    link.add(buttonBehavior);
    add(link);

    // cancel button
    link = new AjaxLink("cancelLink") {
      private static final long serialVersionUID = 0L;

      @Override
      public void onClick(AjaxRequestTarget target) {
        canceled = true;
        // TODO
      }

    };
    link.add(new AttributeModifier("value", true, new StringResourceModel("Cancel", WizardForm.this, null)));
    add(link);
  }

  public Component getNextLink() {
    return get("nextLink");
  }

  public Component getPreviousLink() {
    return get("previousLink");
  }

  public Component getFinishLink() {
    return get("finish");
  }

  protected void gotoNext(AjaxRequestTarget target) {
    WizardStepPanel currentStep = (WizardStepPanel) get("step");
    log.info("gotoNext.currentStep=" + currentStep.getClass().getName());
    WizardStepPanel next = currentStep.getNextStep();
    currentStep.replaceWith(next);
    next.onStep(this, target);
    next.handleWizardState(this, target);
    target.addComponent(this);
  }

  protected void gotoPrevious(AjaxRequestTarget target) {
    WizardStepPanel currentStep = (WizardStepPanel) get("step");
    log.info("gotoPrevious.currentStep=" + currentStep.getClass().getName());
    WizardStepPanel previous = currentStep.getPreviousStep();
    currentStep.replaceWith(previous);
    previous.handleWizardState(this, target);
    target.addComponent(this);
  }

  public boolean isCanceled() {
    return canceled;
  }

  public void setCanceled(boolean canceled) {
    this.canceled = canceled;
  }

  private class WizardButtonBehavior extends AttributeAppender {

    private static final long serialVersionUID = -2793180600410649652L;

    public WizardButtonBehavior() {
      super("class", new Model("disabled"), " ");
    }

    /**
     * Overriden to enable the behaviour if the component is disabled. We want
     * to append the attribute when the component is disabled.
     */
    @Override
    public boolean isEnabled(Component component) {
      return component.isEnabled() == false;
    }
  }
}
