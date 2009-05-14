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
  @JoinColumn(name = "measure_id")
  private Measure measure;

  /**
   * Instrument parameter code.
   */
  private String instrumentParameter;

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

  public InstrumentRunValue() {
  }

  public InstrumentRun getInstrumentRun() {
    return instrumentRun;
  }

  public void setInstrumentRun(InstrumentRun instrumentRun) {
    this.instrumentRun = instrumentRun;
  }

  public Measure getMeasure() {
    return measure;
  }

  public void setMeasure(Measure measure) {
    this.measure = measure;
  }

  /**
   * Returns the code of the associated instrument parameter.
   * 
   * @return instrument parameter code
   */
  public String getInstrumentParameter() {
    return instrumentParameter;
  }

  /**
   * Sets the instrument parameter associated with this instrument run value.
   * 
   * @param instrumentParameter instrument parameter code
   */
  public void setInstrumentParameter(String instrumentParameter) {
    this.instrumentParameter = instrumentParameter;
  }

  public InstrumentParameterCaptureMethod getCaptureMethod() {
    return captureMethod;
  }

  public void setCaptureMethod(InstrumentParameterCaptureMethod captureMethod) {
    this.captureMethod = captureMethod;
  }

  public void setData(Data data) {

    if(data != null) {
      switch(data.getType()) {
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
        integerValue = Long.valueOf(data.getValueAsString());
        break;

      case TEXT:
        textValue = data.getValue();
        break;

      case DATA:
        dataValue = data.getValue();
        break;
      }
    } else {
      booleanValue = null;
      dateValue = null;
      decimalValue = null;
      integerValue = null;
      textValue = null;
      dataValue = null;
    }
  }

  @Transient
  @SuppressWarnings("unchecked")
  public <T> T getValue(DataType type) {
    return (T) getData(type).getValue();
  }

  public Data getData(DataType type) {
    Data data = null;

    switch(type) {
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
      data = new Data(type, dataValue);
      break;
    }

    return data;
  }

}
