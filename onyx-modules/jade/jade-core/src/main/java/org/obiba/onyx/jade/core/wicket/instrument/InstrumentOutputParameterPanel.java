package org.obiba.onyx.jade.core.wicket.instrument;

import java.io.Serializable;

import org.apache.wicket.Component;
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
import org.apache.wicket.spring.injection.annot.SpringBean;
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
import org.obiba.onyx.util.data.Data;
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

  private InstrumentRun instrumentRun;

  private boolean manual = false;

  public InstrumentOutputParameterPanel(String id, IModel instrumentModel) {
    super(id);
    setModel(new DetachableEntityModel(queryService, instrumentModel.getObject()));
    setOutputMarkupId(true);

    // get the current instrument run or create it if there was no input parameters for this instrument
    instrumentRun = activeInstrumentRunService.getInstrumentRun();
    if(instrumentRun == null) {
      instrumentRun = activeInstrumentRunService.start(activeInterviewService.getParticipant(), (Instrument) getModelObject());
    }

    if (instrumentService.isInteractiveInstrument(instrumentRun.getInstrument())) {
      add(new ManualFragment("manual"));
    }
    else {
      add(new EmptyPanel("manual"));
    }
    
    updateInputs(null);
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
    
    KeyValueDataPanel inputs = new KeyValueDataPanel("inputs");
    for(InstrumentOutputParameter param : queryService.match(template)) {
      if(!(param instanceof InstrumentComputedOutputParameter)) {
        Label label = new Label(KeyValueDataPanel.getRowKeyId(), param.getName());
        Component input = null;
        
        // case we going through this multiple times
        InstrumentRunValue runValue = instrumentRun.getInstrumentRunValue(param);
        if (runValue == null) {
          runValue = new InstrumentRunValue();
          runValue.setInstrumentParameter(param);
          runValue.setCaptureMethod(param.getCaptureMethod());
          instrumentRun.addInstrumentRunValue(runValue);
        }
        
        // manual entry forced
        if(manual) {
          runValue.setCaptureMethod(InstrumentParameterCaptureMethod.MANUAL);
        }
        
        switch(runValue.getCaptureMethod()) {
        case MANUAL:
          DataField field = new DataField(KeyValueDataPanel.getRowValueId(), new PropertyModel(runValue, "data"), runValue.getDataType());
          field.setRequired(true);
          field.setLabel(new Model(param.getName()));
          input = field;
          break;
        case AUTOMATIC:
          Data data = null;
          IModel value = (data == null ? new Model("") : new Model((Serializable) data.getValue()));
          input = new Label(KeyValueDataPanel.getRowValueId(), value);
          break;
        }
        inputs.addRow(label, input);
      }
    }
    inputs.setOutputMarkupId(true);

    Component currentInputs = get("inputs");
    if(currentInputs != null) {
      currentInputs.replaceWith(inputs);
    } else {
      add(inputs);
    }

    if(target != null) {
      target.addComponent(inputs);
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
