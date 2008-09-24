package org.obiba.onyx.jade.core.wicket.wizard;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.jade.core.domain.instrument.Instrument;
import org.obiba.onyx.jade.core.service.ActiveInstrumentRunService;
import org.obiba.onyx.jade.core.wicket.instrument.InstrumentSelector;
import org.obiba.onyx.wicket.wizard.WizardForm;
import org.obiba.onyx.wicket.wizard.WizardStepPanel;
import org.obiba.wicket.markup.html.table.DetachableEntityModel;

/**
 * Instrument selection Step.
 * 
 */
public class InstrumentSelectionStep extends WizardStepPanel {

  private static final long serialVersionUID = 4489598868219932761L;

  @SpringBean
  private EntityQueryService queryService;

  @SpringBean(name = "activeInterviewService")
  private ActiveInterviewService activeInterviewService;

  @SpringBean
  private ActiveInstrumentRunService activeInstrumentRunService;

  private InstrumentSelector selector;

  @SuppressWarnings("serial")
  public InstrumentSelectionStep(String id) {
    super(id);
    setOutputMarkupId(true);

    add(new Label(getTitleId(), new StringResourceModel("InstrumentSelection", this, null)));

    add(selector = new InstrumentSelector(getContentId(), new DetachableEntityModel(queryService, activeInstrumentRunService.getInstrumentType())));
  }

  @Override
  public void onStepOutNext(WizardForm form, AjaxRequestTarget target) {
    InstrumentWizardForm instrumentForm = (InstrumentWizardForm) form;
    Instrument instrument = selector.getInstrument();

    activeInstrumentRunService.reset();
    if(instrument != null) {
      activeInstrumentRunService.start(activeInterviewService.getParticipant(), instrument);

      instrumentForm.setUpWizardFlow();
    } else {
      setNextStep(null);
    }
  }

  @Override
  public void handleWizardState(WizardForm form, AjaxRequestTarget target) {
    // No previous step
    form.getPreviousLink().setEnabled(false);
    form.getNextLink().setEnabled(true);
    form.getFinishLink().setEnabled(false);
    if(target != null) {
      target.addComponent(form.getPreviousLink());
      target.addComponent(form.getNextLink());
    }
  }

}
