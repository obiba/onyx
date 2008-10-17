/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.domain.answer;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.obiba.core.domain.AbstractEntity;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;
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

  @ManyToOne
  @JoinColumn(name = "question_answer_id")
  private QuestionAnswer questionAnswer;

  @ManyToOne
  @JoinColumn(name = "parent_category_answer_id")
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

  public QuestionAnswer getParentQuestionAnswer() {
    if (getParentCategoryAnswer() == null)
      return null;
    
    return getParentCategoryAnswer().getQuestionAnswer();
  }
  
  public Data getData() {
    Data data = null;

    switch(getDataType()) {

    case DATE:
      data = DataBuilder.buildDate(dateValue);
      break;

    case DECIMAL:
      data = DataBuilder.buildDecimal(decimalValue);
      break;

    case INTEGER:
      data = DataBuilder.buildInteger(integerValue);
      break;

    case TEXT:
      data = DataBuilder.buildText(textValue);
      break;
    }

    return data;
  }
  
}
