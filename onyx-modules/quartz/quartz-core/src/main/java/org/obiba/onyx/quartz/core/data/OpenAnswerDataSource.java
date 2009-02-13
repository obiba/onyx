/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.data;

import org.obiba.onyx.core.data.IDataSource;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.quartz.core.domain.answer.OpenAnswer;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundle;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundleManager;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireFinder;
import org.obiba.onyx.quartz.core.service.QuestionnaireParticipantService;
import org.obiba.onyx.util.data.Data;

/**
 * Used to get open answer data and unit
 */
public class OpenAnswerDataSource implements IDataSource {

  private static final long serialVersionUID = 1L;

  private QuestionnaireParticipantService questionnaireParticipantService;

  private QuestionnaireBundleManager questionnaireBundleManager;

  private String questionnaire;

  private String question;

  private String category;

  private String openAnswerDefinition;

  public Data getData(Participant participant) {
    if(participant == null) return null;

    OpenAnswer openAnswer = questionnaireParticipantService.getOpenAnswer(participant, questionnaire, question, category, openAnswerDefinition);
    if(openAnswer != null && openAnswer.getCategoryAnswer().getActive()) return openAnswer.getData();

    return null;
  }

  public String getUnit() {
    QuestionnaireBundle questionnaireBundle = questionnaireBundleManager.getBundle(questionnaire);

    if(questionnaireBundle != null) {
      QuestionnaireFinder questionnaireFinder = QuestionnaireFinder.getInstance(questionnaireBundle.getQuestionnaire());

      if(questionnaireFinder != null) {
        OpenAnswerDefinition openAnswerDefinitionObject = questionnaireFinder.findOpenAnswerDefinition(openAnswerDefinition);
        return (openAnswerDefinitionObject != null) ? openAnswerDefinitionObject.getUnit() : null;
      }
    }
    return null;
  }

  public OpenAnswerDataSource(String questionnaire, String question, String category, String openAnswerDefinition) {
    super();
    if(questionnaire == null) throw new IllegalArgumentException("Questionnaire name cannot be null.");
    this.questionnaire = questionnaire;
    if(question == null) throw new IllegalArgumentException("Question name cannot be null.");
    this.question = question;
    if(category == null) throw new IllegalArgumentException("Category name cannot be null.");
    this.category = category;
    if(openAnswerDefinition == null) throw new IllegalArgumentException("OpenAnswerDefinition name cannot be null.");
    this.openAnswerDefinition = openAnswerDefinition;
  }

  public void setQuestionnaireParticipantService(QuestionnaireParticipantService questionnaireParticipantService) {
    this.questionnaireParticipantService = questionnaireParticipantService;
  }

  public void setQuestionnaireBundleManager(QuestionnaireBundleManager questionnaireBundleManager) {
    this.questionnaireBundleManager = questionnaireBundleManager;
  }

}
