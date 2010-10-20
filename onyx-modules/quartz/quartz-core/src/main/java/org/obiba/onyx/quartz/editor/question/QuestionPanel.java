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

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.SimpleFormComponentLabel;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.validator.AbstractValidator;
import org.apache.wicket.validation.validator.StringValidator;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.IHasQuestion;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionType;
import org.obiba.onyx.wicket.behavior.RequiredFormFieldBehavior;

/**
 *
 */
@SuppressWarnings("serial")
public class QuestionPanel extends Panel {

  public QuestionPanel(String id, final IModel<EditedQuestion> model, final IModel<IHasQuestion> parentModel) {
    super(id, model);

    TextField<String> name = new TextField<String>("name", new PropertyModel<String>(model, "element.name"));
    name.setLabel(new ResourceModel("Name"));
    name.add(new RequiredFormFieldBehavior());
    name.add(new AbstractValidator<String>() {
      @Override
      protected void onValidate(IValidatable<String> validatable) {
        Question question = model.getObject().getElement();
        for(Question q : parentModel.getObject().getQuestions()) {
          if(question != q && q.getName().equalsIgnoreCase(validatable.getValue())) {
            error(validatable, "QuestionAlreadyExists");
            return;
          }
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

    List<QuestionType> typeChoices = new ArrayList<QuestionType>(Arrays.asList(QuestionType.values()));
    typeChoices.remove(QuestionType.BOILER_PLATE);
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
    type.add(new AjaxFormComponentUpdatingBehavior("onchange") {

      @Override
      protected void onUpdate(AjaxRequestTarget target) {
        onQuestionTypeChange(target, type.getModelObject());
      }
    });
    add(type);
    add(new SimpleFormComponentLabel("typeLabel", type));

    // form.add(new LocalesPropertiesAjaxTabbedPanel("localesPropertiesTabs", new
    // PropertyModel<Question>(form.getModel(), "element"), localePropertiesModel));
  }

  /**
   * 
   * @param target
   * @param questionType
   */
  public void onQuestionTypeChange(AjaxRequestTarget target, QuestionType questionType) {

  }

}
