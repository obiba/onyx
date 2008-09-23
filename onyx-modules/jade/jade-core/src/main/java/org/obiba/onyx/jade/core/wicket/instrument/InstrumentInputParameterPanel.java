package org.obiba.onyx.jade.core.wicket.instrument;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.SpringWebApplication;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.core.service.UserSessionService;
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

  @SpringBean(name="activeInterviewService")
  private ActiveInterviewService activeInterviewService;

  @SpringBean
  private ActiveInstrumentRunService activeInstrumentRunService;

  @SpringBean(name="userSessionService")
  private UserSessionService userSessionService;
  
  private InstrumentRun instrumentRun;

  public InstrumentInputParameterPanel(String id, IModel instrumentModel) {
    super(id);
    setModel(new DetachableEntityModel(queryService, instrumentModel.getObject()));
    setOutputMarkupId(true);

    Instrument instrument = (Instrument) getModelObject();

    instrumentRun = activeInstrumentRunService.start(activeInterviewService.getParticipant(), instrument);

    KeyValueDataPanel inputs = new KeyValueDataPanel("inputs");
    for(final InstrumentInputParameter param : instrumentService.getInstrumentInputParameter(instrument, false)) {
      // Inject the Spring application context and the user session service
      // into the instrument parameter. NOTE: These are dependencies of 
      // InstrumentParameter.getDescription().
      param.setApplicationContext(((SpringWebApplication)getApplication()).getSpringContextLocator().getSpringContext());
      param.setUserSessionService(userSessionService);
      
      Label label = new Label(KeyValueDataPanel.getRowKeyId(), new Model() {
        public Object getObject() {
          return param.getDescription();
        }
      });
      InstrumentRunValue runValue = new InstrumentRunValue();
      runValue.setCaptureMethod(param.getCaptureMethod());
      runValue.setInstrumentParameter(param);
      instrumentRun.addInstrumentRunValue(runValue);

      DataField field = new DataField(KeyValueDataPanel.getRowValueId(), new PropertyModel(runValue, "data"), runValue.getDataType(), param.getMeasurementUnit());
      field.setRequired(true);
      field.setLabel(new Model() {
        public Object getObject() {
          return param.getDescription();
        }
      });

      inputs.addRow(label, field);
    }
    add(inputs);
  }

}
