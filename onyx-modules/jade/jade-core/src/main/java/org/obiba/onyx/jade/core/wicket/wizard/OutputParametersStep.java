package org.obiba.onyx.jade.core.wicket.wizard;

import java.io.Serializable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentComputedOutputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentOutputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentOutputParameterAlgorithm;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameterCaptureMethod;
import org.obiba.onyx.jade.core.domain.run.InstrumentRun;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;
import org.obiba.onyx.jade.core.service.ActiveInstrumentRunService;
import org.obiba.onyx.jade.core.wicket.instrument.InstrumentOutputParameterPanel;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataType;
import org.obiba.onyx.wicket.wizard.WizardForm;
import org.obiba.onyx.wicket.wizard.WizardStepPanel;

public class OutputParametersStep extends WizardStepPanel {

  private static final long serialVersionUID = 6617334507631332206L;

  @SpringBean
  private EntityQueryService queryService;

  @SpringBean
  private ActiveInstrumentRunService activeInstrumentRunService;
  
  

  public OutputParametersStep(String id) {
    super(id);
    setOutputMarkupId(true);
    add(new Label("title", "Output Parameters"));

    add(new EmptyPanel("panel"));
  }

  @Override
  public void handleWizardState(WizardForm form, AjaxRequestTarget target) {
    WizardStepPanel nextStep = ((InstrumentWizardForm) form).getValidationStep();
    setNextStep(nextStep);
    nextStep.setPreviousStep(this);
    
    form.getNextLink().setEnabled(true);
    form.getPreviousLink().setEnabled(true);
    form.getFinishLink().setEnabled(false);
    if(target != null) {
      target.addComponent(form.getNextLink());
      target.addComponent(form.getPreviousLink());
      target.addComponent(form.getFinishLink());
    }
  }

  @Override
  public void onStepIn(WizardForm form, AjaxRequestTarget target) {
    setContent(target, new InstrumentOutputParameterPanel(getContentId(), new PropertyModel(form, "instrument")));
  }

  @Override
  public void onStepOut(WizardForm form, AjaxRequestTarget target) {
    activeInstrumentRunService.computeOutputParameters();    
  }
}
