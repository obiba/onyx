/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.openAnswer.autocomplete;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormChoiceComponentUpdatingBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.form.SimpleFormComponentLabel;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.validator.AbstractValidator;
import org.apache.wicket.validation.validator.PatternValidator;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinitionSuggestion;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireFinder;
import org.obiba.onyx.quartz.editor.QuartzEditorPanel;
import org.obiba.onyx.quartz.editor.behavior.VariableNameBehavior;
import org.obiba.onyx.quartz.editor.behavior.tooltip.HelpTooltipPanel;
import org.obiba.onyx.quartz.editor.locale.LabelsPanel;
import org.obiba.onyx.quartz.editor.locale.LocaleProperties;
import org.obiba.onyx.quartz.editor.locale.LocalePropertiesUtils;
import org.obiba.onyx.quartz.editor.utils.MapModel;
import org.obiba.onyx.quartz.editor.utils.SaveablePanel;
import org.obiba.onyx.wicket.behavior.RequiredFormFieldBehavior;
import org.obiba.onyx.wicket.reusable.FeedbackWindow;

import static org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinitionSuggestion.Source;

/**
 *
 */
public class AutoCompleteOpenAnswerPanel extends Panel implements SaveablePanel {

//  private final transient Logger logger = LoggerFactory.getLogger(getClass());

  @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "SE_BAD_FIELD",
      justification = "Need to be be re-initialized upon deserialization")
  @SpringBean
  private LocalePropertiesUtils localePropertiesUtils;

  private final VariableNameBehavior variableNameBehavior;

  private TextField<String> variable;

  private String initialName;

  private TextField<String> sizeField;

  private final FeedbackPanel feedbackPanel;

  private final FeedbackWindow feedbackWindow;

  private final IModel<LocaleProperties> localePropertiesModel;

  private final IModel<Questionnaire> questionnaireModel;

  private final IModel<Question> questionModel;

  private LabelsPanel labelsPanel;

  public AutoCompleteOpenAnswerPanel(String id, IModel<OpenAnswerDefinition> model,
      IModel<Category> categoryModel, IModel<Question> questionModel,
      final IModel<Questionnaire> questionnaireModel, IModel<LocaleProperties> localePropertiesModel,
      FeedbackPanel feedbackPanel, FeedbackWindow feedbackWindow) {
    super(id, model);

    this.questionModel = questionModel;
    this.questionnaireModel = questionnaireModel;
    this.localePropertiesModel = localePropertiesModel;
    this.feedbackPanel = feedbackPanel;
    this.feedbackWindow = feedbackWindow;

    final Question question = questionModel.getObject();
    final Category category = categoryModel.getObject();
    final OpenAnswerDefinition openAnswer = model.getObject();

    initialName = model.getObject().getName();
    final TextField<String> name = new TextField<String>("name", new PropertyModel<String>(model, "name"));
    name.setLabel(new ResourceModel("Name"));
    name.add(new RequiredFormFieldBehavior());
    name.add(new PatternValidator(QuartzEditorPanel.ELEMENT_NAME_PATTERN));
    name.add(new AbstractValidator<String>() {
      @Override
      protected void onValidate(IValidatable<String> validatable) {
        if(!StringUtils.equals(initialName, validatable.getValue())) {
          boolean alreadyContains = false;
          if(category != null) {
            Map<String, OpenAnswerDefinition> openAnswerDefinitionsByName = category.getOpenAnswerDefinitionsByName();
            alreadyContains = (openAnswerDefinitionsByName
                .containsKey(validatable.getValue()) && openAnswerDefinitionsByName
                .get(validatable.getValue()) != openAnswer);
          }
          QuestionnaireFinder questionnaireFinder = QuestionnaireFinder.getInstance(questionnaireModel.getObject());
          questionnaireModel.getObject().setQuestionnaireCache(null);
          OpenAnswerDefinition findOpenAnswerDefinition = questionnaireFinder
              .findOpenAnswerDefinition(validatable.getValue());
          if(alreadyContains || findOpenAnswerDefinition != null && findOpenAnswerDefinition != openAnswer) {
            error(validatable, "OpenAnswerAlreadyExists");
          }
        }
      }
    });
    add(name).add(new SimpleFormComponentLabel("nameLabel", name));
    add(new HelpTooltipPanel("nameHelp", new ResourceModel("Name.Tooltip")));

    variable = new TextField<String>("variable",
        new MapModel<String>(new PropertyModel<Map<String, String>>(model, "variableNames"), question.getName()));
    variable.setLabel(new ResourceModel("Variable"));
    add(variable).add(new SimpleFormComponentLabel("variableLabel", variable));
    add(new HelpTooltipPanel("variableHelp", new ResourceModel("Variable.Tooltip")));

    if(category == null) {
      variableNameBehavior = new VariableNameBehavior(name, variable, question.getParentQuestion(), question, null) {
        @Override
        @SuppressWarnings("hiding")
        protected String generateVariableName(Question parentQuestion, Question question, Category category,
            String name) {
          if(StringUtils.isBlank(name)) return "";
          if(category != null) {
            return super.generateVariableName(parentQuestion, question, category, name);
          }
          String variableName = (parentQuestion == null ? "" : parentQuestion.getName() + ".");
          if(question != null) {
            variableName += question.getName() + "." + question.getName() + ".";
          }
          return variableName + StringUtils.trimToEmpty(name);
        }
      };
    } else {
      variableNameBehavior = new VariableNameBehavior(name, variable, question.getParentQuestion(), question, category);
    }

    add(variableNameBehavior);

    TextField<String> unit = new TextField<String>("unit", new PropertyModel<String>(model, "unit"));
    unit.setLabel(new ResourceModel("Unit"));
    add(unit).add(new SimpleFormComponentLabel("unitLabel", unit));
    add(new HelpTooltipPanel("unitHelp", new ResourceModel("Unit.Tooltip")));

    PatternValidator numericPatternValidator = new PatternValidator("\\d*");
    // ui Arguments
    Integer size = openAnswer.getInputSize();
    sizeField = new TextField<String>("size", new Model<String>(size == null ? null : String.valueOf(size)));
    sizeField.add(numericPatternValidator);
    sizeField.setLabel(new ResourceModel("SizeLabel"));
    add(new SimpleFormComponentLabel("sizeLabel", sizeField));
    add(sizeField);

    OpenAnswerDefinitionSuggestion openAnswerSuggestion = new OpenAnswerDefinitionSuggestion(openAnswer);
    Source source = openAnswerSuggestion.getSuggestionSource();

    final WebMarkupContainer sourceConfigContainer = new WebMarkupContainer("sourceConfigContainer");
    sourceConfigContainer.setOutputMarkupId(true);
    add(sourceConfigContainer);
    sourceConfigContainer.add(showSuggestionConfig(source));

    final RadioGroup<OpenAnswerDefinitionSuggestion.Source> radioSource = new RadioGroup<OpenAnswerDefinitionSuggestion.Source>(
        "source",
        new Model<OpenAnswerDefinitionSuggestion.Source>(source));
    radioSource.setLabel(new ResourceModel("SourceOfSuggestions"));
    radioSource.add(new AjaxFormChoiceComponentUpdatingBehavior() {
      @Override
      protected void onUpdate(AjaxRequestTarget target) {
        sourceConfigContainer.addOrReplace(showSuggestionConfig(radioSource.getModelObject()));
        target.addComponent(sourceConfigContainer);
      }
    });
    add(radioSource);

    Radio<OpenAnswerDefinitionSuggestion.Source> sourceItems = new Radio<OpenAnswerDefinitionSuggestion.Source>(
        "sourceItems", new Model<OpenAnswerDefinitionSuggestion.Source>(
        Source.ITEMS_LIST));
    sourceItems.setLabel(new ResourceModel("listOfItems"));
    radioSource.add(sourceItems);
    radioSource.add(new SimpleFormComponentLabel("sourceItemsLabel", sourceItems));

    Radio<OpenAnswerDefinitionSuggestion.Source> sourceVariable = new Radio<OpenAnswerDefinitionSuggestion.Source>(
        "sourceVariable",
        new Model<OpenAnswerDefinitionSuggestion.Source>(Source.VARIABLE_VALUES));
    sourceVariable.setLabel(new ResourceModel("variableValues"));
    radioSource.add(sourceVariable);
    radioSource.add(new SimpleFormComponentLabel("sourceVariableLabel", sourceVariable));

    localePropertiesUtils.load(localePropertiesModel.getObject(), questionnaireModel.getObject(), openAnswer);
    add(labelsPanel = new LabelsPanel("labels", localePropertiesModel, model, feedbackPanel, feedbackWindow));

    CheckBox requiredCheckBox = new CheckBox("required", new PropertyModel<Boolean>(model, "required"));
    requiredCheckBox.setLabel(new ResourceModel("AnswerRequired"));
    add(requiredCheckBox);
    add(new SimpleFormComponentLabel("requiredLabel", requiredCheckBox));

  }

  @SuppressWarnings("unchecked")
  private Panel showSuggestionConfig(Source source) {
    if(source == null) return new EmptyPanel("sourceConfig");
    switch(source) {
      case ITEMS_LIST:
        return new SuggestionItemListPanel("sourceConfig", (IModel<OpenAnswerDefinition>) getDefaultModel(),
            questionnaireModel, feedbackPanel, feedbackWindow);
      case VARIABLE_VALUES:
        return new SuggestionVariableValuesPanel("sourceConfig", (IModel<OpenAnswerDefinition>) getDefaultModel(),
            questionnaireModel, feedbackPanel, feedbackWindow);
      default:
        return new EmptyPanel("sourceConfig");
    }
  }

  @Override
  public void onSave(AjaxRequestTarget target) {
    if(!variableNameBehavior.isVariableNameDefined()) {
      variable.setModelObject(null);
    }
  }
}
