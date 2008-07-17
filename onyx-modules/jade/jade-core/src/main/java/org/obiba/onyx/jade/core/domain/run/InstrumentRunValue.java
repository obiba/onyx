package org.obiba.onyx.jade.core.domain.run;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Lob;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.obiba.core.domain.AbstractEntity;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameterCaptureMethod;

@Entity
public class InstrumentRunValue extends AbstractEntity {

  private static final long serialVersionUID = 267079755213810737L;

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

  public Boolean getBooleanValue() {
    return booleanValue;
  }

  public void setBooleanValue(Boolean booleanValue) {
    this.booleanValue = booleanValue;
  }

  public Date getDateValue() {
    return dateValue;
  }

  public void setDateValue(Date dateValue) {
    this.dateValue = dateValue;
  }

  public Double getDecimalValue() {
    return decimalValue;
  }

  public void setDecimalValue(Double decimalValue) {
    this.decimalValue = decimalValue;
  }

  public Long getIntegerValue() {
    return integerValue;
  }

  public void setIntegerValue(Long integerValue) {
    this.integerValue = integerValue;
  }

  public String getTextValue() {
    return textValue;
  }

  public void setTextValue(String textValue) {
    this.textValue = textValue;
  }

  public byte[] getDataValue() {
    return dataValue;
  }

  public void setDataValue(byte[] dataValue) {
    this.dataValue = dataValue;
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

}
