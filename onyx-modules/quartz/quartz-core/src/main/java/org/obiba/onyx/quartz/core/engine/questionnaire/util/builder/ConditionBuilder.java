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

import org.obiba.onyx.quartz.core.engine.questionnaire.condition.AnswerCondition;
import org.obiba.onyx.quartz.core.engine.questionnaire.condition.ComparisionOperator;
import org.obiba.onyx.quartz.core.engine.questionnaire.condition.Condition;
import org.obiba.onyx.quartz.core.engine.questionnaire.condition.ConditionOperator;
import org.obiba.onyx.quartz.core.engine.questionnaire.condition.DataComparator;
import org.obiba.onyx.quartz.core.engine.questionnaire.condition.MultipleCondition;
import org.obiba.onyx.quartz.core.engine.questionnaire.condition.NoAnswerCondition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireFinder;
import org.obiba.onyx.util.data.Data;

public class ConditionBuilder extends AbstractQuestionnaireElementBuilder<Condition> {

  /**
   * Constructor using {@link QuestionBuilder} to get the {@link Question} it is applied to.
   * @param parent
   * @param condition
   */
  private ConditionBuilder(Questionnaire questionnaire, Condition condition) {
    super(questionnaire);
    this.element = condition;
  }

  // AnswerCondition
  private ConditionBuilder(QuestionBuilder parent, String name, String questionName, String categoryName, String openAnswerDefinitionName, Data data, ComparisionOperator comparisionOperator, Integer occurence) {
    super(parent.getQuestionnaire());

    element = getValidAnswerCondition(name, questionName, categoryName, openAnswerDefinitionName, data, comparisionOperator, occurence);
    element.addQuestion(parent.getElement());

  }

  // MultipleCondition
  private ConditionBuilder(QuestionBuilder parent, String name, ConditionOperator operator) {
    super(parent.getQuestionnaire());

    MultipleCondition multipleCondition = new MultipleCondition();
    multipleCondition.setConditionOperator(operator);
    multipleCondition.setName(name);
    multipleCondition.addQuestion(parent.getElement());
    element = multipleCondition;
  }

  // NoAnswerCondition
  private ConditionBuilder(QuestionBuilder parent, String name) {
    super(parent.getQuestionnaire());

    NoAnswerCondition noAnswerCondition = new NoAnswerCondition();
    noAnswerCondition.setName(name);
    noAnswerCondition.addQuestion(parent.getElement());
    element = noAnswerCondition;
  }

  /**
   * Add a {@link AnswerCondition} to current {@link Question}.
   * @param questionBuilder
   * @param questionName
   * @param categoryName
   * @param dataType
   * @param comparisonOperator
   * @return
   */
  public static ConditionBuilder createQuestionCondition(QuestionBuilder questionBuilder, String name, String questionName, String categoryName, String openAnswerDefinitionName, Data data, ComparisionOperator comparisionOperator, Integer occurence) {
    return (new ConditionBuilder(questionBuilder, name, questionName, categoryName, openAnswerDefinitionName, data, comparisionOperator, occurence));
  }

  /**
   * Add a {@link MultipleCondition} to current {@link Question}.
   * @param questionBuilder
   * @param name
   * @param operator
   * @return
   */
  public static ConditionBuilder createQuestionMultipleCondition(QuestionBuilder questionBuilder, String name, ConditionOperator operator) {
    return (new ConditionBuilder(questionBuilder, name, operator));
  }

  /**
   * Add a {@link MultipleCondition} to current {@link Question}.
   * @param questionBuilder
   * @param name
   * @param operator
   * @return
   */
  public static ConditionBuilder createQuestionNoAnswerCondition(QuestionBuilder questionBuilder, String name) {
    return (new ConditionBuilder(questionBuilder, name));
  }

  /**
   * Add a {@link AnswerCondition} on the current ConditionBuilder.
   * @param name
   * @param questionName
   * @param categoryName
   * @return
   */
  public ConditionBuilder withAnswerCondition(String name, String questionName, String categoryName) {
    return withAnswerCondition(name, questionName, categoryName, null, null, null, null);
  }

  /**
   * Add a {@link AnswerCondition} on the current ConditionBuilder.
   * @param name
   * @param questionName
   * @param categoryName
   * @param openAnswerDefinitionName
   * @param data
   * @param comparisionOperator
   * @param occurence
   * @return
   */
  public ConditionBuilder withAnswerCondition(String name, String questionName, String categoryName, String openAnswerDefinitionName, Data data, ComparisionOperator comparisionOperator, Integer occurence) {
    AnswerCondition answerCondition = getValidAnswerCondition(name, questionName, categoryName, openAnswerDefinitionName, data, comparisionOperator, occurence);

    if(element instanceof MultipleCondition) {
      ((MultipleCondition) element).getConditions().add(answerCondition);
    } else if(element instanceof NoAnswerCondition) {
      ((NoAnswerCondition) element).setCondition(answerCondition);
    }

    element = answerCondition;
    return this;
  }

  /**
   * Add a {@link NoAnswerCondition} on the current ConditionBuilder.
   * @param name
   * @return
   */
  public ConditionBuilder withNoAnswerCondition(String name) {
    NoAnswerCondition noAnswerCondition = new NoAnswerCondition();
    noAnswerCondition.setName(name);

    if(!(element instanceof MultipleCondition)) throw new IllegalArgumentException("You cannot have a no answer condition on a condition of type: " + element.getClass().getName());

    ((MultipleCondition) element).getConditions().add(noAnswerCondition);
    element = noAnswerCondition;
    return this;
  }

  /**
   * Add a {@link MultipleCondition} on the current ConditionBuilder.
   * @param name
   * @return
   */
  public ConditionBuilder withMultipleCondition(String name, ConditionOperator operator) {
    MultipleCondition multipleCondition = new MultipleCondition();
    multipleCondition.setName(name);
    multipleCondition.setConditionOperator(operator);

    if(element instanceof MultipleCondition) {
      ((MultipleCondition) element).getConditions().add(multipleCondition);
    } else if(element instanceof NoAnswerCondition) {
      ((NoAnswerCondition) element).setCondition(multipleCondition);
    } else {
      throw new IllegalArgumentException("You cannot have a multiple condition on a condition of type: " + element.getClass().getName());
    }

    element = multipleCondition;
    return this;
  }

  /**
   * Add a {@link AnswerCondition} on the current ConditionBuilder {@link AnswerCondition} as a parentAnswerCondition.
   * @param name
   * @param questionName
   * @param categoryName
   * @param data
   * @param comparisionOperator
   * @param occurence
   * @return
   */
  public ConditionBuilder withParentAnswerCondition(String name, String questionName, String categoryName, String openAnswerDefinitionName, Data data, ComparisionOperator comparisionOperator, Integer occurence) {
    AnswerCondition parentAnswerCondition = getValidAnswerCondition(name, questionName, categoryName, openAnswerDefinitionName, data, comparisionOperator, occurence);

    if(!(element instanceof AnswerCondition)) throw new IllegalArgumentException("You cannot have a parent answer condition on a condition of type: " + element.getClass().getName());

    ((AnswerCondition) element).setParentAnswerCondition(parentAnswerCondition);
    element = parentAnswerCondition;
    return this;
  }

  /**
   * Returns a valid {@link AnswerCondition}.
   * @param name
   * @param questionName
   * @param categoryName
   * @param data
   * @param comparisionOperator
   * @param occurence
   * @return
   */
  private AnswerCondition getValidAnswerCondition(String name, String questionName, String categoryName, String openAnswerDefinitionName, Data data, ComparisionOperator comparisionOperator, Integer occurence) {
    AnswerCondition answerCondition = new AnswerCondition();
    String questionCategoryName = questionName + "." + categoryName;
    QuestionCategory questionCategory = QuestionnaireFinder.getInstance(questionnaire).findQuestionCategory(questionName, questionCategoryName);
    if(questionCategory == null) throw invalidElementNameException(QuestionCategory.class, questionCategoryName);

    // dataComparator
    DataComparator dataComparator = null;
    if(data != null && comparisionOperator != null) {
      if(questionCategory.getCategory().getOpenAnswerDefinition() == null) throw new IllegalArgumentException("You cannot apply a data validation on a non-OpenAnswerDefinition category");

      dataComparator = new DataComparator(comparisionOperator, data, openAnswerDefinitionName);
    }

    if(QuestionnaireFinder.getInstance(questionnaire).findCondition(name) != null) throw invalidNameUnicityException(Condition.class, name);

    answerCondition.setName(name);
    answerCondition.setQuestionCategory(questionCategory);
    if(dataComparator != null) answerCondition.setDataComparator(dataComparator);
    if(occurence != null) answerCondition.setOccurence(occurence);

    return (answerCondition);
  }

  /**
   * Set the given {@link Condition} as the current one.
   * @param questionnaire
   * @param condition
   * @return
   */
  public static ConditionBuilder inCondition(Questionnaire questionnaire, Condition condition) {
    return new ConditionBuilder(questionnaire, condition);
  }
}
