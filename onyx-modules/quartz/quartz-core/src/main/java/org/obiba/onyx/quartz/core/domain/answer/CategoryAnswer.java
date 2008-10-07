package org.obiba.onyx.quartz.core.domain.answer;

import java.util.Date;

import javax.persistence.Entity;

import org.obiba.core.domain.AbstractEntity;
import org.obiba.onyx.util.data.DataType;

@Entity
public class CategoryAnswer extends AbstractEntity {

  private static final long serialVersionUID = 8308345423791582240L;

  private String categoryName;

  private Integer occurence;

  private Boolean active;

  private DataType dataType;

  private String textValue;

  private Long integerValue;

  private Double decimalValue;

  private Date dateValue;

  private QuestionAnswer questionAnswer;

  private CategoryAnswer parentCategoryAnswer;

  public QuestionAnswer getQuestionAnswer() {
    return questionAnswer;
  }

  public void setQuestionAnswer(QuestionAnswer questionAnswer) {
    this.questionAnswer = questionAnswer;
  }

  public String getCategoryName() {
    return categoryName;
  }

  public void setCategoryName(String categoryName) {
    this.categoryName = categoryName;
  }

  public Integer getOccurence() {
    return occurence;
  }

  public void setOccurence(Integer occurence) {
    this.occurence = occurence;
  }

  public Boolean getActive() {
    return active;
  }

  public void setActive(Boolean active) {
    this.active = active;
  }

  public DataType getDataType() {
    return dataType;
  }

  public void setDataType(DataType dataType) {
    this.dataType = dataType;
  }

  public String getTextValue() {
    return textValue;
  }

  public void setTextValue(String textValue) {
    this.textValue = textValue;
  }

  public Long getIntegerValue() {
    return integerValue;
  }

  public void setIntegerValue(Long integerValue) {
    this.integerValue = integerValue;
  }

  public Double getDecimalValue() {
    return decimalValue;
  }

  public void setDecimalValue(Double decimalValue) {
    this.decimalValue = decimalValue;
  }

  public Date getDateValue() {
    return dateValue;
  }

  public void setDateValue(Date dateValue) {
    this.dateValue = dateValue;
  }

  public CategoryAnswer getParentCategoryAnswer() {
    return parentCategoryAnswer;
  }

  public void setParentCategoryAnswer(CategoryAnswer parentCategoryAnswer) {
    this.parentCategoryAnswer = parentCategoryAnswer;
  }

}
