package org.obiba.onyx.quartz.core.engine.questionnaire.question;

import java.io.Serializable;

import org.obiba.onyx.quartz.core.engine.questionnaire.ILocalizable;
import org.obiba.onyx.quartz.core.engine.questionnaire.IVisitor;


public class QuestionCategory implements Serializable, ILocalizable {

  private static final long serialVersionUID = 5244745063169629959L;

  private Question question;

  private Category category;

  private boolean repeatable;

  private boolean selected;

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

  public boolean isRepeatable() {
    return repeatable;
  }

  public void setRepeatable(boolean repeatable) {
    this.repeatable = repeatable;
  }

  public boolean isSelected() {
    return selected;
  }

  public void setSelected(boolean selected) {
    this.selected = selected;
  }

  public String getExportName() {
    return exportName;
  }

  public void setExportName(String exportName) {
    this.exportName = exportName;
  }
  
  public String getName() {
    return question.getName() + "." + category.getName();
  }
  
  public void accept(IVisitor visitor) {
    visitor.visit(this);
  }

}
