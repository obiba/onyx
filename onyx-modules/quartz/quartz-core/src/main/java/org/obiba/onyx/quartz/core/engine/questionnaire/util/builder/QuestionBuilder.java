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

import java.util.List;
import java.util.Map;

import org.obiba.onyx.core.domain.participant.Gender;
import org.obiba.onyx.quartz.core.engine.questionnaire.answer.DataSource;
import org.obiba.onyx.quartz.core.engine.questionnaire.condition.ConditionOperator;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireBuilder;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireFinder;
import org.obiba.onyx.quartz.core.wicket.layout.IQuestionPanelFactory;
import org.obiba.onyx.quartz.core.wicket.layout.impl.util.QuestionCategoryListToGridPermutator;
import org.obiba.onyx.util.data.ComparisonOperator;
import org.obiba.onyx.util.data.Data;

/**
 * {@link Question} builder, given a {@link Questionnaire} and a current {@link Page}.
 * @author Yannick Marcon
 * 
 */
public class QuestionBuilder extends AbstractQuestionnaireElementBuilder<Question> {

  /**
   * Constructor using {@link PageBuilder} to get the {@link Page} it is applied to.
   * @param parent
   * @param name
   * @param multiple
   * @throws IllegalArgumentException if name does not respect questionnaire element naming pattern.
   */
  private QuestionBuilder(PageBuilder parent, String name, String number, boolean multiple, Class<? extends IQuestionPanelFactory> uiFactoryClass) {
    super(parent);
    if(!checkNamePattern(name)) {
      throw invalidNamePatternException(name);
    }
    if(!checkUniqueQuestionName(name)) {
      throw invalidNameUnicityException(Question.class, name);
    }
    element = new Question(name);
    element.setNumber(number);
    // required by default
    element.setMinCount(1);
    element.setMultiple(multiple);
    try {
      element.setUIFactoryName(uiFactoryClass.newInstance().getBeanName());
    } catch(Exception e) {
      throw invalidQuestionPanelFactoryException(uiFactoryClass, e);
    }
    parent.getElement().addQuestion(element);
  }

  /**
   * Constructor.
   * @param questionnaire
   * @param question
   */
  private QuestionBuilder(QuestionnaireBuilder parent, Question question) {
    super(parent);
    this.element = question;
  }

  /**
   * Create a {@link Question} in the given {@link Page}.
   * @param parent
   * @param name
   * @param multiple
   * @return
   * @throws IllegalArgumentException if name does not respect questionnaire element naming pattern.
   */
  public static QuestionBuilder createQuestion(PageBuilder parent, String name, String number, boolean multiple) {
    return createQuestion(parent, name, number, multiple, parent.getDefaultQuestionUI());
  }

  /**
   * Create a {@link Question} in the given {@link Page}.
   * @param parent
   * @param name
   * @param multiple
   * @return
   * @throws IllegalArgumentException if name does not respect questionnaire element naming pattern.
   */
  public static QuestionBuilder createQuestion(PageBuilder parent, String name, String number, boolean multiple, Class<? extends IQuestionPanelFactory> uiFactoryClass) {
    return new QuestionBuilder(parent, name, number, multiple, uiFactoryClass);
  }

  /**
   * Set the given {@link Question} as the current one.
   * @param questionnaire
   * @param question
   * @return
   */
  public static QuestionBuilder inQuestion(QuestionnaireBuilder parent, Question question) {
    return new QuestionBuilder(parent, question);
  }

  /**
   * Add a required, non multiple, {@link Question} to current {@link Question} and make it current {@link Question}.
   * @param name
   * @return
   */
  public QuestionBuilder withQuestion(String name) {
    return withQuestion(name, null, false);
  }

  /**
   * Add a required, non multiple, with number {@link Question} to current {@link Question} and make it current
   * {@link Question}.
   * @param name
   * @return
   */
  public QuestionBuilder withQuestion(String name, String number) {
    return withQuestion(name, number, false);
  }

  /**
   * Add a required, non multiple, {@link Question} to current {@link Question} and make it current {@link Question}.
   * @param name
   * @param uiFactoryClass
   * @return
   */
  public QuestionBuilder withQuestion(String name, Class<? extends IQuestionPanelFactory> uiFactoryClass) {
    return withQuestion(name, null, false, uiFactoryClass);
  }

  /**
   * Add a required, non multiple, {@link Question} to current {@link Question} and make it current {@link Question}.
   * @param name
   * @param uiFactoryClass
   * @return
   */
  public QuestionBuilder withQuestion(String name, String number, Class<? extends IQuestionPanelFactory> uiFactoryClass) {
    return withQuestion(name, number, false, uiFactoryClass);
  }

  /**
   * Add a required {@link Question} to current {@link Question} and make it current {@link Question}.
   * @param name
   * @param multiple
   * @return
   * @see #getQuestion()
   */
  public QuestionBuilder withQuestion(String name, boolean multiple) {
    return withQuestion(name, null, multiple, getDefaultQuestionUI());
  }

  /**
   * Add a required {@link Question} to current {@link Question} and make it current {@link Question}.
   * @param name
   * @param multiple
   * @return
   * @see #getQuestion()
   */
  public QuestionBuilder withQuestion(String name, String number, boolean multiple) {
    return withQuestion(name, number, multiple, getDefaultQuestionUI());
  }

  /**
   * Add a required {@link Question} to current {@link Question} and make it current {@link Question}.
   * @param name
   * @param multiple
   * @param uiFactoryClass
   * @return
   * @see #getQuestion()
   */
  public QuestionBuilder withQuestion(String name, String number, boolean multiple, Class<? extends IQuestionPanelFactory> uiFactoryClass) {
    if(!checkNamePattern(name)) {
      throw invalidNamePatternException(name);
    }
    if(!checkUniqueQuestionName(name)) {
      throw invalidNameUnicityException(Question.class, name);
    }
    String uiFactoryName;
    try {
      uiFactoryName = uiFactoryClass.newInstance().getBeanName();
    } catch(Exception e) {
      throw invalidQuestionPanelFactoryException(uiFactoryClass, e);
    }
    Question question = new Question(name);
    question.setNumber(number);
    // required by default
    question.setMinCount(1);
    question.setMultiple(multiple);
    question.setUIFactoryName(uiFactoryName);
    element.addQuestion(question);
    element = question;

    return this;
  }

  /**
   * Set the range of answer count for current {@link Question}.
   * @param minCount no limit if null
   * @param maxCount no limit if null
   * @return
   */
  public QuestionBuilder setAnswerCount(Integer minCount, Integer maxCount) {
    element.setMinCount(minCount);
    element.setMaxCount(maxCount);

    return this;
  }

  /**
   * Set the exact answer count (min and max count are equals).
   * @param count
   * @return
   */
  public QuestionBuilder setAnswerCount(Integer count) {
    element.setMinCount(count);
    element.setMaxCount(count);

    return this;
  }

  /**
   * Add argument that will be interpreted by specific question UI.
   * @param key
   * @param value
   * @return
   */
  public QuestionBuilder addUIArgument(String key, String value) {
    element.addUIArgument(key, value);

    return this;
  }

  /**
   * Question categories are displayed in a grid (multiple row and columns), the count of rows can be specified.
   * @param count
   * @return
   */
  public QuestionBuilder setRowCount(int count) {
    return addUIArgument(QuestionCategoryListToGridPermutator.ROW_COUNT_KEY, Integer.toString(count));
  }

  /**
   * Add a {@link Category} to current {@link Question}, make it the current category.
   * @param name
   * @return
   */
  public CategoryBuilder withCategory(String name) {
    return CategoryBuilder.createQuestionCategory(this, name);
  }

  /**
   * Add a set of {@link Category} to current {@link Question}.
   * @param names
   * @return
   */
  public CategoryBuilder withCategories(String... names) {
    CategoryBuilder child = null;

    for(String name : names) {
      child = withCategory(name);
    }

    return child;
  }

  /**
   * Look for the {@link Category} with the given name in the current {@link Questionnaire}, add it (create it if
   * necessary) to the current {@link Question}, make it the current category.
   * @param name
   * @return
   */
  public CategoryBuilder withSharedCategory(String name) {
    Map<Category, List<Question>> map = QuestionnaireFinder.getInstance(questionnaire).findCategories(name);
    if(map.keySet().size() > 1) {
      throw invalidSharedCategoryNameUnicityException(name);
    } else if(map.keySet().isEmpty()) {
      return withCategory(name);
    } else {
      return CategoryBuilder.createQuestionCategory(this, map.keySet().iterator().next());
    }
  }

  /**
   * Look for the {@link Category} with the given name in the current {@link Questionnaire}, add it (create it if
   * necessary) to the current {@link Question}, make it the current category.
   * @param name
   * @param exportName
   * @return
   */
  public CategoryBuilder withSharedCategory(String name, String exportName) {
    CategoryBuilder builder = withSharedCategory(name);
    builder.setExportName(exportName);
    return builder;
  }

  /**
   * Add a set of shared {@link Category} to current {@link Question}.
   * @param names
   * @return
   */
  public CategoryBuilder withSharedCategories(String... names) {
    CategoryBuilder child = null;

    for(String name : names) {
      child = withSharedCategory(name);
    }

    return child;
  }

  /**
   * Add a set of shared {@link Category} to current {@link Question}, with escape attribute.
   * @param escape
   * @param names
   * @return
   */
  public CategoryBuilder withSharedCategories(boolean escape, String... names) {
    CategoryBuilder child = null;

    for(String name : names) {
      child = withSharedCategory(name).setEscape(escape);
    }

    return child;
  }

  /**
   * Condition on any category is chosen in the given question.
   * @param name
   * @param question
   * @return
   */
  public ConditionBuilder setAnswerCondition(String name, String question) {
    return setAnswerCondition(name, question, null);
  }

  /**
   * Condition on a category choice.
   * @param name
   * @param question
   * @param category
   * @return
   */
  public ConditionBuilder setAnswerCondition(String name, String question, String category) {
    return ConditionBuilder.createQuestionCondition(this, name, question, category);
  }

  /**
   * Condition on category choice in another questionnaire.
   * @param name
   * @param questionnaireName
   * @param question
   * @param category
   * @return
   */
  public ConditionBuilder setExternalAnswerCondition(String name, String questionnaireName, String question, String category) {
    return ConditionBuilder.createQuestionCondition(this, name, questionnaireName, question, category);
  }

  /**
   * Compare an open answer from another questionnaire to given data.
   * @param name
   * @param questionnaireName
   * @param questionName
   * @param categoryName
   * @param openAnswerDefinitionName
   * @param comparisonOperator
   * @param data
   * @return
   */
  public ConditionBuilder setDataCondition(String name, String questionnaireName, String questionName, String categoryName, String openAnswerDefinitionName, ComparisonOperator comparisonOperator, Data data) {
    return ConditionBuilder.createQuestionCondition(this, name, questionnaireName, questionName, categoryName, openAnswerDefinitionName, comparisonOperator, data);
  }

  /**
   * Compare an open answer to given data.
   * @param name
   * @param question
   * @param category
   * @param openAnswerDefinition
   * @param comparisonOperator
   * @param data
   * @return
   */
  public ConditionBuilder setDataCondition(String name, String question, String category, String openAnswerDefinition, ComparisonOperator comparisonOperator, Data data) {
    return ConditionBuilder.createQuestionCondition(this, name, question, category, openAnswerDefinition, comparisonOperator, data);
  }

  /**
   * Compare an open answer to given data from data source.
   * @param name
   * @param question
   * @param category
   * @param openAnswerDefinition
   * @param comparisonOperator
   * @param dataSource
   * @return
   */
  public ConditionBuilder setDataCondition(String name, String question, String category, String openAnswerDefinition, ComparisonOperator comparisonOperator, DataSource dataSource) {
    return ConditionBuilder.createQuestionCondition(this, name, question, category, openAnswerDefinition, comparisonOperator, dataSource);
  }

  /**
   * Compare two open answers.
   * @param name
   * @param question
   * @param category
   * @param openAnswerDefinition
   * @param comparisonOperator
   * @param questionName
   * @param categoryName
   * @param openAnswerDefinitionName
   * @return
   */
  public ConditionBuilder setDataCondition(String name, String question, String category, String openAnswerDefinition, ComparisonOperator comparisonOperator, String questionName, String categoryName, String openAnswerDefinitionName) {
    return ConditionBuilder.createQuestionCondition(this, name, question, category, openAnswerDefinition, comparisonOperator, questionName, categoryName, openAnswerDefinitionName);
  }

  /**
   * Compare an open answer to an open answer in another questionnaire.
   * @param name
   * @param question
   * @param category
   * @param openAnswerDefinition
   * @param comparisonOperator
   * @param questionnaireName
   * @param questionName
   * @param categoryName
   * @param openAnswerDefinitionName
   * @return
   */
  public ConditionBuilder setDataCondition(String name, String question, String category, String openAnswerDefinition, ComparisonOperator comparisonOperator, String questionnaireName, String questionName, String categoryName, String openAnswerDefinitionName) {
    return ConditionBuilder.createQuestionCondition(this, name, question, category, openAnswerDefinition, comparisonOperator, questionnaireName, questionName, categoryName, openAnswerDefinitionName);
  }

  /**
   * Compare participant property to given data.
   * @param name
   * @param participantProperty
   * @param comparisonOperator
   * @param data
   * @return
   */
  public ConditionBuilder setDataCondition(String name, String participantProperty, ComparisonOperator comparisonOperator, Data data) {
    return ConditionBuilder.createQuestionCondition(this, name, participantProperty, comparisonOperator, data);
  }

  /**
   * Compare participant property to open answer.
   * @param name
   * @param participantProperty
   * @param comparisonOperator
   * @param question
   * @param category
   * @param openAnswerDefinition
   * @return
   */
  public ConditionBuilder setDataCondition(String name, String participantProperty, ComparisonOperator comparisonOperator, String question, String category, String openAnswerDefinition) {
    return ConditionBuilder.createQuestionCondition(this, name, participantProperty, comparisonOperator, question, category, openAnswerDefinition);
  }

  /**
   * Compare participant gender to given gender.
   * @param name
   * @param comparisonOperator
   * @param gender
   * @return
   */
  public ConditionBuilder setDataCondition(String name, ComparisonOperator comparisonOperator, Gender gender) {
    return ConditionBuilder.createQuestionCondition(this, name, comparisonOperator, gender);
  }

  /**
   * Compare two data from data sources.
   * @param name
   * @param dataSource1
   * @param comparisonOperator
   * @param dataSource2
   * @return
   */
  public ConditionBuilder setDataCondition(String name, DataSource dataSource1, ComparisonOperator comparisonOperator, DataSource dataSource2) {
    return ConditionBuilder.createQuestionCondition(this, name, dataSource1, comparisonOperator, dataSource2);
  }

  /**
   * Set a multiple condition that will contain several conditions to be compared with each other.
   * @param name
   * @param operator
   * @return
   */
  public ConditionBuilder setMultipleCondition(String name, ConditionOperator operator) {
    return ConditionBuilder.createQuestionMultipleCondition(this, name, operator);
  }

  /**
   * Set a condition that will resolve with the the opposit of inner condition resolution.
   * @param name
   * @return
   */
  public ConditionBuilder setNotCondition(String name) {
    return ConditionBuilder.createQuestionNotCondition(this, name);
  }

  /**
   * Check question name unicity.
   * @param name
   * @return
   */
  private boolean checkUniqueQuestionName(String name) {
    return (QuestionnaireFinder.getInstance(questionnaire).findQuestion(name) == null);
  }

  /**
   * Check shared category name unicity.
   * @param name
   * @return
   */
  private IllegalArgumentException invalidSharedCategoryNameUnicityException(String name) {
    return new IllegalArgumentException("There are several categories with name: " + name);
  }

  /**
   * Check question panel factory is operational.
   * @param uiFactoryClass
   * @param e
   * @return
   */
  private IllegalArgumentException invalidQuestionPanelFactoryException(Class<? extends IQuestionPanelFactory> uiFactoryClass, Exception e) {
    return new IllegalArgumentException("Unable to get question panel factory name from " + uiFactoryClass.getName(), e);
  }
}
