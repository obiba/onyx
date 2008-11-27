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

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract to share some facilities between radio/checkbox based question category selectors.
 */
public abstract class AbstractQuestionCategorySelectionPanel extends Panel {

  private static final Logger log = LoggerFactory.getLogger(AbstractQuestionCategorySelectionPanel.class);

  /**
   * The question model (not necessarily the question of the category in the case of shared categories question).
   */
  private IModel questionModel;

  /**
   * Constructor giving the question category model.
   * @param id
   * @param questionCategoryModel
   */
  public AbstractQuestionCategorySelectionPanel(String id, IModel questionModel, IModel questionCategoryModel) {
    super(id, questionCategoryModel);
    this.questionModel = questionModel;
  }

  /**
   * Reset (set non required and null data) the open fields not associated to the current question category.
   * @param parentContainer
   */
  protected void resetOpenAnswerDefinitionPanels(MarkupContainer parentContainer) {

    parentContainer.visitChildren(new Component.IVisitor() {

      public Object component(Component component) {
        if(component instanceof AbstractOpenAnswerDefinitionPanel) {
          if(isToBeReseted((AbstractOpenAnswerDefinitionPanel) component)) {
            log.info("visit.AbstractOpenAnswerDefinitionPanel.model={}", component.getModelObject());
            AbstractOpenAnswerDefinitionPanel openField = (AbstractOpenAnswerDefinitionPanel) component;
            openField.setFieldModelObject(null);
            openField.setRequired(false);
          }
        }
        return CONTINUE_TRAVERSAL;
      }

    });
  }

  /**
   * When an open field is visited for reseting, this method should answer whether or not the operation should be
   * performed (usually depending on its associated model).
   * @param openField
   * @return
   * @see #resetOpenAnswerDefinitionPanels(MarkupContainer)
   */
  protected abstract boolean isToBeReseted(AbstractOpenAnswerDefinitionPanel openField);

  protected IModel getQuestionModel() {
    return questionModel;
  }

  protected Question getQuestion() {
    return (Question) questionModel.getObject();
  }

  protected IModel getQuestionCategoryModel() {
    return getModel();
  }

  protected QuestionCategory getQuestionCategory() {
    return (QuestionCategory) getModel().getObject();
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
   * Called when open field is submitted and error occures.
   * @param target
   * @param questionModel
   * @param questionCategoryModel
   */
  public void onOpenFieldError(AjaxRequestTarget target, IModel questionModel, IModel questionCategoryModel) {

  }

  /**
   * Called when open field is selected: persist the category answer with no data yet.
   * @param target
   */
  public void onOpenFieldSelection(AjaxRequestTarget target, IModel questionModel, IModel questionCategoryModel) {

  }

  /**
   * Called for internal use after open field is selected.
   * @param target
   * @param questionModel
   * @param questionCategoryModel
   * @see #onOpenFieldSelection(AjaxRequestTarget, IModel, IModel)
   */
  protected void onInternalOpenFieldSelection(AjaxRequestTarget target, IModel questionModel, IModel questionCategoryModel) {
    onOpenFieldSelection(target, questionModel, questionCategoryModel);
  }

  /**
   * Called for internal use after open field is submited.
   * @param target
   * @param questionModel
   * @param questionCategoryModel
   * @see #onOpenFieldSubmit(AjaxRequestTarget, IModel, IModel)
   */
  public void onInternalOpenFieldSubmit(AjaxRequestTarget target, IModel questionModel, IModel questionCategoryModel) {
    onOpenFieldSubmit(target, questionModel, questionCategoryModel);
  }

  /**
   * Called when selector is clicked.
   * @param target
   */
  public void onSelection(AjaxRequestTarget target, IModel questionModel, IModel questionCategoryModel) {
  }

  /**
   * Factory method to get the appropriate {@link AbstractOpenAnswerDefinitionPanel} for this question category.
   * @param id
   * @return
   */
  @SuppressWarnings("serial")
  protected AbstractOpenAnswerDefinitionPanel newOpenAnswerDefinitionPanel(String id) {
    AbstractOpenAnswerDefinitionPanel openField;

    if(getQuestionCategory().getCategory().getOpenAnswerDefinition().getOpenAnswerDefinitions().size() == 0) {
      openField = new DefaultOpenAnswerDefinitionPanel(id, getQuestionModel(), getQuestionCategoryModel()) {

        @Override
        public void onSelect(AjaxRequestTarget target, IModel questionModel, IModel questionCategoryModel, IModel openAnswerDefinitionModel) {
          onInternalOpenFieldSelection(target, questionModel, questionCategoryModel);
        }

        @Override
        public void onSubmit(AjaxRequestTarget target, IModel questionModel, IModel questionCategoryModel) {
          onInternalOpenFieldSubmit(target, questionModel, questionCategoryModel);
        }

        @Override
        public void onError(AjaxRequestTarget target, IModel questionModel, IModel questionCategoryModel) {
          onOpenFieldError(target, questionModel, questionCategoryModel);
        }

      };
    } else {
      openField = new MultipleOpenAnswerDefinitionPanel(id, getQuestionModel(), getQuestionCategoryModel()) {

        @Override
        public void onSelect(AjaxRequestTarget target, IModel questionModel, IModel questionCategoryModel, IModel openAnswerDefinitionModel) {
          onInternalOpenFieldSelection(target, questionModel, questionCategoryModel);
        }

        @Override
        public void onSubmit(AjaxRequestTarget target, IModel questionModel, IModel questionCategoryModel) {
          onOpenFieldSubmit(target, questionModel, questionCategoryModel);
        }

        @Override
        public void onError(AjaxRequestTarget target, IModel questionModel, IModel questionCategoryModel) {
          onOpenFieldError(target, questionModel, questionCategoryModel);
        }

      };
    }

    return openField;
  }

  public abstract boolean hasOpenField();
}
