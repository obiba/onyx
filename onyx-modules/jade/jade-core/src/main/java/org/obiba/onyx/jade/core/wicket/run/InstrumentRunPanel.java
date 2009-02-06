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
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
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
  public InstrumentRunPanel(String id, final ModalWindow modal) {
    super(id);

    InstrumentRun run = activeInstrumentRunService.getInstrumentRun();
    if(run == null) {
      throw new IllegalStateException("No instrument run in session.");
    }

    setModel(new DetachableEntityModel(queryService, run));

    build();

    add(new AjaxLink("closeAction") {

      private static final long serialVersionUID = 1L;

      @Override
      public void onClick(AjaxRequestTarget target) {
        modal.close(target);
      }
    });
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

    if(activeInstrumentRunService.hasInterpretativeParameter()) {
      add(getKeyValueDataPanel("interpretatives", new StringResourceModel("Interpretatives", this, null), activeInstrumentRunService.getInterpretativeParameters()));
    } else {
      add(new EmptyPanel("interpretatives"));
    }

    if(isInteractive) {
      if(activeInstrumentRunService.hasInputParameter()) {
        add(getKeyValueDataPanel("inputs", new StringResourceModel("InstrumentInputs", this, null), activeInstrumentRunService.getInputParameters()));
      } else {
        add(new EmptyPanel("inputs"));
      }
      add(new EmptyPanel("operatorAutoInputs"));
    } else {
      // Manual Inputs
      if(activeInstrumentRunService.hasInputParameter(InstrumentParameterCaptureMethod.MANUAL)) {
        add(getKeyValueDataPanel("inputs", new StringResourceModel("OperatorInputs", this, null), activeInstrumentRunService.getInputParameters(InstrumentParameterCaptureMethod.MANUAL)));
      } else {
        add(new EmptyPanel("inputs"));
      }

      // Automatic Inputs
      if(activeInstrumentRunService.hasInputParameter(InstrumentParameterCaptureMethod.AUTOMATIC)) {
        add(getKeyValueDataPanel("operatorAutoInputs", new StringResourceModel("StandardInputs", this, null), activeInstrumentRunService.getInputParameters(InstrumentParameterCaptureMethod.AUTOMATIC)));
      } else {
        add(new EmptyPanel("operatorAutoInputs"));
      }
    }

    if(activeInstrumentRunService.hasOutputParameter()) {
      String key = isInteractive ? "InstrumentOutputs" : "OperatorOutputs";
      add(getKeyValueDataPanel("outputs", new StringResourceModel(key, this, null), activeInstrumentRunService.getOutputParameters()));
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
    }

    return kvPanel;
  }

  private String formatOutput(InstrumentParameter param, InstrumentRunValue runValue, String formatStr) {

    log.debug("Display format for {} is {}", param.getCode(), formatStr);
    Object value = runValue.getData().getValue();
    String valueStr = runValue.getData().getValueAsString();
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

}
