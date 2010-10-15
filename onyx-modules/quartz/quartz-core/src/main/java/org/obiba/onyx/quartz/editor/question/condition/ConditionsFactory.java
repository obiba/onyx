/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.question.condition;

import org.apache.commons.lang.StringUtils;
import org.obiba.onyx.core.data.ComparingDataSource;
import org.obiba.onyx.core.data.ComputingDataSource;
import org.obiba.onyx.core.data.FixedDataSource;
import org.obiba.onyx.core.data.IDataSource;
import org.obiba.onyx.core.data.ParticipantPropertyDataSource;
import org.obiba.onyx.core.domain.participant.Gender;
import org.obiba.onyx.quartz.core.data.QuestionnaireDataSource;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundleManager;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireFinder;
import org.obiba.onyx.quartz.editor.question.condition.datasource.ComparingDS;
import org.obiba.onyx.quartz.editor.question.condition.datasource.QuestionnaireDS;
import org.obiba.onyx.util.data.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

/**
 *
 */
public class ConditionsFactory {

  private final transient Logger log = LoggerFactory.getLogger(getClass());

  private QuestionnaireBundleManager questionnaireBundleManager;

  public Conditions create(Question question) {
    Conditions conditions = new Conditions();
    IDataSource conditionDataSource = question.getCondition();
    if(conditionDataSource != null) {
      if(conditionDataSource instanceof ComputingDataSource) {
        ComputingDataSource computingDataSource = (ComputingDataSource) conditionDataSource;
        conditions.setExpression(computingDataSource.getExpression());
        int index = 1;
        for(IDataSource dataSource : computingDataSource.getDataSources()) {
          if(dataSource instanceof QuestionnaireDataSource) {
            QuestionnaireDataSource questionnaireDataSource = (QuestionnaireDataSource) dataSource;
            QuestionnaireDS questionnaireDS = new QuestionnaireDS();
            questionnaireDS.setQuestionnaire(questionnaireBundleManager.getBundle(questionnaireDataSource.getQuestionnaire()).getQuestionnaire());
            if(questionnaireDS.getQuestionnaire().getQuestionnaireCache() == null) {
              QuestionnaireFinder.getInstance(questionnaireDS.getQuestionnaire()).buildQuestionnaireCache();
            }
            questionnaireDS.setQuestion(questionnaireDS.getQuestionnaire().getQuestionnaireCache().getQuestionCache().get(questionnaireDataSource.getQuestion()));
            questionnaireDS.setCategory(StringUtils.isBlank(questionnaireDataSource.getCategory()) ? null : questionnaireDS.getQuestion().getCategoriesByName().get(questionnaireDataSource.getCategory()));
            questionnaireDS.setOpenAnswerDefinition(StringUtils.isBlank(questionnaireDataSource.getOpenAnswerDefinition()) || questionnaireDS.getCategory() == null ? null : questionnaireDS.getCategory().getOpenAnswerDefinitionsByName().get(questionnaireDataSource.getOpenAnswerDefinition()));
            questionnaireDS.setVariable(index++);
            conditions.getQuestionnaireDataSources().add(questionnaireDS);
          }
        }
      } else if(conditionDataSource instanceof ComparingDataSource) {
        ComparingDataSource comparingDataSource = (ComparingDataSource) conditionDataSource;
        IDataSource left = comparingDataSource.getDataSourceLeft();
        IDataSource right = comparingDataSource.getDataSourceRight();
        if(left instanceof ParticipantPropertyDataSource && right instanceof FixedDataSource) {
          ParticipantPropertyDataSource participantPropertyDataSource = (ParticipantPropertyDataSource) left;
          FixedDataSource fixedDataSource = (FixedDataSource) right;
          String property = participantPropertyDataSource.getProperty();
          Data data = fixedDataSource.getData(null);
          if(StringUtils.isBlank(property)) {
            ComparingDS comparingDS = new ComparingDS();
            comparingDS.setOperator(comparingDataSource.getComparisonOperator());
            comparingDS.setType(data.getType().name());
            comparingDS.setValue(data.getValueAsString());
            conditions.getComparingDataSources().add(comparingDS);
            comparingDS.setVariable(conditions.getNbDataSources());
          } else if(ComparingDS.GENDER_TYPE.equals(property)) {
            ComparingDS comparingDS = new ComparingDS();
            comparingDS.setOperator(comparingDataSource.getComparisonOperator());
            comparingDS.setType(ComparingDS.GENDER_TYPE);
            comparingDS.setGender(Gender.valueOf(data.getValueAsString()));
            conditions.getComparingDataSources().add(comparingDS);
            comparingDS.setVariable(conditions.getNbDataSources());
          }
        }
      }
    }

    if(log.isInfoEnabled()) {
      log.info("expression: " + conditions.getExpression());
      for(QuestionnaireDS ds : conditions.getQuestionnaireDataSources()) {
        log.info(ds.getVariable() + ", questionnaire: " + ds.getQuestionnaire() + ", question: " + ds.getQuestion() + ", category: " + ds.getCategory() + ", open: " + ds.getOpenAnswerDefinition());
      }
      for(ComparingDS ds : conditions.getComparingDataSources()) {
        log.info(ds.getVariable() + ", type: " + ds.getType() + ", value: " + ds.getValue() + ", gender: " + ds.getGender() + ", operator: " + ds.getOperator());
      }
    }

    return conditions;
  }

  @Required
  public void setQuestionnaireBundleManager(QuestionnaireBundleManager questionnaireBundleManager) {
    this.questionnaireBundleManager = questionnaireBundleManager;
  }

}
