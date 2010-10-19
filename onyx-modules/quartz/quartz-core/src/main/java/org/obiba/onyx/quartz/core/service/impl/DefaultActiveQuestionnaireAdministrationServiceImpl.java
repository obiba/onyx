/***********************************************************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 **********************************************************************************************************************/
package org.obiba.onyx.quartz.core.service.impl;

import java.util.Date;
import java.util.Locale;

import org.obiba.core.service.impl.PersistenceManagerAwareService;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.quartz.core.domain.answer.CategoryAnswer;
import org.obiba.onyx.quartz.core.domain.answer.OpenAnswer;
import org.obiba.onyx.quartz.core.domain.answer.QuestionAnswer;
import org.obiba.onyx.quartz.core.domain.answer.QuestionnaireMetric;
import org.obiba.onyx.quartz.core.domain.answer.QuestionnaireParticipant;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;
import org.obiba.onyx.quartz.core.service.INavigationStrategy;
import org.obiba.onyx.util.data.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public abstract class DefaultActiveQuestionnaireAdministrationServiceImpl extends PersistenceManagerAwareService implements ActiveQuestionnaireAdministrationService {

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(DefaultActiveQuestionnaireAdministrationServiceImpl.class);

  private boolean questionnaireDevelopmentMode = false;

  protected ActiveInterviewService activeInterviewService;

  private Questionnaire currentQuestionnaire;

  private Locale defaultLanguage;

  private Locale currentLanguage = null;

  private INavigationStrategy navigationStrategy;

  private Page currentPage;

  @Override
  public boolean isQuestionnaireDevelopmentMode() {
    return questionnaireDevelopmentMode;
  }

  @Override
  public void setQuestionnaireDevelopmentMode(boolean mode) {
    questionnaireDevelopmentMode = mode;
  }

  public Questionnaire getQuestionnaire() {
    return currentQuestionnaire;
  }

  public void setActiveInterviewService(ActiveInterviewService activeInterviewService) {
    this.activeInterviewService = activeInterviewService;
  }

  public void setQuestionnaire(Questionnaire questionnaire) {
    this.currentQuestionnaire = questionnaire;
  }

  public Locale getLanguage() {
    if(questionnaireDevelopmentMode) return defaultLanguage;

    Locale language = currentLanguage;

    if(currentLanguage == null) {
      QuestionnaireParticipant currentQuestionnaireParticipant = getQuestionnaireParticipant();
      if(currentQuestionnaireParticipant != null) {
        currentLanguage = currentQuestionnaireParticipant.getLocale();
        language = currentLanguage;
      }
    }

    if(language == null) {
      language = defaultLanguage;
    }

    return language;
  }

  public void setNavigationStrategy(INavigationStrategy navigationStrategy) {
    this.navigationStrategy = navigationStrategy;
  }

  public QuestionnaireParticipant start(Participant participant, Locale language) {
    currentLanguage = null;
    questionnaireDevelopmentMode = false;

    QuestionnaireParticipant currentQuestionnaireParticipant = getQuestionnaireParticipant();

    if(currentQuestionnaireParticipant == null) {
      currentQuestionnaireParticipant = new QuestionnaireParticipant();
      currentQuestionnaireParticipant.setParticipant(participant);
      currentQuestionnaireParticipant.setQuestionnaireName(currentQuestionnaire.getName());
    }

    currentQuestionnaireParticipant.setQuestionnaireVersion(currentQuestionnaire.getVersion());
    currentQuestionnaireParticipant.setUser(activeInterviewService.getOperator());
    currentQuestionnaireParticipant.setLocale(language);
    currentQuestionnaireParticipant.setTimeStart(new Date());

    currentQuestionnaireParticipant = getPersistenceManager().save(currentQuestionnaireParticipant);

    return currentQuestionnaireParticipant;
  }

  public void end() {
    QuestionnaireParticipant currentQuestionnaireParticipant = getQuestionnaireParticipant();

    if(currentQuestionnaireParticipant == null) {
      throw new IllegalArgumentException("Null QuestionnaireParticipant");
    }

    currentLanguage = null;

    currentQuestionnaireParticipant.setTimeEnd(new Date());
    getPersistenceManager().save(currentQuestionnaireParticipant);
  }

  public Page getCurrentPage() {
    return currentPage;
  }

  public int getCurrentPageNumber() {
    return getQuestionnaire().getPages().indexOf(getCurrentPage()) + 1;
  }

  public int getLastPageNumber() {
    return getQuestionnaire().getPages().indexOf(navigationStrategy.getPageOnLast(this)) + 1;
  }

  public Page getResumePage() {
    return navigationStrategy.getPageOnResume(this, getQuestionnaireParticipant());
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

  public Page beginPage() {
    currentPage = navigationStrategy.getPageOnBegin(this, getCurrentPage());

    if(currentPage != null) {
      // Make inactive the answers to all questions on subsequent pages.
      boolean subsequentPage = false;
      for(Page page : getQuestionnaire().getPages()) {
        if(page.getName() != null && page.getName().equals(currentPage.getName())) {
          subsequentPage = true;
        }
        if(subsequentPage) {
          for(Question question : page.getQuestions()) {
            setActiveAnswers(question, false);
          }
        }
      }

      updateResumePage(currentPage);
    } else {
      // will resume to first page
      updateResumePage(navigationStrategy.getPageOnStart(this));
    }

    return currentPage;
  }

  public Page endPage() {
    currentPage = navigationStrategy.getPageOnEnd(this, getCurrentPage());

    if(currentPage != null) {
      updateResumePage(currentPage);
    } else {
      // will resume to first page
      updateResumePage(navigationStrategy.getPageOnStart(this));
    }

    return currentPage;
  }

  public Page resumePage() {
    questionnaireDevelopmentMode = false;
    currentPage = navigationStrategy.getPageOnResume(this, getQuestionnaireParticipant());
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
    template.setQuestionnaireParticipant(getQuestionnaireParticipant());
    template.setQuestionName(question.getName());
    QuestionAnswer questionAnswer = getPersistenceManager().matchOne(template);

    String comment = null;
    if(questionAnswer != null) {
      comment = questionAnswer.getComment();
    }

    return comment;
  }

  public void setComment(Question question, String comment) {
    QuestionAnswer template = new QuestionAnswer();
    template.setQuestionnaireParticipant(getQuestionnaireParticipant());
    template.setQuestionName(question.getName());
    QuestionAnswer questionAnswer = getPersistenceManager().matchOne(template);

    if(comment == null || comment.trim().length() == 0) {
      if(questionAnswer != null) {
        questionAnswer.setComment(null);
        getPersistenceManager().save(questionAnswer);
      }
    } else {
      if(questionAnswer == null) {
        questionAnswer = template;
      }

      questionAnswer.setComment(comment);
      getPersistenceManager().save(questionAnswer);
    }
  }

  public CategoryAnswer answer(Question question, QuestionCategory questionCategory, OpenAnswerDefinition openAnswerDefinition, Data value) {

    // A "no-answer" category answer is no longer required, since a "real answer" to the question is provided.
    deleteNoAnswerCategoryAnswer(question);

    QuestionAnswer questionAnswer = findAnswer(question);

    CategoryAnswer categoryTemplate = new CategoryAnswer();
    categoryTemplate.setCategoryName(questionCategory.getCategory().getName());
    CategoryAnswer categoryAnswer;

    OpenAnswer openAnswerTemplate = new OpenAnswer();
    if(openAnswerDefinition != null) {
      openAnswerTemplate.setOpenAnswerDefinitionName(openAnswerDefinition.getName());
    }

    OpenAnswer openAnswer = null;

    if(questionAnswer == null) {
      // Create a new QuestionAnswer and make it active
      questionAnswer = new QuestionAnswer();
      questionAnswer.setQuestionnaireParticipant(getQuestionnaireParticipant());
      questionAnswer.setQuestionName(question.getName());
      questionAnswer.setActive(true);

      questionAnswer = getPersistenceManager().save(questionAnswer);
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

      // Make sure the QuestionAnswer is active
      questionAnswer.setActive(true);
      questionAnswer = getPersistenceManager().save(questionAnswer);
    }

    categoryAnswer.setActive(true);
    categoryAnswer = getPersistenceManager().save(categoryAnswer);

    if(value != null) {
      if(openAnswer == null) {
        openAnswer = openAnswerTemplate;
      }
      openAnswer.setDataType(value.getType());
      openAnswer.setData(value);
      categoryAnswer.addOpenAnswer(openAnswer);
      getPersistenceManager().save(openAnswer);
    } else if(openAnswer != null) {
      getPersistenceManager().delete(openAnswer);
    }

    return categoryAnswer;
  }

  public void deleteAnswer(QuestionCategory questionCategory) {
    deleteAnswer(questionCategory.getQuestion(), questionCategory);
  }

  public void deleteAnswer(Question question, QuestionCategory questionCategory) {
    CategoryAnswer categoryAnswer = findAnswer(question, questionCategory);
    if(categoryAnswer != null) {
      deleteAnswers(categoryAnswer);
    }

    addNoAnswerCategoryAnswerIfRequired(question);

    // TODO deal with category answer parent
  }

  public void deleteAnswer(Question question, QuestionCategory questionCategory, OpenAnswerDefinition openAnswerDefinition) {
    OpenAnswer openAnswer = findOpenAnswer(question, questionCategory.getCategory(), openAnswerDefinition);
    if(openAnswer != null) {
      getPersistenceManager().delete(openAnswer);
    }

    // TODO deal with category answer parent
  }

  public void deleteAnswers(Question question) {
    for(CategoryAnswer categoryAnswer : findAnswers(question)) {
      deleteAnswers(categoryAnswer);
    }

    addNoAnswerCategoryAnswerIfRequired(question);

    // TODO deal with category answer parent
  }

  /**
   * Delete the category answer and its open answers.
   * @param categoryAnswer
   */
  public void deleteAnswers(CategoryAnswer categoryAnswer) {
    if(categoryAnswer.getOpenAnswers() != null) {
      for(OpenAnswer openAnswer : categoryAnswer.getOpenAnswers()) {
        getPersistenceManager().delete(openAnswer);
      }
    }
    getPersistenceManager().delete(categoryAnswer);
  }

  protected abstract QuestionAnswer findAnswer(Question question);

  public void setActiveAnswers(Question question, boolean active) {
    // question answers are made active (and created if none, in the case of boiler plates)
    QuestionAnswer questionAnswer = findAnswer(question);

    if(questionAnswer == null) {
      questionAnswer = new QuestionAnswer();
      questionAnswer.setQuestionnaireParticipant(getQuestionnaireParticipant());
      questionAnswer.setQuestionName(question.getName());
    }

    questionAnswer.setActive(active);
    getPersistenceManager().save(questionAnswer);

    if(active) {
      addNoAnswerCategoryAnswerIfRequired(question);
    }

    // each CategoryAnswers is made active.
    for(CategoryAnswer categoryAnswer : findAnswers(question)) {
      categoryAnswer.setActive(active);
      getPersistenceManager().save(categoryAnswer);
    }

    // forward the active to child questions
    for(Question questionChild : question.getQuestions()) {
      setActiveAnswers(questionChild, active);
    }
  }

  public QuestionnaireParticipant getQuestionnaireParticipant() {
    // Note: Don't include questionnaire version in the template. This is in case the version
    // of the questionnaire changes between cancelling and re-starting the questionnaire stage.
    QuestionnaireParticipant questionnaireParticipantTemplate = new QuestionnaireParticipant();
    questionnaireParticipantTemplate.setParticipant(activeInterviewService.getParticipant());
    questionnaireParticipantTemplate.setQuestionnaireName(currentQuestionnaire.getName());

    return getPersistenceManager().matchOne(questionnaireParticipantTemplate);
  }

  public void incrementTimeOnPage(int seconds) {
    QuestionnaireParticipant currentQuestionnaireParticipant = getQuestionnaireParticipant();

    if(currentQuestionnaireParticipant != null && getCurrentPage() != null) {
      QuestionnaireMetric questionnaireMetric = currentQuestionnaireParticipant.getQuestionnaireMetric(getCurrentPage().getName());
      questionnaireMetric.incrementDuration(seconds);

      getPersistenceManager().save(questionnaireMetric);
    }
  }

  private void updateResumePage(Page resumePage) {
    QuestionnaireParticipant currentQuestionnaireParticipant = getQuestionnaireParticipant();

    if(currentQuestionnaireParticipant != null) {
      currentQuestionnaireParticipant.setResumePage(resumePage.getName());
      getPersistenceManager().save(currentQuestionnaireParticipant);
    }
  }

  /**
   * If a "no-answer" category exist for the specified question and no answer is provided, answer the question with the
   * "no-answer" category.
   * @param question The question.
   */
  private void addNoAnswerCategoryAnswerIfRequired(Question question) {
    if(question.hasNoAnswerCategory()) {
      if(findAnswers(question).isEmpty()) {
        answer(question, question.getNoAnswerQuestionCategory());
      }
    }
  }

  /**
   * Delete any existing CategoryAnswer corresponding to a "no-answer" category for the specified question.
   * @param question The question.
   */
  private void deleteNoAnswerCategoryAnswer(Question question) {
    Category category;
    for(CategoryAnswer answer : findAnswers(question)) {
      category = getCategory(question, answer);
      if(category != null && category.isNoAnswer()) {
        deleteAnswers(answer);
      }
    }
  }

  public Category getCategory(Question question, CategoryAnswer answer) {
    Category category;
    String categoryName;
    categoryName = answer.getCategoryName();
    if(question.hasCategories()) {
      category = question.findCategory(categoryName);
    } else if(question.getParentQuestion().hasCategories()) {
      category = question.getParentQuestion().findCategory(categoryName);
    } else {
      category = null;
    }
    return category;
  }
}
