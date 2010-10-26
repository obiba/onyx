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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.SimpleFormComponentLabel;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.validator.AbstractValidator;
import org.apache.wicket.validation.validator.StringValidator;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionType;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireFinder;
import org.obiba.onyx.quartz.editor.locale.LabelsPanel;
import org.obiba.onyx.quartz.editor.locale.LocaleProperties;
import org.obiba.onyx.wicket.behavior.RequiredFormFieldBehavior;
import org.obiba.onyx.wicket.reusable.FeedbackWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
@SuppressWarnings("serial")
public abstract class QuestionPanel extends Panel {

  private transient Logger logger = LoggerFactory.getLogger(getClass());

  public QuestionPanel(String id, final IModel<EditedQuestion> model, final IModel<Questionnaire> questionnaireModel, IModel<LocaleProperties> localePropertiesModel, FeedbackPanel feedbackPanel, FeedbackWindow feedbackWindow) {
    super(id, model);

    TextField<String> name = new TextField<String>("name", new PropertyModel<String>(model, "element.name"));
    name.setLabel(new ResourceModel("Name"));
    name.add(new RequiredFormFieldBehavior());
    name.add(new AbstractValidator<String>() {
      @Override
      protected void onValidate(IValidatable<String> validatable) {
        if(QuestionnaireFinder.getInstance(questionnaireModel.getObject()).findQuestion(validatable.getValue()) != null) {
          error(validatable, "QuestionAlreadyExists");
          return;
        }
      }
    });
    add(name);
    add(new SimpleFormComponentLabel("nameLabel", name));

    TextField<String> variable = new TextField<String>("variable", new PropertyModel<String>(model, "element.variableName"));
    variable.setLabel(new ResourceModel("Variable"));
    variable.add(new StringValidator.MaximumLengthValidator(20));
    add(variable);
    add(new SimpleFormComponentLabel("variableLabel", variable));

    // available choices when question type is already set
    List<QuestionType> typeChoices = null;
    QuestionType questionType = model.getObject().getQuestionType();
    if(questionType == null) {
      typeChoices = new ArrayList<QuestionType>(Arrays.asList(QuestionType.values()));
      typeChoices.remove(QuestionType.BOILER_PLATE);
    } else {
      if(questionType == QuestionType.SINGLE_OPEN_ANSWER) {
        typeChoices = new ArrayList<QuestionType>(Arrays.asList(QuestionType.SINGLE_OPEN_ANSWER));
      } else if(questionType == QuestionType.LIST_CHECKBOX || questionType == QuestionType.LIST_DROP_DOWN || questionType == QuestionType.LIST_RADIO) {
        typeChoices = new ArrayList<QuestionType>(Arrays.asList(QuestionType.LIST_CHECKBOX, QuestionType.LIST_DROP_DOWN, QuestionType.LIST_RADIO));
      } else if(questionType == QuestionType.ARRAY_CHECKBOX || questionType == QuestionType.ARRAY_RADIO) {
        typeChoices = new ArrayList<QuestionType>(Arrays.asList(QuestionType.ARRAY_CHECKBOX, QuestionType.ARRAY_RADIO));
      }
    }

    final DropDownChoice<QuestionType> type = new DropDownChoice<QuestionType>("type", new PropertyModel<QuestionType>(model, "questionType"), typeChoices, new IChoiceRenderer<QuestionType>() {
      @Override
      public Object getDisplayValue(QuestionType questionType) {
        return new StringResourceModel("QuestionType." + questionType, QuestionPanel.this, null).getString();
      }

      @Override
      public String getIdValue(QuestionType questionType, int index) {
        return questionType.name();
      }
    });
    type.setLabel(new ResourceModel("QuestionType"));
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

    // apply UI (tabs) about question type
    if(questionType != null) onQuestionTypeChange(null, questionType);

    add(type);
    add(new SimpleFormComponentLabel("typeLabel", type));

    PropertyModel<Question> questionModel = new PropertyModel<Question>(model, "element");
    logger.info("questionModel.getObject(): " + questionModel.getObject());

    add(new LabelsPanel("labels", localePropertiesModel, questionModel, feedbackPanel, feedbackWindow));
  }

  /**
   * 
   * @param target
   * @param questionType
   */
  public abstract void onQuestionTypeChange(AjaxRequestTarget target, QuestionType questionType);
}
