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
import org.obiba.onyx.core.data.ComputingDataSource;
import org.obiba.onyx.core.data.IDataSource;
import org.obiba.onyx.quartz.core.data.QuestionnaireDataSource;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundleManager;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireFinder;
import org.springframework.beans.factory.annotation.Required;

/**
 *
 */
public class ConditionsFactory {

  private QuestionnaireBundleManager questionnaireBundleManager;

  public Conditions create(Question question) {
    Conditions conditions = new Conditions();
    IDataSource conditionDataSource = question.getCondition();
    if(conditionDataSource != null) {
      if(conditionDataSource instanceof ComputingDataSource) {
        ComputingDataSource computingDataSource = (ComputingDataSource) conditionDataSource;
        conditions.setExpression(computingDataSource.getExpression());
        for(IDataSource dataSource : computingDataSource.getDataSources()) {
          if(dataSource instanceof QuestionnaireDataSource) {
            QuestionnaireDataSource questionnaireDataSource = (QuestionnaireDataSource) dataSource;
            ConditionDataSource cds = new ConditionDataSource();
            cds.setQuestionnaire(questionnaireBundleManager.getBundle(questionnaireDataSource.getQuestionnaire()).getQuestionnaire());
            if(cds.getQuestionnaire().getQuestionnaireCache() == null) {
              QuestionnaireFinder.getInstance(cds.getQuestionnaire()).buildQuestionnaireCache();
            }
            cds.setQuestion(cds.getQuestionnaire().getQuestionnaireCache().getQuestionCache().get(questionnaireDataSource.getQuestion()));
            cds.setCategory(StringUtils.isBlank(questionnaireDataSource.getCategory()) ? null : cds.getQuestion().getCategoriesByName().get(questionnaireDataSource.getCategory()));
            cds.setOpenAnswerDefinition(StringUtils.isBlank(questionnaireDataSource.getOpenAnswerDefinition()) || cds.getCategory() == null ? null : cds.getCategory().getOpenAnswerDefinitionsByName().get(questionnaireDataSource.getOpenAnswerDefinition()));
            conditions.getDataSources().add(cds);
          }
        }
      }
    }
    return conditions;
  }

  @Required
  public void setQuestionnaireBundleManager(QuestionnaireBundleManager questionnaireBundleManager) {
    this.questionnaireBundleManager = questionnaireBundleManager;
  }

}
