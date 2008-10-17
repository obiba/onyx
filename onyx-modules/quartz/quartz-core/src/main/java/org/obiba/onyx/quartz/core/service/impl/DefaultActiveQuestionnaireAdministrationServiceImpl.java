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
import java.util.Locale;

import org.obiba.core.service.impl.PersistenceManagerAwareService;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.quartz.core.domain.answer.CategoryAnswer;
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
public class DefaultActiveQuestionnaireAdministrationServiceImpl extends PersistenceManagerAwareService implements ActiveQuestionnaireAdministrationService {

  private Questionnaire currentQuestionnaire;

  private QuestionnaireParticipant currentQuestionnaireParticipant;

  private Locale defaultLanguage;

  private INavigationStrategy navigationStrategy;
  
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
    // TODO Auto-generated method stub
    return null;
  }

  public Page getStartPage() {
    return navigationStrategy.getPageOnStart(this);
  }
  
  public Page getPreviousPage() {
    return navigationStrategy.getPageOnPrevious(this, getCurrentPage());    
  }
  
  public Page getNextPage() {
    return navigationStrategy.getPageOnNext(this, getCurrentPage());
  }
  
  public void setDefaultLanguage(Locale language) {
    this.defaultLanguage = language;
  }

  public CategoryAnswer answer(QuestionCategory questionCategory, Data value) {
    // TODO Auto-generated method stub
    return null;
  }

  public void deleteAnswer(QuestionCategory questionCategory) {
    // TODO Auto-generated method stub
    
  }

  public void deleteAnswers(Question question) {
    // TODO Auto-generated method stub
    
  }

  public CategoryAnswer findAnswer(QuestionCategory questionCategory) {
    // TODO Auto-generated method stub
    return null;
  }

  public List<CategoryAnswer> findAnswers(Question question) {
    // TODO Auto-generated method stub
    return null;
  }

  public void setActiveAnswers(Question question, boolean active) {
    // TODO Auto-generated method stub
    
  }

  

}
