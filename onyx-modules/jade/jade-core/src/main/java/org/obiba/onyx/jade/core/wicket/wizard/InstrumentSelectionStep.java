 package org.obiba.onyx.jade.core.wicket.wizard;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.jade.core.domain.instrument.Instrument;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentInputParameter;
import org.obiba.onyx.jade.core.wicket.instrument.panel.InstrumentSelector;
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

  DropDownChoice defChoices;

  @SuppressWarnings("serial")
  public InstrumentSelectionStep(String id, final InstrumentWizardForm form, IModel instrumentTypeModel) {
    super(id);
    setOutputMarkupId(true);

    add(new Label(getTitleId(), "1: Instrument Selection"));

    add(new InstrumentSelector(getContentId(), instrumentTypeModel) {

      @Override
      public void onInstrumentSelection(AjaxRequestTarget target, Instrument instrument) {
        form.setInstrument(instrument);
        form.getNextLink().setEnabled(instrument != null);
        target.addComponent(form.getNextLink());
        
        InstrumentInputParameter template = new InstrumentInputParameter();
        template.setInstrument(instrument);
        if (queryService.count(template)>0) {
          setNextStep(form.getInputParametersStep());
          form.getInputParametersStep().setPreviousStep(InstrumentSelectionStep.this);
        }
        else {
          setNextStep(form.getInstructionsStep());
          form.getInstructionsStep().setPreviousStep(InstrumentSelectionStep.this);
        }
      }

    });
  }

  @Override
  public void handleWizardState(WizardForm form, AjaxRequestTarget target) {
    // No previous step
    form.getPreviousLink().setEnabled(false);
    form.getNextLink().setEnabled(((InstrumentWizardForm)form).getInstrument() != null);
    form.getFinishLink().setEnabled(false);
    if(target != null) {
      target.addComponent(form.getPreviousLink());
      target.addComponent(form.getNextLink());
    }
  }

}
