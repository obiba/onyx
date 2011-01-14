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

import java.util.List;

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
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.editor.behavior.tooltip.HelpTooltipPanel;
import org.obiba.onyx.quartz.editor.question.EditedQuestion;

/**
 * Class which manage min/max and required/noAnswer
 */
public class MultipleChoiceCategoryHeaderPanel extends Panel {

  private static final long serialVersionUID = 1L;

  private DropDownChoice<Category> noAnswerCategoryDropDown;

  private TextField<Integer> minCountTextField;

  private CheckBox requiredAnswer;

  private Category previousNoAnswerCategory;

  private Integer previousMinValue;

  @SuppressWarnings("serial")
  public MultipleChoiceCategoryHeaderPanel(String id, final IModel<EditedQuestion> model) {
    super(id, model);

    final Form<Question> form = new Form<Question>("form", new Model<Question>(model.getObject().getElement()));

    final Question question = model.getObject().getElement();

    requiredAnswer = new CheckBox("requiredAnswer", new Model<Boolean>(question.getMinCount() > 0));
    requiredAnswer.setLabel(new ResourceModel("RequiredAnswer"));
    form.add(requiredAnswer).add(new SimpleFormComponentLabel("requiredAnswerLabel", requiredAnswer));

    IModel<List<Category>> choices = new LoadableDetachableModel<List<Category>>() {

      private static final long serialVersionUID = 1L;

      @Override
      protected List<Category> load() {
        return question.getCategories();
      }
    };
    IModel<Category> noAnswerCategoryModel = new Model<Category>(question.getNoAnswerCategory()) {

      @Override
      public void setObject(Category category) {
        super.setObject(category);
        question.setNoAnswerCategory(category);
      }
    };
    IChoiceRenderer<Category> choicesRenderer = new ChoiceRenderer<Category>("name");
    noAnswerCategoryDropDown = new DropDownChoice<Category>("noAnswerCategoryDropDown", noAnswerCategoryModel, choices, choicesRenderer);
    noAnswerCategoryDropDown.setNullValid(true);
    noAnswerCategoryDropDown.setOutputMarkupId(true);
    noAnswerCategoryDropDown.setLabel(new ResourceModel("NoAnswer"));
    noAnswerCategoryDropDown.add(new OnChangeAjaxBehavior() {

      @Override
      protected void onUpdate(AjaxRequestTarget target) {
        // Do nothing, is it only to ajax submit when we change value in dropdown
      }

    });
    form.add(noAnswerCategoryDropDown).add(new SimpleFormComponentLabel("noAnswerLabel", noAnswerCategoryDropDown));

    form.add(new HelpTooltipPanel("noAnswerHelp", new ResourceModel("NoAnswer.Tooltip")));

    minCountTextField = new TextField<Integer>("minCountTextField", new PropertyModel<Integer>(question, "minCount"));
    final TextField<Integer> maxCountTextField = new TextField<Integer>("maxCountTextField", new PropertyModel<Integer>(question, "maxCount"));

    minCountTextField.setLabel(new ResourceModel("Min"));
    SimpleFormComponentLabel minCountLabelComponent = new SimpleFormComponentLabel("minCountLabel", minCountTextField);

    maxCountTextField.setLabel(new ResourceModel("Max"));
    SimpleFormComponentLabel maxCountLabelComponent = new SimpleFormComponentLabel("maxCountLabel", maxCountTextField);

    form.add(minCountTextField, maxCountTextField, minCountLabelComponent, maxCountLabelComponent);
    form.add(new HelpTooltipPanel("minHelp", new ResourceModel("Min.Tooltip")));
    form.add(new HelpTooltipPanel("maxHelp", new ResourceModel("Max.Tooltip")));

    requiredAnswer.add(new OnChangeAjaxBehavior() {

      private static final long serialVersionUID = 1L;

      @Override
      protected void onUpdate(AjaxRequestTarget target) {
        updateEnability();
        target.addComponent(noAnswerCategoryDropDown);
        target.addComponent(minCountTextField);
      }
    });

    form.add(new IFormValidator() {

      @Override
      public void validate(Form<?> form) {
        Integer min = minCountTextField.getConvertedInput();
        Integer max = maxCountTextField.getConvertedInput();
        if(min != null && max != null && min > max) {
          form.error(new StringResourceModel("MinInfMax", MultipleChoiceCategoryHeaderPanel.this, null).getObject());
        }
        if(requiredAnswer.getModelObject() && min != null && min <= 0) {
          form.error(new StringResourceModel("MinMustBeMoreZero", MultipleChoiceCategoryHeaderPanel.this, null).getObject());
        }
      }

      @Override
      public FormComponent<?>[] getDependentFormComponents() {
        return null;
      }
    });

    updateEnability();

    add(form);
  }

  private void updateEnability() {
    boolean required = requiredAnswer.getModelObject();
    noAnswerCategoryDropDown.setEnabled(!required);
    minCountTextField.setEnabled(required);
    if(required) {
      previousNoAnswerCategory = noAnswerCategoryDropDown.getModelObject();
      noAnswerCategoryDropDown.setModelObject(null);
      minCountTextField.setModelObject(previousMinValue);
    } else {
      previousMinValue = minCountTextField.getConvertedInput();
      minCountTextField.setModelObject(0);
      noAnswerCategoryDropDown.setModelObject(previousNoAnswerCategory);
    }
  }

  public void refreshDropDownNoAnswer(AjaxRequestTarget target) {
    target.addComponent(noAnswerCategoryDropDown);
  }
}
