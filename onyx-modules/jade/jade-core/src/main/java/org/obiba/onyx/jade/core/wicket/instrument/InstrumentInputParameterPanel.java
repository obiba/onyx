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

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.FormComponentLabel;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
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
import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentInputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.onyx.jade.core.domain.instrument.InterpretativeParameter;
import org.obiba.onyx.jade.core.domain.instrument.OperatorSource;
import org.obiba.onyx.jade.core.domain.instrument.ParticipantInteractionType;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;
import org.obiba.onyx.jade.core.service.ActiveInstrumentRunService;
import org.obiba.onyx.jade.core.service.InstrumentService;
import org.obiba.onyx.jade.core.wicket.instrument.validation.IntegrityCheckValidator;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;
import org.obiba.onyx.util.data.DataType;
import org.obiba.onyx.wicket.data.DataField;
import org.obiba.onyx.wicket.model.SpringStringResourceModel;
import org.obiba.wicket.markup.html.table.DetachableEntityModel;
import org.obiba.wicket.model.MessageSourceResolvableStringModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Get the input parameters that requires operator provisionning.
 */
public class InstrumentInputParameterPanel extends Panel {

  private static final long serialVersionUID = 3008363510160516288L;

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(InstrumentInputParameterPanel.class);

  private static final String YES = "Yes";

  private static final String NO = "No";

  @SpringBean
  private EntityQueryService queryService;

  @SpringBean
  private InstrumentService instrumentService;

  @SpringBean
  private ActiveInstrumentRunService activeInstrumentRunService;

  private List<RadioGroup> interpretativeRadioGroups = new ArrayList<RadioGroup>();

  private List<IModel> inputRunValueModels = new ArrayList<IModel>();

  private boolean observedTitleSet = false;

  public InstrumentInputParameterPanel(String id) {
    super(id);
    setOutputMarkupId(true);

    InterpretativeParameter template = new InterpretativeParameter();
    template.setInstrumentType(activeInstrumentRunService.getInstrumentType());
    template.setType(ParticipantInteractionType.ASKED);
    if(queryService.count(template) == 0) {
      add(new EmptyPanel("askedInputs"));
    } else {
      add(new InterpretativeFragment("askedInputs", queryService.match(template), ParticipantInteractionType.ASKED));
    }

    template.setType(ParticipantInteractionType.OBSERVED);
    if(queryService.count(template) == 0) {
      add(new EmptyPanel("observedInputs"));
    } else {
      add(new InterpretativeFragment("observedInputs", queryService.match(template), ParticipantInteractionType.OBSERVED));
    }

    InstrumentType instrumentType = activeInstrumentRunService.getInstrumentType();
    List<InstrumentInputParameter> instrumentInputParameters = instrumentService.getInstrumentInputParameter(instrumentType, false);

    if(instrumentInputParameters.size() == 0) {
      add(new EmptyPanel("inputs"));
    } else {
      add(new InputFragment("inputs", instrumentInputParameters));
    }
  }

  public void save() {
    saveInterpretativeInstrumentRunValues();
    saveInputInstrumentRunValues();
  }

  private void saveInterpretativeInstrumentRunValues() {
    for(RadioGroup rg : interpretativeRadioGroups) {
      InterpretativeSelection selection = (InterpretativeSelection) rg.getModelObject();
      InstrumentRunValue runValue = activeInstrumentRunService.getInterpretativeInstrumentRunValue(selection.getParameterName());
      runValue.setData(new Data(DataType.TEXT, selection.getSelectionKey()));
      activeInstrumentRunService.update(runValue);
    }
  }

  private void saveInputInstrumentRunValues() {
    for(IModel runValueModel : inputRunValueModels) {
      activeInstrumentRunService.update((InstrumentRunValue) runValueModel.getObject());
    }
  }

  @SuppressWarnings("serial")
  private class InterpretativeFragment extends Fragment {

    public InterpretativeFragment(String id, List<InterpretativeParameter> interpretativeParameters, ParticipantInteractionType type) {
      super(id, "interpretativeFragment", InstrumentInputParameterPanel.this);

      if(type.equals(ParticipantInteractionType.ASKED)) {
        add(new Label("title", new StringResourceModel("AskParticipantTheFollowingQuestions", InstrumentInputParameterPanel.this, null)));
      } else {
        observedTitleSet = true;
        add(new Label("title", new StringResourceModel("ProvideTheFollowingInformation", InstrumentInputParameterPanel.this, null)));
      }

      RepeatingView repeat = new RepeatingView("repeat");
      add(repeat);

      for(final InterpretativeParameter param : interpretativeParameters) {
        WebMarkupContainer item = new WebMarkupContainer(repeat.newChildId());
        repeat.add(item);

        item.add(new Label("label", new MessageSourceResolvableStringModel(param.getLabel())));

        Data data = activeInstrumentRunService.getInstrumentRunValue(param).getData();
        final String defaultDataValue = data != null ? data.getValueAsString() : null;

        // radio group without default selection
        final RadioGroup radioGroup = new RadioGroup("radioGroup", new Model());
        interpretativeRadioGroups.add(radioGroup);
        radioGroup.setLabel(new MessageSourceResolvableStringModel(param.getLabel()));
        item.add(radioGroup);
        ListView radioList = new ListView("radioItem", Arrays.asList(new String[] { YES, NO })) {

          @Override
          protected void populateItem(ListItem listItem) {
            final String key = listItem.getModelObjectAsString();
            InterpretativeSelection selection = new InterpretativeSelection();
            selection.setSelectionKey(key);
            selection.setParameterName(param.getCode());

            Model selectionModel = new Model(selection);

            if(key.equals(defaultDataValue)) {
              radioGroup.setModel(selectionModel);
            }

            Radio radio = new Radio("radio", selectionModel);
            radio.setLabel(new StringResourceModel(key, InstrumentInputParameterPanel.this, null));

            FormComponentLabel radioLabel = new FormComponentLabel("radioLabel", radio);
            listItem.add(radioLabel);
            radioLabel.add(radio);
            radioLabel.add(new Label("label", radio.getLabel()));
          }

        }.setReuseItems(true);
        radioGroup.add(radioList);
        radioGroup.setRequired(true);
      }
    }

  }

  @SuppressWarnings("serial")
  private static class InterpretativeSelection implements Serializable {

    private String selectionKey;

    private String parameterName;

    public String getSelectionKey() {
      return selectionKey;
    }

    public void setSelectionKey(String selectionKey) {
      this.selectionKey = selectionKey;
    }

    public boolean isSelected() {
      return selectionKey.equals(YES);
    }

    public String getParameterName() {
      return parameterName;
    }

    public void setParameterName(String parameterName) {
      this.parameterName = parameterName;
    }

  }

  @SuppressWarnings("serial")
  private class InputFragment extends Fragment {

    public InputFragment(String id, List<InstrumentInputParameter> instrumentInputParameters) {
      super(id, "inputFragment", InstrumentInputParameterPanel.this);

      if(instrumentInputParameters.size() == 0 || observedTitleSet) {
        add(new EmptyPanel("title"));
      } else {
        add(new Label("title", new StringResourceModel("ProvideTheFollowingInformation", InstrumentInputParameterPanel.this, null)));
      }

      RepeatingView repeat = new RepeatingView("repeat");
      add(repeat);

      for(final InstrumentInputParameter param : instrumentInputParameters) {
        WebMarkupContainer item = new WebMarkupContainer(repeat.newChildId());
        repeat.add(item);

        InstrumentRunValue runValue = activeInstrumentRunService.getInstrumentRunValue(param);
        final IModel runValueModel = new DetachableEntityModel(queryService, runValue);
        inputRunValueModels.add(runValueModel);

        List<Data> choices = null;
        if(param.getInputSource() instanceof OperatorSource) {
          String choiceString = ((OperatorSource) param.getInputSource()).getChoices();
          if(choiceString != null) {
            // Choices are in a comma separated string
            String parts[] = choiceString.split(",");
            if(parts != null && parts.length > 0) {
              choices = new ArrayList<Data>(parts.length);
              for(String choice : parts) {
                choices.add(DataBuilder.build(param.getDataType(), choice));
              }
            }
          }
        }

        DataField field;
        if(choices != null && choices.size() > 0) {
          field = new DataField("field", new PropertyModel(runValueModel, "data"), runValue.getDataType(), choices, new IChoiceRenderer() {

            public Object getDisplayValue(Object object) {
              Data data = (Data) object;
              return new SpringStringResourceModel(data.getValueAsString()).getString();
            }

            public String getIdValue(Object object, int index) {
              Data data = (Data) object;
              return data.getValueAsString();
            }

          }, param.getMeasurementUnit());
        } else {
          field = new DataField("field", new PropertyModel(runValueModel, "data"), runValue.getDataType(), param.getMeasurementUnit());
        }
        field.setRequired(true);
        field.setLabel(new MessageSourceResolvableStringModel(param.getLabel()));
        field.add(new AjaxFormComponentUpdatingBehavior("onblur") {
          protected void onUpdate(AjaxRequestTarget target) {
            activeInstrumentRunService.update((InstrumentRunValue) runValueModel.getObject());
          }
        });
        IntegrityCheckValidator.addChecks(field, param.getIntegrityChecks());
        item.add(field);

        FormComponentLabel label = new FormComponentLabel("label", field.getField());
        item.add(label);

        Label labelText = new Label("labelText", new MessageSourceResolvableStringModel(param.getLabel()));
        label.add(labelText);
      }
    }
  }
}
