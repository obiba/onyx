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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.FormComponentLabel;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.validator.StringValidator;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentOutputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;
import org.obiba.onyx.jade.core.service.ActiveInstrumentRunService;
import org.obiba.onyx.jade.core.wicket.DataModel;
import org.obiba.onyx.jade.core.wicket.instrument.validation.IntegrityCheckValidator;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataType;
import org.obiba.onyx.wicket.data.DataField;
import org.obiba.onyx.wicket.data.DataValidator;
import org.obiba.onyx.wicket.model.SpringStringResourceModel;
import org.obiba.onyx.wicket.reusable.FeedbackWindow;
import org.obiba.wicket.model.MessageSourceResolvableStringModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Displays instrument fields to be entered manually. These fields are ones that are normally filled automatically but
 * that have been marked to allow manual input. This is useful in cases of instrument/onyx communications failure. The
 * configuration used to mark the fields has the form: {@code <manualCaptureAllowed>true</manualCaptureAllowed>}.
 */
public class InstrumentManualOutputParameterPanel extends Panel {

  private static final long serialVersionUID = 3008363510160516288L;

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(InstrumentManualOutputParameterPanel.class);

  private FeedbackWindow feedbackWindow;

  @SpringBean
  private ActiveInstrumentRunService activeInstrumentRunService;

  private final Map<InstrumentOutputParameter, DataModel> outputDataModels = new HashMap<InstrumentOutputParameter, DataModel>();

  public InstrumentManualOutputParameterPanel(String id) {
    super(id);
    setOutputMarkupId(true);
    add(new AttributeModifier("class", true, new Model("manual-input-panel")));

    InstrumentType instrumentType = activeInstrumentRunService.getInstrumentType();

    if(!instrumentType.isManualCaptureAllowed()) {
      add(new EmptyPanel("outputs"));
    } else {
      add(new OutputFragment("outputs"));
    }

    feedbackWindow = new FeedbackWindow("feedback");
    feedbackWindow.setOutputMarkupId(true);
    add(feedbackWindow);
  }

  public void saveOutputInstrumentRunValues() {
    activeInstrumentRunService.addManuallyCapturedOutputParameterValues(getOutputParameterData());
  }

  private Map<String, Data> getOutputParameterData() {
    Map<String, Data> outputParameterData = new HashMap<String, Data>(outputDataModels.size());
    for(Map.Entry<InstrumentOutputParameter, DataModel> entry : outputDataModels.entrySet()) {
      outputParameterData.put(entry.getKey().getCode(), (Data) entry.getValue().getObject());
    }
    return outputParameterData;
  }

  @SuppressWarnings("serial")
  private class OutputFragment extends Fragment {

    public OutputFragment(String id) {
      super(id, "outputFragment", InstrumentManualOutputParameterPanel.this);

      add(new Label("title", new StringResourceModel("ProvideTheFollowingInformation", InstrumentManualOutputParameterPanel.this, null)));

      String errMessage = activeInstrumentRunService.updateReadOnlyInputParameterRunValue();
      if(errMessage != null) error(errMessage);

      RepeatingView repeat = new RepeatingView("repeat");
      add(repeat);

      for(InstrumentOutputParameter param : activeInstrumentRunService.getInstrumentType().getManualCaptureAllowedOutputParameters()) {
        WebMarkupContainer item = new WebMarkupContainer(repeat.newChildId());
        repeat.add(item);

        InstrumentRunValue runValue = new InstrumentRunValue();
        runValue.setInstrumentParameter(param.getCode());
        runValue.setInstrumentRun(activeInstrumentRunService.getInstrumentRun());
        runValue.setCaptureMethod(param.getCaptureMethod());

        DataModel dataModel = new DataModel(runValue.getData(param.getDataType()));
        outputDataModels.put(param, dataModel);

        final String paramCode = param.getCode();

        List<Data> choices = null;
        if(param.getDataSource() == null) {
          choices = param.getAllowedValues();
        }

        DataField field;
        if(choices != null && choices.size() > 0) {
          field = new DataField("field", dataModel, param.getDataType(), choices, new IChoiceRenderer() {

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
          field = new DataField("field", dataModel, param.getDataType(), param.getMeasurementUnit()) {
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

        IntegrityCheckValidator.addChecks(param, field);
        item.add(field);

        FormComponentLabel label = new FormComponentLabel("label", field.getField());
        item.add(label);

        Label labelText = new Label("labelText", new MessageSourceResolvableStringModel(param.getLabel()));
        label.add(labelText);
      }
    }
  }

  public FeedbackWindow getFeedbackWindow() {
    return feedbackWindow;
  }

}