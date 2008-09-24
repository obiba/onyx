package org.obiba.onyx.jade.core.wicket.wizard;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.StringResourceModel;
import org.obiba.onyx.jade.core.wicket.instrument.InstructionsPanel;
import org.obiba.onyx.wicket.wizard.WizardForm;
import org.obiba.onyx.wicket.wizard.WizardStepPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InstructionsStep extends WizardStepPanel {

  private static final long serialVersionUID = -2511672064460152210L;

  private static final Logger log = LoggerFactory.getLogger(InstructionsStep.class);

  private boolean launched = false;

  public InstructionsStep(String id) {
    super(id);
    setOutputMarkupId(true);

    add(new Label("title", new StringResourceModel("Instructions", this, null)));
  }

  @Override
  public void handleWizardState(WizardForm form, AjaxRequestTarget target) {
    form.getPreviousLink().setEnabled(getPreviousStep() != null);
    form.getNextLink().setEnabled(true);
    form.getFinishLink().setEnabled(false);
    if(target != null) {
      target.addComponent(form.getPreviousLink());
      target.addComponent(form.getNextLink());
    }
  }

  @SuppressWarnings("serial")
  @Override
  public void onStepIn(final WizardForm form, AjaxRequestTarget target) {
    setContent(target, new InstructionsPanel(getContentId()) {

      @Override
      public void onInstrumentLaunch() {
        log.info("onInstrumentLaunch");
        launched = true;
      }

    });
  }

  @Override
  public void onStepOutPrevious(WizardForm form, AjaxRequestTarget target) {
    if(launched) {
      setPreviousStep(null);
      form.getPreviousLink().setEnabled(false);
      target.addComponent(form);
    }
  }

  @Override
  public void onStepOutNext(WizardForm form, AjaxRequestTarget target) {
    if(launched) {
      ((InstrumentWizardForm) form).setUpWizardFlow();
    } else {
      error(getString("InstrumentApplicationMustBeStarted"));
      setNextStep(null);
      if(form.getFeedbackPanel() != null) target.addComponent(form.getFeedbackPanel());
    }
  }

}
