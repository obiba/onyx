package org.obiba.onyx.jade.core.wicket.instrument;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.jade.core.domain.instrument.Instrument;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentInputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentOutputParameter;
import org.obiba.onyx.jade.core.domain.run.InstrumentRun;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;
import org.obiba.onyx.jade.core.service.ActiveInstrumentRunService;
import org.obiba.onyx.util.data.Data;
import org.obiba.wicket.markup.html.panel.KeyValueDataPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValidationPanel extends Panel {

  private static final long serialVersionUID = 3008363510160516288L;

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(ValidationPanel.class);

  @SpringBean
  private EntityQueryService queryService;

  @SpringBean
  private ActiveInstrumentRunService activeInstrumentRunService;

  private InstrumentRun instrumentRun;

  public ValidationPanel(String id, IModel instrumentModel) {
    super(id);
    Instrument inst = (Instrument) instrumentModel.getObject();
    setOutputMarkupId(true);

    Instrument instrument = queryService.get(Instrument.class, inst.getId());
    InstrumentInputParameter templateIn = new InstrumentInputParameter();
    templateIn.setInstrument(instrument);

    instrumentRun = activeInstrumentRunService.getInstrumentRun();

    if(queryService.count(templateIn) > 0) {
      KeyValueDataPanel kv = new KeyValueDataPanel("inputs", new StringResourceModel("DataInputs", this, null));
      for(InstrumentInputParameter param : queryService.match(templateIn)) {
        Label label = new Label(KeyValueDataPanel.getRowKeyId(), param.getDescription());
        InstrumentRunValue runValue = instrumentRun.getInstrumentRunValue(param);
        String unit = param.getMeasurementUnit();
        if(unit == null) unit = "";
        IModel value = (runValue.getValue() == null ? new Model(unit) : new Model(runValue.getValue() + " " + unit));
        kv.addRow(label, new Label(KeyValueDataPanel.getRowValueId(), value));
      }
      add(kv);
    } else {
      add(new EmptyPanel("inputs"));
    }

    InstrumentOutputParameter templateOut = new InstrumentOutputParameter();
    templateOut.setInstrument(instrument);

    if(queryService.count(templateOut) > 0) {
      KeyValueDataPanel kv = new KeyValueDataPanel("outputs", new StringResourceModel("DataOutputs", this, null));
      for(InstrumentOutputParameter param : queryService.match(templateOut)) {
        Label label = new Label(KeyValueDataPanel.getRowKeyId(), param.getDescription());
        InstrumentRunValue runValue = instrumentRun.getInstrumentRunValue(param);
        if(runValue != null) {
          String unit = param.getMeasurementUnit();
          if(unit == null) unit = "";
          IModel value = (runValue.getValue() == null ? new Model(unit) : new Model(runValue.getValue() + " " + unit));
          kv.addRow(label, new Label(KeyValueDataPanel.getRowValueId(), value));
        }
      }
      add(kv);
    } else {
      add(new EmptyPanel("outputs"));
    }
  }

}
