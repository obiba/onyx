/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.test.provider;

import org.obiba.onyx.quartz.core.domain.answer.CategoryAnswer;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;

/**
 * Interface for answer providers.
 * 
 * Given a question, an answer provider returns an answer to the question.
 */
public interface AnswerProvider {

  /**
   * Returns an answer to the specified question.
   * 
   * @param question question
   * @return answer
   */
  public CategoryAnswer getAnswer(Question question);
}
