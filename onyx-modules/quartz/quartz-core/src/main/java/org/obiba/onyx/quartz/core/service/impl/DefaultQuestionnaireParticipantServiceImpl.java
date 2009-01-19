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

import java.util.List;

import org.obiba.core.service.impl.PersistenceManagerAwareService;
import org.obiba.onyx.quartz.core.domain.answer.CategoryAnswer;
import org.obiba.onyx.quartz.core.domain.answer.OpenAnswer;
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

    QuestionAnswer questionAnswerTemplate = new QuestionAnswer();
    questionAnswerTemplate.setQuestionnaireParticipant(questionnaireParticipant);

    for(QuestionAnswer questionAnswer : getPersistenceManager().match(questionAnswerTemplate)) {
      CategoryAnswer template = new CategoryAnswer();
      template.setQuestionAnswer(questionAnswer);
      List<CategoryAnswer> categoryAnswerList = getPersistenceManager().match(template);

      for(CategoryAnswer categoryAnswer : categoryAnswerList) {

        OpenAnswer openAnswerTemplate = new OpenAnswer();
        openAnswerTemplate.setCategoryAnswer(categoryAnswer);

        for(OpenAnswer openAnswer : getPersistenceManager().match(openAnswerTemplate)) {
          getPersistenceManager().delete(openAnswer);
        }
        getPersistenceManager().delete(categoryAnswer);
      }
      getPersistenceManager().delete(questionAnswer);
    }

    getPersistenceManager().delete(questionnaireParticipant);
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

}
