/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.service.impl;

import java.util.Date;
import java.util.List;

import org.obiba.core.service.impl.PersistenceManagerAwareService;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.quartz.core.domain.answer.CategoryAnswer;
import org.obiba.onyx.quartz.core.domain.answer.QuestionAnswer;
import org.obiba.onyx.quartz.core.domain.answer.QuestionnaireParticipant;
import org.obiba.onyx.quartz.core.service.QuestionnaireParticipantService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class DefaultQuestionnaireParticipantServiceImpl extends PersistenceManagerAwareService implements QuestionnaireParticipantService {

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(DefaultQuestionnaireParticipantServiceImpl.class);

  public void deleteQuestionnaireParticipant(QuestionnaireParticipant questionnaireParticipant) {
    if(questionnaireParticipant == null) return;
    if(questionnaireParticipant.getId() == null) return;
    getPersistenceManager().delete(questionnaireParticipant);
  }

  public void deleteAllQuestionnairesParticipant(Participant participant) {
    QuestionnaireParticipant template = new QuestionnaireParticipant();
    template.setParticipant(participant);
    List<QuestionnaireParticipant> questionnaires = getPersistenceManager().match(template);
    for(QuestionnaireParticipant questionnaireParticipant : questionnaires) {
      deleteQuestionnaireParticipant(questionnaireParticipant);
    }
  }

  public void inactivateQuestionnaireParticipant(QuestionnaireParticipant questionnaireParticipant) {
    if(questionnaireParticipant == null) return;
    if(questionnaireParticipant.getId() == null) return;

    QuestionAnswer questionAnswerTemplate = new QuestionAnswer();
    questionAnswerTemplate.setQuestionnaireParticipant(questionnaireParticipant);

    for(QuestionAnswer questionAnswer : getPersistenceManager().match(questionAnswerTemplate)) {
      CategoryAnswer template = new CategoryAnswer();
      template.setQuestionAnswer(questionAnswer);
      List<CategoryAnswer> categoryAnswerList = getPersistenceManager().match(template);

      for(CategoryAnswer categoryAnswer : categoryAnswerList) {
        categoryAnswer.setActive(false);
        getPersistenceManager().save(categoryAnswer);
      }
      questionAnswer.setActive(false);
      getPersistenceManager().save(questionAnswer);
    }
  }

  public void endQuestionnaireParticipant(Participant participant, String questionnaireName) {
    QuestionnaireParticipant questionnaireParticipant = getQuestionnaireParticipant(participant, questionnaireName);
    if(questionnaireParticipant == null) throw new IllegalArgumentException("Cannot find participant " + participant + " for questionnaire " + questionnaireName);

    questionnaireParticipant.setTimeEnd(new Date());
    getPersistenceManager().save(questionnaireParticipant);
  }

}
