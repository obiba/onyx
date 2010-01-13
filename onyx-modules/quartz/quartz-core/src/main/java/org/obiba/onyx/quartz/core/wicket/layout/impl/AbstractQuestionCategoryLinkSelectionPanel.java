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

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.quartz.core.domain.answer.CategoryAnswer;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;
import org.obiba.onyx.quartz.core.wicket.layout.IQuestionCategorySelectionListener;
import org.obiba.onyx.quartz.core.wicket.layout.IQuestionCategorySelectionStateHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public abstract class AbstractQuestionCategoryLinkSelectionPanel extends BaseQuestionCategorySelectionPanel implements IQuestionCategorySelectionStateHolder {

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(AbstractQuestionCategoryLinkSelectionPanel.class);

  //
  // Instance Variables
  //

  @SpringBean
  protected ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationService;

  private boolean selected;

  //
  // Constructors
  //

  public AbstractQuestionCategoryLinkSelectionPanel(String id, IModel questionModel, IModel questionCategoryModel, IModel labelModel, IModel descriptionModel) {
    super(id, questionModel, questionCategoryModel);
    setOutputMarkupId(true);

    updateState();

    addLinkComponent(labelModel, descriptionModel);
  }

  //
  // IQuestionCategorySelectionStateHolder Methods
  //

  public boolean isSelected() {
    return activeQuestionnaireAdministrationService.findAnswer(getQuestion(), getQuestionCategory()) != null;
  }

  public boolean wasSelected() {
    return selected;
  }

  public boolean updateState() {
    selected = isSelected();

    return selected;
  }

  public OpenAnswerDefinition getOpenAnswerDefinition() {
    return null;
  }

  //
  // Methods
  //

  protected abstract void addLinkComponent(IModel labelModel, IModel descriptionModel);

  protected void handleSelectionEvent(AjaxRequestTarget target) {
    // persist (or not)
    // if it was selected, deselect it
    boolean isSelected = isSelected();
    Question question = getQuestion();
    QuestionCategory questionCategory = getQuestionCategory();

    if(!question.isMultiple() || questionCategory.isEscape()) {
      // exclusive choice, only one answer per question
      activeQuestionnaireAdministrationService.deleteAnswers(question);
    } else {
      // in case of multiple answer, make sure when selecting a regular category that a previously selected one is
      // deselected
      if(!questionCategory.isEscape()) {
        for(CategoryAnswer categoryAnswer : activeQuestionnaireAdministrationService.findAnswers(question)) {
          QuestionCategory qCategory = question.findQuestionCategory(categoryAnswer.getCategoryName());
          if(qCategory == null && question.getParentQuestion() != null) {
            // case of shared category
            qCategory = question.getParentQuestion().findQuestionCategory(categoryAnswer.getCategoryName());
          }
          if(qCategory != null && qCategory.isEscape()) {
            activeQuestionnaireAdministrationService.deleteAnswer(question, qCategory);
          }
        }
      }
      // delete the previous answer for this category
      activeQuestionnaireAdministrationService.deleteAnswer(question, questionCategory);
    }
    if(!isSelected) {
      activeQuestionnaireAdministrationService.answer(question, questionCategory);
    }

    fireSelectionEvent(target, !isSelected);
  }

  protected void fireSelectionEvent(AjaxRequestTarget target, boolean isSelected) {
    // fire event to other selectors in case of exclusive choice
    IQuestionCategorySelectionListener listener = (IQuestionCategorySelectionListener) findParent(IQuestionCategorySelectionListener.class);
    if(listener != null) {
      listener.onQuestionCategorySelection(target, getQuestionModel(), getQuestionCategoryModel(), isSelected);
    }
  }
}
