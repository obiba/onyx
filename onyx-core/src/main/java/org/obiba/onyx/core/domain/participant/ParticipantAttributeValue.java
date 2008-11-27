/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.domain.participant;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.obiba.core.domain.AbstractEntity;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;
import org.obiba.onyx.util.data.DataType;

/**
 * Participant attribute value.
 */
@Entity
public class ParticipantAttributeValue extends AbstractEntity {
  //
  // Constants
  //

  private static final long serialVersionUID = 1L;

  //
  // Instance Variables
  //

  @ManyToOne
  @JoinColumn(name = "participant_id")
  private Participant participant;

  private String attributeName;

  private DataType attributeType;

  private Double decimalValue;

  private Long integerValue;

  @Temporal(TemporalType.TIMESTAMP)
  private Date dateValue;

  @Column(length = 2000)
  private String textValue;

  //
  // Methods
  //

  public void setParticipant(Participant participant) {
    this.participant = participant;
  }

  public Participant getParticipant() {
    return participant;
  }

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
    }
  }

  public Data getData() {
    Data data = null;

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
