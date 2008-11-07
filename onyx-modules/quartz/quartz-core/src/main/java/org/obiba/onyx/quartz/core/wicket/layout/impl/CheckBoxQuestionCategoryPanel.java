package org.obiba.onyx.quartz.core.wicket.layout.impl;

import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.CheckGroup;
import org.apache.wicket.markup.html.form.FormComponentLabel;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
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

/**
 * UI for rendering a question category as a radio and an optionally associated open answer field.
 */
public class CheckBoxQuestionCategoryPanel extends Panel {

  private static final long serialVersionUID = 1L;

  private static final Logger log = LoggerFactory.getLogger(CheckBoxQuestionCategoryPanel.class);

  @SpringBean
  private ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationService;

  private DefaultOpenAnswerDefinitionPanel openField;

  private CheckBox checkbox;

  /**
   * The question model (not necessarily the question of the category in the case of shared categories question).
   */
  private IModel questionModel;

  /**
   * Constructor, using the question of the category and making the category label visible.
   * 
   * @param id
   * @param questionCategoryModel
   */
  public CheckBoxQuestionCategoryPanel(String id, IModel questionCategoryModel, CheckGroup checkGroup) {
    this(id, new QuestionnaireModel(((QuestionCategory) questionCategoryModel.getObject()).getQuestion()), questionCategoryModel, checkGroup, true);
  }

  /**
   * Constructor.
   * 
   * @param id
   * @param questionModel
   * @param questionCategoryModel
   * @param radioLabelVisible
   */
  @SuppressWarnings("serial")
  public CheckBoxQuestionCategoryPanel(String id, IModel questionModel, IModel questionCategoryModel, CheckGroup checkGroup, boolean radioLabelVisible) {
    super(id, questionCategoryModel);
    this.questionModel = questionModel;
    setOutputMarkupId(true);

    // previous answer or default selection
    QuestionCategory questionCategory = (QuestionCategory) getModelObject();
    CategoryAnswer previousAnswer = activeQuestionnaireAdministrationService.findAnswer((Question) questionModel.getObject(), questionCategory);

    QuestionCategoryCheckBoxModel selectionModel = new QuestionCategoryCheckBoxModel(checkGroup.getModel(), questionCategoryModel);
    if(previousAnswer != null) selectionModel.select();

    checkbox = new CheckBox("checkbox", selectionModel);
    checkbox.setLabel(new QuestionnaireStringResourceModel(questionCategoryModel, "label"));
    // persist selection on change event
    // and make sure there is no active open field previously selected
    checkbox.add(new AjaxEventBehavior("onchange") {

      @Override
      protected void onEvent(AjaxRequestTarget target) {
        // toggle selection
        getSelectionModel().setObject(!getSelectionModel().isSelected());

        Question question = (Question) CheckBoxQuestionCategoryPanel.this.questionModel.getObject();
        QuestionCategory questionCategory = (QuestionCategory) CheckBoxQuestionCategoryPanel.this.getModelObject();
        if(getSelectionModel().isSelected()) {
          activeQuestionnaireAdministrationService.answer(question, questionCategory, null);
        } else {
          activeQuestionnaireAdministrationService.deleteAnswer(question, questionCategory);
        }

        onCheckBoxSelection(target, CheckBoxQuestionCategoryPanel.this.questionModel, CheckBoxQuestionCategoryPanel.this.getModel());
      }

    });

    FormComponentLabel checkboxLabel = new FormComponentLabel("categoryLabel", checkbox);
    add(checkboxLabel);
    checkboxLabel.add(checkbox);
    checkboxLabel.add(new Label("label", checkbox.getLabel()).setRenderBodyOnly(true).setVisible(radioLabelVisible));

    if(questionCategory.getCategory().getOpenAnswerDefinition() != null) {
      // there is an open field
      // hide the associated radio and fake selection on click event of open field
      openField = new DefaultOpenAnswerDefinitionPanel("open", questionModel, getModel()) {

        @Override
        public void onSelect(AjaxRequestTarget target, IModel questionModel, IModel questionCategoryModel) {
          // ignore if already selected
          if(getSelectionModel().isSelected()) return;

          // set checkbox as selected
          getSelectionModel().setObject(true);

          activeQuestionnaireAdministrationService.answer((Question) questionModel.getObject(), (QuestionCategory) getModelObject(), null);
          // target.addComponent(CheckBoxQuestionCategoryPanel.this);
          onOpenFieldSelection(target, questionModel, questionCategoryModel);
        }

      };
      add(openField);
      openField.setRequired(previousAnswer != null && questionCategory.getQuestion().isRequired());
    } else {
      // no open answer
      add(new EmptyPanel("open").setVisible(false));
    }
  }

  public QuestionCategoryCheckBoxModel getSelectionModel() {
    return (QuestionCategoryCheckBoxModel) checkbox.getModel();
  }

  /**
   * Called when open field is selected: persist the category answer with no data yet.
   * @param target
   */
  public void onOpenFieldSelection(AjaxRequestTarget target, IModel questionModel, IModel questionCategoryModel) {
  }

  /**
   * Call when radio is selected: persist the category answer.
   * @param target
   */
  public void onCheckBoxSelection(AjaxRequestTarget target, IModel questionModel, IModel questionCategoryModel) {
  }

  /**
   * Get the associated open field.
   * @return null if there is no associated {@link OpenAnswerDefinition}
   */
  public DefaultOpenAnswerDefinitionPanel getOpenField() {
    return openField;
  }

}
