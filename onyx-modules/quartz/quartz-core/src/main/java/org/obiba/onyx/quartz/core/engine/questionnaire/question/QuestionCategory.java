package org.obiba.onyx.quartz.core.engine.questionnaire.question;

import java.io.Serializable;

public class QuestionCategory implements Serializable {

  private static final long serialVersionUID = 5244745063169629959L;

  private Question question;

  private Category category;
  
  private Boolean repeatable;

  private Boolean selected;

  private String exportName;

  public QuestionCategory() {
  }

  public Question getQuestion() {
    return question;
  }

  public void setQuestion(Question question) {
    this.question = question;
  }

  public Category getCategory() {
    return category;
  }

  public void setCategory(Category category) {
    this.category = category;
  }

  public Boolean getRepeatable() {
    return repeatable;
  }

  public void setRepeatable(Boolean repeatable) {
    this.repeatable = repeatable;
  }

  public Boolean getSelected() {
    return selected;
  }

  public void setSelected(Boolean selected) {
    this.selected = selected;
  }

  public String getExportName() {
    return exportName;
  }

  public void setExportName(String exportName) {
    this.exportName = exportName;
  }

}
