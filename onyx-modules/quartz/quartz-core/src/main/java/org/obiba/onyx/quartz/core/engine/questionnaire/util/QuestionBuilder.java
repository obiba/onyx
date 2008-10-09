package org.obiba.onyx.quartz.core.engine.questionnaire.util;

import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;

public class QuestionBuilder extends AbstractQuestionnaireElementBuilder<Question> {

  private PageBuilder parent;
  
  private QuestionBuilder(PageBuilder parent, String name, boolean multiple) {
    this.parent = parent;
    withQuestion(name, multiple);
  }
  
  public PageBuilder parent() {
    return parent;
  }
  
  public static QuestionBuilder createQuestion(PageBuilder parent, String name, boolean multiple) {
    return new QuestionBuilder(parent, name, multiple);
  }
  
  /**
   * Add a required, non multiple, {@link Question} to current {@link Page} and make it current {@link Question}.
   * @param name
   * @return
   * @see #getQuestion()
   */
  public QuestionBuilder withQuestion(String name) {
    if(!checkNamePattern(name)) {
      throw invalidNamePatternException(name);
    }
    element = new Question(name);
    element.setRequired(true);
    element.setMultiple(false);
    parent.getElement().addQuestion(element);

    return this;
  }
  
  /**
   * Add a required {@link Question} to current {@link Page} and make it current {@link Question}.
   * @param name
   * @param multiple
   * @return
   * @see #getQuestion()
   */
  public QuestionBuilder withQuestion(String name, boolean multiple) {
    if(!checkNamePattern(name)) {
      throw invalidNamePatternException(name);
    }
    element = new Question(name);
    element.setRequired(true);
    element.setMultiple(multiple);
    parent.getElement().addQuestion(element);

    return this;
  }
  
  /**
   * Set if current {@link Question} is required.
   * @param required
   * @return
   */
  public QuestionBuilder setQuestionRequired(boolean required) {
    element.setRequired(required);

    return this;
  }

  /**
   * Set the range of answer count for current {@link Question}.
   * @param minCount no limit if null
   * @param maxCount no limit if null
   * @return
   */
  public QuestionBuilder setQuestionAnswerCount(Integer minCount, Integer maxCount) {
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
    return CategoryBuilder.createCategory(this, name);
  }
  
  /**
   * Add a {@link Category} to current {@link Question}, make it the current category.
   * @param name
   * @return
   */
  public CategoryBuilder withCategory(Category category) {
    return CategoryBuilder.createCategory(this, category);
  }
    
  /**
   * Add a set of global {@link Category} to current {@link Question}.
   * @param categories
   * @return
   * @see #getQuestion()
   */
  public CategoryBuilder withCategories(Category... categories) {
    CategoryBuilder child = null;
    
    for(Category category : categories) {
      child = withCategory(category);
    }

    return child;
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
}
