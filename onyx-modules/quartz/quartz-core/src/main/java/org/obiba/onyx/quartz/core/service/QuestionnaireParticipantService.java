/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.service;

import java.util.List;

import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.quartz.core.domain.answer.CategoryAnswer;
import org.obiba.onyx.quartz.core.domain.answer.OpenAnswer;
import org.obiba.onyx.quartz.core.domain.answer.QuestionnaireParticipant;

public interface QuestionnaireParticipantService {

  /**
   * Get the last questionnaire for participant and questionnaire name.
   * @param participant
   * @param questionnaireName
   * @return
   */
  public QuestionnaireParticipant getQuestionnaireParticipant(Participant participant, String questionnaireName);

  /**
   * Delete specified questionnaire participant.
   * @param questionnaireParticipantId
   */
  public void deleteQuestionnaireParticipant(QuestionnaireParticipant questionnaireParticipant);

  /**
   * Set the answers of given questionnaire participant as being inactive.
   * @param questionnaireParticipant
   */
  public void inactivateQuestionnaireParticipant(QuestionnaireParticipant questionnaireParticipant);

  /**
   * End the questionnaire participant by setting the end date.
   * @param questionnaireParticipant
   */
  public void endQuestionnaireParticipant(Participant participant, String questionnaireName);

  /**
   * Get the comment on the question.
   * @param participant
   * @param questionnaireName
   * @param questionName
   * @return
   */
  public String getQuestionComment(Participant participant, String questionnaireName, String questionName);

  /**
   * Get the active category answers for the question.
   * @param participant
   * @param questionnaireName
   * @param questionName
   * @return
   */
  public List<CategoryAnswer> getCategoryAnswers(Participant participant, String questionnaireName, String questionName);

  /**
   * Get the category answer.
   * @param participant
   * @param questionnaireName
   * @param questionName
   * @param categoryName
   * @return null if category was not selected
   */
  public CategoryAnswer getCategoryAnswer(Participant participant, String questionnaireName, String questionName, String categoryName);

  /**
   * Get the active open answer for the question open answer definition.
   * @param participant
   * @param questionnaireName
   * @param questionName
   * @param categoryName
   * @param openAnswerName
   * @return
   */
  public OpenAnswer getOpenAnswer(Participant participant, String questionnaireName, String questionName, String categoryName, String openAnswerName);

  /**
   * Get whether the question is flagged to be active.
   * @param participant
   * @param questionnaireName
   * @param questionName
   * @return false if active flag is false or null
   */
  public Boolean isQuestionActive(Participant participant, String questionnaireName, String questionName);

}
