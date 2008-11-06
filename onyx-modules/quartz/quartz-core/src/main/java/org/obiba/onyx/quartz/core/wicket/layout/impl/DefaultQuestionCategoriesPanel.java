package org.obiba.onyx.quartz.core.wicket.layout.impl;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.quartz.core.domain.answer.CategoryAnswer;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireModel;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireStringResourceModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultQuestionCategoriesPanel extends Panel {

  private static final long serialVersionUID = 5144933183339704600L;

  private static final Logger log = LoggerFactory.getLogger(DefaultQuestionCategoriesPanel.class);

  @SpringBean
  private ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationService;

  private DefaultOpenAnswerDefinitionPanel currentOpenField;

  public DefaultQuestionCategoriesPanel(String id, IModel questionModel) {
    super(id, questionModel);
    setOutputMarkupId(true);

    Question question = (Question) getModelObject();
    if(!question.isMultiple()) {
      addRadioGroup(question);
    } else {
      // addCheckBoxGroup(question);
    }
  }

  /**
   * Add a radio group, used by single choice question.
   * @param question
   */
  @SuppressWarnings("serial")
  private void addRadioGroup(Question question) {
    final RadioGroup radioGroup = new RadioGroup("categories", new Model());
    radioGroup.setRequired(!question.isBoilerPlate() && question.isRequired());
    add(radioGroup);

    RepeatingView repeater = new RepeatingView("category");
    radioGroup.add(repeater);

    for(QuestionCategory questionCategory : ((Question) getModelObject()).getQuestionCategories()) {
      WebMarkupContainer item = new WebMarkupContainer(repeater.newChildId());
      repeater.add(item);
      item.setModel(new QuestionnaireModel(questionCategory));

      RadioQuestionCategoryPanel radio;
      item.add(radio = new RadioQuestionCategoryPanel("input", item.getModel()) {

        @Override
        public void onOpenFieldSelection(AjaxRequestTarget target, IModel questionModel, IModel questionCategoryModel) {
          // ignore if multiple click in the same open field
          if(this.getOpenField().equals(currentOpenField)) return;

          Question question = (Question) questionModel.getObject();

          // make sure a previously selected open field is not asked for
          if(currentOpenField != null) {
            currentOpenField.setRequired(false);
          }
          // make the open field active
          currentOpenField = this.getOpenField();
          currentOpenField.setRequired(question.isRequired() ? true : false);
          // make sure radio selection does not conflict with open field selection
          radioGroup.setModel(new Model());
          radioGroup.setRequired(false);
          // update all
          target.addComponent(DefaultQuestionCategoriesPanel.this);
          // persist selection
          super.onOpenFieldSelection(target, questionModel, questionCategoryModel);
        }

        @Override
        public void onRadioSelection(AjaxRequestTarget target, IModel questionModel, IModel questionCategoryModel) {
          Question question = (Question) questionModel.getObject();
          // make the radio group active for the selection
          radioGroup.setModel(questionCategoryModel);
          radioGroup.setRequired(question.isRequired() ? true : false);
          // make inactive the previously selected open field
          if(currentOpenField != null) {
            currentOpenField.setData(null);
            currentOpenField.setRequired(false);
            target.addComponent(currentOpenField);
            currentOpenField = null;
          }
          // persist selection
          super.onRadioSelection(target, questionModel, questionCategoryModel);
        }

      });

      // previous answer or default selection
      CategoryAnswer previousAnswer = activeQuestionnaireAdministrationService.findAnswer(questionCategory);
      if(radio.getOpenField() != null) {
        if(previousAnswer != null) {
          radio.getOpenField().setRequired(question.isRequired() ? true : false);
          radioGroup.setRequired(false);
          currentOpenField = radio.getOpenField();
        } else if(questionCategory.isSelected()) {
          radio.getOpenField().setRequired(question.isRequired() ? true : false);
          activeQuestionnaireAdministrationService.answer(questionCategory, null);
        } else {
          // make sure it is not asked for as it is not selected at creation time
          radio.getOpenField().setRequired(false);
        }
      } else {
        if(previousAnswer != null) {
          radioGroup.setModel(item.getModel());
        } else if(questionCategory.isSelected()) {
          radioGroup.setModel(item.getModel());
          activeQuestionnaireAdministrationService.answer(questionCategory, null);
        }
      }
    }
    radioGroup.setLabel(new QuestionnaireStringResourceModel(question, "label"));
  }

  /**
   * Add a check box group, used by multiple choice question.
   * @param question
   */
  // @SuppressWarnings("serial")
  // private void addCheckBoxGroup(Question question) {
  // final List<IModel> checkedItems = new ArrayList<IModel>();
  //
  // RepeatingView repeater = new RepeatingView("category");
  //
  // for(final QuestionCategory questionCategory : ((Question) getModelObject()).getQuestionCategories()) {
  // WebMarkupContainer item = new WebMarkupContainer(repeater.newChildId());
  // repeater.add(item);
  //
  // final QuestionCategorySelection categorySelection = new QuestionCategorySelection(questionCategory);
  // item.setModel(new PropertyModel(categorySelection, "selection"));
  //
  // CheckBoxInput checkBoxInput = new CheckBoxInput("input", item.getModel());
  // checkBoxInput.checkbox.setLabel(new QuestionnaireStringResourceModel(questionCategory, "label"));
  //
  // FormComponentLabel checkBoxLabel = new FormComponentLabel("categoryLabel", checkBoxInput.checkbox);
  // item.add(checkBoxLabel);
  // checkBoxLabel.add(checkBoxInput);
  // checkBoxLabel.add(new Label("label", checkBoxInput.checkbox.getLabel()).setRenderBodyOnly(true));
  //
  // final DefaultOpenAnswerDefinitionPanel openField = createOpenAnswerDefinitionPanel(item, questionCategory);
  //
  // checkBoxInput.checkbox.add(new AjaxEventBehavior("onchange") {
  //
  // @Override
  // protected void onEvent(AjaxRequestTarget target) {
  // log.info("checkbox.onchange.{}.{}", questionCategory.getQuestion().getName(),
  // questionCategory.getCategory().getName());
  // if(openField != null) {
  // openField.setFieldEnabled(!openField.isFieldEnabled());
  // target.addComponent(openField);
  // }
  // // multiple choice
  // if(!categorySelection.isSelected()) {
  // activeQuestionnaireAdministrationService.deleteAnswer(questionCategory);
  // } else {
  // // TODO get the open answer
  // activeQuestionnaireAdministrationService.answer(questionCategory, null);
  // }
  // }
  //
  // });
  //
  // // previous answer or default selection
  // CategoryAnswer previousAnswer = activeQuestionnaireAdministrationService.findAnswer(questionCategory);
  // if(previousAnswer != null) {
  // checkedItems.add(item.getModel());
  // if(openField != null) {
  // openField.setFieldEnabled(true);
  // }
  // } else if(questionCategory.isSelected()) {
  // checkedItems.add(item.getModel());
  // if(openField != null) {
  // openField.setFieldEnabled(true);
  // }
  // activeQuestionnaireAdministrationService.answer(questionCategory, null);
  // }
  // }
  // ;
  //
  // CheckGroup checkGroup = new CheckGroup("categories", checkedItems);
  // add(checkGroup);
  // checkGroup.add(repeater);
  // checkGroup.setRequired(question.getQuestionCategories().size() > 0 && question.isRequired());
  // checkGroup.setLabel(new QuestionnaireStringResourceModel(question, "label"));
  // }
  /**
   * Create an open answer definition panel if given {@link QuestionCategory} has a {@link OpenAnswerDefinition}
   * associated to.
   * @param parent
   * @param questionCategory
   * @return null if no open answer definition
   */
  @SuppressWarnings("serial")
  private DefaultOpenAnswerDefinitionPanel createOpenAnswerDefinitionPanel(WebMarkupContainer parent, final QuestionCategory questionCategory) {
    DefaultOpenAnswerDefinitionPanel openField;

    if(questionCategory.getCategory().getOpenAnswerDefinition() != null) {
      openField = new DefaultOpenAnswerDefinitionPanel("open", new QuestionnaireModel(questionCategory.getQuestion()), new QuestionnaireModel(questionCategory)) {

        @Override
        public void onSelect(AjaxRequestTarget target, IModel questionModel, IModel questionCategoryModel) {

        }

      };
      // openField.setFieldEnabled(false);
      parent.add(openField);
    } else {
      openField = null;
      parent.add(new EmptyPanel("open"));
    }

    return openField;
  }

  /**
   * The checkbox input chunk.
   */
  @SuppressWarnings("serial")
  private class CheckBoxInput extends Fragment {

    CheckBox checkbox;

    public CheckBoxInput(String id, IModel model) {
      super(id, "checkboxInput", DefaultQuestionCategoriesPanel.this);
      setOutputMarkupId(true);
      add(checkbox = new CheckBox("checkbox", model));
    }

  }
}
