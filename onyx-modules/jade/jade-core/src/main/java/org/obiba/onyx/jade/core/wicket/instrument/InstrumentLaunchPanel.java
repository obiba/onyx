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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormChoiceComponentUpdatingBehavior;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.value.ValueMap;
import org.apache.wicket.validation.validator.StringValidator;
import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentInputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentOutputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameterCaptureMethod;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.onyx.jade.core.domain.run.InstrumentRun;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;
import org.obiba.onyx.jade.core.service.ActiveInstrumentRunService;
import org.obiba.onyx.jade.core.service.InstrumentService;
import org.obiba.onyx.jade.core.wicket.InstrumentRunValueDataModel;
import org.obiba.onyx.jade.core.wicket.instrument.validation.IntegrityCheckValidator;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataType;
import org.obiba.onyx.wicket.behavior.ButtonDisableBehavior;
import org.obiba.onyx.wicket.data.DataField;
import org.obiba.onyx.wicket.data.DataValidator;
import org.obiba.onyx.wicket.model.MagmaStringResourceModel;
import org.obiba.onyx.wicket.model.SpringStringResourceModel;
import org.obiba.wicket.markup.html.table.DetachableEntityModel;
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

  private static final String AUTOMATIC = "AutomaticCapture";

  private static final String MANUAL = "ManualCapture";

  @SpringBean
  private EntityQueryService queryService;

  @SpringBean
  private ActiveInstrumentRunService activeInstrumentRunService;

  @SpringBean
  private InstrumentService instrumentService;

  private final InstrumentType instrumentType;

  private AutomaticCaptureFragment automaticCapture;

  private Component manualCapture;

  private MeasuresPanel measures;

  private String selectedCaptureMethod = AUTOMATIC;

  private List<IModel<InstrumentRunValue>> outputRunValueModels = new ArrayList<IModel<InstrumentRunValue>>();

  @SuppressWarnings("serial")
  public InstrumentLaunchPanel(String id) {
    super(id);
    InstrumentRun currentRun = activeInstrumentRunService.getInstrumentRun();
    setDefaultModel(new Model<InstrumentRun>(currentRun));
    setOutputMarkupId(true);

    instrumentType = activeInstrumentRunService.getInstrumentType();

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

    automaticCapture = new AutomaticCaptureFragment("automaticCapture");
    add(automaticCapture);

    // capture method
    final RadioGroup radioGroup = new RadioGroup<String>("radioGroup", new PropertyModel<String>(this, "selectedCaptureMethod"));
    radioGroup.add(new AjaxFormChoiceComponentUpdatingBehavior() {
      @Override
      protected void onUpdate(AjaxRequestTarget target) {
        log.info("radioGroup.onUpdate={}", selectedCaptureMethod);
        if (measures != null) measures.showManualCapture(selectedCaptureMethod.equals(MANUAL));
        if (manualCapture != null) manualCapture.setVisible(selectedCaptureMethod.equals(MANUAL));
        automaticCapture.setVisible(selectedCaptureMethod.equals(AUTOMATIC));
        target.addComponent(InstrumentLaunchPanel.this);
      }
    });
    add(radioGroup);
    radioGroup.setVisible(instrumentType.hasManualCaptureOutputParameters());
    ListView radioList = new ListView<String>("radioItem", Arrays.asList(new String[] { AUTOMATIC, MANUAL })) {

      @Override
      protected void populateItem(final ListItem item) {
        Radio radio = new Radio<String>("radio", item.getModel());
        radio.setLabel(new StringResourceModel((String) item.getModelObject(), InstrumentLaunchPanel.this, null));
        item.add(radio);
        item.add(new SimpleFormComponentLabel("radioLabel", radio));
      }

    }.setReuseItems(true);
    radioGroup.add(radioList);
    radioGroup.setRequired(true);

    if (instrumentType.hasManualCaptureOutputParameters(false)) {
      manualCapture = new ManualCaptureFragment("manualCapture");
      add(manualCapture);
      manualCapture.setVisible(false);
    } else {
      add(new EmptyPanel("manualCapture"));
    }

    if (instrumentType.isRepeatable()) {
      add(measures = new MeasuresPanel("measures") {
        @Override
        public void onAddClick(AjaxRequestTarget target) {
          // Note that "Manual" instrument has been launched.
          InstrumentLaunchPanel.this.onInstrumentLaunch();
        }

        @Override
        public void onRefresh(AjaxRequestTarget target) {
          automaticCapture.setStartButtonEnabled(!isSkipMeasurement() && measures.getMeasureCount() < measures.getExpectedMeasureCount());
          target.addComponent(automaticCapture);
        }

        @Override
        public void onSkipUpdate(AjaxRequestTarget target) {
          automaticCapture.setStartButtonEnabled(!isSkipMeasurement() && measures.getMeasureCount() < measures.getExpectedMeasureCount());
          target.addComponent(automaticCapture);
        }

      });
      measures.showManualCapture(false);
    } else {
      add(new EmptyPanel("measures"));
    }

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

  public void saveManualOutputInstrumentRunValues() {
    for(IModel<InstrumentRunValue> runValueModel : outputRunValueModels) {
      activeInstrumentRunService.update(runValueModel.getObject());
    }
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

  public class AutomaticCaptureFragment extends Fragment {

    private Link<Object> startButton;

    public AutomaticCaptureFragment(String id) {
      super(id, "automaticCaptureFragment", InstrumentLaunchPanel.this);
      setOutputMarkupId(true);
      // general instructions and launcher
      add(new Label("general", new StringResourceModel("StartMeasurementWithInstrument", InstrumentLaunchPanel.this,
        new Model<ValueMap>(new ValueMap("name=" + instrumentType.getName())))));

      String codebase = instrumentService.getInstrumentInstallPath(instrumentType);
      final InstrumentLauncher launcher = new InstrumentLauncher(instrumentType, codebase);
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
    }

    public void setStartButtonEnabled(boolean enabled) {
      startButton.setEnabled(enabled);
    }
  }

  public class ManualCaptureFragment extends Fragment {
    public ManualCaptureFragment(String id) {
      super(id, "manualCaptureFragment", InstrumentLaunchPanel.this);
      setOutputMarkupId(true);
      add(new Label("title", new StringResourceModel("ProvideTheFollowingInformation", InstrumentLaunchPanel.this, null)));
      add(new ManualOutputsFragment("manualOutputs"));
    }
  }

  public class ManualOutputsFragment extends Fragment {

    public ManualOutputsFragment(String id) {
      super(id, "manualOutputsFragment", InstrumentLaunchPanel.this);

      RepeatingView repeat = new RepeatingView("repeat");
      add(repeat);

      for (InstrumentOutputParameter param : instrumentType.getManualCaptureOutputParameters()) {
        if (!instrumentType.isRepeatable(param)) {
          WebMarkupContainer item = new WebMarkupContainer(repeat.newChildId());
          repeat.add(item);

          InstrumentRunValue runValue = activeInstrumentRunService.getOrCreateInstrumentRunValue(param);

          IModel<InstrumentRunValue> runValueModel = new DetachableEntityModel<InstrumentRunValue>(queryService, runValue);
          outputRunValueModels.add(runValueModel);

          DataField field = makeDataField(param, runValueModel);
          item.add(field);

          FormComponentLabel label = new FormComponentLabel("label", field.getField());
          item.add(label);

          Label labelText = new Label("labelText", new MessageSourceResolvableStringModel(param.getLabel()));
          label.add(labelText);
        }
      }
    }

    private DataField makeDataField(InstrumentOutputParameter param, final IModel<InstrumentRunValue> runValueModel) {
      List<Data> choices = null;
      if(param.getDataSource() == null) {
        choices = param.getAllowedValues();
      }

      DataField field;

      if(choices != null && choices.size() > 0) {
        field = new DataField("field", new InstrumentRunValueDataModel(runValueModel, param.getDataType()), param.getDataType(), choices, new IChoiceRenderer() {

          public Object getDisplayValue(Object object) {
            Data data = (Data) object;
            return new SpringStringResourceModel(data.getValueAsString()).getString();
          }

          public String getIdValue(Object object, int index) {
            Data data = (Data) object;
            return data.getValueAsString();
          }

        }, param.getMeasurementUnit());
        field.setRequired(true);

      } else {
        final String paramCode = param.getCode();
        field = new DataField("field", new InstrumentRunValueDataModel(runValueModel, param.getDataType()), param.getDataType(), param.getMeasurementUnit()) {
          @Override
          public boolean isRequired() {
            return activeInstrumentRunService.getInstrumentType().getInstrumentParameter(paramCode).isRequired(activeInstrumentRunService.getParticipant());
          }
        };

        if(param.getDataType().equals(DataType.TEXT) && (field.getField().getClass().equals(java.awt.TextField.class) || field.getField().getClass().equals(TextArea.class))) {
          field.getField().add(new DataValidator(new StringValidator.MaximumLengthValidator(2000), param.getDataType()));
        }
      }

      field.setLabel(new MessageSourceResolvableStringModel(param.getLabel()));
      field.add(new AjaxFormComponentUpdatingBehavior("onblur") {
        protected void onUpdate(AjaxRequestTarget target) {
          activeInstrumentRunService.update((InstrumentRunValue) runValueModel.getObject());
        }
      });

      IntegrityCheckValidator.addChecks(param, field);

      return field;
    }

  }

}
