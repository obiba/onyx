/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.magma;

import org.obiba.onyx.quartz.core.domain.answer.QuestionAnswer;
import org.obiba.onyx.quartz.core.engine.questionnaire.QuestionnaireVariableNameResolver;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireFinder;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireUniqueVariableNameResolver;

/**
 *
 */
public class QuestionnaireComment {

  private QuestionnaireFinder questionnaireFinder;

  private String variable;

  private String comment;

  public QuestionnaireComment(Questionnaire questionnaire, QuestionAnswer answer) {
    this.questionnaireFinder = new QuestionnaireFinder(questionnaire);
    QuestionnaireVariableNameResolver variableNameResolver = new QuestionnaireUniqueVariableNameResolver();

    Question question = questionnaireFinder.findQuestion(answer.getQuestionName());
    if(question == null) {
      throw new IllegalStateException("A question has been removed from questionnaire '" + questionnaire.getName() + "' before all interviews were completed and exported: " + answer.getQuestionName());
    }

    this.variable = variableNameResolver.variableName(question);
    this.comment = answer.getComment();
  }

  public String getComment() {
    return comment;
  }

  public String getVariable() {
    return variable;
  }

}
