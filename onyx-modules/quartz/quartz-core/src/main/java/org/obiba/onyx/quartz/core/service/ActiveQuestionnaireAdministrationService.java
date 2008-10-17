/***********************************************************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 **********************************************************************************************************************/
package org.obiba.onyx.quartz.core.service;

import java.util.List;
import java.util.Locale;

import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.quartz.core.domain.answer.CategoryAnswer;
import org.obiba.onyx.quartz.core.domain.answer.QuestionAnswer;
import org.obiba.onyx.quartz.core.domain.answer.QuestionnaireParticipant;
import org.obiba.onyx.quartz.core.engine.questionnaire.condition.Condition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.util.data.Data;

public interface ActiveQuestionnaireAdministrationService {

  public void setCurrentPage(Page page);

  /**
   * Get the current {@link Page}.
   * @return
   */
  public Page getCurrentPage();

  /**
   * Get the start {@link Page}.
   * @return
   */
  public Page getStartPage();

  /**
   * Get the {@link Page} previous to current {@link Page}.
   * @return
   */
  public Page getPreviousPage();

  /**
   * Get the {@link Page} next current {@link Page} (resolving {@link Condition}).
   * @return
   */
  public Page getNextPage();

  /**
   * Get the language chosen for the {@link Questionnaire}.
   * @return
   * @see #setDefaultLanguage(Locale)
   */
  public Locale getLanguage();

  /**
   * Set the {@link Questionnaire}.
   * @param questionnaire
   */
  public void setQuestionnaire(Questionnaire questionnaire);

  /**
   * Get the {@link Questionnaire}.
   * @return
   */
  public Questionnaire getQuestionnaire();

  /**
   * Get or create a new {@link QuesitonnaireParticipant} for the current {@link Questionnaire}.
   * @param participant
   * @param language
   * @return
   */
  public QuestionnaireParticipant start(Participant participant, Locale language);

  /**
   * Set the default language if participant has not chosen a questionnaire language yet.
   * @param language
   */
  public void setDefaultLanguage(Locale language);

  /**
   * Get the answers for a {@link Question}.
   * @param question
   * @return empty list if not found
   */
  public List<CategoryAnswer> findAnswers(Question question);

  /**
   * Get the answer for the {@link QuestionCategory}.
   * @param questionCategory
   * @return null if not found
   */
  public CategoryAnswer findAnswer(QuestionCategory questionCategory);

  /**
   * Save or update the question and category answers.
   * @param questionCategory
   * @param value
   * @return
   */
  public CategoryAnswer answer(QuestionCategory questionCategory, Data value);

  /**
   * Set all question category answers being active (or not), including {@link Question} children answers.
   * @param question
   * @param active
   */
  public void setActiveAnswers(Question question, boolean active);

  /**
   * Delete (if any) the {@link CategoryAnswer} of the given {@link Question}, parent {@link QuestionAnswer} and any
   * related answers of {@link Question} children.
   * @param question
   */
  public void deleteAnswers(Question question);

  /**
   * Delete (if any) the {@link CategoryAnswer} of the given {@link Question}, parent {@link QuestionAnswer} and any
   * related answers of {@link Question} children.
   * @param questionCategory
   */
  public void deleteAnswer(QuestionCategory questionCategory);

  /**
   * Clean active service when questionnaire is interrupted or canceled
   */
  public void stopCurrentQuestionnaire();
  
}
