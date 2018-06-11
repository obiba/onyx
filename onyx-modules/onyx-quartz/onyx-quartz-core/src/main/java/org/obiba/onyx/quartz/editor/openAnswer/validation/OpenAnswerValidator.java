/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.openAnswer.validation;

import java.io.Serializable;

import org.obiba.magma.Variable;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.util.data.ComparisonOperator;

/**
 *
 */
public class OpenAnswerValidator implements Serializable {

  private static final long serialVersionUID = 1L;

  public static enum Type {
    QUESTION_CATEGORY, VARIABLE, JAVASCRIPT;
  }

  private Type type;

  private ComparisonOperator operator;

  private Question question;

  private Category category;

  private OpenAnswerDefinition openAnswer;

  private Variable variable;

  private String script;

  public Type getType() {
    return type;
  }

  public void setType(Type type) {
    this.type = type;
  }

  public Question getQuestion() {
    return question;
  }

  public void setQuestion(Question question) {
    this.question = question;
  }

  public Category getCategory() {
    return category;
  }

  public void setCategory(Category category) {
    this.category = category;
  }

  public OpenAnswerDefinition getOpenAnswer() {
    return openAnswer;
  }

  public void setOpenAnswer(OpenAnswerDefinition openAnswer) {
    this.openAnswer = openAnswer;
  }

  public ComparisonOperator getOperator() {
    return operator;
  }

  public void setOperator(ComparisonOperator operator) {
    this.operator = operator;
  }

  public Variable getVariable() {
    return variable;
  }

  public void setVariable(Variable variable) {
    this.variable = variable;
  }

  public String getScript() {
    return script;
  }

  public void setScript(String script) {
    this.script = script;
  }

}
