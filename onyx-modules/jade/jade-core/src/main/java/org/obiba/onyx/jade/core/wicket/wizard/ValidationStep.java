package org.obiba.onyx.jade.core.wicket.wizard;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunStatus;
import org.obiba.onyx.jade.core.service.ActiveInstrumentRunService;
import org.obiba.onyx.jade.core.wicket.instrument.ValidationPanel;
import org.obiba.onyx.wicket.wizard.WizardForm;
import org.obiba.onyx.wicket.wizard.WizardStepPanel;

public class ValidationStep extends WizardStepPanel {

  private static final long serialVersionUID = -2477348071975440201L;

  @SpringBean
  protected ActiveInstrumentRunService activeInstrumentRunService;
  
  public ValidationStep(String id) {
    super(id);

    add(new Label("title", "Validation"));
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

  @Override
  public void onStepIn(WizardForm form, AjaxRequestTarget target) {
    // TODO change this when integrity check will be added
    activeInstrumentRunService.setInstrumentRunStatus(InstrumentRunStatus.COMPLETED);
    setContent(target, new ValidationPanel(getContentId()));
  }

}
