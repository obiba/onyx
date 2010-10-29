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

import org.obiba.magma.Variable;
import org.obiba.magma.type.BooleanType;
import org.obiba.onyx.core.data.IDataSource;
import org.obiba.onyx.core.data.VariableDataSource;
import org.obiba.onyx.quartz.core.data.QuestionnaireDataSource;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireFinder;

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

  /**
   * Returns a valid {@link QuestionnaireDataSource} for current questionnaire.
   * @param questionName
   * @param categoryName
   * @param openAnswerName
   * @return
   */
  private IDataSource getValidVariableDataSource(String questionName, String categoryName, String openAnswerName) {
    Question question = QuestionnaireFinder.getInstance(questionnaire).findQuestion(questionName);
    QuestionCategory questionCategory = null;
    if(question == null) throw invalidElementNameException(Question.class, questionName);

    if(categoryName != null && !categoryName.equals(QuestionnaireDataSource.ANY_CATEGORY)) {
      if(question.getCategories().size() > 0) {
        questionCategory = question.findQuestionCategory(categoryName);
        if(questionCategory == null) throw invalidElementNameException(Category.class, categoryName);
      } else {
        Question parentQuestion = question.getParentQuestion();
        questionCategory = parentQuestion.findQuestionCategory(categoryName);
        if(questionCategory == null) throw invalidElementNameException(Category.class, categoryName);
      }
    }

    String variableName;
    if(openAnswerName != null) {
      if(questionCategory == null) {
        throw invalidElementNameException(OpenAnswerDefinition.class, openAnswerName);
      }

      OpenAnswerDefinition open = questionCategory.getCategory().findOpenAnswerDefinition(openAnswerName);
      if(open == null) {
        throw invalidElementNameException(OpenAnswerDefinition.class, openAnswerName);
      }
      variableName = variableNameResolver.variableName(question, questionCategory, open);
    } else if(questionCategory != null) {
      variableName = variableNameResolver.variableName(question, questionCategory);
    } else {
      // make a boolean derived variable that represents the fact that an answer was made
      variableName = variableNameResolver.variableName(question);
      String conditionVariableName = variableName + "_answered";
      if(!questionnaire.hasVariable(conditionVariableName)) {
        Variable.Builder varBuilder = new Variable.Builder(conditionVariableName, BooleanType.get(), "Participant");
        varBuilder.addAttribute("script", "$('" + variableName + "').isNull().not()");
        questionnaire.addVariable(varBuilder.build());
      }
      variableName = conditionVariableName;
    }

    return new VariableDataSource(questionnaire.getName() + ":" + variableName);
  }

}
