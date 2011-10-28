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
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.FormComponentLabel;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.quartz.core.domain.answer.CategoryAnswer;
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
public class QuestionCategoryCheckBoxPanel extends AbstractQuestionCategorySelectionPanel {

  private static final long serialVersionUID = 1L;

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(QuestionCategoryCheckBoxPanel.class);

  @SpringBean
  private ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationService;

  private AbstractOpenAnswerDefinitionPanel openField;

  private CheckBox checkbox;

  /**
   * Constructor, using the question of the category and making the category label visible.
   * 
   * @param id
   * @param questionCategoryModel
   * @param selectionsModel check group selections model
   */
  public QuestionCategoryCheckBoxPanel(String id, IModel<QuestionCategory> questionCategoryModel, IModel selectionsModel) {
    this(id, new QuestionnaireModel(questionCategoryModel.getObject().getQuestion()), questionCategoryModel, selectionsModel, true);
  }

  /**
   * Constructor.
   * 
   * @param id
   * @param questionModel
   * @param questionCategoryModel
   * @param selectionsModel check group selections model
   * @param radioLabelVisible
   */
  @SuppressWarnings("serial")
  public QuestionCategoryCheckBoxPanel(String id, IModel<Question> questionModel, IModel<QuestionCategory> questionCategoryModel, IModel selectionsModel, boolean radioLabelVisible) {
    super(id, questionModel, questionCategoryModel);
    setOutputMarkupId(true);

    // previous answer or default selection
    QuestionCategory questionCategory = (QuestionCategory) getDefaultModelObject();
    QuestionCategoryCheckBoxModel selectionModel = new QuestionCategoryCheckBoxModel(selectionsModel, questionCategoryModel);

    if(!activeQuestionnaireAdministrationService.isQuestionnaireDevelopmentMode()) {
      CategoryAnswer previousAnswer = activeQuestionnaireAdministrationService.findAnswer((Question) questionModel.getObject(), questionCategory);
      if(previousAnswer != null) selectionModel.select();
    }

    checkbox = new CheckBox("checkbox", selectionModel);
    checkbox.setLabel(new QuestionnaireStringResourceModel(questionCategoryModel, "label"));

    // persist selection on change event
    // and make sure there is no active open field previously selected
    if(!activeQuestionnaireAdministrationService.isQuestionnaireDevelopmentMode()) {
      checkbox.add(new AjaxEventBehavior("onchange") {

        @Override
        protected void onEvent(AjaxRequestTarget target) {
          // toggle selection
          // note: call for setModelObject to ensure modelChanged trigger is properly called
          checkbox.setModelObject(!getSelectionModel().isSelected());

          if(getSelectionModel().isSelected()) {
            activeQuestionnaireAdministrationService.answer(getQuestion(), getQuestionCategory(), getQuestionCategory().getCategory().getOpenAnswerDefinition(), null);
          } else {
            activeQuestionnaireAdministrationService.deleteAnswer(getQuestion(), getQuestionCategory());
          }
          if(getOpenField() != null) {
            if(!getSelectionModel().isSelected()) {
              resetOpenAnswerDefinitionPanels(target, getOpenField(), QuestionCategoryCheckBoxPanel.this.getDefaultModel());
              updateFeedbackPanel(target);
            }
          }

          fireQuestionCategorySelection(target, getQuestionModel(), QuestionCategoryCheckBoxPanel.this.getDefaultModel(), getSelectionModel().isSelected());
        }

      });
    }

    FormComponentLabel checkboxLabel = new FormComponentLabel("categoryLabel", checkbox);
    add(checkboxLabel);
    checkboxLabel.add(checkbox);
    checkboxLabel.add(new Label("label", checkbox.getLabel()).setRenderBodyOnly(true).setVisible(radioLabelVisible));

    if(questionCategory.getCategory().getOpenAnswerDefinition() != null) {
      // there is an open field
      openField = newOpenAnswerDefinitionPanel("open");
      add(openField);

      checkbox.add(new AttributeAppender("class", new Model<String>("checkbox-open"), " "));
      checkboxLabel.add(new AttributeModifier("class", new Model<String>("label-open")));

    } else {
      // no open answer
      add(new EmptyPanel("open").setVisible(false));
    }
  }

  public QuestionCategoryCheckBoxModel getSelectionModel() {
    return (QuestionCategoryCheckBoxModel) checkbox.getModel();
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
  public void onQuestionCategorySelection(AjaxRequestTarget target, IModel questionModel, IModel questionCategoryModel, boolean isSelected) {
    if(!getSelectionModel().isSelected()) {
      // set checkbox as selected
      getSelectionModel().select();
    }

    fireQuestionCategorySelection(target, questionModel, questionCategoryModel, isSelected);
  }
}
