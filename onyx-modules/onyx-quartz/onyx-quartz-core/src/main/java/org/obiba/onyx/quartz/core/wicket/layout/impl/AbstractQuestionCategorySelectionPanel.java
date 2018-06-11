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
import org.apache.wicket.model.IModel;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.wicket.layout.IQuestionCategorySelectionListener;
import org.obiba.onyx.quartz.core.wicket.layout.impl.standard.DefaultOpenAnswerDefinitionPanel;
import org.obiba.onyx.quartz.core.wicket.layout.impl.standard.MultipleDefaultOpenAnswerDefinitionPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract to share some facilities between radio/checkbox based question category selectors.
 */
public abstract class AbstractQuestionCategorySelectionPanel extends BaseQuestionCategorySelectionPanel implements IQuestionCategorySelectionListener {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private static final Logger log = LoggerFactory.getLogger(AbstractQuestionCategorySelectionPanel.class);

  /**
   * Constructor giving the question category model.
   * @param id
   * @param questionCategoryModel
   */
  public AbstractQuestionCategorySelectionPanel(String id, IModel<Question> questionModel, IModel<QuestionCategory> questionCategoryModel) {
    super(id, questionModel, questionCategoryModel);
  }

  /**
   * Factory method to get the appropriate {@link AbstractOpenAnswerDefinitionPanel} for this question category.
   * @param id
   * @return
   */
  protected AbstractOpenAnswerDefinitionPanel newOpenAnswerDefinitionPanel(String id) {
    AbstractOpenAnswerDefinitionPanel openField;

    if(getQuestionCategory().getCategory().getOpenAnswerDefinition().hasChildOpenAnswerDefinitions() == false) {
      // case there is a simple open answer
      openField = new DefaultOpenAnswerDefinitionPanel(id, getQuestionModel(), getQuestionCategoryModel());
    } else {
      // case there are multiple open answers
      openField = new MultipleDefaultOpenAnswerDefinitionPanel(id, getQuestionModel(), getQuestionCategoryModel());
    }

    return openField;
  }

  /**
   * Reset (set null data) the open fields not associated to the current question category.
   * @param parentContainer
   */
  protected void resetOpenAnswerDefinitionPanels(final AjaxRequestTarget target, MarkupContainer parentContainer, final IModel<QuestionCategory> questionCategoryModel) {

    if(AbstractOpenAnswerDefinitionPanel.class.isInstance(parentContainer)) {
      AbstractOpenAnswerDefinitionPanel openField = (AbstractOpenAnswerDefinitionPanel) parentContainer;
      openField.resetField();
      target.addComponent(openField);
    }

    parentContainer.visitChildren(AbstractOpenAnswerDefinitionPanel.class, new Component.IVisitor<AbstractOpenAnswerDefinitionPanel>() {

      public Object component(AbstractOpenAnswerDefinitionPanel openField) {
        if(!questionCategoryModel.equals(openField.getDefaultModel())) {
          log.debug("visit.AbstractOpenAnswerDefinitionPanel.model={}", openField.getDefaultModelObject());
          openField.resetField();
          target.addComponent(openField);
        }
        return CONTINUE_TRAVERSAL;
      }

    });
  }

  /**
   * Does question category has an associated open answer field.
   * @return
   */
  public abstract boolean hasOpenField();

  protected void fireQuestionCategorySelection(AjaxRequestTarget target, IModel<Question> questionModel, IModel<QuestionCategory> questionCategoryModel, boolean isSelected) {
    log.debug("fireQuestionCategorySelection({},{},{})", new Object[] { questionModel.getObject(), questionCategoryModel.getObject(), Boolean.valueOf(isSelected) });
    IQuestionCategorySelectionListener listener = findParent(IQuestionCategorySelectionListener.class);
    if(listener != null) {
      listener.onQuestionCategorySelection(target, questionModel, questionCategoryModel, isSelected);
    }
  }

}
