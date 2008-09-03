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
      data = new Data(getDataType(), booleanValue);
      break;

    case DATE:
      data = new Data(getDataType(), dateValue);
      break;

    case DECIMAL:
      data = new Data(getDataType(), decimalValue);
      break;

    case INTEGER:
      data = new Data(getDataType(), integerValue);
      break;

    case TEXT:
      data = new Data(getDataType(), textValue);
      break;

    case DATA:
      data = new Data(getDataType(), dataValue);
      break;
    }

    return data;
  }

}
