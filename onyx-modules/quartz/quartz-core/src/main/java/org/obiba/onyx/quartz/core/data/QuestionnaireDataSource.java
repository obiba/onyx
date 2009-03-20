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
import org.obiba.onyx.quartz.core.domain.answer.CategoryAnswer;
import org.obiba.onyx.quartz.core.domain.answer.OpenAnswer;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundle;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundleManager;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireFinder;
import org.obiba.onyx.quartz.core.service.QuestionnaireParticipantService;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Used to get open answer data and unit
 */
public class QuestionnaireDataSource implements IDataSource {

  private static final long serialVersionUID = 1L;

  private static final Logger log = LoggerFactory.getLogger(QuestionnaireDataSource.class);

  private QuestionnaireParticipantService questionnaireParticipantService;

  private QuestionnaireBundleManager questionnaireBundleManager;

  private String questionnaire;

  private String question;

  private String category;

  private String openAnswerDefinition;

  public QuestionnaireDataSource(String questionnaire, String question) {
    super();
    initialize(questionnaire, question, null, null);
  }

  public QuestionnaireDataSource(String questionnaire, String question, String category) {
    super();
    initialize(questionnaire, question, category, null);
  }

  public QuestionnaireDataSource(String questionnaire, String question, String category, String openAnswerDefinition) {
    super();
    initialize(questionnaire, question, category, openAnswerDefinition);
  }

  private void initialize(String questionnaire, String question, String category, String openAnswerDefinition) {
    if(questionnaire == null) throw new IllegalArgumentException("Questionnaire name cannot be null.");
    this.questionnaire = questionnaire;
    if(question == null) throw new IllegalArgumentException("Question name cannot be null.");
    this.question = question;
    if(openAnswerDefinition != null && category == null) throw new IllegalArgumentException("Both category and open answer name cannot be null.");
    this.category = category;
    this.openAnswerDefinition = openAnswerDefinition;
  }

  public Data getData(Participant participant) {
    if(participant == null) return null;

    Data data = null;
    if(openAnswerDefinition != null) {
      // get the open answer
      OpenAnswer openAnswer = questionnaireParticipantService.getOpenAnswer(participant, questionnaire, question, category, openAnswerDefinition);

      if(openAnswer != null && openAnswer.getCategoryAnswer().getActive()) {
        data = openAnswer.getData();
      } else {

      }
    } else if(category != null) {
      // get if category was selected
      CategoryAnswer categoryAnswer = questionnaireParticipantService.getCategoryAnswer(participant, questionnaire, question, category);

      if(categoryAnswer != null) {
        data = DataBuilder.buildBoolean(categoryAnswer.isActive());
      } else {
        data = DataBuilder.buildBoolean((Boolean) null);
      }
    } else {
      // get if question was asked (but not necessarily answered)
      Boolean active = questionnaireParticipantService.isQuestionActive(participant, questionnaire, question);
      data = DataBuilder.buildBoolean(active);
    }

    log.debug("data={}", data);
    return data;
  }

  public String getUnit() {
    if(openAnswerDefinition != null) {
      QuestionnaireBundle questionnaireBundle = questionnaireBundleManager.getBundle(questionnaire);

      if(questionnaireBundle != null) {
        QuestionnaireFinder questionnaireFinder = QuestionnaireFinder.getInstance(questionnaireBundle.getQuestionnaire());

        if(questionnaireFinder != null) {
          OpenAnswerDefinition openAnswerDefinitionObject = questionnaireFinder.findOpenAnswerDefinition(openAnswerDefinition);
          return (openAnswerDefinitionObject != null) ? openAnswerDefinitionObject.getUnit() : null;
        }
      }
    }
    return null;
  }

  public void setQuestionnaireParticipantService(QuestionnaireParticipantService questionnaireParticipantService) {
    this.questionnaireParticipantService = questionnaireParticipantService;
  }

  public void setQuestionnaireBundleManager(QuestionnaireBundleManager questionnaireBundleManager) {
    this.questionnaireBundleManager = questionnaireBundleManager;
  }

}
