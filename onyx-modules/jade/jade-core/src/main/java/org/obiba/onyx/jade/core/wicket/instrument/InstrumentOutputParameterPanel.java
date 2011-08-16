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

import java.awt.TextField;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.FormComponentLabel;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.validator.StringValidator;
import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentInputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentOutputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameterCaptureMethod;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;
import org.obiba.onyx.jade.core.service.ActiveInstrumentRunService;
import org.obiba.onyx.jade.core.wicket.InstrumentRunValueDataModel;
import org.obiba.onyx.jade.core.wicket.instrument.validation.IntegrityCheckValidator;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;
import org.obiba.onyx.util.data.DataType;
import org.obiba.onyx.wicket.data.DataField;
import org.obiba.onyx.wicket.data.DataValidator;
import org.obiba.onyx.wicket.model.SpringStringResourceModel;
import org.obiba.wicket.markup.html.table.DetachableEntityModel;
import org.obiba.wicket.model.MessageSourceResolvableStringModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.DefaultMessageSourceResolvable;

public class InstrumentOutputParameterPanel extends Panel {

  private static final long serialVersionUID = 3008363510160516288L;

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(InstrumentOutputParameterPanel.class);

  @SpringBean
  private EntityQueryService queryService;

  @SpringBean
  private ActiveInstrumentRunService activeInstrumentRunService;

  private List<IModel<InstrumentRunValue>> outputRunValueModels = new ArrayList<IModel<InstrumentRunValue>>();

  private List<IModel<InstrumentRunValue>> inputRunValueModels = new ArrayList<IModel<InstrumentRunValue>>();

  private MeasuresPanel measures;

  @SuppressWarnings("serial")
  public InstrumentOutputParameterPanel(String id) {
    super(id);
    setOutputMarkupId(true);

    InstrumentType instrumentType = activeInstrumentRunService.getInstrumentType();

    if(getOutputParametersOriginallyMarkedForManualCapture().size() == 0) {
      add(new EmptyPanel("outputs"));
    } else {
      add(new OutputFragment("outputs"));
    }

    if(!instrumentType.hasInputParameter(InstrumentParameterCaptureMethod.AUTOMATIC)) {
      add(new EmptyPanel("inputs"));
    } else {
      add(new InputFragment("inputs"));
    }

    if(instrumentType.isRepeatable()) {
      add(measures = new MeasuresPanel("measures"));
    } else {
      add(new EmptyPanel("measures"));
    }

  }

  private List<InstrumentOutputParameter> getOutputParametersOriginallyMarkedForManualCapture() {
    List<InstrumentOutputParameter> result = new ArrayList<InstrumentOutputParameter>();
    InstrumentType instrumentType = activeInstrumentRunService.getInstrumentType();
    List<InstrumentOutputParameter> outputParams = instrumentType.getOutputParameters(InstrumentParameterCaptureMethod.MANUAL);

    for(InstrumentOutputParameter param : outputParams) {
      if(!param.isManualCaptureAllowed()) {
        result.add(param);
      }
    }
    return result;
  }

  public void saveOutputInstrumentRunValues() {
    for(IModel<InstrumentRunValue> runValueModel : outputRunValueModels) {
      activeInstrumentRunService.update(runValueModel.getObject());
    }
  }

  public boolean isSkipMeasurement() {
    if(measures != null) {
      return measures.isSkipMeasurement();
    }
    return false;
  }

  @SuppressWarnings("serial")
  private class OutputFragment extends Fragment {

    public OutputFragment(String id) {
      super(id, "outputFragment", InstrumentOutputParameterPanel.this);

      add(new Label("title", new StringResourceModel("ProvideTheFollowingInformation", InstrumentOutputParameterPanel.this, null)));

      String errMessage = activeInstrumentRunService.updateReadOnlyInputParameterRunValue();
      if(errMessage != null) error(errMessage);

      RepeatingView repeat = new RepeatingView("repeat");
      add(repeat);

      for(InstrumentOutputParameter param : getOutputParametersOriginallyMarkedForManualCapture()) {
        if(!activeInstrumentRunService.getInstrumentType().isRepeatable(param)) {

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

        if(param.getDataType().equals(DataType.TEXT) && (field.getField().getClass().equals(TextField.class) || field.getField().getClass().equals(TextArea.class))) {
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

  @SuppressWarnings("serial")
  private class InputFragment extends Fragment {

    public InputFragment(String id) {
      super(id, "inputFragment", InstrumentOutputParameterPanel.this);

      add(new Label("title", new StringResourceModel("AutomaticEnteredInformation", InstrumentOutputParameterPanel.this, null)));

      RepeatingView repeat = new RepeatingView("repeat");
      add(repeat);

      for(InstrumentInputParameter param : activeInstrumentRunService.getInstrumentType().getInputParameters(InstrumentParameterCaptureMethod.AUTOMATIC)) {
        WebMarkupContainer item = new WebMarkupContainer(repeat.newChildId());
        repeat.add(item);

        InstrumentRunValue runValue = activeInstrumentRunService.getOrCreateInstrumentRunValue(param);

        IModel<InstrumentRunValue> runValueModel = new DetachableEntityModel<InstrumentRunValue>(queryService, runValue);
        inputRunValueModels.add(runValueModel);

        DataField field = new DataField("field", new InstrumentRunValueDataModel(runValueModel, param.getDataType()), param.getDataType(), param.getMeasurementUnit());
        field.setLabel(new MessageSourceResolvableStringModel(param.getLabel()));

        if(param.getDataType().equals(DataType.TEXT)) {
          MessageSourceResolvableStringModel fieldModel = new MessageSourceResolvableStringModel(new DefaultMessageSourceResolvable(runValue.getData(param.getDataType()).getValueAsString()));
          field.setFieldModel(new Model<Data>(DataBuilder.buildText(fieldModel.getObject().toString())));
        }

        field.setFieldEnabled(false);

        item.add(field);

        FormComponentLabel label = new FormComponentLabel("label", field.getField());
        item.add(label);

        Label labelText = new Label("labelText", new MessageSourceResolvableStringModel(param.getLabel()));
        label.add(labelText);
      }
    }
  }
}
