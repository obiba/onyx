/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.core.wicket.run;

import java.text.DateFormat;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
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
import org.obiba.onyx.jade.core.service.ActiveInstrumentRunService;
import org.obiba.onyx.jade.core.service.InstrumentRunService;
import org.obiba.onyx.jade.core.service.InstrumentService;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.wicket.model.SpringStringResourceModel;
import org.obiba.onyx.wicket.util.DateModelUtils;
import org.obiba.wicket.markup.html.panel.KeyValueDataPanel;
import org.obiba.wicket.markup.html.table.DetachableEntityModel;
import org.obiba.wicket.model.MessageSourceResolvableStringModel;

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

    InstrumentRun run = instrumentRunService.getLastCompletedInstrumentRun(activeInterviewService.getParticipant(), (InstrumentType) instrumentTypeModel.getObject());
    if(run == null) {
      throw new IllegalStateException("No instrument run in session.");
    }

    setModel(new DetachableEntityModel(queryService, run));

    build();
  }

  private void build() {
    InstrumentRun run = (InstrumentRun) InstrumentRunPanel.this.getModelObject();

    KeyValueDataPanel kvPanel = new KeyValueDataPanel("run", new StringResourceModel("RunInfo", this, null));
    add(kvPanel);
    kvPanel.addRow(new StringResourceModel("Operator", this, null), new PropertyModel(run, "user.fullName"));
    kvPanel.addRow(new StringResourceModel("StartDate", this, null), DateModelUtils.getDateTimeModel(new PropertyModel(this, "dateTimeFormat"), new PropertyModel(run, "timeStart")));

    if(run.getTimeEnd() != null) {
      kvPanel.addRow(new StringResourceModel("EndDate", this, null), DateModelUtils.getShortDateTimeModel(new PropertyModel(run, "timeEnd")));
    }

    boolean isInteractive = instrumentService.isInteractiveInstrument(run.getInstrumentType());

    InterpretativeParameter interpretative = new InterpretativeParameter();
    interpretative.setInstrumentType(run.getInstrumentType());

    if(queryService.count(interpretative) > 0) {
      add(getKeyValueDataPanel("interpretatives", new StringResourceModel("Interpretatives", this, null), queryService.match(interpretative)));
    } else {
      add(new EmptyPanel("interpretatives"));
    }

    InstrumentInputParameter input = new InstrumentInputParameter();
    input.setInstrumentType(run.getInstrumentType());
    if(!isInteractive) input.setCaptureMethod(InstrumentParameterCaptureMethod.MANUAL);

    if(queryService.count(input) > 0) {
      String key = isInteractive ? "InstrumentInputs" : "OperatorInputs";
      add(getKeyValueDataPanel("inputs", new StringResourceModel(key, this, null), queryService.match(input)));
    } else {
      add(new EmptyPanel("inputs"));
    }

    InstrumentOutputParameter output = new InstrumentOutputParameter();
    output.setInstrumentType(run.getInstrumentType());

    if(queryService.count(output) > 0) {
      String key = isInteractive ? "InstrumentOutputs" : "OperatorOutputs";
      add(getKeyValueDataPanel("outputs", new StringResourceModel(key, this, null), queryService.match(output)));
    } else {
      add(new EmptyPanel("outputs"));
    }
  }

  public DateFormat getDateTimeFormat() {
    return userSessionService.getDateTimeFormat();
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

        Label label = new Label(KeyValueDataPanel.getRowKeyId(), new MessageSourceResolvableStringModel(param.getLabel()));

        Data data = runValue.getData();
        Label value;
        if(data != null && data.getValue() != null) {
          if(param instanceof InterpretativeParameter) {
            value = new Label(KeyValueDataPanel.getRowValueId(), new StringResourceModel(data.getValueAsString(), this, null));
          } else {
            String unit = param.getMeasurementUnit();
            if(unit == null) {
              unit = "";
            }
            value = new Label(KeyValueDataPanel.getRowValueId(), new SpringStringResourceModel(data.getValueAsString()).getString() + " " + unit);
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
