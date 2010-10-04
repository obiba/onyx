/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.obiba.core.domain.AbstractEntity;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;
import org.obiba.onyx.util.data.DataType;

/**
 * Attribute value.
 */
@MappedSuperclass
public class AttributeValue extends AbstractEntity {

  private static final long serialVersionUID = 1L;

  @Column(nullable = false)
  private String attributeName;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private DataType attributeType;

  private Double decimalValue;

  private Long integerValue;

  @Temporal(TemporalType.TIMESTAMP)
  private Date dateValue;

  @Column(length = 2000)
  private String textValue;

  public void setAttributeName(String attributeName) {
    this.attributeName = attributeName;
  }

  public String getAttributeName() {
    return attributeName;
  }

  public void setAttributeType(DataType attributeType) {
    this.attributeType = attributeType;
  }

  public DataType getAttributeType() {
    return attributeType;
  }

  @SuppressWarnings("incomplete-switch")
  public void setData(Data data) {
    if(data != null) {
      if(data.getType() == getAttributeType()) {
        switch(getAttributeType()) {
        case DECIMAL:
          decimalValue = data.getValue();
          break;

        case INTEGER:
          integerValue = data.getValue();
          break;

        case DATE:
          dateValue = data.getValue();
          break;

        case TEXT:
          textValue = data.getValue();
          break;
        }
      } else {
        throw new IllegalArgumentException("DataType " + getAttributeType() + " expected, " + data.getType() + " received.");
      }
    } else {
      decimalValue = null;
      integerValue = null;
      dateValue = null;
      textValue = null;
    }
  }

  public Data getData() {
    Data data = null;

    if(getAttributeType() == null) return null;

    switch(getAttributeType()) {
    case DECIMAL:
      data = DataBuilder.buildDecimal(decimalValue);
      break;

    case INTEGER:
      data = DataBuilder.buildInteger(integerValue);
      break;

    case DATE:
      data = DataBuilder.buildDate(dateValue);
      break;

    case TEXT:
      data = DataBuilder.buildText(textValue);
      break;
    }

    return data;
  }

  @Transient
  @SuppressWarnings("unchecked")
  public <T> T getValue() {
    return (T) getData().getValue();
  }
}
