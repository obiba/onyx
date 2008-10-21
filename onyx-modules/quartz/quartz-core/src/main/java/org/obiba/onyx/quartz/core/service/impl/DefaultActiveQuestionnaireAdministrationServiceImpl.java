/***********************************************************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 **********************************************************************************************************************/
package org.obiba.onyx.quartz.core.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.obiba.core.service.impl.PersistenceManagerAwareService;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.quartz.core.domain.answer.CategoryAnswer;
import org.obiba.onyx.quartz.core.domain.answer.QuestionAnswer;
import org.obiba.onyx.quartz.core.domain.answer.QuestionnaireParticipant;
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

    if(currentQuestionnaireParticipant != null) throw new IllegalArgumentException("Invalid questionnaireParticipant for specified questionnaire");

    QuestionnaireParticipant questionnaireParticipantTemplate = new QuestionnaireParticipant();
    questionnaireParticipantTemplate.setParticipant(participant);
    questionnaireParticipantTemplate.setQuestionnaireName(currentQuestionnaire.getName());
    questionnaireParticipantTemplate.setQuestionnaireVersion(currentQuestionnaire.getVersion());
    questionnaireParticipantTemplate.setLocale(language);

    currentQuestionnaireParticipant = getPersistenceManager().save(questionnaireParticipantTemplate);

    return currentQuestionnaireParticipant;
  }

  public Page getCurrentPage() {
    return currentPage;
  }

  public Page startPage() {
    currentPage = navigationStrategy.getPageOnStart(this);
    return currentPage;
  }

  public Page previousPage() {
    currentPage = navigationStrategy.getPageOnPrevious(this, getCurrentPage());
    return currentPage;
  }

  public Page nextPage() {
    currentPage = navigationStrategy.getPageOnNext(this, getCurrentPage());

    if(currentQuestionnaireParticipant != null) {
      currentQuestionnaireParticipant.setResumePage(currentPage != null ? currentPage.getName() : null);
      getPersistenceManager().save(currentQuestionnaireParticipant);
    }

    return currentPage;
  }

  public Page resumePage() {
    currentPage = navigationStrategy.getPageOnResume(this, currentQuestionnaireParticipant);
    return currentPage;
  }

  public void setDefaultLanguage(Locale language) {
    this.defaultLanguage = language;
  }

  public CategoryAnswer answer(QuestionCategory questionCategory, Data value) {
    QuestionAnswer template = new QuestionAnswer();
    template.setQuestionnaireParticipant(currentQuestionnaireParticipant);
    template.setQuestionName(questionCategory.getQuestion().getName());

    QuestionAnswer questionAnswer = getPersistenceManager().matchOne(template);
    CategoryAnswer categoryTemplate = new CategoryAnswer();
    categoryTemplate.setCategoryName(questionCategory.getCategory().getName());
    CategoryAnswer categoryAnswer;

    if(questionAnswer == null) {
      questionAnswer = getPersistenceManager().save(template);
      categoryAnswer = categoryTemplate;
      categoryAnswer.setQuestionAnswer(questionAnswer);
    } else {
      categoryTemplate.setQuestionAnswer(questionAnswer);
      categoryAnswer = getPersistenceManager().matchOne(categoryTemplate);
      if(categoryAnswer == null) categoryAnswer = categoryTemplate;
    }

    if(value != null) {
      categoryAnswer.setDataType(value.getType());
      categoryAnswer.setData(value);
    }

    return getPersistenceManager().save(categoryAnswer);
  }

  public void deleteAnswer(QuestionCategory questionCategory) {
    List<QuestionAnswer> questionAnswers = new ArrayList<QuestionAnswer>();

    CategoryAnswer categoryAnswer = findAnswer(questionCategory);
    questionAnswers.add(categoryAnswer.getQuestionAnswer());

    // Pas pour ce scrum
    /*
     * for(CategoryAnswer childAnswer : categoryAnswer.getChildrenCategoryAnswers()) {
     * if(!questionAnswers.contains(childAnswer.getQuestionAnswer()))
     * questionAnswers.add(childAnswer.getQuestionAnswer()); getPersistenceManager().delete(childAnswer); }
     */
    getPersistenceManager().delete(categoryAnswer);

    for(QuestionAnswer questionAnswer : questionAnswers) {
      if(questionAnswer.getCategoryAnswers() == null) getPersistenceManager().delete(questionAnswer);
    }
  }

  public void deleteAnswers(Question question) {
    QuestionAnswer questionAnswer = new QuestionAnswer();

    for(CategoryAnswer categoryAnswer : findAnswers(question)) {
      if(questionAnswer == null) questionAnswer = categoryAnswer.getQuestionAnswer();
      getPersistenceManager().delete(categoryAnswer);

      // Pas pour ce scrum
      /*
       * for(CategoryAnswer childAnswer : categoryAnswer.getChildrenCategoryAnswers()) {
       * if(!questionAnswers.contains(childAnswer.getQuestionAnswer()))
       * questionAnswers.add(childAnswer.getQuestionAnswer()); getPersistenceManager().delete(childAnswer); }
       */
    }

    if(questionAnswer.getCategoryAnswers() == null) getPersistenceManager().delete(questionAnswer);

    // Pas pour ce scrum
    /*
     * for(Question questionChild : question.getQuestions()) { deleteAnswers(questionChild); }
     */
  }

  public void setActiveAnswers(Question question, boolean active) {
    for(CategoryAnswer template : findAnswers(question)) {
      CategoryAnswer categoryAnswer = getPersistenceManager().matchOne(template);
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

  protected QuestionnaireParticipant getQuestionnaireParticipant() {
    return (getPersistenceManager().refresh(currentQuestionnaireParticipant));
  }

}
