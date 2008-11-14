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
            openField.setData(null);
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

}
