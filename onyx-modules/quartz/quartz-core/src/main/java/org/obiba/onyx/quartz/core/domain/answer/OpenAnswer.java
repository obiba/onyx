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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.obiba.core.domain.AbstractEntity;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;
import org.obiba.onyx.util.data.DataType;

@Entity
public class OpenAnswer extends AbstractEntity {

  private static final long serialVersionUID = 8772952316177874064L;

  @Enumerated(EnumType.STRING)
  private DataType dataType;

  @Column(length = 2000)
  private String textValue;

  private Long integerValue;

  private Double decimalValue;

  @Temporal(TemporalType.TIMESTAMP)
  private Date dateValue;

  private String openAnswerDefinitionName;

  @ManyToOne
  @JoinColumn(name = "category_answer_id")
  private CategoryAnswer categoryAnswer;

  public CategoryAnswer getCategoryAnswer() {
    return categoryAnswer;
  }

  public void setCategoryAnswer(CategoryAnswer categoryAnswer) {
    this.categoryAnswer = categoryAnswer;
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

  public Data getData() {
    Data data = null;

    if(getDataType() == null) return null;

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

  public void setData(Data data) {

    if(data != null) {
      if(data.getType() == getDataType()) {

        switch(getDataType()) {
        case DATE:
          dateValue = data.getValue();
          break;

        case DECIMAL:
          decimalValue = data.getValue();
          break;

        case INTEGER:
          integerValue = data.getValue();
          break;

        case TEXT:
          textValue = data.getValue();
          break;
        }
      } else {
        throw new IllegalArgumentException("DataType " + getDataType() + " expected, " + data.getType() + " received.");
      }
    }
  }

  public String getOpenAnswerDefinitionName() {
    return openAnswerDefinitionName;
  }

  public void setOpenAnswerDefinitionName(String openAnswerDefinitionName) {
    this.openAnswerDefinitionName = openAnswerDefinitionName;
  }
}
