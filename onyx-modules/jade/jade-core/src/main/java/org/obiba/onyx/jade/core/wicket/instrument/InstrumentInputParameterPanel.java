package org.obiba.onyx.jade.core.wicket.instrument;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
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
import org.apache.wicket.spring.SpringWebApplication;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.core.service.UserSessionService;
import org.obiba.onyx.jade.core.domain.instrument.Instrument;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentInputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InterpretativeParameter;
import org.obiba.onyx.jade.core.domain.instrument.ParticipantInteractionType;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;
import org.obiba.onyx.jade.core.service.ActiveInstrumentRunService;
import org.obiba.onyx.jade.core.service.InstrumentService;
import org.obiba.onyx.jade.core.wicket.instrument.validation.IntegrityCheckValidator;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataType;
import org.obiba.onyx.wicket.data.DataField;
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

  private static final String YES = "Yes";

  private static final String NO = "No";

  private static final String DOESNOT_KNOW = "DoesNotKnow";

  @SpringBean
  private EntityQueryService queryService;

  @SpringBean
  private InstrumentService instrumentService;

  @SpringBean
  private ActiveInstrumentRunService activeInstrumentRunService;

  @SpringBean(name = "userSessionService")
  private UserSessionService userSessionService;

  private List<RadioGroup> interpretativeRadioGroups = new ArrayList<RadioGroup>();

  private List<IModel> inputRunValueModels = new ArrayList<IModel>();

  private boolean observedTitleSet = false;

  public InstrumentInputParameterPanel(String id) {
    super(id);
    setOutputMarkupId(true);

    InterpretativeParameter template = new InterpretativeParameter();
    template.setInstrument(activeInstrumentRunService.getInstrument());
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

    Instrument instrument = activeInstrumentRunService.getInstrument();
    List<InstrumentInputParameter> instrumentInputParameters = instrumentService.getInstrumentInputParameter(instrument, false);

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

        param.setApplicationContext(((SpringWebApplication) getApplication()).getSpringContextLocator().getSpringContext());
        param.setUserSessionService(userSessionService);

        item.add(new Label("label", new PropertyModel(param, "description")));

        Data data = activeInstrumentRunService.getInterpretativeInstrumentRunValue(param.getName()).getData();
        final String defaultDataValue = data != null ? data.getValueAsString() : null;
        
        // radio group without default selection
        final RadioGroup radioGroup = new RadioGroup("radioGroup", new Model());
        interpretativeRadioGroups.add(radioGroup);
        radioGroup.setLabel(new PropertyModel(param, "description"));
        item.add(radioGroup);
        ListView radioList = new ListView("radioItem", Arrays.asList(new String[] { YES, NO, DOESNOT_KNOW })) {

          @Override
          protected void populateItem(ListItem listItem) {
            final String key = listItem.getModelObjectAsString();
            InterpretativeSelection selection = new InterpretativeSelection();
            selection.setSelectionKey(key);
            selection.setParameterName(param.getName());
            
            Model selectionModel = new Model(selection);
            
            if (key.equals(defaultDataValue)) {
              radioGroup.setModel(selectionModel);
            }

            listItem.add(new Radio("radio", selectionModel));
            listItem.add(new Label("label", new StringResourceModel(key, InstrumentInputParameterPanel.this, null)));
          }

        }.setReuseItems(true);
        radioGroup.add(radioList);
        radioGroup.setRequired(true);
      }
    }

  }

  @SuppressWarnings("serial")
  private class InterpretativeSelection implements Serializable {

    private String selectionKey;

    private String parameterName;

    public String getSelectionKey() {
      return selectionKey;
    }

    public void setSelectionKey(String selectionKey) {
      this.selectionKey = selectionKey;
    }

    public boolean isSelected() {
      return selectionKey.equals(YES) || selectionKey.equals(DOESNOT_KNOW);
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

        // Inject the Spring application context and the user session service
        // into the instrument parameter. NOTE: These are dependencies of
        // InstrumentParameter.getDescription().
        param.setApplicationContext(((SpringWebApplication) getApplication()).getSpringContextLocator().getSpringContext());
        param.setUserSessionService(userSessionService);

        Label label = new Label("label", new PropertyModel(param, "description"));
        item.add(label);

        InstrumentRunValue runValue = activeInstrumentRunService.getInputInstrumentRunValue(param.getName());
        final IModel runValueModel = new DetachableEntityModel(queryService, runValue);
        inputRunValueModels.add(runValueModel);

        DataField field = new DataField("field", new PropertyModel(runValueModel, "data"), runValue.getDataType(), param.getMeasurementUnit());
        field.setRequired(true);
        field.setLabel(new PropertyModel(param, "description"));
        field.add(new AjaxFormComponentUpdatingBehavior("onblur") {
          protected void onUpdate(AjaxRequestTarget target) {
            activeInstrumentRunService.update((InstrumentRunValue) runValueModel.getObject());
          }
        });
        IntegrityCheckValidator.addChecks(field, param.getIntegrityChecks());
        item.add(field);
      }
    }
  }
}
