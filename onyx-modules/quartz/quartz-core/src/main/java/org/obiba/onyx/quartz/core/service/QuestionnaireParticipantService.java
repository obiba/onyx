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

import org.obiba.onyx.core.domain.participant.Participant;
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
   * Delete specified questionnaireParticipany
   * @param questionnaireParticipantId
   */
  public void deleteQuestionnaireParticipant(QuestionnaireParticipant questionnaireParticipant);
}
