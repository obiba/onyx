package org.obiba.onyx.jade.core.wicket.instrument;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.time.Duration;
import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.jade.core.domain.instrument.Instrument;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentComputedOutputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentOutputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameterCaptureMethod;
import org.obiba.onyx.jade.core.domain.run.InstrumentRun;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;
import org.obiba.onyx.jade.core.service.ActiveInstrumentRunService;
import org.obiba.onyx.jade.core.service.InstrumentService;
import org.obiba.onyx.wicket.data.DataField;
import org.obiba.wicket.markup.html.panel.KeyValueDataPanel;
import org.obiba.wicket.markup.html.table.DetachableEntityModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InstrumentOutputParameterPanel extends Panel {

  private static final long serialVersionUID = 3008363510160516288L;

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(InstrumentOutputParameterPanel.class);

  @SpringBean
  private EntityQueryService queryService;

  @SpringBean
  private InstrumentService instrumentService;

  @SpringBean
  private ActiveInterviewService activeInterviewService;

  @SpringBean
  private ActiveInstrumentRunService activeInstrumentRunService;

  private boolean manual = false;

  @SuppressWarnings("serial")
  public InstrumentOutputParameterPanel(String id, IModel instrumentModel) {
    super(id);
    setModel(new DetachableEntityModel(queryService, instrumentModel.getObject()));
    setOutputMarkupId(true);

    // get the current instrument run or create it if there was no input parameters for this instrument
    InstrumentRun instrumentRun = activeInstrumentRunService.getInstrumentRun();
    if(instrumentRun == null) {
      instrumentRun = activeInstrumentRunService.start(activeInterviewService.getParticipant(), (Instrument) getModelObject());
    }

    if(instrumentService.isInteractiveInstrument(instrumentRun.getInstrument())) {
      add(new ManualFragment("manual"));
    } else {
      add(new EmptyPanel("manual"));
    }

    updateInputs(null);

    add(new AbstractAjaxTimerBehavior(Duration.seconds(5)) {

      @Override
      protected void onTimer(AjaxRequestTarget target) {
        if(!isManual()) updateInputs(target);
      }

    });
  }

  public boolean isManual() {
    return manual;
  }

  public void setManual(boolean manual) {
    this.manual = manual;
  }

  private void updateInputs(AjaxRequestTarget target) {
    InstrumentOutputParameter template = new InstrumentOutputParameter();
    template.setInstrument((Instrument) getModelObject());

    InstrumentRun instrumentRun = activeInstrumentRunService.refresh();

    KeyValueDataPanel outputs = new KeyValueDataPanel("outputs", new StringResourceModel("DataOutputs", this, null));
    for(InstrumentOutputParameter param : queryService.match(template)) {
      if(!(param instanceof InstrumentComputedOutputParameter)) {
        Label label = new Label(KeyValueDataPanel.getRowKeyId(), param.getName());
        Component output = null;

        // case we going through this multiple times
        InstrumentRunValue runValue = instrumentRun.getInstrumentRunValue(param);

        if(manual || param.getCaptureMethod().equals(InstrumentParameterCaptureMethod.MANUAL)) {

          if(runValue == null) {
            runValue = new InstrumentRunValue();
            runValue.setInstrumentParameter(param);
            runValue.setCaptureMethod(param.getCaptureMethod());
            instrumentRun.addInstrumentRunValue(runValue);
            activeInstrumentRunService.validate();
          }
          // manual entry forced
          runValue.setCaptureMethod(InstrumentParameterCaptureMethod.MANUAL);

          DataField field = new DataField(KeyValueDataPanel.getRowValueId(), new PropertyModel(runValue, "data"), runValue.getDataType());
          field.setRequired(true);
          field.setLabel(new Model(param.getName()));
          output = field;

        } else if(runValue != null) {
          output = new Label(KeyValueDataPanel.getRowValueId(), new PropertyModel(runValue, "data.value"));
        }

        if(output != null) {
          outputs.addRow(label, output);
        }
      }
    }
    outputs.setOutputMarkupId(true);

    Component currentOutputs = get("outputs");
    if(currentOutputs != null) {
      currentOutputs.replaceWith(outputs);
    } else {
      add(outputs);
    }

    if(target != null) {
      target.addComponent(outputs);
    }
  }

  @SuppressWarnings("serial")
  private class ManualFragment extends Fragment {

    @SuppressWarnings("serial")
    public ManualFragment(String id) {
      super(id, "manualFragment", InstrumentOutputParameterPanel.this);
      CheckBox cb = new CheckBox("manual", new PropertyModel(InstrumentOutputParameterPanel.this, "manual"));
      cb.add(new OnChangeAjaxBehavior() {

        @Override
        protected void onUpdate(AjaxRequestTarget target) {
          updateInputs(target);
        }

      });
      add(cb);
    }

  }

}
