/***********************************************************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 **********************************************************************************************************************/
package org.obiba.onyx.quartz.core.wicket.layout.impl;

import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.FormComponentLabel;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
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
  public RadioQuestionCategoryPanel(String id, IModel questionCategoryModel, RadioGroup radioGroup) {
    this(id, new QuestionnaireModel(((QuestionCategory) questionCategoryModel.getObject()).getQuestion()), questionCategoryModel, radioGroup, true);
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
  public RadioQuestionCategoryPanel(String id, IModel questionModel, IModel questionCategoryModel, final RadioGroup radioGroup, boolean radioLabelVisible) {
    super(id, questionCategoryModel);
    this.questionModel = questionModel;

    // previous answer or default selection
    QuestionCategory questionCategory = (QuestionCategory) questionCategoryModel.getObject();
    Question question = (Question) questionModel.getObject();
    CategoryAnswer previousAnswer = activeQuestionnaireAdministrationService.findAnswer(question, questionCategory);
    // log.info("categoryAnswer.{}={}", question + "." + questionCategory, previousAnswer);
    // log.info("questionAnswer={}", previousAnswer != null ? previousAnswer.getQuestionAnswer() : null);

    Radio radio = new Radio("radio", questionCategoryModel);
    radio.setLabel(new QuestionnaireStringResourceModel(questionCategoryModel, "label"));
    // persist selection on change event
    // and make sure there is no active open field previously selected
    radio.add(new AjaxEventBehavior("onchange") {

      @Override
      protected void onEvent(AjaxRequestTarget target) {
        Question question = (Question) RadioQuestionCategoryPanel.this.questionModel.getObject();
        QuestionCategory questionCategory = (QuestionCategory) RadioQuestionCategoryPanel.this.getModelObject();

        // make the radio group active for the selection
        radioGroup.setModel(RadioQuestionCategoryPanel.this.getModel());
        if(getOpenField() != null) {
          getOpenField().setRequired(question.isRequired());
        }

        // exclusive choice, only one answer per question
        activeQuestionnaireAdministrationService.deleteAnswers(question);
        activeQuestionnaireAdministrationService.answer(question, questionCategory, questionCategory.getCategory().getOpenAnswerDefinition(), null);

        onRadioSelection(target, RadioQuestionCategoryPanel.this.questionModel, RadioQuestionCategoryPanel.this.getModel());
      }

    });

    FormComponentLabel radioLabel = new FormComponentLabel("categoryLabel", radio);
    add(radioLabel);
    radioLabel.add(radio);
    radioLabel.add(new Label("label", radio.getLabel()).setRenderBodyOnly(true).setVisible(radioLabelVisible));

    if(questionCategory.getCategory().getOpenAnswerDefinition() != null) {
      // there is an open field
      // hide the associated radio and fake selection on click event of open field
      openField = new DefaultOpenAnswerDefinitionPanel("open", questionModel, questionCategoryModel, new QuestionnaireModel(questionCategory.getCategory().getOpenAnswerDefinition())) {

        @Override
        public void onSelect(AjaxRequestTarget target, IModel questionModel, IModel questionCategoryModel) {
          Question question = (Question) questionModel.getObject();
          QuestionCategory questionCategory = (QuestionCategory) questionCategoryModel.getObject();
          log.info("openField.onSelect={}.{}", question, questionCategory);

          // make sure radio selection does not conflict with open field selection
          radioGroup.setModel(RadioQuestionCategoryPanel.this.getModel());
          getOpenField().setRequired(question.isRequired());

          // exclusive choice, only one answer per question
          CategoryAnswer previousAnswer = activeQuestionnaireAdministrationService.findAnswer(question, questionCategory);
          if(previousAnswer == null) {
            activeQuestionnaireAdministrationService.deleteAnswers(question);
            activeQuestionnaireAdministrationService.answer(question, questionCategory, questionCategory.getCategory().getOpenAnswerDefinition(), null);
          }

          onOpenFieldSelection(target, questionModel, questionCategoryModel);
        }

        @Override
        public void onSubmit(AjaxRequestTarget target, IModel questionModel, IModel questionCategoryModel) {
          onOpenFieldSubmit(target, questionModel, questionCategoryModel);
        }

      };
      openField.setRequired(false);
      add(openField);
      // make radio associated to open answer optionally visible using css styling
      radio.add(new AttributeAppender("class", new Model("radio-open"), " "));

    } else {
      // no open answer
      add(new EmptyPanel("open").setVisible(false));
    }

    // previous answer or default selection
    if(previousAnswer != null) {
      radioGroup.setModel(questionCategoryModel);
      if(openField != null) {
        openField.setRequired(questionCategory.getQuestion().isRequired());
      }
    } else if(questionCategory.isSelected()) {
      radioGroup.setModel(questionCategoryModel);
      if(openField != null) {
        openField.setRequired(questionCategory.getQuestion().isRequired());
      }
      activeQuestionnaireAdministrationService.answer(question, questionCategory, questionCategory.getCategory().getOpenAnswerDefinition(), null);
    }
  }

  /**
   * Called when open field is submitted.
   * @param target
   * @param questionModel
   * @param questionCategoryModel
   */
  public void onOpenFieldSubmit(AjaxRequestTarget target, IModel questionModel, IModel questionCategoryModel) {

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
  public void onRadioSelection(AjaxRequestTarget target, IModel questionModel, IModel questionCategoryModel) {
  }

  /**
   * Get the associated open field.
   * @return null if there is no associated {@link OpenAnswerDefinition}
   */
  public DefaultOpenAnswerDefinitionPanel getOpenField() {
    return openField;
  }

}
