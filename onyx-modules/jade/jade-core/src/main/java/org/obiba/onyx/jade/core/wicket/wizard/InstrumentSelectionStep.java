package org.obiba.onyx.jade.core.wicket.wizard;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.jade.core.domain.instrument.Instrument;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentInputParameter;
import org.obiba.onyx.jade.core.wicket.instrument.InstrumentSelector;
import org.obiba.onyx.wicket.wizard.WizardForm;
import org.obiba.onyx.wicket.wizard.WizardStepPanel;

/**
 * Instrument selection Step.
 * 
 */
public class InstrumentSelectionStep extends WizardStepPanel {

  private static final long serialVersionUID = 4489598868219932761L;

  @SpringBean
  private EntityQueryService queryService;

  private InstrumentSelector selector;

  @SuppressWarnings("serial")
  public InstrumentSelectionStep(String id, final InstrumentWizardForm form, IModel instrumentTypeModel) {
    super(id);
    setOutputMarkupId(true);

    add(new Label(getTitleId(), new StringResourceModel("InstrumentSelection", this, null)));

    add(selector = new InstrumentSelector(getContentId(), instrumentTypeModel));
  }

  @Override
  public void onStepOutNext(WizardForm form, AjaxRequestTarget target) {
    InstrumentWizardForm instrumentForm = (InstrumentWizardForm) form;
    Instrument instrument = selector.getInstrument();

    if(instrument != null) {
      instrumentForm.setInstrument(instrument);

      InstrumentInputParameter template = new InstrumentInputParameter();
      template.setInstrument(instrument);
      if(queryService.count(template) > 0) {
        setNextStep(instrumentForm.getInputParametersStep());
        instrumentForm.getInputParametersStep().setPreviousStep(InstrumentSelectionStep.this);
      } else {
        setNextStep(instrumentForm.getInstructionsStep());
        instrumentForm.getInstructionsStep().setPreviousStep(InstrumentSelectionStep.this);
      }
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
