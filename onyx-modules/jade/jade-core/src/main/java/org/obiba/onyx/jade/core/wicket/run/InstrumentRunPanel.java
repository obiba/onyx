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
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.value.ValueMap;
import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.core.service.UserSessionService;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameterCaptureMethod;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.onyx.jade.core.domain.instrument.InterpretativeParameter;
import org.obiba.onyx.jade.core.domain.run.InstrumentRun;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;
import org.obiba.onyx.jade.core.domain.run.Measure;
import org.obiba.onyx.jade.core.service.ActiveInstrumentRunService;
import org.obiba.onyx.jade.core.service.InstrumentRunService;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.wicket.model.SpringStringResourceModel;
import org.obiba.onyx.wicket.util.DateModelUtils;
import org.obiba.wicket.markup.html.panel.KeyValueDataPanel;
import org.obiba.wicket.markup.html.table.DetachableEntityModel;
import org.obiba.wicket.model.MessageSourceResolvableStringModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InstrumentRunPanel extends Panel {

  private static final Logger log = LoggerFactory.getLogger(InstrumentRunPanel.class);

  private static final long serialVersionUID = -3652647014649095945L;

  @SpringBean
  private EntityQueryService queryService;

  @SpringBean
  private ActiveInstrumentRunService activeInstrumentRunService;

  @SpringBean
  private InstrumentRunService instrumentRunService;

  @SpringBean(name = "activeInterviewService")
  private ActiveInterviewService activeInterviewService;

  @SpringBean(name = "userSessionService")
  private UserSessionService userSessionService;

  private Measure measure = null;

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

    setDefaultModel(new DetachableEntityModel(queryService, run));

    build();
  }

  /**
   * Build the panel with the current instrument run and a specified measure.
   * @param id
   * @param modal
   * @param measure
   */
  public InstrumentRunPanel(String id, final ModalWindow modal, Measure measure) {
    super(id);

    InstrumentRun run = activeInstrumentRunService.getInstrumentRun();
    if(run == null) {
      throw new IllegalStateException("No instrument run in session.");
    }
    modal.setTitle(getTitle(run));
    setDefaultModel(new DetachableEntityModel(queryService, run));
    if(measure != null) setMeasure(measure);

    build();
  }

  public IModel<String> getTitle(InstrumentRun run) {
    String instrumentName = new SpringStringResourceModel(run.getInstrumentType() + ".description", run.getInstrumentType()).getString();
    return new StringResourceModel("CollectedDataTitle", this, new Model<ValueMap>(new ValueMap("instrument=" + instrumentName)));
  }

  /**
   * Build the panel with the last completed run for the given instrument type.
   * @param id
   * @param instrumentTypeModel
   */
  public InstrumentRunPanel(String id, IModel instrumentTypeModel) {
    super(id);

    InstrumentType instrumentType = (InstrumentType) instrumentTypeModel.getObject();
    InstrumentRun run = instrumentRunService.getInstrumentRun(activeInterviewService.getParticipant(), instrumentType.getName());
    if(!run.isCompletedOrContraindicated()) run = null; // We only want the last completed run.
    if(run == null) {
      throw new IllegalStateException("No instrument run in session.");
    }

    setDefaultModel(new DetachableEntityModel(queryService, run));

    build();
  }

  private void build() {
    InstrumentRun run = (InstrumentRun) InstrumentRunPanel.this.getDefaultModelObject();

    KeyValueDataPanel kvPanel = new KeyValueDataPanel("run", new StringResourceModel("RunInfo", this, null));
    add(kvPanel);
    kvPanel.addRow(new StringResourceModel("Operator", this, null), new PropertyModel(run, "user.fullName"));

    if(getMeasure() != null) {
      kvPanel.addRow(new StringResourceModel("StartDate", this, null), DateModelUtils.getDateTimeModel(new PropertyModel(this, "dateTimeFormat"), new PropertyModel(getMeasure(), "time")));
    } else {
      kvPanel.addRow(new StringResourceModel("StartDate", this, null), DateModelUtils.getDateTimeModel(new PropertyModel(this, "dateTimeFormat"), new PropertyModel(run, "timeStart")));
    }

    if(run.getTimeEnd() != null) {
      kvPanel.addRow(new StringResourceModel("EndDate", this, null), DateModelUtils.getShortDateTimeModel(new PropertyModel(run, "timeEnd")));
    }

    InstrumentType instrumentType = activeInstrumentRunService.getInstrumentType();

    if(instrumentType.hasInterpretativeParameter()) {
      add(getKeyValueDataPanel("interpretatives", new StringResourceModel("Interpretatives", this, null), instrumentType.getInterpretativeParameters()));
    } else {
      add(new EmptyPanel("interpretatives"));
    }

    boolean isInteractive = instrumentType.isInteractive();
    if(isInteractive) {
      if(instrumentType.hasInputParameter()) {
        add(getKeyValueDataPanel("inputs", new StringResourceModel("InstrumentInputs", this, null), instrumentType.getInputParameters()));
      } else {
        add(new EmptyPanel("inputs"));
      }
      add(new EmptyPanel("operatorAutoInputs"));
    } else {
      // Manual Inputs
      if(instrumentType.hasInputParameter(InstrumentParameterCaptureMethod.MANUAL)) {
        add(getKeyValueDataPanel("inputs", new StringResourceModel("OperatorInputs", this, null), instrumentType.getInputParameters(InstrumentParameterCaptureMethod.MANUAL)));
      } else {
        add(new EmptyPanel("inputs"));
      }

      // Automatic Inputs
      if(instrumentType.hasInputParameter(InstrumentParameterCaptureMethod.AUTOMATIC)) {
        add(getKeyValueDataPanel("operatorAutoInputs", new StringResourceModel("StandardInputs", this, null), instrumentType.getInputParameters(InstrumentParameterCaptureMethod.AUTOMATIC)));
      } else {
        add(new EmptyPanel("operatorAutoInputs"));
      }
    }

    if(instrumentType.hasOutputParameter()) {
      String key = isInteractive ? "InstrumentOutputs" : "OperatorOutputs";
      add(getKeyValueDataPanel("outputs", new StringResourceModel(key, this, null), instrumentType.getOutputParameters()));
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

    InstrumentRun run = (InstrumentRun) getDefaultModelObject();
    for(Object parameter : parameters) {
      InstrumentParameter param = (InstrumentParameter) parameter;

      if(!activeInstrumentRunService.getInstrumentType().isRepeatable(param)) {
        InstrumentRunValue runValue = run.getInstrumentRunValue(param);

        // do not show COMPUTED values or missing values
        if(runValue != null && !runValue.getCaptureMethod().equals(InstrumentParameterCaptureMethod.COMPUTED)) {
          addRow(kvPanel, param, runValue, null);
        }
      } else {
        Integer pos = (getMeasure() == null) ? 1 : null;
        for(Measure measure : run.getMeasures()) {
          if(getMeasure() != null && !getMeasure().getId().equals(measure.getId())) continue;
          for(InstrumentRunValue runValue : measure.getInstrumentRunValues()) {
            if(runValue.getInstrumentParameter().equals(param.getCode())) {
              addRow(kvPanel, param, runValue, pos);
            }
          }
          if(pos != null) pos++;
        }
      }
    }

    return kvPanel;
  }

  @SuppressWarnings("serial")
  private void addRow(KeyValueDataPanel kvPanel, InstrumentParameter param, InstrumentRunValue runValue, final Integer measurePosition) {
    Label label = new Label(KeyValueDataPanel.getRowKeyId(), new MessageSourceResolvableStringModel(param.getLabel()) {
      @Override
      public Object getObject() {
        if(measurePosition != null) {
          return super.getObject() + " " + measurePosition;
        } else {
          return super.getObject();
        }
      }
    });

    Data data = runValue.getData(param.getDataType());
    Label value;
    if(data != null && data.getValue() != null) {

      // Apply formatter on output if one has been defined.
      String formatStr = param.getDisplayFormat();
      String formattedOutput;
      if(formatStr != null) {
        formattedOutput = formatOutput(param, runValue, formatStr);
      } else {
        formattedOutput = data.getValueAsString();
      }

      if(param instanceof InterpretativeParameter) {
        value = new Label(KeyValueDataPanel.getRowValueId(), new StringResourceModel(formattedOutput, this, null));
      } else {
        String unit = param.getMeasurementUnit();
        if(unit == null) {
          unit = "";
        }
        value = new Label(KeyValueDataPanel.getRowValueId(), new SpringStringResourceModel(formattedOutput).getString() + " " + unit);
      }
    } else {
      value = new Label(KeyValueDataPanel.getRowValueId());
    }

    kvPanel.addRow(label, value);
  }

  private String formatOutput(InstrumentParameter param, InstrumentRunValue runValue, String formatStr) {

    log.debug("Display format for {} is {}", param.getCode(), formatStr);
    Object value = runValue.getData(param.getDataType()).getValue();
    String valueStr = runValue.getData(param.getDataType()).getValueAsString();
    String formattedValue;
    try {
      formattedValue = String.format(formatStr, value);
      log.debug("Applied format \"{}\" to parameter {} (value={}).  Result is {}", new Object[] { formatStr, param.getCode(), valueStr, formattedValue });
    } catch(Exception ex) {
      log.error("Cannot apply the following formatting \"{}\" to parameter {} (value={})", new Object[] { formatStr, param.getCode(), valueStr });
      throw new RuntimeException(ex);
    }

    return formattedValue;
  }

  public Measure getMeasure() {
    return measure;
  }

  public void setMeasure(Measure measure) {
    this.measure = measure;
  }

}
