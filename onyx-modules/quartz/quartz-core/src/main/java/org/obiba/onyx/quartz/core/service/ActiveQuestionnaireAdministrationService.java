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

import java.util.Locale;

import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.quartz.core.domain.answer.CategoryAnswer;
import org.obiba.onyx.quartz.core.domain.answer.QuestionAnswer;
import org.obiba.onyx.quartz.core.domain.answer.QuestionnaireParticipant;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;

public interface ActiveQuestionnaireAdministrationService {

  public Page getCurrentPage();
  
  public Page getStartPage();
  
  public Page getPreviousPage();
  
  public Page getNextPage();

  public Locale getLanguage();
  
  public Questionnaire getQuestionnaire();
  
  //public QuestionnaireParticipant getQuestionnaireParticipant();

  public QuestionAnswer findQuestionAnswer(Question question);
  
  public QuestionAnswer answerQuestion(Question question, CategoryAnswer categoryAnswer);
  
  public void setQuestionnaire(Questionnaire questionnaire);
  
  public QuestionnaireParticipant start(Participant participant, Locale language);
  
  public void setDefaultLanguage(Locale language);
  
}
