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
    add(new Label("title", "4: Output Parameters"));

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
    // check the ones to be computed
    InstrumentRun instrumentRun = activeInstrumentRunService.getInstrumentRun();

    InstrumentOutputParameter template = new InstrumentOutputParameter();
    template.setInstrument(instrumentRun.getInstrument());

    for(InstrumentOutputParameter param : queryService.match(template)) {
      if(param instanceof InstrumentComputedOutputParameter) {
        InstrumentComputedOutputParameter computedParam = (InstrumentComputedOutputParameter) param;
        if(computedParam.getAlgorithm().equals(InstrumentOutputParameterAlgorithm.AVERAGE)) {
          InstrumentRunValue computedRunValue = instrumentRun.getInstrumentRunValue(computedParam);
          if(computedRunValue == null) {
            computedRunValue = new InstrumentRunValue();
            computedRunValue.setInstrumentParameter(computedParam);
            computedRunValue.setCaptureMethod(InstrumentParameterCaptureMethod.AUTOMATIC);
            instrumentRun.addInstrumentRunValue(computedRunValue);
          }

          double sum = 0;
          int count = 0;
          for(InstrumentOutputParameter p : computedParam.getInstrumentOutputParameters()) {
            count++;
            InstrumentRunValue runValue = instrumentRun.getInstrumentRunValue(p);
            if (runValue.getDataType().equals(DataType.DECIMAL)) {
            Double value = runValue.getValue();
            sum += value;
            }
            else if (runValue.getDataType().equals(DataType.INTEGER)) {
              Long value = runValue.getValue();
              sum += value.doubleValue();
            }
          }
          double avg = sum / count;

          Serializable avgValue = null;
          if(computedRunValue.getDataType().equals(DataType.DECIMAL)) avgValue = avg;
          else if(computedRunValue.getDataType().equals(DataType.INTEGER)) avgValue = (new Double(avg)).longValue();

          if(avgValue != null) computedRunValue.setData(new Data(computedRunValue.getDataType(), avgValue));

        }
      }
    }
  }
}
