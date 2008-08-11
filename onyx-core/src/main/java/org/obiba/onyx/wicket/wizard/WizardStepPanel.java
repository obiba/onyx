package org.obiba.onyx.wicket.wizard;

import org.apache.wicket.Component;
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

  protected void setContent(AjaxRequestTarget target, Component content) {
    if(!content.getId().equals(getContentId())) throw new IllegalArgumentException("Expected content id is " + getContentId() + " but " + content.getId() + " was found.");

    Component current = get(getContentId());
    if(current == null) {
      add(content);
    } else {
      current.replaceWith(content);
      if(target != null) {
        target.addComponent(get(getContentId()));
      }
    }

  }

  /**
   * Called when "next" button is pressed to go to this step.
   * @param form
   * @param target
   */
  public void onStep(WizardForm form, AjaxRequestTarget target) {

  }

  public abstract void handleWizardState(WizardForm form, AjaxRequestTarget target);

  public static String getContentId() {
    return "panel";
  }

  public static String getTitleId() {
    return "title";
  }

}
