/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.category;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.BooleanUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.SimpleFormComponentLabel;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.validation.IFormValidator;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionType;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireSharedCategory;
import org.obiba.onyx.quartz.editor.behavior.tooltip.HelpTooltipPanel;
import org.obiba.onyx.quartz.editor.question.EditedQuestion;

/**
 * Class which manage min/max and required/noAnswer
 */
public class MultipleChoiceCategoryHeaderPanel extends Panel {

  private static final long serialVersionUID = 1L;

  private DropDownChoice<QuestionCategory> noAnswerCategoryDropDown;

  private TextField<Integer> minCountTextField;

  private TextField<Integer> maxCountTextField;

  private CheckBox requiredAnswer;

  private QuestionCategory previousOrFirstNoAnswerQuestionCategory;

  private Integer previousOrFirstMinValue;

  private IModel<Questionnaire> questionnaireModel;

  private IModel<QuestionCategory> noAnswerCategoryModel;

  private EditedQuestion editedQuestion;

  private Question question;

  private LoadableDetachableModel<List<QuestionCategory>> choices;

  @SuppressWarnings("serial")
  public MultipleChoiceCategoryHeaderPanel(String id, final IModel<Questionnaire> questionnaireModel, final IModel<EditedQuestion> model) {
    super(id, model);
    setOutputMarkupId(true);
    this.questionnaireModel = questionnaireModel;

    final Form<Question> form = new Form<Question>("form", new Model<Question>(model.getObject().getElement()));

    editedQuestion = model.getObject();
    question = model.getObject().getElement();

    choices = new LoadableDetachableModel<List<QuestionCategory>>() {

      private static final long serialVersionUID = 1L;

      @Override
      protected List<QuestionCategory> load() {
        List<QuestionCategory> missingQuestionCategories = question.getMissingQuestionCategories();
        List<QuestionCategory> correctQuestionCategories = new ArrayList<QuestionCategory>();
        for(QuestionCategory questionCategory : missingQuestionCategories) {
          boolean noAnswer = questionCategory.getCategory().isNoAnswer();
          boolean sharedIfLink = QuestionnaireSharedCategory.isSharedIfLink(questionCategory, questionnaireModel.getObject());
          if(!sharedIfLink || noAnswer) {
            correctQuestionCategories.add(questionCategory);
            if(sharedIfLink && noAnswer) {
              correctQuestionCategories.clear();
              correctQuestionCategories.add(questionCategory);
              return correctQuestionCategories;
            }
          }
        }
        return correctQuestionCategories;
      }
    };

    requiredAnswer = new CheckBox("requiredAnswer", new Model<Boolean>());

    requiredAnswer.setLabel(new ResourceModel("RequiredAnswer"));
    form.add(requiredAnswer).add(new SimpleFormComponentLabel("requiredAnswerLabel", requiredAnswer));

    noAnswerCategoryModel = new Model<QuestionCategory>() {

      public QuestionCategory getObject() {
        return question.getNoAnswerQuestionCategory();
      }

      @Override
      public void setObject(QuestionCategory questionCategory) {
        super.setObject(questionCategory);
        question.setNoAnswerCategory(questionCategory == null ? null : questionCategory.getCategory());
      }
    };
    IChoiceRenderer<QuestionCategory> choicesRenderer = new ChoiceRenderer<QuestionCategory>("category.name");
    noAnswerCategoryDropDown = new DropDownChoice<QuestionCategory>("noAnswerCategoryDropDown", noAnswerCategoryModel, choices, choicesRenderer);
    noAnswerCategoryDropDown.setNullValid(true);
    noAnswerCategoryDropDown.setLabel(new ResourceModel("NoAnswer"));
    noAnswerCategoryDropDown.add(new OnChangeAjaxBehavior() {

      @Override
      protected void onUpdate(AjaxRequestTarget target) {
        // Do nothing, it is only to ajax update model when we change value in dropdown
      }

    });

    form.add(noAnswerCategoryDropDown).add(new SimpleFormComponentLabel("noAnswerLabel", noAnswerCategoryDropDown));

    form.add(new HelpTooltipPanel("noAnswerHelp", new ResourceModel("NoAnswer.Tooltip")));

    minCountTextField = new TextField<Integer>("minCountTextField", new PropertyModel<Integer>(question, "minCount"));
    previousOrFirstMinValue = minCountTextField.getModelObject();
    minCountTextField.setLabel(new ResourceModel("Min"));
    minCountTextField.add(new OnChangeAjaxBehavior() {

      @Override
      protected void onUpdate(AjaxRequestTarget target) {
        // Do nothing, it is only to ajax update model when we change value
      }
    });
    SimpleFormComponentLabel minCountLabelComponent = new SimpleFormComponentLabel("minCountLabel", minCountTextField);

    maxCountTextField = new TextField<Integer>("maxCountTextField", new PropertyModel<Integer>(question, "maxCount"));
    maxCountTextField.setLabel(new ResourceModel("Max"));
    SimpleFormComponentLabel maxCountLabelComponent = new SimpleFormComponentLabel("maxCountLabel", maxCountTextField);

    form.add(minCountTextField, maxCountTextField, minCountLabelComponent, maxCountLabelComponent);
    form.add(new HelpTooltipPanel("minHelp", new ResourceModel("Min.Tooltip")));
    form.add(new HelpTooltipPanel("maxHelp", new ResourceModel("Max.Tooltip")));

    requiredAnswer.add(new OnChangeAjaxBehavior() {

      private static final long serialVersionUID = 1L;

      @Override
      protected void onUpdate(AjaxRequestTarget target) {
        clickRequired(target);
      }
    });

    form.add(new IFormValidator() {

      @Override
      public void validate(@SuppressWarnings("hiding") Form<?> form) {
        Integer min = minCountTextField.getConvertedInput();
        Integer max = maxCountTextField.getConvertedInput();
        if(min != null && max != null && min > max) {
          form.error(new StringResourceModel("MinInfMax", MultipleChoiceCategoryHeaderPanel.this, null).getObject());
        }
        if(BooleanUtils.isTrue(requiredAnswer.getModelObject()) && min != null && min <= 0) {
          form.error(new StringResourceModel("MinMustBeMoreZero", MultipleChoiceCategoryHeaderPanel.this, null).getObject());
        }
      }

      @Override
      public FormComponent<?>[] getDependentFormComponents() {
        return null;
      }
    });
    add(form);
  }

  /**
   * @return
   */
  private boolean isSingleChoice() {
    return editedQuestion.getQuestionType() == QuestionType.LIST_RADIO || editedQuestion.getQuestionType() == QuestionType.LIST_DROP_DOWN || editedQuestion.getQuestionType() == QuestionType.ARRAY_RADIO;
  }

  private void clickRequired(AjaxRequestTarget target) {
    boolean required = requiredAnswer.getModelObject();
    noAnswerCategoryDropDown.setEnabled(!required);
    minCountTextField.setEnabled(required);
    if(required) {
      previousOrFirstNoAnswerQuestionCategory = noAnswerCategoryDropDown.getModelObject();
      noAnswerCategoryDropDown.setModelObject(null);
      minCountTextField.setModelObject(previousOrFirstMinValue);
    } else {
      previousOrFirstMinValue = minCountTextField.getModelObject();
      minCountTextField.setModelObject(null);
      noAnswerCategoryDropDown.setModelObject(previousOrFirstNoAnswerQuestionCategory);
    }
    if(isSingleChoice()) {
      minCountTextField.setModelObject(BooleanUtils.isTrue(requiredAnswer.getModelObject()) ? 1 : null);
      minCountTextField.setEnabled(false);
    }

    target.addComponent(noAnswerCategoryDropDown);
    target.addComponent(minCountTextField);
  }

  @Override
  protected void onBeforeRender() {
    super.onBeforeRender();
    QuestionCategory noAnswerQuestionCategory = null;
    List<QuestionCategory> choiceList = choices.getObject();
    if(choiceList.size() == 1 && QuestionnaireSharedCategory.isSharedIfLink(choiceList.iterator().next(), questionnaireModel.getObject())) {
      noAnswerQuestionCategory = choiceList.iterator().next();
    } else {
      noAnswerQuestionCategory = noAnswerCategoryModel.getObject();
    }
    if(noAnswerQuestionCategory != null && QuestionnaireSharedCategory.isSharedIfLink(noAnswerQuestionCategory, questionnaireModel.getObject())) {
      noAnswerCategoryDropDown.setNullValid(false);
      requiredAnswer.setEnabled(false);
      minCountTextField.setEnabled(false);
      noAnswerCategoryDropDown.setModelObject(noAnswerQuestionCategory);
      minCountTextField.setModelObject(null);
    } else {
      requiredAnswer.setEnabled(true);
      boolean minSupZero = question.getMinCount() == null ? false : question.getMinCount() > 0;
      noAnswerCategoryDropDown.setEnabled(!minSupZero);
      noAnswerCategoryDropDown.setNullValid(true);
      minCountTextField.setEnabled(minSupZero);
      noAnswerCategoryDropDown.setModelObject(noAnswerQuestionCategory);
      requiredAnswer.setModelObject(minSupZero);
    }
    if(isSingleChoice()) {
      minCountTextField.setModelObject(BooleanUtils.isTrue(requiredAnswer.getModelObject()) ? 1 : null);
      minCountTextField.setEnabled(false);
      maxCountTextField.setModelObject(1);
      maxCountTextField.setEnabled(false);
    } else {
      maxCountTextField.setEnabled(true);
    }
  }
}
