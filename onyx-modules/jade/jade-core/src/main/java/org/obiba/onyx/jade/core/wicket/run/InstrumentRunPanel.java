package org.obiba.onyx.jade.core.wicket.run;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.SpringWebApplication;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.core.service.UserSessionService;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentInputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentOutputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameterCaptureMethod;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.onyx.jade.core.domain.instrument.InterpretativeParameter;
import org.obiba.onyx.jade.core.domain.run.InstrumentRun;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;
import org.obiba.onyx.jade.core.domain.run.ParticipantInterview;
import org.obiba.onyx.jade.core.service.ActiveInstrumentRunService;
import org.obiba.onyx.jade.core.service.InstrumentRunService;
import org.obiba.onyx.jade.core.service.InstrumentService;
import org.obiba.onyx.util.data.Data;
import org.obiba.wicket.markup.html.panel.KeyValueDataPanel;
import org.obiba.wicket.markup.html.table.DetachableEntityModel;

public class InstrumentRunPanel extends Panel {

  private static final long serialVersionUID = -3652647014649095945L;

  @SpringBean
  private EntityQueryService queryService;

  @SpringBean
  private ActiveInstrumentRunService activeInstrumentRunService;

  @SpringBean
  private InstrumentRunService instrumentRunService;

  @SpringBean
  private InstrumentService instrumentService;

  @SpringBean(name = "activeInterviewService")
  private ActiveInterviewService activeInterviewService;

  @SpringBean(name = "userSessionService")
  private UserSessionService userSessionService;

  /**
   * Build the panel with the current instrument run.
   * @param id
   */
  public InstrumentRunPanel(String id) {
    super(id);

    InstrumentRun run = activeInstrumentRunService.getInstrumentRun();
    if(run == null) {
      throw new IllegalStateException("No instrument run in session.");
    }

    setModel(new DetachableEntityModel(queryService, run));

    build();
  }

  /**
   * Build the panel with the last completed run for the given instrument type.
   * @param id
   * @param instrumentTypeModel
   */
  public InstrumentRunPanel(String id, IModel instrumentTypeModel) {
    super(id);

    ParticipantInterview template = new ParticipantInterview();
    template.setParticipant(activeInterviewService.getParticipant());

    InstrumentRun run = instrumentRunService.getLastCompletedInstrumentRun(queryService.matchOne(template), (InstrumentType) instrumentTypeModel.getObject());
    if(run == null) {
      throw new IllegalStateException("No instrument run in session.");
    }

    setModel(new DetachableEntityModel(queryService, run));

    build();
  }

  private void build() {
    InstrumentRun run = (InstrumentRun) InstrumentRunPanel.this.getModelObject();

    boolean isInteractive = instrumentService.isInteractiveInstrument(run.getInstrument());

    InterpretativeParameter interpretative = new InterpretativeParameter();
    interpretative.setInstrument(run.getInstrument());

    if(queryService.count(interpretative) > 0) {
      add(getKeyValueDataPanel("interpretatives", new StringResourceModel("Interpretatives", this, null), queryService.match(interpretative)));
    } else {
      add(new EmptyPanel("interpretatives"));
    }

    InstrumentInputParameter input = new InstrumentInputParameter();
    input.setInstrument(run.getInstrument());

    if(queryService.count(input) > 0) {
      String key = isInteractive ? "InstrumentInputs" : "OperatorInputs";
      add(getKeyValueDataPanel("inputs", new StringResourceModel(key, this, null), queryService.match(input)));
    } else {
      add(new EmptyPanel("inputs"));
    }

    InstrumentOutputParameter output = new InstrumentOutputParameter();
    output.setInstrument(run.getInstrument());

    if(queryService.count(output) > 0) {
      String key = isInteractive ? "InstrumentOutputs" : "OperatorOutputs";
      add(getKeyValueDataPanel("outputs", new StringResourceModel(key, this, null), queryService.match(output)));
    } else {
      add(new EmptyPanel("outputs"));
    }
  }

  @SuppressWarnings("unchecked")
  private Component getKeyValueDataPanel(String id, IModel titleModel, List parameters) {

    KeyValueDataPanel kvPanel = new KeyValueDataPanel(id, titleModel);
    add(kvPanel);

    InstrumentRun run = (InstrumentRun) getModelObject();
    for(Object parameter : parameters) {
      InstrumentParameter param = (InstrumentParameter) parameter;
      InstrumentRunValue runValue = run.getInstrumentRunValue(param);

      // do not show COMPUTED values or misssing values
      if(runValue != null && !runValue.getCaptureMethod().equals(InstrumentParameterCaptureMethod.COMPUTED)) {

        // Inject the Spring application context and the user session service
        // into the instrument parameter. NOTE: These are dependencies of
        // InstrumentParameter.getDescription().
        param.setApplicationContext(((SpringWebApplication) getApplication()).getSpringContextLocator().getSpringContext());
        param.setUserSessionService(userSessionService);

        Label label = new Label(KeyValueDataPanel.getRowKeyId(), new PropertyModel(param, "description"));

        Data data = runValue.getData();
        Label value;
        if(data != null && data.getValue() != null) {
          if (param instanceof InterpretativeParameter) {
            value = new Label(KeyValueDataPanel.getRowValueId(), new StringResourceModel(data.getValueAsString(), this, null));
          }
          else {
            String unit = param.getMeasurementUnit();
            if(unit == null) {
              unit = "";
            }
            value = new Label(KeyValueDataPanel.getRowValueId(), data.getValueAsString() + " " + unit);
          }
        } else {
          value = new Label(KeyValueDataPanel.getRowValueId());
        }

        kvPanel.addRow(label, value);
      }
    }

    return kvPanel;
  }
}
