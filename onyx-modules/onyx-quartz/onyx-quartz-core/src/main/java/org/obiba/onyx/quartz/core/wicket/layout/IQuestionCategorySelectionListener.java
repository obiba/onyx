/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.wicket.layout;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;

/**
 * Listener that want to be warned a question category has been (or not) selected in a Ajax context.
 */
public interface IQuestionCategorySelectionListener {

  /**
   * Called when a child question category selector has changed.
   * @param target
   * @param questionModel
   * @param questionCategoryModel
   * @param isSelected
   */
  public void onQuestionCategorySelection(AjaxRequestTarget target, IModel<Question> questionModel, IModel<QuestionCategory> questionCategoryModel, boolean isSelected);

}
