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

  @SpringBean(name="activeInterviewService")
  private ActiveInterviewService activeInterviewService;

  @SpringBean
  private ActiveInstrumentRunService activeInstrumentRunService;

  private boolean manual = false;

  private ManualFragment manualFragment = new ManualFragment("manual");

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

    initManualOutputs();

    if(instrumentService.isInteractiveInstrument(instrumentRun.getInstrument())) {
      initAutomaticOutputs(null);
      add(manualFragment);
    } else {
      add(new EmptyPanel("manual"));
      add(new EmptyPanel("automaticOutputs"));
    }
  }

  public boolean isManual() {
    return manual;
  }

  public void setManual(boolean manual) {
    this.manual = manual;
  }

  private void initManualOutputs() {
    InstrumentOutputParameter template = new InstrumentOutputParameter();
    template.setInstrument((Instrument) getModelObject());
    template.setCaptureMethod(InstrumentParameterCaptureMethod.MANUAL);

    InstrumentRun instrumentRun = activeInstrumentRunService.getInstrumentRun();

    if(queryService.count(template) == 0) {
      add(new EmptyPanel("manualOutputs"));
    } else {
      KeyValueDataPanel outputs = new KeyValueDataPanel("manualOutputs", new StringResourceModel("ManualDataOutputs", this, null));
      for(InstrumentOutputParameter param : queryService.match(template)) {
        Label label = new Label(KeyValueDataPanel.getRowKeyId(), param.getDescription());

        // case we going through this multiple times
        InstrumentRunValue runValue = instrumentRun.getInstrumentRunValue(param);
        if(runValue == null) {
          runValue = new InstrumentRunValue();
          runValue.setInstrumentParameter(param);
          runValue.setCaptureMethod(param.getCaptureMethod());
          instrumentRun.addInstrumentRunValue(runValue);
          activeInstrumentRunService.validate();
        }

        DataField field = new DataField(KeyValueDataPanel.getRowValueId(), new PropertyModel(runValue, "data"), runValue.getDataType(), param.getMeasurementUnit());
        field.setRequired(true);
        field.setLabel(new Model(param.getDescription()));

        outputs.addRow(label, field);
      }
      add(outputs);
    }
  }

  private void initAutomaticOutputs(AjaxRequestTarget target) {
    manualFragment.setVisible(false);
    InstrumentOutputParameter template = new InstrumentOutputParameter();
    template.setInstrument((Instrument) getModelObject());
    template.setCaptureMethod(InstrumentParameterCaptureMethod.AUTOMATIC);

    Component newOutputs = null;
    if(queryService.count(template) == 0) {
      newOutputs = new EmptyPanel("automaticOutputs");
    } else {
      InstrumentRun instrumentRun = activeInstrumentRunService.refresh();

      KeyValueDataPanel outputs = new KeyValueDataPanel("automaticOutputs", new StringResourceModel("AutomaticDataOutputs", this, null));
      for(InstrumentOutputParameter param : queryService.match(template)) {
        Label label = new Label(KeyValueDataPanel.getRowKeyId(), param.getDescription());
        Component output = null;

        // case we going through this multiple times
        InstrumentRunValue runValue = instrumentRun.getInstrumentRunValue(param);
        
        if(manual) {
          manualFragment.setVisible(true);
          if(runValue == null) {
            runValue = new InstrumentRunValue();
            runValue.setInstrumentParameter(param);
            runValue.setCaptureMethod(param.getCaptureMethod());
            instrumentRun.addInstrumentRunValue(runValue);
            activeInstrumentRunService.validate();
          }

          if(runValue.getData().getValueAsString() == null)
            runValue.setCaptureMethod(InstrumentParameterCaptureMethod.MANUAL);
          
          DataField field = new DataField(KeyValueDataPanel.getRowValueId(), new PropertyModel(runValue, "data"), runValue.getDataType());
          field.setRequired(true);
          if(runValue.getCaptureMethod().equals(InstrumentParameterCaptureMethod.AUTOMATIC)) field.setFieldEnabled(false);
          field.setLabel(new Model(param.getName()));
          output = field;

        } else if(runValue != null && runValue.getData() != null && runValue.getData().getValueAsString() != null) {
          output = new Label(KeyValueDataPanel.getRowValueId(), new RunValueLabelModel(runValue));
        } else {
          manualFragment.setVisible(true);
          output = new Label(KeyValueDataPanel.getRowValueId(), "");
        }

        if(output != null) {
          outputs.addRow(label, output);
        }
      }
      outputs.setOutputMarkupId(true);
      newOutputs = outputs;
    }

    Component currentOutputs = get("automaticOutputs");
    if(currentOutputs != null) {
      currentOutputs.replaceWith(newOutputs);
    } else {
      add(newOutputs);
    }

    if(target != null) {
      target.addComponent(newOutputs);
    }
  }

  @SuppressWarnings("serial")
  private class ManualFragment extends Fragment {

    private AbstractAjaxTimerBehavior timer;

    @SuppressWarnings("serial")
    public ManualFragment(String id) {
      super(id, "manualFragment", InstrumentOutputParameterPanel.this);
      setOutputMarkupId(true);
      timer = new AbstractAjaxTimerBehavior(Duration.seconds(5)) {

        @Override
        protected void onTimer(AjaxRequestTarget target) {
          if(!isManual()) initAutomaticOutputs(target);
        }

      };

      CheckBox cb = new CheckBox("manual", new PropertyModel(InstrumentOutputParameterPanel.this, "manual"));
      cb.setOutputMarkupId(true);
      cb.add(new OnChangeAjaxBehavior() {

        @Override
        protected void onUpdate(AjaxRequestTarget target) {
          initAutomaticOutputs(target);
        }

      });
      add(timer);
      add(cb);
    }

  }

}
