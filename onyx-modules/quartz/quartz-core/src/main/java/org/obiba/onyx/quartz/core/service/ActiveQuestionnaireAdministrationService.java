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
import org.obiba.onyx.quartz.core.domain.answer.OpenAnswer;
import org.obiba.onyx.quartz.core.domain.answer.QuestionAnswer;
import org.obiba.onyx.quartz.core.domain.answer.QuestionnaireParticipant;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.util.data.Data;

public interface ActiveQuestionnaireAdministrationService {

  /**
   * Returns the current questionnaire page.
   * 
   * @return current page
   */
  public Page getCurrentPage();

  /**
   * Returns the page at which the questionnaire will resume.
   * 
   * @return page at which to resume (or <code>null</code>, if the questionnaire has not been interrupted)
   */
  public Page getResumePage();

  /**
   * Positions the questionnaire at the start page.
   * 
   * @return new current page (start page)
   */
  public Page startPage();

  /**
   * Positions the questionnaire at the last page.
   * 
   * @return new current page (last page)
   */
  public Page lastPage();

  /**
   * Moves back to the previous page of the questionnaire.
   * 
   * @return new current page (previous page)
   */
  public Page previousPage();

  /**
   * Advances to the next page of the questionnaire.
   * 
   * @return new current page (next page)
   */
  public Page nextPage();

  /**
   * Positions the questionnaire at the resume page.
   * 
   * @return new current page (resume page)
   */
  public Page resumePage();

  /**
   * Indicates whether the questionnaire is currently positioned at the start page.
   * 
   * @return <code>true</code> if currently at the start page
   */
  public boolean isOnStartPage();

  /**
   * Indicates whether the questionnaire is currently positioned at the last page.
   * 
   * @return <code>true</code> if currently at the last page
   */
  public boolean isOnLastPage();

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

  public void resume(Participant participant);

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
   * Get the answer for the {@link QuestionCategory}.
   * @param question
   * @param questionCategory
   * @return null if not found
   */
  public CategoryAnswer findAnswer(Question question, QuestionCategory questionCategory);

  /**
   * Get the answer for the {@link Category}.
   * @param question
   * @param category
   * @return null if not found
   */
  public CategoryAnswer findAnswer(Question question, Category category);

  /**
   * Get the openAnswer for the {@link QuestionCategory} and the {@link OpenAnswerDefinition}.
   * @param questionCategory
   * @param openAnswerDefinition
   * @return null if not found
   */
  public OpenAnswer findOpenAnswer(QuestionCategory questionCategory, OpenAnswerDefinition openAnswerDefinition);

  /**
   * Get the openAnswer for the {@link Question}, {@link Category} and the {@link OpenAnswerDefinition}.
   * @param question
   * @param category
   * @param openAnswerDefinition
   * @return null if not found
   */
  public OpenAnswer findOpenAnswer(Question question, Category category, OpenAnswerDefinition openAnswerDefinition);

  /**
   * Save or update the question and category answers.
   * @param questionCategory
   * @return
   */
  public CategoryAnswer answer(QuestionCategory questionCategory);

  /**
   * Save or update the question and category answers.
   * @param questionCategory
   * @param openAnswerDefinition
   * @param value
   * @return
   */
  public CategoryAnswer answer(QuestionCategory questionCategory, OpenAnswerDefinition openAnswerDefinition, Data value);

  /**
   * Save or update the question and category answers.
   * @param question
   * @param questionCategory
   * @param value
   * @return
   */
  public CategoryAnswer answer(Question question, QuestionCategory questionCategory);

  /**
   * Save or update the question and category answers.
   * @param question
   * @param questionCategory
   * @param value
   * @return
   */
  public CategoryAnswer answer(Question question, QuestionCategory questionCategory, OpenAnswerDefinition openAnswerDefinition, Data value);

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
   * Delete (if any) the {@link CategoryAnswer} of the given {@link Question}, parent {@link QuestionAnswer} and any
   * related answers of {@link Question} children.
   * @param questionCategory
   */
  public void deleteAnswer(Question question, QuestionCategory questionCategory);

  /**
   * Clean active service when questionnaire is interrupted or canceled
   */
  public void stopCurrentQuestionnaire();

  /**
   * Add a comment to a question answer.
   * 
   * @param question The question for which the comment will be added.
   * @param comment The comment.
   * @return The question answer.
   */
  public QuestionAnswer addComment(Question question, String comment);

  /**
   * Get the comment attached to the question answer.
   * 
   * @param question The question.
   * @return The comment for the question.
   */
  public String getComment(Question question);

}
