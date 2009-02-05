/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.wicket.layout.impl.simplified;

import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;

/**
 * An interface for implementing components that are able to say if a question category is currently selected.
 */
public interface IQuestionCategorySelectionStateHolder {

  /**
   * Get the concerned question.
   * @return
   */
  public Question getQuestion();

  /**
   * Get the concerned question category.
   * @return
   */
  public QuestionCategory getQuestionCategory();

  /**
   * Is question category selected.
   * @return
   */
  public boolean isQuestionCategorySelected();

}
