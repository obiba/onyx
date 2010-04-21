/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.engine.questionnaire.util;

import java.util.HashMap;
import java.util.Map;

import org.obiba.onyx.quartz.core.engine.questionnaire.IQuestionnaireElement;
import org.obiba.onyx.quartz.core.engine.questionnaire.QuestionnaireVariableNameResolver;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.springframework.util.Assert;

public class QuestionnaireUniqueVariableNameResolver implements QuestionnaireVariableNameResolver {

  private final Map<String, IQuestionnaireElement> variableNames;

  public QuestionnaireUniqueVariableNameResolver() {
    this.variableNames = new HashMap<String, IQuestionnaireElement>();
  }

  public String variableName(Question question) {
    String variableName = question.getVariableName();
    if(variableName == null) {
      String prefix = question.getParentQuestion() != null ? variableName(question.getParentQuestion()) + '.' : "";
      variableName = prefix + question.getName();
    }
    if(variableNames.containsKey(variableName) && question != variableNames.get(variableName)) throw new QuestionnaireVariableNameNotUniqueException(variableName, question, variableNames.get(variableName));
    variableNames.put(variableName, question);
    return variableName;
  }

  public String variableName(Question question, QuestionCategory questionCategory) {
    String variableName = questionCategory.getVariableName(question.getName());
    if(variableName == null) {
      variableName = variableName(question) + '.' + questionCategory.getCategory().getName();
    }
    if(variableNames.containsKey(variableName) && questionCategory != variableNames.get(variableName)) throw new QuestionnaireVariableNameNotUniqueException(variableName, questionCategory, variableNames.get(variableName));
    variableNames.put(variableName, questionCategory);
    return variableName;
  }

  public String variableName(Question question, QuestionCategory questionCategory, OpenAnswerDefinition oad) {
    Assert.notNull(question, "question must not be null");
    Assert.notNull(questionCategory, "questionCategory must not be null");
    Assert.notNull(oad, "oad must not be null");
    String variableName = oad.getVariableName(question.getName());
    if(variableName == null) {
      variableName = variableName(question, questionCategory) + '.' + oad.getName();
    }
    if(variableNames.containsKey(variableName) && oad != variableNames.get(variableName)) throw new QuestionnaireVariableNameNotUniqueException(variableName, oad, variableNames.get(variableName));
    variableNames.put(variableName, oad);
    return variableName;
  }
}
