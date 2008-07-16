package org.obiba.onyx.jade.core.wicket.panel.wizard;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WizardForm extends Form {
  
  private static final Logger log = LoggerFactory.getLogger(WizardForm.class);
  
  public WizardForm(String id) {
    super(id);

    setOutputMarkupId(true);
    setMultiPart(true);
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
    WizardStepPanel next = currentStep.getPreviousStep();
    currentStep.replaceWith(next);
    next.handleWizardState(this, target);
    target.addComponent(this);
  }
}
