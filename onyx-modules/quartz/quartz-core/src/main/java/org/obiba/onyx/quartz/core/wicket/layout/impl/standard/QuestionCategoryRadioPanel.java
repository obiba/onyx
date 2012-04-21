/***********************************************************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 **********************************************************************************************************************/
package org.obiba.onyx.quartz.core.wicket.layout.impl.standard;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.FormComponentLabel;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.quartz.core.domain.answer.CategoryAnswer;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;
import org.obiba.onyx.quartz.core.wicket.layout.impl.AbstractOpenAnswerDefinitionPanel;
import org.obiba.onyx.quartz.core.wicket.layout.impl.AbstractQuestionCategorySelectionPanel;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireModel;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireStringResourceModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * UI for rendering a question category as a radio and an optionally associated open answer field.
 */
public class QuestionCategoryRadioPanel extends AbstractQuestionCategorySelectionPanel {

  private static final long serialVersionUID = 1L;

  private static final Logger log = LoggerFactory.getLogger(QuestionCategoryRadioPanel.class);

  @SpringBean
  private ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationService;

  private AbstractOpenAnswerDefinitionPanel openField;

  private RadioGroup<QuestionCategory> radioGroup;

  /**
   * Constructor, using the question of the category and making the category label visible.
   * 
   * @param id
   * @param questionCategoryModel
   */
  public QuestionCategoryRadioPanel(String id, IModel<QuestionCategory> questionCategoryModel, RadioGroup<QuestionCategory> radioGroup) {
    this(id, new QuestionnaireModel<Question>(questionCategoryModel.getObject().getQuestion()), questionCategoryModel, radioGroup, true);
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
  public QuestionCategoryRadioPanel(String id, IModel<Question> questionModel, IModel<QuestionCategory> questionCategoryModel, RadioGroup<QuestionCategory> radioGroup, boolean radioLabelVisible) {
    super(id, questionModel, questionCategoryModel);
    this.radioGroup = radioGroup;

    // previous answer or default selection
    QuestionCategory questionCategory = (QuestionCategory) questionCategoryModel.getObject();
    Question question = (Question) questionModel.getObject();

    Radio<QuestionCategory> radio = new Radio<QuestionCategory>("radio", questionCategoryModel);
    radio.setLabel(new QuestionnaireStringResourceModel(questionCategoryModel, "label"));
    // persist selection on change event
    // and make sure there is no active open field previously selected
    if(!activeQuestionnaireAdministrationService.isQuestionnaireDevelopmentMode()) {
      radio.add(new AjaxEventBehavior("onchange") {

        @Override
        protected void onEvent(AjaxRequestTarget target) {

          // make the radio group active for the selection
          QuestionCategoryRadioPanel.this.radioGroup.setModel(getQuestionCategoryModel());

          // exclusive choice, only one answer per question
          activeQuestionnaireAdministrationService.deleteAnswers(getQuestion());
          activeQuestionnaireAdministrationService.answer(getQuestion(), getQuestionCategory());

          // make sure a previously selected open field is not asked for
          resetOpenAnswerDefinitionPanels(target, QuestionCategoryRadioPanel.this.radioGroup, getQuestionCategoryModel());

          updateFeedbackPanel(target);

          fireQuestionCategorySelection(target, getQuestionModel(), getQuestionCategoryModel(), true);
        }

      });
    }

    FormComponentLabel radioLabel = new FormComponentLabel("categoryLabel", radio);
    add(radioLabel);
    radioLabel.add(radio);
    radioLabel.add(new Label("label", radio.getLabel()).setRenderBodyOnly(true).setVisible(radioLabelVisible).setEscapeModelStrings(false));

    if(questionCategory.getCategory().getOpenAnswerDefinition() != null) {
      // there is an open field
      openField = newOpenAnswerDefinitionPanel("open");
      add(openField);

      // make radio associated to open answer optionally visible using css styling
      radio.add(new AttributeAppender("class", new Model<String>("radio-open"), " "));
      radioLabel.add(new AttributeModifier("class", new Model<String>("label-open")));

    } else {
      // no open answer
      add(new EmptyPanel("open").setVisible(false));
    }

    // previous answer or default selection
    if(!activeQuestionnaireAdministrationService.isQuestionnaireDevelopmentMode()) {
      CategoryAnswer previousAnswer = activeQuestionnaireAdministrationService.findAnswer(question, questionCategory);

      if(previousAnswer != null) {
        radioGroup.setModel(questionCategoryModel);
      }
    }

  }

  /**
   * Get the associated open field.
   * @return null if there is no associated {@link OpenAnswerDefinition}
   */
  public AbstractOpenAnswerDefinitionPanel getOpenField() {
    return openField;
  }

  @Override
  public boolean hasOpenField() {
    return openField != null;
  }

  @Override
  public void onQuestionCategorySelection(AjaxRequestTarget target, IModel<Question> questionModel, IModel<QuestionCategory> questionCategoryModel, boolean isSelected) {
    log.debug("onQuestionCategorySelection={}:{}", questionModel.getObject(), questionCategoryModel.getObject());

    if(!radioGroup.getModel().equals(questionCategoryModel)) {

      // make sure radio selection does not conflict with open field selection
      radioGroup.setModel(questionCategoryModel);

      // exclusive choice
      Category category = ((QuestionCategory) questionCategoryModel.getObject()).getCategory();
      for(CategoryAnswer categoryAnswer : activeQuestionnaireAdministrationService.findActiveAnswers((Question) questionModel.getObject())) {
        if(!categoryAnswer.getCategoryName().equals(category.getName())) {
          activeQuestionnaireAdministrationService.deleteAnswers(categoryAnswer);
        }
      }

      // make sure a previously selected open field is not asked for
      resetOpenAnswerDefinitionPanels(target, QuestionCategoryRadioPanel.this.radioGroup, questionCategoryModel);
    }

    // warn parent
    fireQuestionCategorySelection(target, questionModel, questionCategoryModel, isSelected);
  }

}
