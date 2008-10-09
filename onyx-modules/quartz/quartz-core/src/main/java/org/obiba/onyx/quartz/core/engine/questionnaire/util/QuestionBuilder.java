package org.obiba.onyx.quartz.core.engine.questionnaire.util;

import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;

public class QuestionBuilder extends AbstractQuestionnaireElementBuilder<Question> {

  private QuestionBuilder(PageBuilder parent, String name, boolean multiple) {
    super(parent.getQuestionnaire());
    if(!checkNamePattern(name)) {
      throw invalidNamePatternException(name);
    }
    element = new Question(name);
    element.setRequired(true);
    element.setMultiple(false);
    parent.getElement().addQuestion(element);
  }

  public QuestionBuilder(Questionnaire questionnaire, Question question) {
    super(questionnaire);
    this.element = question;
  }

  public static QuestionBuilder createQuestion(PageBuilder parent, String name, boolean multiple) {
    return new QuestionBuilder(parent, name, multiple);
  }

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
   * Add a shared {@link Category} to current {@link Question}, make it the current category.
   * @param name
   * @return
   */
  public CategoryBuilder withSharedCategory(String name) {
    Category category = questionnaire.findCategory(name);
    if(category == null) {
      return withCategory(name);
    } else {
      return CategoryBuilder.createQuestionCategory(this, category);
    }
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

}
