/***********************************************************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 **********************************************************************************************************************/
package org.obiba.onyx.quartz.core.service.impl;

import java.util.Locale;

import org.obiba.core.service.impl.PersistenceManagerAwareService;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.quartz.core.domain.answer.CategoryAnswer;
import org.obiba.onyx.quartz.core.domain.answer.OpenAnswer;
import org.obiba.onyx.quartz.core.domain.answer.QuestionAnswer;
import org.obiba.onyx.quartz.core.domain.answer.QuestionnaireParticipant;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;
import org.obiba.onyx.quartz.core.service.INavigationStrategy;
import org.obiba.onyx.util.data.Data;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public abstract class DefaultActiveQuestionnaireAdministrationServiceImpl extends PersistenceManagerAwareService implements ActiveQuestionnaireAdministrationService {

  private Questionnaire currentQuestionnaire;

  private QuestionnaireParticipant currentQuestionnaireParticipant;

  private Locale defaultLanguage;

  private INavigationStrategy navigationStrategy;

  private Page currentPage;

  public Questionnaire getQuestionnaire() {
    return currentQuestionnaire;
  }

  public void setQuestionnaire(Questionnaire questionnaire) {
    this.currentQuestionnaire = questionnaire;
    this.currentQuestionnaireParticipant = null;
  }

  public Locale getLanguage() {
    if(currentQuestionnaireParticipant == null) return defaultLanguage;
    return currentQuestionnaireParticipant.getLocale();
  }

  public void setNavigationStrategy(INavigationStrategy navigationStrategy) {
    this.navigationStrategy = navigationStrategy;
  }

  public QuestionnaireParticipant start(Participant participant, Locale language) {

    if(currentQuestionnaireParticipant != null) {
      // refresh it
      currentQuestionnaireParticipant = getQuestionnaireParticipant();
    } else {
      QuestionnaireParticipant questionnaireParticipantTemplate = new QuestionnaireParticipant();
      questionnaireParticipantTemplate.setParticipant(participant);
      questionnaireParticipantTemplate.setQuestionnaireName(currentQuestionnaire.getName());
      questionnaireParticipantTemplate.setQuestionnaireVersion(currentQuestionnaire.getVersion());

      // find an old questionnaire participant if any
      currentQuestionnaireParticipant = getPersistenceManager().matchOne(questionnaireParticipantTemplate);
      if(currentQuestionnaireParticipant == null) {
        currentQuestionnaireParticipant = questionnaireParticipantTemplate;
      }
    }

    currentQuestionnaireParticipant.setLocale(language);

    currentQuestionnaireParticipant = getPersistenceManager().save(currentQuestionnaireParticipant);

    return currentQuestionnaireParticipant;
  }

  public void resume(Participant participant) {
    QuestionnaireParticipant questionnaireParticipantTemplate = new QuestionnaireParticipant();
    questionnaireParticipantTemplate.setParticipant(participant);
    questionnaireParticipantTemplate.setQuestionnaireName(currentQuestionnaire.getName());
    questionnaireParticipantTemplate.setQuestionnaireVersion(currentQuestionnaire.getVersion());

    currentQuestionnaireParticipant = getPersistenceManager().matchOne(questionnaireParticipantTemplate);
  }

  public Page getCurrentPage() {
    return currentPage;
  }

  public Page getResumePage() {
    return navigationStrategy.getPageOnResume(this, currentQuestionnaireParticipant);
  }

  public Page startPage() {
    currentPage = navigationStrategy.getPageOnStart(this);

    updateResumePage(currentPage);

    return currentPage;
  }

  public Page lastPage() {
    currentPage = navigationStrategy.getPageOnLast(this);

    updateResumePage(currentPage);

    return currentPage;
  }

  public Page previousPage() {
    currentPage = navigationStrategy.getPageOnPrevious(this, getCurrentPage());

    if(currentPage != null) {
      updateResumePage(currentPage);
    }

    return currentPage;
  }

  public Page nextPage() {
    currentPage = navigationStrategy.getPageOnNext(this, getCurrentPage());

    if(currentPage != null) {
      updateResumePage(currentPage);
    } else {
      // will resume to first page
      updateResumePage(navigationStrategy.getPageOnStart(this));
    }

    return currentPage;
  }

  public Page resumePage() {
    currentPage = navigationStrategy.getPageOnResume(this, currentQuestionnaireParticipant);
    return currentPage;
  }

  public boolean isOnStartPage() {
    return (currentPage != null && currentPage.equals(navigationStrategy.getPageOnStart(this)));
  }

  public boolean isOnLastPage() {
    return (currentPage != null && navigationStrategy.getPageOnNext(this, currentPage) == null);
  }

  public void setDefaultLanguage(Locale language) {
    this.defaultLanguage = language;
  }

  public CategoryAnswer answer(QuestionCategory questionCategory) {
    return answer(questionCategory.getQuestion(), questionCategory, null, null);
  }

  public CategoryAnswer answer(QuestionCategory questionCategory, OpenAnswerDefinition openAnswerDefinition, Data value) {
    return answer(questionCategory.getQuestion(), questionCategory, openAnswerDefinition, value);
  }

  public CategoryAnswer answer(Question question, QuestionCategory questionCategory) {
    return answer(question, questionCategory, null, null);
  }

  public String getComment(Question question) {
    QuestionAnswer template = new QuestionAnswer();
    template.setQuestionnaireParticipant(currentQuestionnaireParticipant);
    template.setQuestionName(question.getName());
    QuestionAnswer questionAnswer = getPersistenceManager().matchOne(template);
    if(questionAnswer != null) {
      return questionAnswer.getComment();
    } else {
      return null;
    }
  }

  public QuestionAnswer addComment(Question question, String comment) {
    QuestionAnswer template = new QuestionAnswer();
    template.setQuestionnaireParticipant(currentQuestionnaireParticipant);
    template.setQuestionName(question.getName());
    QuestionAnswer questionAnswer = getPersistenceManager().matchOne(template);

    if(questionAnswer == null) {
      questionAnswer = template;
    }

    questionAnswer.setComment(comment);
    getPersistenceManager().save(questionAnswer);

    return questionAnswer;
  }

  public CategoryAnswer answer(Question question, QuestionCategory questionCategory, OpenAnswerDefinition openAnswerDefinition, Data value) {
    QuestionAnswer template = new QuestionAnswer();
    template.setQuestionnaireParticipant(currentQuestionnaireParticipant);
    template.setQuestionName(question.getName());

    QuestionAnswer questionAnswer = getPersistenceManager().matchOne(template);
    CategoryAnswer categoryTemplate = new CategoryAnswer();
    categoryTemplate.setCategoryName(questionCategory.getCategory().getName());
    CategoryAnswer categoryAnswer;

    OpenAnswer openAnswerTemplate = new OpenAnswer();
    if(openAnswerDefinition != null) openAnswerTemplate.setOpenAnswerDefinitionName(openAnswerDefinition.getName());
    OpenAnswer openAnswer = null;

    if(questionAnswer == null) {
      questionAnswer = getPersistenceManager().save(template);
      categoryAnswer = categoryTemplate;
      categoryAnswer.setQuestionAnswer(questionAnswer);
    } else {
      categoryTemplate.setQuestionAnswer(questionAnswer);
      categoryAnswer = getPersistenceManager().matchOne(categoryTemplate);
      if(categoryAnswer == null) {
        categoryAnswer = categoryTemplate;
      } else {
        openAnswerTemplate.setCategoryAnswer(categoryAnswer);
        openAnswer = getPersistenceManager().matchOne(openAnswerTemplate);
      }
    }

    if(value != null) {
      if(openAnswer == null) openAnswer = openAnswerTemplate;
      openAnswer.setDataType(value.getType());
      openAnswer.setData(value);
      getPersistenceManager().save(openAnswer);
      categoryAnswer.addOpenAnswer(openAnswer);
    }

    categoryAnswer.setActive(true);
    return getPersistenceManager().save(categoryAnswer);
  }

  public void deleteAnswer(QuestionCategory questionCategory) {
    deleteAnswer(questionCategory.getQuestion(), questionCategory);
  }

  public void deleteAnswer(Question question, QuestionCategory questionCategory) {
    CategoryAnswer categoryAnswer = findAnswer(question, questionCategory);
    if(categoryAnswer != null) {
      deleteAnswers(categoryAnswer);
    }

    // TODO deal with category answer parent
  }

  public void deleteAnswers(Question question) {
    QuestionAnswer questionAnswer = null;

    for(CategoryAnswer categoryAnswer : findAnswers(question)) {
      if(questionAnswer == null) questionAnswer = categoryAnswer.getQuestionAnswer();
      deleteAnswers(categoryAnswer);
    }

    // TODO deal with category answer parent
  }

  /**
   * Delete the category answer and its open answers.
   * @param categoryAnswer
   */
  private void deleteAnswers(CategoryAnswer categoryAnswer) {
    if(categoryAnswer.getOpenAnswers() != null) {
      for(OpenAnswer openAnswer : categoryAnswer.getOpenAnswers()) {
        getPersistenceManager().delete(openAnswer);
      }
    }
    getPersistenceManager().delete(categoryAnswer);
  }

  public void setActiveAnswers(Question question, boolean active) {
    for(CategoryAnswer categoryAnswer : findAnswers(question)) {
      categoryAnswer.setActive(active);
      getPersistenceManager().save(categoryAnswer);
    }

    for(Question questionChild : question.getQuestions()) {
      setActiveAnswers(questionChild, active);
    }
  }

  public void stopCurrentQuestionnaire() {
    if(currentQuestionnaireParticipant != null) {
      currentQuestionnaireParticipant = null;
      currentQuestionnaire = null;
    }
  }

  public QuestionnaireParticipant getQuestionnaireParticipant() {
    return (getPersistenceManager().refresh(currentQuestionnaireParticipant));
  }

  private void updateResumePage(Page resumePage) {
    currentQuestionnaireParticipant = getQuestionnaireParticipant();

    if(currentQuestionnaireParticipant != null) {
      currentQuestionnaireParticipant.setResumePage(resumePage.getName());
      getPersistenceManager().save(currentQuestionnaireParticipant);
    }
  }
}
