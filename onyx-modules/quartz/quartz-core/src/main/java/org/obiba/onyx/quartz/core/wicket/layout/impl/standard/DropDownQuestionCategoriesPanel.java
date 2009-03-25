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
import org.obiba.onyx.quartz.core.wicket.layout.IQuestionCategorySelectionListener;
import org.obiba.onyx.quartz.core.wicket.layout.impl.AbstractOpenAnswerDefinitionPanel;
import org.obiba.onyx.quartz.core.wicket.layout.impl.BaseQuestionCategorySelectionPanel;
import org.obiba.onyx.quartz.core.wicket.layout.impl.util.QuestionCategoriesProvider;
import org.obiba.onyx.quartz.core.wicket.layout.impl.util.QuestionCategoryEscapeFilter;
import org.obiba.onyx.quartz.core.wicket.layout.impl.validation.AnswerCountValidator;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireModel;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireStringResourceModel;
import org.obiba.onyx.wicket.behavior.InvalidFormFieldBehavior;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Build a drop down choice panel, used by single choice question, and add escape categories in a radio group if there
 * are any.
 */
public class DropDownQuestionCategoriesPanel extends BaseQuestionCategorySelectionPanel implements IQuestionCategorySelectionListener {

  private static final long serialVersionUID = 5144933183339704600L;

  private static final Logger log = LoggerFactory.getLogger(DropDownQuestionCategoriesPanel.class);

  private static final String OPEN_ID = "open";

  @SpringBean
  private ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationService;

  private IModel selectedQuestionCategoryModel;

  private DropDownChoice questionCategoriesDropDownChoice;

  private DefaultEscapeQuestionCategoriesPanel escapeQuestionCategoriesPanel;

  private AbstractOpenAnswerDefinitionPanel openField;

  @SuppressWarnings("serial")
  public DropDownQuestionCategoriesPanel(String id, IModel questionModel) {
    super(id, questionModel, null);
    setOutputMarkupId(true);

    Question question = getQuestion();

    // This component is visible when an open answer is needed
    add(new EmptyPanel("open"));

    // When navigating to previous question
    CategoryAnswer previousAnswer = null;
    List<CategoryAnswer> categoryAnswers = activeQuestionnaireAdministrationService.findAnswers(question);
    if(categoryAnswers != null && categoryAnswers.size() != 0) {

      previousAnswer = categoryAnswers.get(0);

      for(QuestionCategory questionCategory : question.getQuestionCategories()) {
        if(questionCategory.getCategory().getName().equals(previousAnswer.getCategoryName())) {
          selectedQuestionCategoryModel = new QuestionnaireModel(questionCategory);
          break;
        }
      }

      updateOpenAnswerDefinitionPanel(null, selectedQuestionCategoryModel);
    }

    questionCategoriesDropDownChoice = new DropDownChoice("questionCategories", new PropertyModel(this, "selectedQuestionCategory"), new PropertyModel(this, "questionCategories"), new QuestionCategoryChoiceRenderer());
    questionCategoriesDropDownChoice.setOutputMarkupId(true);

    questionCategoriesDropDownChoice.setLabel(new QuestionnaireStringResourceModel(question, "label"));
    questionCategoriesDropDownChoice.setNullValid(true);

    // Set model on submission
    questionCategoriesDropDownChoice.add(new OnChangeAjaxBehavior() {

      @Override
      protected void onUpdate(final AjaxRequestTarget target) {

        log.debug("onUpdate()={}", selectedQuestionCategoryModel != null ? selectedQuestionCategoryModel.getObject() : null);

        updateOpenAnswerDefinitionPanel(target, selectedQuestionCategoryModel);

        // Exclusive choice, only one answer per question
        activeQuestionnaireAdministrationService.deleteAnswers(getQuestion());
        if(selectedQuestionCategoryModel != null) {
          activeQuestionnaireAdministrationService.answer((QuestionCategory) selectedQuestionCategoryModel.getObject());
        }

        if(escapeQuestionCategoriesPanel != null) {
          escapeQuestionCategoriesPanel.setNoSelection();
          target.addComponent(escapeQuestionCategoriesPanel);
        }

        updateFeedbackPanel(target);

        fireQuestionCategorySelected(target, getQuestionModel(), selectedQuestionCategoryModel == null ? null : selectedQuestionCategoryModel);
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
      add(escapeQuestionCategoriesPanel = new DefaultEscapeQuestionCategoriesPanel("escapeCategories", getQuestionModel()));
      escapeQuestionCategoriesPanel.add(new AnswerCountValidator(getQuestionModel()));
    } else {
      add(new EmptyPanel("escapeCategories").setVisible(false));
    }

    add(new InvalidFormFieldBehavior());
  }

  private boolean hasEscapeQuestionCategories() {
    for(Category category : getQuestion().getCategories()) {
      if(category.isEscape()) return true;
    }
    return false;
  }

  public List<IModel> getQuestionCategories() {
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

      return (new QuestionnaireStringResourceModel((IModel) object, "label").getString());
    }

    public String getIdValue(Object object, int index) {
      if(object == null) return null;

      return ((QuestionnaireModel) object).getElementName();
    }
  }

  /**
   * Update the open answer definition panel if given {@link QuestionCategory} has a {@link OpenAnswerDefinition}
   * associated to.
   * @param questionCategory
   */
  @SuppressWarnings("serial")
  private void updateOpenAnswerDefinitionPanel(AjaxRequestTarget target, IModel questionCategoryModel) {
    boolean changed = false;

    if(questionCategoryModel == null) {
      openField = null;
      if(!EmptyPanel.class.isInstance(get(OPEN_ID))) {
        get(OPEN_ID).replaceWith(new EmptyPanel("open").setOutputMarkupId(true));
        changed = true;
      }
    } else {
      QuestionCategory questionCategory = (QuestionCategory) questionCategoryModel.getObject();
      OpenAnswerDefinition openAnswerDefinition = questionCategory.getCategory().getOpenAnswerDefinition();

      if(openAnswerDefinition != null) {
        if(openAnswerDefinition.getOpenAnswerDefinitions().size() == 0) {
          openField = new DefaultOpenAnswerDefinitionPanel("open", getQuestionModel(), questionCategoryModel);
        } else {
          openField = new MultipleDefaultOpenAnswerDefinitionPanel("open", getQuestionModel(), questionCategoryModel);
        }
        get(OPEN_ID).replaceWith(openField);
        changed = true;
      } else {
        if(!EmptyPanel.class.isInstance(get(OPEN_ID))) {
          get(OPEN_ID).replaceWith(new EmptyPanel("open").setOutputMarkupId(true));
          changed = true;
        }
      }
    }

    if(changed && target != null) {
      target.addComponent(get(OPEN_ID));
    }
  }

  public IModel getSelectedQuestionCategory() {
    return selectedQuestionCategoryModel;
  }

  public void setSelectedQuestionCategory(IModel selectedQuestionCategoryModel) {
    this.selectedQuestionCategoryModel = selectedQuestionCategoryModel;
  }

  public void onQuestionCategorySelection(AjaxRequestTarget target, IModel questionModel, IModel questionCategoryModel, boolean isSelected) {
    log.debug("onQuestionCategorySelection()={}", selectedQuestionCategoryModel != null ? selectedQuestionCategoryModel.getObject() : null);

    if(((QuestionCategory) questionCategoryModel.getObject()).isEscape()) {
      // called from escape category
      if(selectedQuestionCategoryModel != null) {
        target.appendJavascript("document.getElementById('" + questionCategoriesDropDownChoice.getMarkupId() + "').selectedIndex = 0;");
      }
      setSelectedQuestionCategory(null);
      questionCategoriesDropDownChoice.setRequired(false);
      updateOpenAnswerDefinitionPanel(target, null);
    }

    updateFeedbackPanel(target);

    // forward event to parent
    fireQuestionCategorySelected(target, questionModel, questionCategoryModel);
  }

}