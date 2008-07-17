package org.obiba.onyx.jade.core.wicket.panel.wizard;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Panel;

public abstract class WizardStepPanel extends Panel {

  private static final long serialVersionUID = -6326593868471433867L;

  protected WizardStepPanel previous;

  protected WizardStepPanel next;

  public WizardStepPanel(String id) {
    super(id);
  }

  public void setNextStep(WizardStepPanel next) {
    this.next = next;
  }

  public WizardStepPanel getNextStep() {
    return next;
  }

  public void setPreviousStep(WizardStepPanel previous) {
    this.previous = previous;
  }

  public WizardStepPanel getPreviousStep() {
    return previous;
  }

  /**
   * Called when "next" button is pressed to go to this step.
   * @param form
   * @param target
   */
  public void onStep(WizardForm form, AjaxRequestTarget target) {

  }

  public abstract void handleWizardState(WizardForm form, AjaxRequestTarget target);

}
