package org.obiba.onyx.jade.core.wicket.instrument;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.jade.core.domain.instrument.Instrument;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentInputParameter;
import org.obiba.onyx.jade.core.domain.run.InstrumentRun;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;
import org.obiba.onyx.jade.core.service.ActiveInstrumentRunService;
import org.obiba.onyx.jade.core.service.InstrumentService;
import org.obiba.onyx.wicket.data.DataField;
import org.obiba.wicket.markup.html.panel.KeyValueDataPanel;
import org.obiba.wicket.markup.html.table.DetachableEntityModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Get the input parameters that requires operator provisionning.
 * @author Yannick Marcon
 *
 */
public class InstrumentInputParameterPanel extends Panel {

  private static final long serialVersionUID = 3008363510160516288L;

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(InstrumentInputParameterPanel.class);

  @SpringBean
  private EntityQueryService queryService;

  @SpringBean
  private InstrumentService instrumentService;

  @SpringBean
  private ActiveInterviewService activeInterviewService;

  @SpringBean
  private ActiveInstrumentRunService activeInstrumentRunService;

  private InstrumentRun instrumentRun;

  public InstrumentInputParameterPanel(String id, IModel instrumentModel) {
    super(id);
    setModel(new DetachableEntityModel(queryService, instrumentModel.getObject()));
    setOutputMarkupId(true);

    Instrument instrument = (Instrument) getModelObject();

    instrumentRun = activeInstrumentRunService.start(activeInterviewService.getParticipant(), instrument);

    KeyValueDataPanel inputs = new KeyValueDataPanel("inputs");
    for(InstrumentInputParameter param : instrumentService.getInstrumentInputParameter(instrument, false)) {
      Label label = new Label(KeyValueDataPanel.getRowKeyId(), param.getDescription());
      InstrumentRunValue runValue = new InstrumentRunValue();
      runValue.setCaptureMethod(param.getCaptureMethod());
      runValue.setInstrumentParameter(param);
      instrumentRun.addInstrumentRunValue(runValue);

      DataField field = new DataField(KeyValueDataPanel.getRowValueId(), new PropertyModel(runValue, "data"), runValue.getDataType(), param.getMeasurementUnit());
      field.setRequired(true);
      field.setLabel(new Model(param.getName()));

      inputs.addRow(label, field);
    }
    add(inputs);
  }

}
