/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.core.wicket.instrument;

import java.io.Serializable;
import java.util.Locale;

import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.time.Duration;
import org.apache.wicket.util.value.ValueMap;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentInputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameterCaptureMethod;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.onyx.jade.core.domain.run.InstrumentRun;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;
import org.obiba.onyx.jade.core.service.ActiveInstrumentRunService;
import org.obiba.onyx.jade.core.service.InstrumentService;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.wicket.behavior.ButtonDisableBehavior;
import org.obiba.onyx.wicket.model.MagmaStringResourceModel;
import org.obiba.onyx.wicket.model.SpringStringResourceModel;
import org.obiba.wicket.model.MessageSourceResolvableStringModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Get the input parameters that are from read-only sources and give the instructions to the operator:
 * <ul>
 * <li>General information with instrument launcher (if available)</li>
 * <li>instructions to enter manually captured input parameters (if needed)</li>
 * </ul>
 */
public abstract class InstrumentLaunchPanel extends Panel {

  private static final long serialVersionUID = 8250439838157103589L;

  private static final Logger log = LoggerFactory.getLogger(InstrumentLaunchPanel.class);

  @SpringBean
  private ActiveInstrumentRunService activeInstrumentRunService;

  @SpringBean
  private InstrumentService instrumentService;

  private MeasuresPanel measures;

  private Link<Object> startButton;

  @SuppressWarnings("serial")
  public InstrumentLaunchPanel(String id) {
    super(id);
    InstrumentRun currentRun = activeInstrumentRunService.getInstrumentRun();
    setDefaultModel(new Model<InstrumentRun>(currentRun));

    setOutputMarkupId(true);

    final InstrumentType instrumentType = activeInstrumentRunService.getInstrumentType();
    String codebase = instrumentService.getInstrumentInstallPath(instrumentType);

    // instrument instructions
    add(new Label("instrument-instructions", new MagmaStringResourceModel(new MessageSourceResolvableStringModel(instrumentType.getInstructions())) {

      @Override
      protected String getTableContext() {
        return instrumentType.getName();
      }

      @Override
      protected Participant getParticipant() {
        return activeInstrumentRunService.getParticipant();
      }

      @Override
      protected Locale getLocale() {
        return InstrumentLaunchPanel.this.getLocale();
      }
    }).setEscapeModelStrings(false));

    // general instructions and launcher
    add(new Label("general", new StringResourceModel("StartMeasurementWithInstrument", this, new Model<ValueMap>(new ValueMap("name=" + instrumentType.getName())))));

    final InstrumentLauncher launcher = new InstrumentLauncher(instrumentType, codebase);

    if(instrumentType.hasManualCaptureOutputParameters() || instrumentType.isRepeatable()) {
      add(measures = new MeasuresPanel("measures") {
        @Override
        public void onAddClick(AjaxRequestTarget target) {
          // Note that "Manual" instrument has been launched.
          InstrumentLaunchPanel.this.onInstrumentLaunch();
        }

        @Override
        public void onRefresh(AjaxRequestTarget target) {
          startButton.setEnabled(!isSkipMeasurement() && measures.getMeasureCount() < measures.getExpectedMeasureCount());
          target.addComponent(startButton);
        }

        @Override
        public void onSkipUpdate(AjaxRequestTarget target) {
          startButton.setEnabled(!isSkipMeasurement() && measures.getMeasureCount() < measures.getExpectedMeasureCount());
          target.addComponent(startButton);
        }

      });
    } else {
      add(new EmptyPanel("measures"));
    }

    if(instrumentType.isRepeatable()) {
      add(new AbstractAjaxTimerBehavior(Duration.seconds(10)) {

        protected void onTimer(AjaxRequestTarget target) {
          if(!measures.isSkipMeasurement() && !isMeasureComplete()) {
            measures.refresh(target);
          }
        }

      });
    }

    startButton = new Link<Object>("start") {

      @Override
      public void onClick() {
        launcher.launch();
        InstrumentLaunchPanel.this.onInstrumentLaunch();
      }

    };
    startButton.add(new ButtonDisableBehavior());
    startButton.setOutputMarkupId(true);
    startButton.setEnabled(!isMeasureComplete());
    add(startButton);

    String errMessage = activeInstrumentRunService.updateReadOnlyInputParameterRunValue();
    if(errMessage != null) error(errMessage);

    RepeatingView repeat = new RepeatingView("repeat");
    add(repeat);

    // get all the input run values that requires manual capture
    boolean manualCaptureRequired = false;
    for(InstrumentInputParameter param : instrumentType.getInputParameters(InstrumentParameterCaptureMethod.MANUAL)) {

      final String paramCode = param.getCode();

      // We don't want to display parameters that were manually entered by the user in the previous step.
      // These will be automatically sent to the instrument.
      if(param.getDataSource() != null) {

        manualCaptureRequired = true;

        WebMarkupContainer item = new WebMarkupContainer(repeat.newChildId());
        repeat.add(item);

        item.add(new Label("instruction", new StringResourceModel("TypeTheValueInTheInstrument", InstrumentLaunchPanel.this, new Model<Serializable>() {
          public Serializable getObject() {
            InstrumentInputParameter param = (InstrumentInputParameter) activeInstrumentRunService.getInstrumentType().getInstrumentParameter(paramCode);
            InstrumentRunValue runValue = activeInstrumentRunService.getInstrumentRunValue(paramCode);
            ValueMap map = new ValueMap();
            map.put("description", new MessageSourceResolvableStringModel(param.getLabel()).getObject());
            Data data = runValue.getData(param.getDataType());
            if(data != null && data.getValue() != null) {
              map.put("value", new SpringStringResourceModel(data.getValueAsString()).getString());
              String unit = param.getMeasurementUnit();
              if(unit == null) {
                unit = "";
              }
              map.put("unit", unit);
            }
            return map;
          }
        })));
      }
    }

    Label instructions = new Label("instructions", new StringResourceModel("Instructions", InstrumentLaunchPanel.this, null));
    instructions.setVisible(manualCaptureRequired);
    add(instructions);
  }

  /**
   * Called when instrument launcher is clicked.
   */
  public abstract void onInstrumentLaunch();

  public boolean isSkipMeasurement() {
    if(measures != null) {
      return measures.isSkipMeasurement();
    }
    return false;
  }

  public boolean isMeasureComplete() {
    if(measures != null) {
      return measures.isMeasureComplete();
    }
    return false;
  }

}
