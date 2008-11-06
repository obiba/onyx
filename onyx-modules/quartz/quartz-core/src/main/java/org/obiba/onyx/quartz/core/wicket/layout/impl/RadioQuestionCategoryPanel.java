package org.obiba.onyx.quartz.core.wicket.layout.impl;

import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.FormComponentLabel;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
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
public class RadioQuestionCategoryPanel extends Panel {

  private static final long serialVersionUID = 1L;

  private static final Logger log = LoggerFactory.getLogger(RadioQuestionCategoryPanel.class);

  @SpringBean
  private ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationService;

  private DefaultOpenAnswerDefinitionPanel openField;

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
  public RadioQuestionCategoryPanel(String id, IModel questionCategoryModel) {
    this(id, new QuestionnaireModel(((QuestionCategory) questionCategoryModel.getObject()).getQuestion()), questionCategoryModel, true);
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
  public RadioQuestionCategoryPanel(String id, IModel questionModel, IModel questionCategoryModel, boolean radioLabelVisible) {
    super(id, questionCategoryModel);
    this.questionModel = questionModel;

    Radio radio = new Radio("radio", questionCategoryModel);
    radio.setLabel(new QuestionnaireStringResourceModel(questionCategoryModel, "label"));

    FormComponentLabel radioLabel = new FormComponentLabel("categoryLabel", radio);
    add(radioLabel);
    radioLabel.add(radio);
    radioLabel.add(new Label("label", radio.getLabel()).setRenderBodyOnly(true).setVisible(radioLabelVisible));

    // previous answer or default selection
    final QuestionCategory questionCategory = (QuestionCategory) questionCategoryModel.getObject();

    if(questionCategory.getCategory().getOpenAnswerDefinition() != null) {
      // there is an open field
      // hide the associated radio and fake selection on click event of open field
      openField = new DefaultOpenAnswerDefinitionPanel("open", new QuestionnaireModel(questionCategory)) {

        @Override
        public void onSelect(AjaxRequestTarget target) {
          log.info("open.onclick.{}", questionCategory.getName());
          onOpenFieldSelection(target, RadioQuestionCategoryPanel.this.questionModel, RadioQuestionCategoryPanel.this.getModel());
        }

      };
      add(openField);
      radio.setVisible(false);

    } else {
      // no open answer
      add(new EmptyPanel("open").setVisible(false));
      // persist selection on change event
      // and make sure there is no active open field previously selected
      radio.add(new AjaxEventBehavior("onchange") {

        @Override
        protected void onEvent(AjaxRequestTarget target) {
          log.info("radio.onchange.{}", questionCategory.getName());
          onRadioSelection(target, RadioQuestionCategoryPanel.this.questionModel, RadioQuestionCategoryPanel.this.getModel());
        }

      });
    }
  }

  /**
   * Called when open field is selected: persist the category answer with no data yet.
   * @param target
   */
  public void onOpenFieldSelection(AjaxRequestTarget target, IModel questionModel, IModel questionCategoryModel) {
    // exclusive choice, only one answer per question
    activeQuestionnaireAdministrationService.deleteAnswers((Question) questionModel.getObject());
    activeQuestionnaireAdministrationService.answer((Question) questionModel.getObject(), (QuestionCategory) getModelObject(), null);
  }

  /**
   * Call when radio is selected: persist the category answer.
   * @param target
   */
  public void onRadioSelection(AjaxRequestTarget target, IModel questionModel, IModel questionCategoryModel) {
    // exclusive choice, only one answer per question
    activeQuestionnaireAdministrationService.deleteAnswers((Question) questionModel.getObject());
    activeQuestionnaireAdministrationService.answer((Question) questionModel.getObject(), (QuestionCategory) getModelObject(), null);
  }

  /**
   * Get the associated open field.
   * @return null if there is no associated {@link OpenAnswerDefinition}
   */
  public DefaultOpenAnswerDefinitionPanel getOpenField() {
    return openField;
  }

}
