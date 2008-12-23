/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.engine.variable;

import org.obiba.onyx.engine.variable.Entity;
import org.obiba.onyx.quartz.core.engine.questionnaire.ILocalizable;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;

/**
 * Defines the way questionnaire elements is transformed to variable/entities.
 */
public interface IQuestionToVariableMappingStrategy {

  /**
   * Build an entity from a questionnaire.
   * @param questionnaire
   * @return
   */
  public Entity getEntity(Questionnaire questionnaire);

  /**
   * Build an entity from a question, that will be added to questionnaire's entity.
   * @param question
   * @return
   */
  public Entity getEntity(Question question);

  /**
   * Get the question (if any) from the questionnaire corresponding to the given entity.
   * @param entity
   * @return null if not found
   */
  public ILocalizable getQuestionnaireElement(Questionnaire questionnaire, Entity entity);

}
