package org.obiba.onyx.quartz.core.engine.questionnaire.util.builder;

import java.util.List;
import java.util.Map;

import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireFinder;

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
  private QuestionBuilder(PageBuilder parent, String name, boolean multiple) {
    super(parent.getQuestionnaire());
    if(!checkNamePattern(name)) {
      throw invalidNamePatternException(name);
    }
    if(!checkUniqueQuestionName(name)) {
      throw invalidNameUnicityException(Question.class, name);
    }
    element = new Question(name);
    element.setRequired(true);
    element.setMultiple(false);
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
    return new QuestionBuilder(parent, name, multiple);
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
   * Add a required {@link Question} to current {@link Question} and make it current {@link Question}.
   * @param name
   * @param multiple
   * @return
   * @see #getQuestion()
   */
  public QuestionBuilder withQuestion(String name, boolean multiple) {
    if(!checkNamePattern(name)) {
      throw invalidNamePatternException(name);
    }
    if(!checkUniqueQuestionName(name)) {
      throw invalidNameUnicityException(Question.class, name);
    }
    Question question = new Question(name);
    question.setRequired(true);
    question.setMultiple(false);
    element.addQuestion(question);

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
    if (map.keySet().size() > 1) {
      throw invalidSharedCategoryNameUnicityException(name);
    }
    else if (map.keySet().isEmpty()) {
      return withCategory(name);
    }
    else {
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
}
