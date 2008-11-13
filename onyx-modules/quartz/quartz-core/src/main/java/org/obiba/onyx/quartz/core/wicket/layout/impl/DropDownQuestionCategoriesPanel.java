/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.wicket.layout.impl;

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.quartz.core.domain.answer.CategoryAnswer;
import org.obiba.onyx.quartz.core.domain.answer.OpenAnswer;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireModel;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireStringResourceModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Build a drop down choice panel, used by single choice question.
 */
public class DropDownQuestionCategoriesPanel extends Panel {

  private static final long serialVersionUID = 5144933183339704600L;

  private static final Logger log = LoggerFactory.getLogger(DefaultQuestionCategoriesPanel.class);

  @SpringBean
  private ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationService;

  @SpringBean
  private EntityQueryService queryService;

  private QuestionCategory selectedQuestionCategory;

  private DefaultOpenAnswerDefinitionPanel openField;

  public DropDownQuestionCategoriesPanel(String id, IModel questionModel) {

    super(id, questionModel);
    setOutputMarkupId(true);

    Question question = (Question) getModelObject();

    if(!question.isMultiple()) {
      addDropdownChoice(question);
    }
  }

  /**
   * Add a drop down choice, used by single choice question.
   * @param question
   */
  @SuppressWarnings("serial")
  private void addDropdownChoice(final Question question) {

    // This component is visible when an open answer is needed
    add(new EmptyPanel("open"));

    // When navigating to previous question
    if(activeQuestionnaireAdministrationService.findAnswers(question).size() != 0) {

      CategoryAnswer previousAnswer = activeQuestionnaireAdministrationService.findAnswers(question).get(0);

      if(selectedQuestionCategory == null) {
        for(QuestionCategory questionCategory : question.getQuestionCategories()) {
          if(questionCategory.getCategory().getName().equals(previousAnswer.getCategoryName())) {
            selectedQuestionCategory = questionCategory;
            break;
          }
        }
      }

      // Previous question contains an open answer
      OpenAnswer template = new OpenAnswer();
      template.setCategoryAnswer(activeQuestionnaireAdministrationService.findAnswer(selectedQuestionCategory));
      List<OpenAnswer> previousOpenAnswers = queryService.match(template);

      if(previousOpenAnswers != null && previousOpenAnswers.size() > 0) {
        openField = new DefaultOpenAnswerDefinitionPanel("open", new QuestionnaireModel(question), new QuestionnaireModel(selectedQuestionCategory));
        get("open").replaceWith(openField);
      }
    }

    final DropDownChoice questionCategoriesDropDownChoice = new DropDownChoice("questionCategories", new PropertyModel(this, "selectedQuestionCategory"), new PropertyModel(question, "questionCategories"), new QuestionCategoryChoiceRenderer());
    questionCategoriesDropDownChoice.setOutputMarkupId(true);

    questionCategoriesDropDownChoice.setLabel(new QuestionnaireStringResourceModel(question, "label"));
    questionCategoriesDropDownChoice.setRequired(question.isRequired() ? true : false);
    questionCategoriesDropDownChoice.setNullValid(true);

    // Set model on submission
    questionCategoriesDropDownChoice.add(new OnChangeAjaxBehavior() {

      @Override
      protected void onUpdate(AjaxRequestTarget target) {

        log.info("onUpdate()={}", selectedQuestionCategory);

        createOpenAnswerDefinitionPanel(selectedQuestionCategory);

        // Exclusive choice, only one answer per question
        activeQuestionnaireAdministrationService.deleteAnswers(selectedQuestionCategory.getQuestion());
        if(selectedQuestionCategory.getCategory().getOpenAnswerDefinition() == null) {
          activeQuestionnaireAdministrationService.answer(selectedQuestionCategory);
        }

        // Update component
        target.addComponent(DropDownQuestionCategoriesPanel.this);
      }
    });

    add(questionCategoriesDropDownChoice);
  }

  /**
   * Render text to dislay for drow down choice items
   */
  private class QuestionCategoryChoiceRenderer implements IChoiceRenderer {

    private static final long serialVersionUID = 1L;

    // Text to be displayed to an end user
    public Object getDisplayValue(Object object) {
      QuestionCategory questionCat = (QuestionCategory) object;

      if(questionCat.getCategory().getOpenAnswerDefinition() != null) {
        return (new QuestionnaireStringResourceModel(questionCat.getCategory().getOpenAnswerDefinition(), "label").getString());
      }
      return (new QuestionnaireStringResourceModel(questionCat, "label").getString());
    }

    public String getIdValue(Object object, int index) {
      QuestionCategory questionCat = (QuestionCategory) object;
      return questionCat.getName();
    }
  }

  /**
   * Create an open answer definition panel if given {@link QuestionCategory} has a {@link OpenAnswerDefinition}
   * associated to.
   * @param questionCategory
   */
  @SuppressWarnings("serial")
  private void createOpenAnswerDefinitionPanel(final QuestionCategory questionCategory) {

    if(questionCategory.getCategory().getOpenAnswerDefinition() != null) {

      openField = new DefaultOpenAnswerDefinitionPanel("open", new QuestionnaireModel(questionCategory.getQuestion()), new QuestionnaireModel(questionCategory));

      get("open").replaceWith(openField);
    } else {
      openField = null;
      get("open").replaceWith(new EmptyPanel("open"));
    }
  }

  public QuestionCategory getSelectedQuestionCategory() {
    return selectedQuestionCategory;
  }

  public void setSelectedQuestionCategory(QuestionCategory selectedQuestionCategory) {
    this.selectedQuestionCategory = selectedQuestionCategory;
  }
}