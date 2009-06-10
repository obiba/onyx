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

import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.engine.variable.Variable;
import org.obiba.onyx.engine.variable.VariableData;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundle;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.service.QuestionnaireParticipantService;

/**
 * Defines the way questionnaire elements is transformed to variable/entities.
 */
public interface IQuestionToVariableMappingStrategy {

  /**
   * Build an entity from a questionnaire.
   * @param questionnaire
   * @return
   */
  public Variable getVariable(Questionnaire questionnaire);

  /**
   * Build an entity from a question, that will be added to questionnaire's entity.
   * @param question
   * @return
   */
  public Variable getVariable(Question question);

  /**
   * Given a questionnaire element variable, find the variable corresponding to the questionnaire.
   * @param variable
   * @return
   */
  public Variable getQuestionnaireVariable(Variable variable);

  /**
   * @param questionnaireParticipantService
   * @param participant
   * @param variable
   * @param questionnaire
   * @return
   */
  public VariableData getVariableData(QuestionnaireParticipantService questionnaireParticipantService, Participant participant, Variable variable, VariableData variableData, Questionnaire questionnaire);

  /**
   * Set the current questionnaire bundle.
   * @param bundle
   */
  public void setQuestionnaireBundle(QuestionnaireBundle bundle);

}
