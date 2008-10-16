package org.obiba.onyx.quartz.core.wicket.wizard;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.obiba.onyx.wicket.wizard.WizardForm;
import org.obiba.onyx.wicket.wizard.WizardStepPanel;

public class ConclusionStep extends WizardStepPanel {
  //
  // Constants
  //

  private static final long serialVersionUID = 1L;

  //
  // Constructors
  //

  public ConclusionStep(String id) {
    super(id);

    // TODO: Initialize components.
  }

  //
  // WizardStepPanel Methods
  //

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