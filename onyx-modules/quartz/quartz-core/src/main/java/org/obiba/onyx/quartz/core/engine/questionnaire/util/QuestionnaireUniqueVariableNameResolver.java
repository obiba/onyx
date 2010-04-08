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
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;

public class QuestionnaireUniqueVariableNameResolver implements QuestionnaireVariableNameResolver {

  private final Questionnaire questionnaire;

  private final Map<String, IQuestionnaireElement> variableNames;

  public QuestionnaireUniqueVariableNameResolver(Questionnaire questionnaire) {
    this.questionnaire = questionnaire;
    this.variableNames = new HashMap<String, IQuestionnaireElement>();
  }

  public String variableName(Question question) {
    String variableName = question.getVariableName();
    if(variableName == null) {
      String prefix = question.getParentQuestion() != null ? variableName(question.getParentQuestion()) : questionnaire.getName();
      variableName = prefix + '.' + question.getName();
    }
    if(variableNames.containsKey(variableName)) throw new QuestionnaireVariableNameNotUniqueException(variableName, question, variableNames.get(variableName));
    variableNames.put(variableName, question);
    return variableName;
  }

  public String variableName(Question question, QuestionCategory questionCategory) {
    String variableName = questionCategory.getVariableName();
    if(variableName == null) {
      variableName = variableName(question) + '.' + questionCategory.getCategory().getName();
    }
    if(variableNames.containsKey(variableName)) throw new QuestionnaireVariableNameNotUniqueException(variableName, questionCategory, variableNames.get(variableName));
    variableNames.put(variableName, questionCategory);
    return variableName;
  }

  public String variableName(Question question, QuestionCategory questionCategory, OpenAnswerDefinition oad) {
    String variableName = oad.getVariableName(question.getName());
    if(variableName == null) {
      variableName = variableName(question, questionCategory) + '.' + oad.getName();
    }
    if(variableNames.containsKey(variableName)) throw new QuestionnaireVariableNameNotUniqueException(variableName, oad, variableNames.get(variableName));
    variableNames.put(variableName, oad);
    return variableName;
  }
}
