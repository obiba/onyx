/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.question;

import static org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionType.ARRAY_CHECKBOX;
import static org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionType.ARRAY_RADIO;
import static org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionType.BOILER_PLATE;
import static org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionType.LIST_CHECKBOX;
import static org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionType.LIST_DROP_DOWN;
import static org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionType.LIST_RADIO;
import static org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionType.SINGLE_AUDIO_RECORDING;
import static org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionType.SINGLE_OPEN_ANSWER;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.SimpleFormComponentLabel;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.validator.AbstractValidator;
import org.apache.wicket.validation.validator.PatternValidator;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionType;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireFinder;
import org.obiba.onyx.quartz.editor.QuartzEditorPanel;
import org.obiba.onyx.quartz.editor.behavior.VariableNameBehavior;
import org.obiba.onyx.quartz.editor.behavior.tooltip.HelpTooltipPanel;
import org.obiba.onyx.quartz.editor.locale.LabelsPanel;
import org.obiba.onyx.quartz.editor.locale.LocaleProperties;
import org.obiba.onyx.wicket.behavior.RequiredFormFieldBehavior;
import org.obiba.onyx.wicket.reusable.FeedbackWindow;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

/**
 *
 */
@SuppressWarnings("serial")
public abstract class QuestionPanel extends Panel {

  private final VariableNameBehavior variableNameBehavior;

  private final String initialName;

  public QuestionPanel(String id, final IModel<EditedQuestion> model, final IModel<Questionnaire> questionnaireModel, IModel<LocaleProperties> localePropertiesModel, FeedbackPanel feedbackPanel, FeedbackWindow feedbackWindow, boolean useQuestionType, QuestionType... forceAllowedType) {
    super(id, model);

    final TextField<String> name = new TextField<String>("name", new PropertyModel<String>(model, "element.name"));
    name.setLabel(new ResourceModel("Name"));
    name.add(new RequiredFormFieldBehavior());
    name.add(new PatternValidator(QuartzEditorPanel.ELEMENT_NAME_PATTERN));
    final Question question = model.getObject().getElement();
    initialName = question.getName();

    name.add(new AbstractValidator<String>() {
      @Override
      protected void onValidate(final IValidatable<String> validatable) {
        if(!StringUtils.equals(initialName, validatable.getValue())) {
          QuestionnaireFinder questionnaireFinder = QuestionnaireFinder.getInstance(questionnaireModel.getObject());
          questionnaireModel.getObject().setQuestionnaireCache(null);
          Question findQuestion = questionnaireFinder.findQuestion(validatable.getValue());
          Collection<Question> sameNameQuestions = Collections2.filter(question.getQuestions(), new Predicate<Question>() {

            @Override
            public boolean apply(Question input) {
              return input.getName().equals(validatable.getValue());
            }
          });
          if(findQuestion != null && findQuestion != question || !sameNameQuestions.isEmpty()) {
            error(validatable, "QuestionAlreadyExists");
          }
        }
      }
    });
    add(name);
    add(new SimpleFormComponentLabel("nameLabel", name));
    add(new HelpTooltipPanel("nameHelp", new ResourceModel("Name.Tooltip")));

    final TextField<String> variable = new TextField<String>("variable", new PropertyModel<String>(model, "element.variableName"));
    variable.setLabel(new ResourceModel("Variable"));
    add(variable);
    add(new SimpleFormComponentLabel("variableLabel", variable));
    add(new HelpTooltipPanel("variableHelp", new ResourceModel("Variable.Tooltip")));

    add(variableNameBehavior = new VariableNameBehavior(name, variable, question.getParentQuestion(), null, null));

    // available choices when question type is already set
    List<QuestionType> typeChoices = null;
    if(useQuestionType) {
      if(forceAllowedType.length == 0) {

        QuestionType questionType = model.getObject().getQuestionType();
        if(questionType == null) {
          typeChoices = new ArrayList<QuestionType>(Arrays.asList(QuestionType.values()));
        } else {
          switch(questionType) {

          case BOILER_PLATE:
            typeChoices = new ArrayList<QuestionType>(Arrays.asList(BOILER_PLATE));
            break;

          case SINGLE_OPEN_ANSWER:
          case SINGLE_AUDIO_RECORDING:
            typeChoices = new ArrayList<QuestionType>(Arrays.asList(SINGLE_OPEN_ANSWER, LIST_CHECKBOX, LIST_DROP_DOWN, LIST_RADIO, SINGLE_AUDIO_RECORDING));
            break;

          case LIST_CHECKBOX:
          case LIST_DROP_DOWN:
          case LIST_RADIO:
            List<QuestionType> asList = null;
            if(Questionnaire.SIMPLIFIED_UI.equals(questionnaireModel.getObject().getUiType())) {
              asList = Arrays.asList(LIST_CHECKBOX, LIST_RADIO);
            } else {
              asList = Arrays.asList(LIST_CHECKBOX, LIST_DROP_DOWN, LIST_RADIO);
            }
            typeChoices = new ArrayList<QuestionType>(asList);
            break;

          case ARRAY_CHECKBOX:
          case ARRAY_RADIO:
            typeChoices = new ArrayList<QuestionType>(Arrays.asList(ARRAY_CHECKBOX, ARRAY_RADIO));
            break;

          }
        }

      } else {
        typeChoices = new ArrayList<QuestionType>(Arrays.asList(forceAllowedType));
      }
    }

    final DropDownChoice<QuestionType> type = new DropDownChoice<QuestionType>("type", new PropertyModel<QuestionType>(model, "questionType"), typeChoices, new IChoiceRenderer<QuestionType>() {
      @Override
      public Object getDisplayValue(QuestionType type1) {
        return new StringResourceModel("QuestionType." + type1, QuestionPanel.this, null).getString();
      }

      @Override
      public String getIdValue(QuestionType type1, int index) {
        return type1.name();
      }
    });
    type.setLabel(new ResourceModel("QuestionType"));
    type.add(new RequiredFormFieldBehavior());
    // submit the whole form instead of just the questionType component
    type.add(new AjaxFormSubmitBehavior("onchange") {
      @Override
      protected void onSubmit(AjaxRequestTarget target) {
        String value = type.getValue(); // use value because model is not set if validation error
        if(value != null) onQuestionTypeChange(target, QuestionType.valueOf(value));
      }

      @Override
      protected void onError(AjaxRequestTarget target) {
        Session.get().getFeedbackMessages().clear(); // we don't want to validate fields now
        onSubmit(target);
      }
    });

    WebMarkupContainer typeContainer = new WebMarkupContainer("typeContainer");
    typeContainer.setVisible(useQuestionType);
    add(typeContainer);

    typeContainer.add(type);
    typeContainer.add(new SimpleFormComponentLabel("typeLabel", type));
    typeContainer.add(new HelpTooltipPanel("typeHelp", new ResourceModel("QuestionType.Tooltip")));

    add(new HelpTooltipPanel("labelsHelp", new Model<String>(new StringResourceModel("LanguagesProperties.Tooltip", this, null).getString() + "<br /><img align=\"center\" src=\"" + RequestCycle.get().urlFor(new ResourceReference(QuestionPanel.class, "labels-with-help.png")) + "\" />")));

    Map<String, IModel<String>> labelsTooltips = new HashMap<String, IModel<String>>();
    for(String key : questionnaireModel.getObject().getPropertyKeyProvider().getProperties(new Question())) {
      labelsTooltips.put(key, new ResourceModel("Question.Tooltip." + key));
    }
    add(new LabelsPanel("labels", localePropertiesModel, new PropertyModel<Question>(model, "element"), feedbackPanel, feedbackWindow, question.getParentQuestion() != null, labelsTooltips));
  }

  /**
   * 
   * @param target
   */
  public void onSave(AjaxRequestTarget target) {
    EditedQuestion editedQuestion = (EditedQuestion) getDefaultModelObject();
    Question question = editedQuestion.getElement();
    if(!variableNameBehavior.isVariableNameDefined()) {
      question.setVariableName(null);
    }
    // update keys of variables names
    if(!question.getName().equals(initialName)) {
      for(Category category : question.getCategories()) {
        updateVariableNameKeys(category.getVariableNames(), question.getName());
        Map<String, OpenAnswerDefinition> openAnswerDefinitionsByName = category.getOpenAnswerDefinitionsByName();
        for(OpenAnswerDefinition oad : openAnswerDefinitionsByName.values()) {
          updateVariableNameKeys(oad.getVariableNames(), question.getName());
        }
      }
    }
    // update category name for single open answer
    if(editedQuestion.getQuestionType() == SINGLE_OPEN_ANSWER || editedQuestion.getQuestionType() == SINGLE_AUDIO_RECORDING) {
      List<Category> categories = question.getCategories();
      if(!categories.isEmpty() && categories.size() == 1) {
        categories.get(0).setName(question.getName());
      }
    }
  }

  private void updateVariableNameKeys(Map<String, String> variableNames, String newKey) {
    if(variableNames.containsKey(initialName)) {
      String value = variableNames.get(initialName);
      if(StringUtils.isNotBlank(value)) {
        variableNames.put(newKey, value);
        variableNames.remove(initialName);
      }
    }
  }

  /**
   * 
   * @param target
   * @param questionType
   */
  public abstract void onQuestionTypeChange(AjaxRequestTarget target, QuestionType questionType);

}
