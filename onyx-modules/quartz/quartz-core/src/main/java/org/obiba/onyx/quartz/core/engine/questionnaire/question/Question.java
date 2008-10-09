package org.obiba.onyx.quartz.core.engine.questionnaire.question;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.obiba.onyx.quartz.core.engine.questionnaire.condition.Condition;

public class Question implements Serializable, ILocalizable {

  private static final long serialVersionUID = -7795909448581432466L;

  private String name;

  private String number;

  private Page page;

  private Boolean required;

  private Boolean multiple;

  private Integer minCount;

  private Integer maxCount;

  private List<QuestionCategory> questionCategories;

  private Condition condition;

  private Question parentQuestion;

  private List<Question> questions;

  public Question(String name) {
    this.name = name;
  }

  public Page getPage() {
    return page;
  }

  public void setPage(Page page) {
    this.page = page;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getNumber() {
    return number;
  }

  public void setNumber(String number) {
    this.number = number;
  }

  public Boolean getRequired() {
    return required;
  }

  public void setRequired(Boolean required) {
    this.required = required;
  }

  public Boolean getMultiple() {
    return multiple;
  }

  public void setMultiple(Boolean multiple) {
    this.multiple = multiple;
  }

  public List<QuestionCategory> getQuestionCategories() {
    return questionCategories != null ? questionCategories : (questionCategories = new ArrayList<QuestionCategory>());
  }

  /**
   * Get the underlying {@link Category} list.
   * @return
   */
  public List<Category> getCategories() {
    List<Category> categories = new ArrayList<Category>();

    for(QuestionCategory questionCategory : getQuestionCategories()) {
      categories.add(questionCategory.getCategory());
    }

    return categories;
  }

  public void addQuestionCategory(QuestionCategory questionCategory) {
    if(questionCategory != null) {
      getQuestionCategories().add(questionCategory);
      questionCategory.setQuestion(this);
    }
  }

  public Condition getCondition() {
    return condition;
  }

  public void setCondition(Condition condition) {
    this.condition = condition;
  }

  public Question getParentQuestion() {
    return parentQuestion;
  }

  public void setParentQuestion(Question parentQuestion) {
    this.parentQuestion = parentQuestion;
  }

  public List<Question> getQuestions() {
    return questions != null ? questions : (questions = new ArrayList<Question>());
  }

  public void addQuestion(Question question) {
    if(question != null) {
      getQuestions().add(question);
    }
  }

  private static final String[] PROPERTIES = { "label", "instructions", "caption", "help", "image" };

  public String getPropertyKey(String property) {
    for(String key : PROPERTIES) {
      if(key.equals(property)) {
        return getClass().getSimpleName() + "." + getName() + "." + property;
      }
    }
    throw new IllegalArgumentException("Invalid property for class " + getClass().getName() + ": " + property);
  }

  public String[] getProperties() {
    return PROPERTIES;
  }

  public Integer getMinCount() {
    return minCount;
  }

  public void setMinCount(Integer minCount) {
    this.minCount = minCount;
  }

  public Integer getMaxCount() {
    return maxCount;
  }

  public void setMaxCount(Integer maxCount) {
    this.maxCount = maxCount;
  }
  
  //
  // Find methods
  //
  public Category findCategory(String name) {
    for (Category category : getCategories()) {
      if (category.getName().equals(name)) {
        return category;
      }
    }
    return null;
  }
  
  public QuestionCategory findQuestionCategory(String name) {
    for (QuestionCategory qCategory : getQuestionCategories()) {
      if (qCategory.getCategory().getName().equals(name)) {
        return qCategory;
      }
    }
    return null;
  }
  

}
