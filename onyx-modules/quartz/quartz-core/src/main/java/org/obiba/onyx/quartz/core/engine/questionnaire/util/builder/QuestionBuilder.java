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
import org.obiba.onyx.core.data.ComparingDataSource;
import org.obiba.onyx.core.data.ComputingDataSource;
import org.obiba.onyx.core.data.FixedDataSource;
import org.obiba.onyx.core.data.IDataSource;
import org.obiba.onyx.core.data.ParticipantPropertyDataSource;
import org.obiba.onyx.core.data.VariableDataSource;
import org.obiba.onyx.core.domain.participant.Gender;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireBuilder;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireFinder;
import org.obiba.onyx.quartz.core.wicket.layout.IQuestionPanelFactory;
import org.obiba.onyx.quartz.core.wicket.layout.impl.util.QuestionCategoryListToGridPermutator;
import org.obiba.onyx.util.data.ComparisonOperator;
import org.obiba.onyx.util.data.DataType;

import com.google.common.collect.Multimap;

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
   * Explicitly set the {@link Question} variable name.
   */
  public QuestionBuilder setVariableName(String variableName) {
    if(!checkNamePattern(variableName)) throw invalidNamePatternException(variableName);
    element.setVariableName(variableName);
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
   * Make the current {@link Question} optional.
   * @return The QuestionBuilder.
   */
  public QuestionBuilder optional() {
    element.setMinCount(0);
    element.setMaxCount(1);
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
   * Add a {@link Category} to current {@link Question}, make it the current category.
   * @param name
   * @param exportName
   * @return
   * @deprecated export name is not supported any more
   */
  @Deprecated
  public CategoryBuilder withCategory(String name, String exportName) {
    return withCategory(name).setExportName(exportName);
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
    Multimap<Category, Question> map = QuestionnaireFinder.getInstance(questionnaire).findCategories(name);
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
   * Set the condition based on a provided derived variable that will be added to the questionnaire. Check that a
   * variable with same name does not already exist and that the variable value type is boolean.
   * @param variable
   * @return
   */
  public QuestionBuilder setQuestionnaireVariableCondition(Variable variable) {
    if(!variable.getValueType().equals(BooleanType.get())) {
      throw new IllegalArgumentException("Boolean type is expected for questionnaire variable used as a question condition: " + variable.getName());
    }
    addVariable(variable);
    getElement().setCondition(new VariableDataSource(questionnaire.getName() + ":" + variable.getName()));
    return this;
  }

  /**
   * Set the condition based on a newly created derived variable that is added in the questionnaire. Check that a
   * variable with same name does not already exist and set the boolean value type.
   * @param variableName
   * @param script
   * @return
   */
  public QuestionBuilder setQuestionnaireVariableCondition(String variableName, String script) {
    addVariable(new Variable.Builder(variableName, BooleanType.get(), "Participant").addAttribute("script", script).build());
    getElement().setCondition(new VariableDataSource(questionnaire.getName() + ":" + variableName));
    return this;
  }

  /**
   * Set the condition based on the derived variable name defined in the questionnaire. Check is made that the variable
   * exists in the questionnaire and the value type is boolean.
   * @param variableName
   * @return
   */
  public QuestionBuilder setQuestionnaireVariableCondition(String variableName) {
    Variable var = QuestionnaireFinder.getInstance(questionnaire).findVariable(variableName);
    if(var == null) throw new IllegalArgumentException("No such variable in the questionnaire with name: " + variableName);
    if(!var.getValueType().equals(BooleanType.get())) {
      throw new IllegalArgumentException("Boolean type is expected for questionnaire variable used as a question condition: " + variableName);
    }
    getElement().setCondition(new VariableDataSource(questionnaire.getName() + ":" + variableName));
    return this;
  }

  /**
   * Set the condition based on the value of a variable identified by the provided path. No check is made on whether the
   * variable with given path exists or if the variable value type is boolean or if the variable entity type is
   * Participant.
   * @param variablePath
   * @return
   */
  public QuestionBuilder setVariableCondition(String variablePath) {
    return setVariableCondition(new VariableDataSource(variablePath));
  }

  /**
   * Set the condition based on a variable condition. No check is made on whether the variable with given path exists or
   * if the variable value type is boolean or if the variable entity type is Participant.
   * @param ds
   * @return
   * @see QuestionnaireBuilder#newDataSource(String) and {@link QuestionnaireBuilder#newDataSource(String, String)}
   */
  public QuestionBuilder setVariableCondition(VariableDataSource ds) {
    getElement().setCondition(checkVariablePath(ds));
    return this;
  }

  /**
   * Condition on any category is chosen in the given question in current questionnaire.
   * @param question
   * @return
   */
  public QuestionBuilder setCondition(String question) {
    getElement().setCondition(ConditionBuilder.createQuestionCondition(this, question, null, null).getElement());
    return this;
  }

  /**
   * Condition on a category choice in current questionnaire.
   * @param question
   * @param category
   * @return
   */
  public QuestionBuilder setCondition(String question, String category) {
    getElement().setCondition(ConditionBuilder.createQuestionCondition(this, question, category, null).getElement());
    return this;
  }

  /**
   * Condition on question answering and/or category choice in another questionnaire.
   * @param questionnaireName
   * @param question
   * @param category can be null
   * @return
   * @deprecated use {@link #setVariableCondition(String)} instead
   */
  @Deprecated
  public QuestionBuilder setCondition(String questionnaire, String question, String category) {
    getElement().setCondition(ConditionBuilder.createQuestionCondition(this, questionnaire, question, category, null).getElement());
    return this;
  }

  /**
   * Add the data source condition (must be of boolean type).
   * @param dataSource
   * @return
   * @deprecated use {@link #setVariableCondition(String)} instead
   */
  @Deprecated
  public QuestionBuilder setCondition(IDataSource dataSource) {
    getElement().setCondition(dataSource);
    return this;
  }

  /**
   * Set a {@link ComparingDataSource} condition.
   * @param dataSource1
   * @param operator
   * @param dataSource2
   * @return
   * @deprecated use {@link #setVariableCondition(String)} or {@link #setQuestionnaireVariableCondition(String)} instead
   */
  @Deprecated
  public QuestionBuilder setCondition(IDataSource dataSource1, ComparisonOperator operator, IDataSource dataSource2) {
    getElement().setCondition(new ComparingDataSource(dataSource1, operator, dataSource2));
    return this;
  }

  /**
   * Set a {@link ComparingDataSource} condition over gender participant property.
   * @param operator
   * @param gender
   * @return
   * @deprecated use {@link #setVariableCondition(String)} or {@link #setQuestionnaireVariableCondition(String)} instead
   */
  @Deprecated
  public QuestionBuilder setCondition(ComparisonOperator operator, Gender gender) {
    getElement().setCondition(new ComparingDataSource(new ParticipantPropertyDataSource("gender"), operator, new FixedDataSource(gender.toString())));
    return this;
  }

  /**
   * Add a {@link ComputingDataSource} as a condition.
   * @param expression
   * @param dataSources
   * @return
   * @deprecated use {@link #setVariableCondition(String)} or {@link #setQuestionnaireVariableCondition(String)} instead
   */
  @Deprecated
  public QuestionBuilder setCondition(String expression, IDataSource... dataSources) {
    getElement().setCondition(new ComputingDataSource(DataType.BOOLEAN, expression).addDataSources(dataSources));
    return this;
  }

  /**
   * Get the question variable path.
   * @param name
   * @return
   */
  public String getVariablePath() {
    return getQuestionnaire().getName() + ":" + getVariableName();
  }

  /**
   * Get the question variable name.
   * @return
   */
  public String getVariableName() {
    return variableNameResolver.variableName(getElement());
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
