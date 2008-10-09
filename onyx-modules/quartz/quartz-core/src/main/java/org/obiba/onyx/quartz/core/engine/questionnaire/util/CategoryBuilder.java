package org.obiba.onyx.quartz.core.engine.questionnaire.util;

import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.util.data.DataType;

public class CategoryBuilder extends AbstractQuestionnaireElementBuilder<Category> {

  private QuestionBuilder parent;

  private QuestionCategory questionCategory;

  private CategoryBuilder(QuestionBuilder parent, String name) {
    this.parent = parent;
    element = new Category(name);
    if(parent != null) {
      questionCategory = new QuestionCategory();
      questionCategory.setSelected(false);
      questionCategory.setCategory(element);
      parent.getElement().addQuestionCategory(questionCategory);
    }
  }

  private CategoryBuilder(QuestionBuilder parent, Category category) {
    this.parent = parent;
    element = category;
    questionCategory = new QuestionCategory();
    questionCategory.setSelected(false);
    questionCategory.setCategory(element);
    parent.getElement().addQuestionCategory(questionCategory);
  }

  /**
   * Add a {@link Category} to current {@link Question}, make it the current category.
   * @param name
   * @param selected if is selected by default
   * @return
   */
  public static CategoryBuilder createCategory(QuestionBuilder parent, String name) {
    return new CategoryBuilder(parent, name);
  }

  /**
   * Add a {@link Category} to current {@link Question}, make it the current category.
   * @param name
   * @param selected if is selected by default
   * @return
   */
  public static CategoryBuilder createCategory(QuestionBuilder parent, Category category) {
    return new CategoryBuilder(parent, category);
  }

  public QuestionBuilder parent() {
    return parent;
  }

  public CategoryBuilder setExportName(String exportName) {
    questionCategory.setExportName(exportName);

    return this;
  }

  public CategoryBuilder setSelected(boolean selected) {
    questionCategory.setSelected(selected);

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

}
