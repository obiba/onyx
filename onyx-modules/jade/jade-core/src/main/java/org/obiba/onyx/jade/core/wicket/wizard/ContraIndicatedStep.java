package org.obiba.onyx.jade.core.wicket.wizard;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunStatus;
import org.obiba.onyx.jade.core.service.ActiveInstrumentRunService;
import org.obiba.onyx.jade.core.wicket.instrument.ContraIndicatedPanel;
import org.obiba.onyx.wicket.wizard.WizardForm;
import org.obiba.onyx.wicket.wizard.WizardStepPanel;

/**
 * Termination Step after contra-indication has been selected for this instrument run. No possibility to come back.
 * 
 */
public class ContraIndicatedStep extends WizardStepPanel {

  private static final long serialVersionUID = 2498999653493497444L;
  
  @SpringBean
  protected ActiveInstrumentRunService activeInstrumentRunService;

  public ContraIndicatedStep(String id) {
    super(id);
    setOutputMarkupId(true);
    
    add(new Label(getTitleId(), new StringResourceModel("InstrumentRunContraIndicated", this, null)));
  }

  @Override
  public void onStepInNext(WizardForm form, AjaxRequestTarget target) {
    activeInstrumentRunService.setInstrumentRunStatus(InstrumentRunStatus.CONTRA_INDICATED);
    setContent(target, new ContraIndicatedPanel(getContentId()));
  }

  @Override
  public void handleWizardState(WizardForm form, AjaxRequestTarget target) {
    form.getPreviousLink().setEnabled(true);
    form.getNextLink().setEnabled(false);
    form.getFinishLink().setEnabled(true);
    if(target != null) {
      target.addComponent(form.getPreviousLink());
      target.addComponent(form.getNextLink());
      target.addComponent(form.getFinishLink());
    }
  }

}
