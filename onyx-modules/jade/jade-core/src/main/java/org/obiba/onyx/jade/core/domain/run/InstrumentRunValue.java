/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.core.domain.run;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.obiba.core.domain.AbstractEntity;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameterCaptureMethod;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;
import org.obiba.onyx.util.data.DataType;

@Entity
public class InstrumentRunValue extends AbstractEntity {

  private static final long serialVersionUID = 267079755213810737L;

  @ManyToOne
  @JoinColumn(name = "instrument_run_id")
  private InstrumentRun instrumentRun;

  @ManyToOne
  @JoinColumn(name = "instrument_parameter_id")
  private InstrumentParameter instrumentParameter;

  private Boolean booleanValue;

  @Temporal(TemporalType.TIMESTAMP)
  private Date dateValue;

  private Double decimalValue;

  private Long integerValue;

  @Column(length = 2000)
  private String textValue;

  @Lob
  @Column(length = Integer.MAX_VALUE)
  private byte[] dataValue;

  @Enumerated(EnumType.STRING)
  private InstrumentParameterCaptureMethod captureMethod;

  @Enumerated(EnumType.STRING)
  private ValueIntegrity valueIntegrity;

  public InstrumentRunValue() {
  }

  public InstrumentRun getInstrumentRun() {
    return instrumentRun;
  }

  public void setInstrumentRun(InstrumentRun instrumentRun) {
    this.instrumentRun = instrumentRun;
  }

  public InstrumentParameter getInstrumentParameter() {
    return instrumentParameter;
  }

  public void setInstrumentParameter(InstrumentParameter instrumentParameter) {
    this.instrumentParameter = instrumentParameter;
  }

  public InstrumentParameterCaptureMethod getCaptureMethod() {
    return captureMethod;
  }

  public void setCaptureMethod(InstrumentParameterCaptureMethod captureMethod) {
    this.captureMethod = captureMethod;
  }

  public ValueIntegrity getValueIntegrity() {
    return valueIntegrity;
  }

  public void setValueIntegrity(ValueIntegrity valueIntegrity) {
    this.valueIntegrity = valueIntegrity;
  }

  public DataType getDataType() {
    return instrumentParameter.getDataType();
  }

  public void setData(Data data) {

    if(data != null) {
      if(data.getType() == getDataType()) {

        switch(getDataType()) {
        case BOOLEAN:
          booleanValue = data.getValue();
          break;

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

        case DATA:
          dataValue = data.getValue();
          break;
        }
      } else {
        throw new IllegalArgumentException("DataType " + getDataType() + " expected, " + data.getType() + " received.");
      }
    }
  }

  @Transient
  @SuppressWarnings("unchecked")
  public <T> T getValue() {
    return (T)getData().getValue();
  }

  public Data getData() {
    Data data = null;

    switch(getDataType()) {
    case BOOLEAN:
      data = DataBuilder.buildBoolean(booleanValue);
      break;

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

    case DATA:
      data = new Data(getDataType(), dataValue);
      break;
    }

    return data;
  }

}
