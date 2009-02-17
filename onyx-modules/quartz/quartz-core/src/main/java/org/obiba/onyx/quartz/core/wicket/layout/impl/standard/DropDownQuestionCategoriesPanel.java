/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.wicket.layout.impl.standard;

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.quartz.core.domain.answer.CategoryAnswer;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;
import org.obiba.onyx.quartz.core.wicket.layout.impl.AbstractOpenAnswerDefinitionPanel;
import org.obiba.onyx.quartz.core.wicket.layout.impl.BaseQuestionCategorySelectionPanel;
import org.obiba.onyx.quartz.core.wicket.layout.impl.util.QuestionCategoriesProvider;
import org.obiba.onyx.quartz.core.wicket.layout.impl.util.QuestionCategoryEscapeFilter;
import org.obiba.onyx.quartz.core.wicket.layout.impl.validation.AnswerCountValidator;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireModel;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireStringResourceModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Build a drop down choice panel, used by single choice question, and add escape categories in a radio group if there
 * are any.
 */
public class DropDownQuestionCategoriesPanel extends BaseQuestionCategorySelectionPanel {

  private static final long serialVersionUID = 5144933183339704600L;

  private static final Logger log = LoggerFactory.getLogger(DefaultQuestionCategoriesPanel.class);

  @SpringBean
  private ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationService;

  private QuestionCategory selectedQuestionCategory;

  private DropDownChoice questionCategoriesDropDownChoice;

  private DefaultEscapeQuestionCategoriesPanel escapeQuestionCategoriesPanel;

  private AbstractOpenAnswerDefinitionPanel openField;

  @SuppressWarnings("serial")
  public DropDownQuestionCategoriesPanel(String id, IModel questionModel) {
    super(id, questionModel);
    setOutputMarkupId(true);

    Question question = (Question) getModelObject();

    // This component is visible when an open answer is needed
    add(new EmptyPanel("open"));

    // When navigating to previous question
    CategoryAnswer previousAnswer = null;
    if(activeQuestionnaireAdministrationService.findAnswers(question).size() != 0) {

      previousAnswer = activeQuestionnaireAdministrationService.findAnswers(question).get(0);

      for(QuestionCategory questionCategory : question.getQuestionCategories()) {
        if(questionCategory.getCategory().getName().equals(previousAnswer.getCategoryName())) {
          selectedQuestionCategory = questionCategory;
          break;
        }
      }

      updateOpenAnswerDefinitionPanel(selectedQuestionCategory);
    }

    questionCategoriesDropDownChoice = new DropDownChoice("questionCategories", new PropertyModel(this, "selectedQuestionCategory"), new PropertyModel(this, "questionCategories"), new QuestionCategoryChoiceRenderer());
    questionCategoriesDropDownChoice.setOutputMarkupId(true);

    questionCategoriesDropDownChoice.setLabel(new QuestionnaireStringResourceModel(question, "label"));
    questionCategoriesDropDownChoice.setNullValid(true);

    // Set model on submission
    questionCategoriesDropDownChoice.add(new OnChangeAjaxBehavior() {

      @Override
      protected void onUpdate(final AjaxRequestTarget target) {

        log.info("onUpdate()={}", selectedQuestionCategory);

        updateOpenAnswerDefinitionPanel(selectedQuestionCategory);

        // Exclusive choice, only one answer per question
        activeQuestionnaireAdministrationService.deleteAnswers(getQuestion());
        if(selectedQuestionCategory != null && selectedQuestionCategory.getCategory().getOpenAnswerDefinition() == null) {
          activeQuestionnaireAdministrationService.answer(selectedQuestionCategory);
        }

        fireQuestionAnswerChanged(target, getQuestionModel(), selectedQuestionCategory == null ? null : new QuestionnaireModel(selectedQuestionCategory));

        if(escapeQuestionCategoriesPanel != null) {
          escapeQuestionCategoriesPanel.setNoSelection();
        }

        updateFeedbackPanel(target);
        // Update component
        target.addComponent(DropDownQuestionCategoriesPanel.this);
      }

      @Override
      protected void onError(final AjaxRequestTarget target, RuntimeException e) {
        updateFeedbackPanel(target);
        // Update component
        target.addComponent(DropDownQuestionCategoriesPanel.this);
      }

    });
    add(questionCategoriesDropDownChoice);

    if(hasEscapeQuestionCategories()) {
      add(escapeQuestionCategoriesPanel = new DefaultEscapeQuestionCategoriesPanel("escapeCategories", getQuestionModel()) {
        @Override
        public void onSelection(AjaxRequestTarget target, IModel questionModel, IModel questionCategoryModel) {
          setSelectedQuestionCategory(null);
          questionCategoriesDropDownChoice.setRequired(false);
          updateOpenAnswerDefinitionPanel(null);
          target.addComponent(DropDownQuestionCategoriesPanel.this);
        }
      });
      escapeQuestionCategoriesPanel.add(new AnswerCountValidator(getQuestionModel()));
    } else {
      add(new EmptyPanel("escapeCategories").setVisible(false));
    }
  }

  private IModel getQuestionModel() {
    return getModel();
  }

  private Question getQuestion() {
    return (Question) getModelObject();
  }

  private boolean hasEscapeQuestionCategories() {
    for(Category category : ((Question) getModelObject()).getCategories()) {
      if(category.isEscape()) return true;
    }
    return false;
  }

  public List<QuestionCategory> getQuestionCategories() {
    QuestionCategoriesProvider provider = new QuestionCategoriesProvider(getQuestionModel(), new QuestionCategoryEscapeFilter(false));
    return provider.getDataList();
  }

  /**
   * Render text to dislay for drow down choice items
   */
  private class QuestionCategoryChoiceRenderer implements IChoiceRenderer {

    private static final long serialVersionUID = 1L;

    // Text to be displayed to an end user
    public Object getDisplayValue(Object object) {
      if(object == null) return null;

      return (new QuestionnaireStringResourceModel((QuestionCategory) object, "label").getString());
    }

    public String getIdValue(Object object, int index) {
      if(object == null) return null;

      return ((QuestionCategory) object).getName();
    }
  }

  /**
   * Update the open answer definition panel if given {@link QuestionCategory} has a {@link OpenAnswerDefinition}
   * associated to.
   * @param questionCategory
   */
  @SuppressWarnings("serial")
  private void updateOpenAnswerDefinitionPanel(QuestionCategory questionCategory) {
    if(questionCategory == null) {
      openField = null;
      get("open").replaceWith(new EmptyPanel("open"));
    } else {
      OpenAnswerDefinition openAnswerDefinition = questionCategory.getCategory().getOpenAnswerDefinition();

      if(openAnswerDefinition != null) {
        if(openAnswerDefinition.getOpenAnswerDefinitions().size() == 0) {
          openField = new DefaultOpenAnswerDefinitionPanel("open", getQuestionModel(), new QuestionnaireModel(questionCategory));
        } else {
          openField = new MultipleDefaultOpenAnswerDefinitionPanel("open", getQuestionModel(), new QuestionnaireModel(questionCategory));
        }
        get("open").replaceWith(openField);
      } else {
        openField = null;
        get("open").replaceWith(new EmptyPanel("open"));
      }
    }
  }

  public QuestionCategory getSelectedQuestionCategory() {
    return selectedQuestionCategory;
  }

  public void setSelectedQuestionCategory(QuestionCategory selectedQuestionCategory) {
    this.selectedQuestionCategory = selectedQuestionCategory;
  }

}