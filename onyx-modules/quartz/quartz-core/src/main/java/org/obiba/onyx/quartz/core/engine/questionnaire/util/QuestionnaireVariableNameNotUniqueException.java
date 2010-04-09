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

import org.obiba.onyx.quartz.core.engine.questionnaire.IQuestionnaireElement;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;

/**
 * An questionnaire variable name explicitly set by the user is not unique.
 */
public class QuestionnaireVariableNameNotUniqueException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  private final String variableName;

  private final IQuestionnaireElement duplicate;

  private final IQuestionnaireElement original;

  public QuestionnaireVariableNameNotUniqueException(String variableName, IQuestionnaireElement duplicate, IQuestionnaireElement original) {
    this.variableName = variableName;
    this.duplicate = duplicate;
    this.original = original;
  }

  @Override
  public String getMessage() {
    StringBuilder sb = new StringBuilder().append("The user configured variable name [").append(variableName).append("] is use by both ");
    sb = buildQuestionnaireElementDetails(duplicate, sb);
    sb.append(" and ");
    sb = buildQuestionnaireElementDetails(original, sb);
    sb.append(".");
    return sb.toString();
  }

  private StringBuilder buildQuestionnaireElementDetails(IQuestionnaireElement element, StringBuilder sb) {
    if(element != null) {
      sb.append("[").append(element.getName()).append("] of type [").append(getType(element)).append("]");
    } else {
      sb.append("null");
    }
    return sb;
  }

  private String getType(IQuestionnaireElement element) {
    if(element == null) return "null";
    if(element instanceof Question) {
      return "Question";
    } else if(element instanceof QuestionCategory) {
      return "QuestionCategory";
    } else if(element instanceof OpenAnswerDefinition) {
      return "OpenAnswerDefintion";
    } else {
      return "unknown";
    }
  }
}
