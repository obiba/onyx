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
   * Called when "next" button submit the current step form to go to next step.
   * @param form
   * @param target
   */
  public void onStepOutNext(WizardForm form, AjaxRequestTarget target) {
    
  }
  
  /**
   * Called when "previous" button was pressed to leave this step by going to previous step.
   * @param form
   * @param target
   */
  public void onStepOutPrevious(WizardForm form, AjaxRequestTarget target) {
    
  }
  
  /**
   * Called when "next" button was pressed to go to this step coming from previous step.
   * @param form
   * @param target
   */
  public void onStepInNext(WizardForm form, AjaxRequestTarget target) {

  }
  
  /**
   * Called when "previous" button was pressed to go to this step coming from next step.
   * @param form
   * @param target
   */
  public void onStepInPrevious(WizardForm form, AjaxRequestTarget target) {

  }

  public abstract void handleWizardState(WizardForm form, AjaxRequestTarget target);

  public static String getContentId() {
    return "panel";
  }

  public static String getTitleId() {
    return "title";
  }

}
