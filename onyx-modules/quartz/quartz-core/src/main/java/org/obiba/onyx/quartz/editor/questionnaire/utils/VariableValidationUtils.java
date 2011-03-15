/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.questionnaire.utils;

import java.util.Map;

import org.obiba.magma.Variable;
import org.obiba.onyx.core.data.ComparingDataSource;
import org.obiba.onyx.core.data.VariableDataSource;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.finder.QuestionnaireCache;
import org.obiba.onyx.quartz.editor.variable.VariableUtils;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.CollectionUtils;

public class VariableValidationUtils {

  private VariableUtils variableUtils;

  /**
   * Return item where variable is used in
   * @param variable
   * @param questionnaireCache
   * @return
   */
  public Question findUsedInQuestion(Variable variable, QuestionnaireCache questionnaireCache) {
    if(variable == null) return null;
    Map<String, Question> questions = questionnaireCache.getQuestionCache();
    for(Question question : questions.values()) {
      if(question.getCondition() instanceof VariableDataSource) {
        Variable variableFound = variableUtils.findVariable((VariableDataSource) question.getCondition());
        if(variable.equals(variableFound)) {
          return question;
        }
      }
    }
    return null;
  }

  /**
   * Return item where variable is used in
   * @param variable
   * @param questionnaireCache
   * @return
   */
  public OpenAnswerDefinition findUsedInOpenAnswer(Variable variable, QuestionnaireCache questionnaireCache) {
    if(variable == null) return null;
    for(OpenAnswerDefinition openAnswer : questionnaireCache.getOpenAnswerDefinitionCache().values()) {
      if(CollectionUtils.isEmpty(openAnswer.getOpenAnswerDefinitions())) {
        for(ComparingDataSource comparingDataSource : openAnswer.getValidationDataSources()) {
          VariableDataSource variableDataSource = (VariableDataSource) comparingDataSource.getDataSourceRight();
          Variable variableFound = variableUtils.findVariable(variableDataSource);
          if(variable.equals(variableFound)) {
            return openAnswer;
          }
        }
      }
    }
    return null;
  }

  @Required
  public void setVariableUtils(VariableUtils variableUtils) {
    this.variableUtils = variableUtils;
  }

}
