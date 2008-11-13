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

import org.obiba.onyx.quartz.core.engine.questionnaire.condition.ComparisionOperator;
import org.obiba.onyx.quartz.core.engine.questionnaire.condition.ConditionOperator;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireFinder;
import org.obiba.onyx.quartz.core.wicket.layout.IQuestionPanelFactory;
import org.obiba.onyx.quartz.core.wicket.layout.impl.DefaultQuestionPanelFactory;
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
  private QuestionBuilder(PageBuilder parent, String name, boolean multiple, Class<? extends IQuestionPanelFactory> uiFactoryClass) {
    super(parent.getQuestionnaire());
    if(!checkNamePattern(name)) {
      throw invalidNamePatternException(name);
    }
    if(!checkUniqueQuestionName(name)) {
      throw invalidNameUnicityException(Question.class, name);
    }
    element = new Question(name);
    element.setRequired(true);
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
  private QuestionBuilder(Questionnaire questionnaire, Question question) {
    super(questionnaire);
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
  public static QuestionBuilder createQuestion(PageBuilder parent, String name, boolean multiple) {
    return createQuestion(parent, name, multiple, DefaultQuestionPanelFactory.class);
  }

  /**
   * Create a {@link Question} in the given {@link Page}.
   * @param parent
   * @param name
   * @param multiple
   * @return
   * @throws IllegalArgumentException if name does not respect questionnaire element naming pattern.
   */
  public static QuestionBuilder createQuestion(PageBuilder parent, String name, boolean multiple, Class<? extends IQuestionPanelFactory> uiFactoryClass) {
    return new QuestionBuilder(parent, name, multiple, uiFactoryClass);
  }

  /**
   * Set the given {@link Question} as the current one.
   * @param questionnaire
   * @param question
   * @return
   */
  public static QuestionBuilder inQuestion(Questionnaire questionnaire, Question question) {
    return new QuestionBuilder(questionnaire, question);
  }

  /**
   * Add a required, non multiple, {@link Question} to current {@link Question} and make it current {@link Question}.
   * @param name
   * @return
   */
  public QuestionBuilder withQuestion(String name) {
    return withQuestion(name, false);
  }

  /**
   * Add a required, non multiple, {@link Question} to current {@link Question} and make it current {@link Question}.
   * @param name
   * @param uiFactoryClass
   * @return
   */
  public QuestionBuilder withQuestion(String name, Class<? extends IQuestionPanelFactory> uiFactoryClass) {
    return withQuestion(name, false, uiFactoryClass);
  }

  /**
   * Add a required {@link Question} to current {@link Question} and make it current {@link Question}.
   * @param name
   * @param multiple
   * @return
   * @see #getQuestion()
   */
  public QuestionBuilder withQuestion(String name, boolean multiple) {
    return withQuestion(name, multiple, DefaultQuestionPanelFactory.class);
  }

  /**
   * Add a required {@link Question} to current {@link Question} and make it current {@link Question}.
   * @param name
   * @param multiple
   * @param uiFactoryClass
   * @return
   * @see #getQuestion()
   */
  public QuestionBuilder withQuestion(String name, boolean multiple, Class<? extends IQuestionPanelFactory> uiFactoryClass) {
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
    question.setRequired(true);
    question.setMultiple(multiple);
    question.setUIFactoryName(uiFactoryName);
    element.addQuestion(question);
    element = question;

    return this;
  }

  /**
   * Set if current {@link Question} is required.
   * @param required
   * @return
   */
  public QuestionBuilder setRequired(boolean required) {
    element.setRequired(required);

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
   * Check question name unicity.
   * @param name
   * @return
   */
  private boolean checkUniqueQuestionName(String name) {
    return (QuestionnaireFinder.getInstance(questionnaire).findQuestion(name) == null);
  }

  private IllegalArgumentException invalidSharedCategoryNameUnicityException(String name) {
    return new IllegalArgumentException("There are several categories with name: " + name);
  }

  private IllegalArgumentException invalidQuestionPanelFactoryException(Class<? extends IQuestionPanelFactory> uiFactoryClass, Exception e) {
    return new IllegalArgumentException("Unable to get question panel factory name from " + uiFactoryClass.getName(), e);
  }

  public ConditionBuilder setAnswerCondition(String name, String question) {
    return setAnswerCondition(name, question, null, null, null, null, null);
  }

  public ConditionBuilder setAnswerCondition(String name, String question, String category, String openAnswerDefinition) {
    return setAnswerCondition(name, question, category, openAnswerDefinition, null, null, null);
  }

  public ConditionBuilder setAnswerCondition(String name, String question, String category, String openAnswerDefinition, Data data, ComparisionOperator comparisionOperator, Integer occurence) {
    return ConditionBuilder.createQuestionCondition(this, name, question, category, openAnswerDefinition, data, comparisionOperator, occurence);
  }

  public ConditionBuilder setMultipleCondition(String name, ConditionOperator operator) {
    return ConditionBuilder.createQuestionMultipleCondition(this, name, operator);
  }

  public ConditionBuilder setNoAnswerCondition(String name) {
    return ConditionBuilder.createQuestionNoAnswerCondition(this, name);
  }

}
