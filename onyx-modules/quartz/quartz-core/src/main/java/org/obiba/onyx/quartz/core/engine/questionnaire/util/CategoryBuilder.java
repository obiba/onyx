package org.obiba.onyx.quartz.core.engine.questionnaire.util;

import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.util.data.DataType;

public class CategoryBuilder extends AbstractQuestionnaireElementBuilder<Category> {

  private QuestionCategory questionCategory;

  private CategoryBuilder(QuestionBuilder parent, Category category) {
    super(parent.getQuestionnaire());
    this.element = category;
    this.questionCategory = createQuestionCategory(parent.getElement());
  }

  /**
   * Add a {@link Category} to current {@link Question}, make it the current category.
   * @param name
   * @param selected if is selected by default
   * @return
   */
  public static CategoryBuilder createQuestionCategory(QuestionBuilder parent, String name) {
    if(!checkNamePattern(name)) {
      throw invalidNamePatternException(name);
    }
    return new CategoryBuilder(parent, new Category(name));
  }

  /**
   * Add a {@link Category} to current {@link Question}, make it the current category.
   * @param name
   * @param selected if is selected by default
   * @return
   */
  public static CategoryBuilder createQuestionCategory(QuestionBuilder parent, Category category) {
    return new CategoryBuilder(parent, category);
  }

  public CategoryBuilder setExportName(String exportName) {
    questionCategory.setExportName(exportName);
    return this;
  }

  public CategoryBuilder setSelected(boolean selected) {
    questionCategory.setSelected(selected);
    return this;
  }

  public CategoryBuilder setRepeatable(boolean repeatable) {
    questionCategory.setRepeatable(repeatable);
    return this;
  }

  /**
   * Set the {@link OpenAnswerDefinition} to the current category.
   * @param name
   * @param dataType
   * @return
   */
  public OpenAnswerDefinitionBuilder withOpenAnswerDefinition(String name, DataType dataType) {
    return OpenAnswerDefinitionBuilder.createOpenAnswerDefinition(this, name, dataType);
  }

  /**
   * Set the {@link OpenAnswerDefinition} to the current category.
   * @param openAnswerDefinition
   * @return
   */
  public OpenAnswerDefinitionBuilder withOpenAnswerDefinition(OpenAnswerDefinition openAnswerDefinition) {
    return OpenAnswerDefinitionBuilder.createOpenAnswerDefinition(this, openAnswerDefinition);
  }

  public CategoryBuilder withCategory(String name) {
    this.element = new Category(name);
    questionCategory = createQuestionCategory(questionCategory.getQuestion());
    return this;
  }

  public CategoryBuilder withCategories(String... names) {
    for(String name : names) {
      withCategory(name);
    }
    return this;
  }

  public CategoryBuilder withSharedCategory(String name) {
    Category category = questionnaire.findCategory(name);
    if(category == null) {
      return withCategory(name);
    } else {
      this.element = category;
      questionCategory = createQuestionCategory(questionCategory.getQuestion());
      return this;
    }
  }

  public CategoryBuilder withSharedCategories(String... names) {
    for(String name : names) {
      withSharedCategory(name);
    }
    return this;
  }

  private QuestionCategory createQuestionCategory(Question question) {
    QuestionCategory questionCategory = new QuestionCategory();
    questionCategory.setSelected(false);
    questionCategory.setCategory(element);
    question.addQuestionCategory(questionCategory);
    return questionCategory;
  }

}
