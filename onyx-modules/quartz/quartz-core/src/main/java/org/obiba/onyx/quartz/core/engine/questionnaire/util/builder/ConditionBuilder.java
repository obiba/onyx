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
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireFinder;

public class ConditionBuilder extends AbstractQuestionnaireElementBuilder<IDataSource> {

  /**
   * Constructor of an {@link ExternalAnswerCondition}.
   * @param parent
   * @param name
   * @param questionnaireName
   * @param questionName
   * @param categoryName
   */
  private ConditionBuilder(AbstractQuestionnaireElementBuilder<?> parent, String questionnaireName, String questionName, String categoryName, String openAnswerName) {
    super(parent);
    if(getQuestionnaire().getName().equals(questionnaireName)) {
      this.element = getValidQuestionnaireDataSource(questionName, categoryName, openAnswerName);
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
  private IDataSource getValidQuestionnaireDataSource(String questionName, String categoryName, String openAnswerName) {
    Question question = QuestionnaireFinder.getInstance(questionnaire).findQuestion(questionName);
    Category category = null;
    if(question == null) throw invalidElementNameException(Question.class, questionName);

    if(categoryName != null && !categoryName.equals(QuestionnaireDataSource.ANY_CATEGORY)) {
      if(question.getCategories().size() > 0) {
        category = question.findCategory(categoryName);
        if(category == null) throw invalidElementNameException(Category.class, categoryName);
      } else {
        Question parentQuestion = question.getParentQuestion();
        category = parentQuestion.findCategory(categoryName);
        if(category == null) throw invalidElementNameException(Category.class, categoryName);
      }
    }

    if(openAnswerName != null) {
      if(category == null) {
        throw invalidElementNameException(OpenAnswerDefinition.class, openAnswerName);
      }

      OpenAnswerDefinition open = category.findOpenAnswerDefinition(openAnswerName);
      if(open == null) {
        throw invalidElementNameException(OpenAnswerDefinition.class, openAnswerName);
      }
    }

    return new QuestionnaireDataSource(getQuestionnaire().getName(), questionName, categoryName, openAnswerName);
  }

}
