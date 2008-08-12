package org.obiba.onyx.jade.core.wicket.instrument.panel;

import java.io.Serializable;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.extensions.markup.html.form.DateTextField;
import org.apache.wicket.extensions.yui.calendar.DatePicker;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.validator.NumberValidator;
import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.jade.core.domain.instrument.Instrument;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentInputParameter;
import org.obiba.onyx.jade.core.domain.run.ParticipantInterview;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataType;
import org.obiba.wicket.markup.html.panel.KeyValueDataPanel;
import org.obiba.wicket.markup.html.table.DetachableEntityModel;

public class InstrumentParameterPanel extends Panel {

  private static final long serialVersionUID = 3008363510160516288L;

  @SpringBean
  private EntityQueryService queryService;
  
  @SpringBean
  private ActiveInterviewService activeInterviewService;

  public InstrumentParameterPanel(String id, IModel instrumentModel) {
    super(id);
    setModel(new DetachableEntityModel(queryService, instrumentModel.getObject()));
    setOutputMarkupId(true);

    Instrument instrument = (Instrument) getModelObject();
    InstrumentInputParameter template = new InstrumentInputParameter();
    template.setInstrument(instrument);
    
    ParticipantInterview participantInterview = new ParticipantInterview();
    participantInterview.setParticipant(activeInterviewService.getParticipant());
    participantInterview = queryService.matchOne(participantInterview);
    
    KeyValueDataPanel inputs = new KeyValueDataPanel("inputs");
    for(InstrumentInputParameter param : queryService.match(template)) {
      Label label = new Label(KeyValueDataPanel.getRowKeyId(), param.getName());
      Component input = null;
      switch(param.getCaptureMethod()) {
      case MANUAL:
        switch(param.getDataType()) {
        case TEXT:
        case DATA:
          input = new InputTextField(KeyValueDataPanel.getRowValueId());
        case BOOLEAN:
          input = new InputBooleanField(KeyValueDataPanel.getRowValueId());
        case DATE:
          input = new InputDateField(KeyValueDataPanel.getRowValueId());
        case INTEGER:
        case DECIMAL:
          input = new InputNumberField(KeyValueDataPanel.getRowValueId(), param.getDataType());
        }
      case AUTOMATIC:
        Data data = param.getInputSource().getData(participantInterview);
        IModel value = (data == null ? new Model("") : new Model((Serializable)data.getValue())); 
        input = new Label(KeyValueDataPanel.getRowValueId(), value);
      }
      inputs.addRow(label, input);
    }
    add(inputs);
  }

  @SuppressWarnings("serial")
  private class InputTextField extends Fragment {

    public InputTextField(String id) {
      super(id, "inputFieldFragment", InstrumentParameterPanel.this);
      add(new TextField("field"));
    }

  }

  @SuppressWarnings("serial")
  private class InputNumberField extends Fragment {

    public InputNumberField(String id, DataType type) {
      super(id, "inputFieldFragment", InstrumentParameterPanel.this);
      TextField tf = new TextField("field");
      // TODO min/max long/double
      tf.add(NumberValidator.POSITIVE);
      add(tf);
    }

  }

  @SuppressWarnings("serial")
  private class InputDateField extends Fragment {

    public InputDateField(String id) {
      super(id, "inputFieldFragment", InstrumentParameterPanel.this);
      DateTextField date = new DateTextField("field");
      date.add(new DatePicker());
      add(date);
    }

  }

  @SuppressWarnings("serial")
  private class InputBooleanField extends Fragment {

    public InputBooleanField(String id) {
      super(id, "inputFieldFragment", InstrumentParameterPanel.this);
      add(new CheckBox("field").add(new AttributeModifier("type", new Model("checkbox"))));
    }

  }

}
