package org.obiba.onyx.quartz.core.service;

import java.io.Serializable;

import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.quartz.core.domain.answer.QuestionnaireParticipant;

public interface QuestionnaireParticipantService {

  /**
   * Get the last questionnaire for participant and questionnaire name.
   * @param participant
   * @param questionnaireName
   * @return
   */
  public QuestionnaireParticipant getLastQuestionnaireParticipant(Participant participant, String questionnaireName);
  
  /**
   * Delete specified questionnaireParticipany
   * @param questionnaireParticipantId
   */
  public void deleteQuestionnaireParticipant(Serializable questionnaireParticipantId);
}
