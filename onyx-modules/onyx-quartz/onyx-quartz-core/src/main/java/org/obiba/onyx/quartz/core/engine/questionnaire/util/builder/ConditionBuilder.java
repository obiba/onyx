/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.engine.questionnaire.util.builder;

import org.obiba.onyx.core.data.IDataSource;
import org.obiba.onyx.quartz.core.data.QuestionnaireDataSource;

public class ConditionBuilder extends AbstractQuestionnaireElementBuilder<IDataSource> {

  /**
   * Constructor of a Question Condition.
   * @param parent
   * @param name
   * @param questionnaireName
   * @param questionName
   * @param categoryName
   */
  private ConditionBuilder(AbstractQuestionnaireElementBuilder<?> parent, String questionnaireName, String questionName, String categoryName, String openAnswerName) {
    super(parent);
    if(getQuestionnaire().getName().equals(questionnaireName)) {
      this.element = getValidVariableDataSource(questionName, categoryName, openAnswerName);
    } else {
      // cannot check names
      element = new QuestionnaireDataSource(questionnaireName, questionName, categoryName, openAnswerName);
    }
  }

  /**
   * Build a {@link QuestionnaireDataSource}.
   * @param builder
   * @param question
   * @param category
   * @param openAnswerDefinition
   * @return
   */
  public static ConditionBuilder createQuestionCondition(AbstractQuestionnaireElementBuilder<?> builder, String question, String category, String openAnswer) {
    return new ConditionBuilder(builder, builder.getQuestionnaire().getName(), question, category, openAnswer);
  }

  /**
   * Build a {@link QuestionnaireDataSource}.
   * @param builder
   * @param questionnaireName
   * @param question
   * @param category
   * @return
   */
  public static ConditionBuilder createQuestionCondition(AbstractQuestionnaireElementBuilder<?> builder, String questionnaireName, String question, String category, String openAnswer) {
    return new ConditionBuilder(builder, questionnaireName, question, category, openAnswer);
  }

}
