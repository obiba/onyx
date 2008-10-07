package org.obiba.onyx.quartz.core.engine.questionnaire.util;

import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Section;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataType;

public class QuestionnaireBuilder {

  /**
   * Current {@link Questionnaire}.
   */
  private Questionnaire questionnaire;

  /**
   * Current {@link Section}.
   */
  private Section section;

  /**
   * Current {@link Page}.
   */
  private Page page;

  /**
   * Current {@link Question}.
   */
  private Question question;

  /**
   * Current {@link Category}.
   */
  private Category category;

  /**
   * Current {@link OpenAnswerDefinition}.
   */
  private OpenAnswerDefinition openAnswerDefinition;

  private QuestionnaireBuilder() {
  }

  /**
   * Create a new {@link Questionnaire}.
   * @param name
   * @param version
   * @return
   */
  public static QuestionnaireBuilder createQuestionnaire(String name, String version) {
    QuestionnaireBuilder builder = new QuestionnaireBuilder();
    builder.questionnaire = new Questionnaire(name, version);
    return builder;
  }

  /**
   * Add a top level {@link Section} to current {@link Questionnaire}, and make it the current {@link Section}
   * @param name
   * @return
   * @see #getSection()
   */
  public QuestionnaireBuilder withSection(String name) {
    section = new Section(name);
    questionnaire.addSection(section);

    return this;
  }

  /**
   * Add sub {@link Section} to given {@link Section} and make it the current {@link Section}.
   * @param section
   * @param name
   * @return
   * @see #getSection()
   */
  public QuestionnaireBuilder withSection(Section section, String name) {
    Section subSection = new Section(name);
    subSection.setQuestionnaire(questionnaire);
    section.addSection(subSection);
    this.section = subSection;

    return this;
  }

  /**
   * Add a {@link Page} to current {@link Section} and make it the current {@link Page}.
   * @param name
   * @return
   * @see #getPage()
   */
  public QuestionnaireBuilder withPage(String name) {
    page = new Page(name);
    questionnaire.addPage(page);
    section.addPage(page);

    return this;
  }

  /**
   * Add a required, non multiple, {@link Question} to current {@link Page} and make it current {@link Question}.
   * @param name
   * @return
   * @see #getQuestion()
   */
  public QuestionnaireBuilder withQuestion(String name) {
    question = new Question(name);
    question.setRequired(true);
    question.setMultiple(false);
    page.addQuestion(question);

    return this;
  }

  /**
   * Add a required {@link Question} to current {@link Page} and make it current {@link Question}.
   * @param name
   * @param multiple
   * @return
   * @see #getQuestion()
   */
  public QuestionnaireBuilder withQuestion(String name, boolean multiple) {
    question = new Question(name);
    question.setRequired(true);
    question.setMultiple(multiple);
    page.addQuestion(question);

    return this;
  }

  /**
   * Set if current {@link Question} is required.
   * @param required
   * @return
   */
  public QuestionnaireBuilder setQuestionRequired(boolean required) {
    question.setRequired(required);

    return this;
  }

  /**
   * Set the range of answer count for current {@link Question}.
   * @param minCount no limit if null
   * @param maxCount no limit if null
   * @return
   */
  public QuestionnaireBuilder setQuestionAnswerCount(Integer minCount, Integer maxCount) {
    question.setMinCount(minCount);
    question.setMaxCount(maxCount);

    return this;
  }

  /**
   * Add a {@link Category} to current {@link Question}, make it the current category.
   * @param name
   * @param selected is selected by default
   * @return
   * @see #getQuestion()
   */
  public QuestionnaireBuilder withCategory(String name, boolean selected) {
    Category category = new Category(name);
    QuestionCategory questionCategory = new QuestionCategory();
    questionCategory.setSelected(selected);
    questionCategory.setCategory(category);
    question.addQuestionCategory(questionCategory);
    this.category = category;

    return this;
  }

  /**
   * Add a {@link Category} to current {@link Question}, make it the current category.
   * @param name
   * @return
   * @see #getQuestion()
   */
  public QuestionnaireBuilder withCategory(String name) {
    Category category = new Category(name);
    QuestionCategory questionCategory = new QuestionCategory();
    questionCategory.setCategory(category);
    question.addQuestionCategory(questionCategory);
    this.category = category;

    return this;
  }

  /**
   * Add a global {@link Category} to current {@link Question}, make it the current category.
   * @param name
   * @return
   * @see #getQuestion()
   */
  public QuestionnaireBuilder withCategory(Category category) {
    QuestionCategory questionCategory = new QuestionCategory();
    questionCategory.setCategory(category);
    question.addQuestionCategory(questionCategory);
    this.category = category;

    return this;
  }

  /**
   * Add a global {@link Category} to current {@link Question} with an export name, make it the current category.
   * @param name
   * @param exportName
   * @return
   * @see #getQuestion()
   */
  public QuestionnaireBuilder withCategory(Category category, String exportName) {
    QuestionCategory questionCategory = new QuestionCategory();
    questionCategory.setCategory(category);
    questionCategory.setExportName(exportName);
    question.addQuestionCategory(questionCategory);
    this.category = category;

    return this;
  }

  /**
   * Add a global {@link Category} to current {@link Question}, make it the current category.
   * @param name
   * @param selected is selected by default
   * @return
   * @see #getQuestion()
   */
  public QuestionnaireBuilder withCategory(Category category, boolean selected) {
    QuestionCategory questionCategory = new QuestionCategory();
    questionCategory.setSelected(selected);
    questionCategory.setCategory(category);
    question.addQuestionCategory(questionCategory);
    this.category = category;

    return this;
  }

  /**
   * Add a global {@link Category} to current {@link Question} with an export name, make it the current category.
   * @param name
   * @param selected is selected by default
   * @param exportName
   * @return
   * @see #getQuestion()
   */
  public QuestionnaireBuilder withCategory(Category category, boolean selected, String exportName) {
    QuestionCategory questionCategory = new QuestionCategory();
    questionCategory.setSelected(selected);
    questionCategory.setCategory(category);
    questionCategory.setExportName(exportName);
    question.addQuestionCategory(questionCategory);
    this.category = category;

    return this;
  }

  /**
   * Set the {@link OpenAnswerDefinition} to the current category.
   * @param openAnswerDefinition
   * @return
   */
  public QuestionnaireBuilder withOpenAnswerDefinition(OpenAnswerDefinition openAnswerDefinition) {
    category.setOpenAnswerDefinition(openAnswerDefinition);
    this.openAnswerDefinition = openAnswerDefinition;

    return this;
  }

  /**
   * Set the {@link OpenAnswerDefinition} to the current category.
   * @param openAnswerDefinition
   * @return
   */
  public QuestionnaireBuilder withOpenAnswerDefinition(String name, DataType dataType) {
    OpenAnswerDefinition openAnswerDefinition = new OpenAnswerDefinition(name, dataType);
    category.setOpenAnswerDefinition(openAnswerDefinition);
    this.openAnswerDefinition = openAnswerDefinition;

    return this;
  }

  /**
   * Set the absolute range to the current {@link OpenAnswerDefinition}.
   * @param minValue no limit if null
   * @param maxValue no limit if null
   * @return
   */
  public QuestionnaireBuilder setOpenAnswerDefinitionAbsoluteValues(Data minValue, Data maxValue) {
    if (minValue != null && !minValue.getType().equals(openAnswerDefinition.getDataType())) {
      throw new IllegalArgumentException("Wrong data type for absolute min value: " + openAnswerDefinition.getDataType() + " expected, " + minValue.getType() + " found.");
    }
    if (maxValue != null && !maxValue.getType().equals(openAnswerDefinition.getDataType())) {
      throw new IllegalArgumentException("Wrong data type for absolute max value: " + openAnswerDefinition.getDataType() + " expected, " + maxValue.getType() + " found.");
    }
    
    openAnswerDefinition.setAbsoluteMinValue(minValue);
    openAnswerDefinition.setAbsoluteMaxValue(maxValue);
    
    return this;
  }
  
  /**
   * Set the usual range to the current {@link OpenAnswerDefinition}.
   * @param minValue no limit if null
   * @param maxValue no limit if null
   * @return
   */
  public QuestionnaireBuilder setOpenAnswerDefinitionUsualValues(Data minValue, Data maxValue) {
    if (minValue != null && !minValue.getType().equals(openAnswerDefinition.getDataType())) {
      throw new IllegalArgumentException("Wrong data type for usual min value: " + openAnswerDefinition.getDataType() + " expected, " + minValue.getType() + " found.");
    }
    if (maxValue != null && !maxValue.getType().equals(openAnswerDefinition.getDataType())) {
      throw new IllegalArgumentException("Wrong data type for usual max value: " + openAnswerDefinition.getDataType() + " expected, " + maxValue.getType() + " found.");
    }

    openAnswerDefinition.setUsualMinValue(minValue);
    openAnswerDefinition.setUsualMaxValue(maxValue);
    
    return this;
  }
  
  /**
   * Set the unit to the current {@link OpenAnswerDefinition}.
   * @param unit
   * @return
   */
  public QuestionnaireBuilder setOpenAnswerDefinitionUnit(String unit) {
    openAnswerDefinition.setUnit(unit);
    
    return this;
  }
  
  /**
   * Set the data format to the current {@link OpenAnswerDefinition}.
   * @param format
   * @return
   */
  public QuestionnaireBuilder setOpenAnswerDefinitionFormat(String format) {
    openAnswerDefinition.setFormat(format);
    
    return this;
  }
  
  /**
   * Set the default data to the current {@link OpenAnswerDefinition}.
   * @param data
   * @return
   */
  public QuestionnaireBuilder setOpenAnswerDefinitionDefaultData(Data data) {
    if (data != null && !data.getType().equals(openAnswerDefinition.getDataType())) {
      throw new IllegalArgumentException("Wrong data type for default data: " + openAnswerDefinition.getDataType() + " expected, " + data.getType() + " found.");
    }
    
    openAnswerDefinition.setDefaultData(data);
    
    return this;
  }
  
  /**
   * Add a set of global {@link Category} to current {@link Question}.
   * @param categories
   * @return
   * @see #getQuestion()
   */
  public QuestionnaireBuilder withCategories(Category... categories) {
    for(Category category : categories) {
      withCategory(category);
    }

    return this;
  }

  /**
   * Add a set of {@link Category} to current {@link Question}.
   * @param names
   * @return
   */
  public QuestionnaireBuilder withCategories(String... names) {
    for(String name : names) {
      withCategory(name);
    }

    return this;
  }

  /**
   * Get the current {@link Page}.
   * @return
   */
  public Page getPage() {
    return page;
  }

  /**
   * Get the current {@link Question}.
   * @return
   */
  public Question getQuestion() {
    return question;
  }

  /**
   * Get the current {@link Section}.
   * @return
   */
  public Section getSection() {
    return section;
  }

  /**
   * Get the current {@link Questionnaire}.
   * @return
   */
  public Questionnaire getQuestionnaire() {
    return questionnaire;
  }

  /**
   * Get the current {@link Category}
   * @return
   */
  public Category getCategory() {
    return category;
  }

  /**
   * Get the current {@link OpenAnswerDefinition}
   * @return
   */
  public OpenAnswerDefinition getOpenAnswerDefinition() {
    return openAnswerDefinition;
  }

}
