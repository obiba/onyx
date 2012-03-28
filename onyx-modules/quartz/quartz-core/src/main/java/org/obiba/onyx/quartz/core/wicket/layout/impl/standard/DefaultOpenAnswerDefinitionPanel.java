/***********************************************************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 **********************************************************************************************************************/
package org.obiba.onyx.quartz.core.wicket.layout.impl.standard;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.upload.FileUploadException;
import org.apache.wicket.util.value.ValueMap;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.validator.StringValidator;
import org.obiba.onyx.quartz.core.domain.answer.OpenAnswer;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition.OpenAnswerType;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinitionAudio;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinitionSuggestion;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;
import org.obiba.onyx.quartz.core.wicket.layout.impl.AbstractOpenAnswerDefinitionPanel;
import org.obiba.onyx.quartz.core.wicket.layout.impl.util.OpenAnswerDefinitionValidatorFactory;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireStringResourceModel;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireStringResourceModelHelper;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;
import org.obiba.onyx.util.data.DataType;
import org.obiba.onyx.wicket.behavior.InvalidFormFieldBehavior;
import org.obiba.onyx.wicket.data.DataField;
import org.obiba.onyx.wicket.data.DataField.AudioDataListener;
import org.obiba.onyx.wicket.data.DataField.IAutoCompleteDataConverter;
import org.obiba.onyx.wicket.data.DataField.IAutoCompleteDataProvider;
import org.obiba.onyx.wicket.data.DataValidator;
import org.obiba.onyx.wicket.wizard.WizardForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

public class DefaultOpenAnswerDefinitionPanel extends AbstractOpenAnswerDefinitionPanel {

  private static final long serialVersionUID = 8950481253772691811L;

  public static final int MAXIMUM_LENGTH = 2000;

  private static final Logger log = LoggerFactory.getLogger(DefaultOpenAnswerDefinitionPanel.class);

  @SpringBean
  private ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationService;

  private DataField openField;

  /**
   * Constructor.
   * 
   * @param id
   * @param questionModel
   * @param questionCategoryModel
   */
  public DefaultOpenAnswerDefinitionPanel(String id, IModel<Question> questionModel, IModel<QuestionCategory> questionCategoryModel) {
    super(id, questionModel, questionCategoryModel);
    setup();
  }

  public DefaultOpenAnswerDefinitionPanel(String id, IModel<Question> questionModel, IModel<QuestionCategory> questionCategoryModel, IModel<OpenAnswerDefinition> openAnswerDefinitionModel) {
    super(id, questionModel, questionCategoryModel, openAnswerDefinitionModel);
    setup();
  }

  private void setup() {
    setOutputMarkupId(true);

    if(!activeQuestionnaireAdministrationService.isQuestionnaireDevelopmentMode()) {
      OpenAnswer previousAnswer = activeQuestionnaireAdministrationService.findOpenAnswer(getQuestion(), getQuestionCategory().getCategory(), getOpenAnswerDefinition());
      if(previousAnswer != null) {
        setData(previousAnswer.getData());
      }
    }

    QuestionnaireStringResourceModel openLabel = new QuestionnaireStringResourceModel(getOpenAnswerDefinitionModel(), "label");
    QuestionnaireStringResourceModel unitLabel = new QuestionnaireStringResourceModel(getOpenAnswerDefinitionModel(), "unitLabel");

    add(new Label("label", openLabel));

    if(getOpenAnswerDefinition().getDefaultValues().size() == 1) {
      setData(getOpenAnswerDefinition().getDefaultValues().get(0));
    }

    openField = createDataField(unitLabel);
    openField.getField().setOutputMarkupId(true);
    add(openField);

    // validators
    if(!activeQuestionnaireAdministrationService.isQuestionnaireDevelopmentMode()) {
      for(IValidator<?> validator : OpenAnswerDefinitionValidatorFactory.getValidators(getOpenAnswerDefinitionModel(), activeQuestionnaireAdministrationService.getQuestionnaireParticipant().getParticipant())) {
        openField.add(validator);
      }
    }

    // at least this validator for textual input
    if(getOpenAnswerDefinition().getDataType().equals(DataType.TEXT) && getOpenAnswerDefinition().getDefaultValues().isEmpty()) {
      // see OpenAnswer.textValue column length
      openField.add(new DataValidator(new StringValidator.MaximumLengthValidator(MAXIMUM_LENGTH), DataType.TEXT));
    }

    // behaviors
    openField.add(new InvalidFormFieldBehavior());

    if(!activeQuestionnaireAdministrationService.isQuestionnaireDevelopmentMode()) {
      openField.add(new AjaxFormComponentUpdatingBehavior("onchange") {
        private static final long serialVersionUID = 1L;

        @Override
        protected void onUpdate(AjaxRequestTarget target) {
          // persist data
          activeQuestionnaireAdministrationService.answer(getQuestion(), getQuestionCategory(), getOpenAnswerDefinition(), getData());

          // clean a previous error message
          updateFeedback(target);

          fireQuestionCategorySelection(target, getQuestionModel(), getQuestionCategoryModel(), true);
        }

        @Override
        protected void onError(AjaxRequestTarget target, RuntimeException e) {
          super.onError(target, e);
          // display error messages
          updateFeedback(target);
        }
      });

      if(getOpenAnswerDefinition().getDefaultValues().size() == 0) {
        openField.add(new AjaxEventBehavior("onclick") {

          private static final long serialVersionUID = 1L;

          @Override
          protected void onEvent(AjaxRequestTarget target) {
            // persist data
            // do not fire event if category was already selected
            if(activeQuestionnaireAdministrationService.findAnswer(getQuestion(), getQuestionCategory().getCategory()) == null) {
              openField.focusField(target);

              // persist data for category
              activeQuestionnaireAdministrationService.answer(getQuestion(), getQuestionCategory());

              fireQuestionCategorySelection(target, getQuestionModel(), getQuestionCategoryModel(), true);
            }
          }

        });
      }
    }

    // set the label of the field
    openField.setLabel(QuestionnaireStringResourceModelHelper.getStringResourceModel(getQuestion(), getQuestionCategory(), getOpenAnswerDefinition()));
  }

  /**
   * Refresh wizard feedback panel and input field.
   * 
   * @param target
   */
  private void updateFeedback(final AjaxRequestTarget target) {
    WizardForm wizard = findParent(WizardForm.class);
    if(wizard != null && wizard.getFeedbackWindow() != null) {
      if(wizard.getFeedbackMessage() != null) wizard.getFeedbackWindow().show(target);
    }
    target.appendJavascript("Resizer.resizeWizard();");
    target.addComponent(openField.getField());
  }

  public void setFieldEnabled(boolean enabled) {
    openField.setFieldEnabled(enabled);
  }

  public boolean isFieldEnabled() {
    return openField.isFieldEnabled();
  }

  @Override
  public void resetField() {
    openField.setFieldModelObject(null);
    openField.getField().clearInput();
  }

  private DataField createDataField(QuestionnaireStringResourceModel unitLabel) {
    ValueMap arguments = getOpenAnswerDefinition().getUIArgumentsValueMap();
    if(getOpenAnswerDefinition().getDefaultValues().size() > 1) {
      return createDataFieldWithDefaultValues(unitLabel);
    } else if(getOpenAnswerDefinition().isAudioAnswer()) {
      return createAudioRecordingDataField(arguments);
    } else if(getOpenAnswerDefinition().getOpenAnswerType() == OpenAnswerType.AUTO_COMPLETE) {
      return createAutoCompleteDataField(arguments);
    }
    return createDefaultDataField(arguments, unitLabel);
  }

  private DataField createDataFieldWithDefaultValues(QuestionnaireStringResourceModel unitLabel) {
    return new DataField("open", new PropertyModel<Data>(this, "data"), getOpenAnswerDefinition().getDataType(), getOpenAnswerDefinition().getDefaultValues(), new IChoiceRenderer<Data>() {
      private static final long serialVersionUID = 1L;

      @Override
      public Object getDisplayValue(Data data) {
        return new QuestionnaireStringResourceModel(getOpenAnswerDefinitionModel(), data.getValueAsString()).getObject();
      }

      @Override
      public String getIdValue(Data data, int index) {
        return data.getValueAsString();
      }

    }, unitLabel.getString());
  }

  private DataField createAudioRecordingDataField(ValueMap arguments) {
    OpenAnswerDefinitionAudio openAnswerAudio = new OpenAnswerDefinitionAudio(getOpenAnswerDefinition());

    DataField audioField = new DataField("open", new PropertyModel<Data>(this, "data"), getOpenAnswerDefinition().getDataType(), openAnswerAudio.getSamplingRate(), openAnswerAudio.getMaxDuration());
    audioField.addListener(new AudioDataListener() {
      private static final long serialVersionUID = 1L;

      @Override
      public void onDataUploaded() {
        // persist data
        activeQuestionnaireAdministrationService.answer(getQuestion(), getQuestionCategory(), getOpenAnswerDefinition(), getData());
      }

      @Override
      public void onAudioDataProcessed(AjaxRequestTarget target) {
        updateFeedback(target); // clean a previous error message
        fireQuestionCategorySelection(target, getQuestionModel(), getQuestionCategoryModel(), true);
      }

      @Override
      public void onError(FileUploadException exception, Map<String, Object> exceptionModel) {
        log.error("FileUploadException", exception);
        error(new StringResourceModel("FileUploadError", DefaultOpenAnswerDefinitionPanel.this, null).getObject());
        AjaxRequestTarget target = AjaxRequestTarget.get();
        if(target != null) updateFeedback(target);
      }
    });
    return audioField;
  }

  private DataField createAutoCompleteDataField(ValueMap arguments) {
    final OpenAnswerDefinitionSuggestion suggestion = new OpenAnswerDefinitionSuggestion(getOpenAnswerDefinition());
    final IAutoCompleteDataConverter converter = new IAutoCompleteDataConverter() {

      private static final long serialVersionUID = 1L;

      @Override
      public Data getModelObject(String key) {
        return DataBuilder.buildText(key);
      }

      @Override
      public String getKey(Data data) {
        return data.getValueAsString();
      }

      @Override
      public String getDisplayValue(Data data, String partial) {
        String key = getKey(data);
        String label = new QuestionnaireStringResourceModel(getOpenAnswerDefinitionModel(), key).getString();
        if(StringUtils.isNotBlank(label)) {
          if(partial != null) {
            label = label.replace(partial, "<span class='strong'>" + partial + "</span>");
          }
          return key + " : " + label;
        }
        return key;
      }

    };
    final IAutoCompleteDataProvider provider = suggestion.getSuggestionSource() == OpenAnswerDefinitionSuggestion.Source.ITEMS_LIST ? new ItemListDataProvider() : new VariableDataProvider();
    return new DataField("open", new PropertyModel<Data>(this, "data"), DataType.TEXT, provider, converter);
  }

  private DataField createDefaultDataField(ValueMap arguments, QuestionnaireStringResourceModel unitLabel) {
    Integer rows = getOpenAnswerDefinition().getInputNbRows();
    Integer columns = getOpenAnswerDefinition().getInputSize();
    DataField openField = new DataField("open", new PropertyModel<Data>(this, "data"), getOpenAnswerDefinition().getDataType(), unitLabel.getString(), columns, rows);
    if(rows != null && rows > 1 && getOpenAnswerDefinition().getDataType().equals(DataType.TEXT)) {
      add(new AttributeAppender("class", new Model<String>("open-area"), " "));
    }
    return openField;
  }

  private abstract class AbstractAutoCompleteDataProvider implements IAutoCompleteDataProvider {

    @Override
    public Iterable<Data> getChoices(final String partial) {
      return Iterables.transform(Iterables.filter(computeChoices(partial), new Predicate<String>() {
        @Override
        public boolean apply(String input) {
          return input.toLowerCase().contains(partial);
        }
      }), getFunc());
    }

    protected OpenAnswerDefinitionSuggestion getSuggestion() {
      return new OpenAnswerDefinitionSuggestion(getOpenAnswerDefinition());
    }

    private Function<String, Data> getFunc() {
      return new Function<String, Data>() {

        @Override
        public Data apply(String input) {
          return DataBuilder.buildText(input);
        }
      };
    }

    abstract Iterable<String> computeChoices(String partial);
  }

  private class ItemListDataProvider extends AbstractAutoCompleteDataProvider {

    @Override
    Iterable<String> computeChoices(String partial) {
      return getSuggestion().getSuggestionItems();
    }

  }

  private class VariableDataProvider extends AbstractAutoCompleteDataProvider {

    @Override
    Iterable<String> computeChoices(String partial) {
      return ImmutableList.of();
    }

  }

}
