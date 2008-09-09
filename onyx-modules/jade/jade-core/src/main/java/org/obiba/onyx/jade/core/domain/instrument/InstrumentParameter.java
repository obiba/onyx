package org.obiba.onyx.jade.core.domain.instrument;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Index;
import org.obiba.core.domain.AbstractEntity;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;
import org.obiba.onyx.util.data.DataType;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "instrument_parameter_type", discriminatorType = DiscriminatorType.STRING, length=100)
@DiscriminatorValue("InstrumentParameter")
public abstract class InstrumentParameter extends AbstractEntity {

  @Column(length = 200)
  @Index(name = "name_index")
  private String name;

  @Column(length = 200)
  private String description;

  @Column(length = 20)
  private String measurementUnit;

  @Enumerated(EnumType.STRING)
  private DataType dataType;

  @Enumerated(EnumType.STRING)
  private InstrumentParameterCaptureMethod captureMethod;

  @ManyToOne
  @JoinColumn(name = "instrument_id")
  private Instrument instrument;

  @OneToMany(mappedBy = "instrumentParameter")
  private List<InstrumentRunValue> instrumentRunValues;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getMeasurementUnit() {
    return measurementUnit;
  }

  public void setMeasurementUnit(String measurementUnit) {
    this.measurementUnit = measurementUnit;
  }

  public DataType getDataType() {
    return dataType;
  }

  public void setDataType(DataType dataType) {
    this.dataType = dataType;
  }

  public InstrumentParameterCaptureMethod getCaptureMethod() {
    return captureMethod;
  }

  public void setCaptureMethod(InstrumentParameterCaptureMethod captureMethod) {
    this.captureMethod = captureMethod;
  }

  public Instrument getInstrument() {
    return instrument;
  }

  public void setInstrument(Instrument instrument) {
    this.instrument = instrument;
  }

  public List<InstrumentRunValue> getInstrumentRunValues() {
    return instrumentRunValues != null ? instrumentRunValues : (instrumentRunValues = new ArrayList<InstrumentRunValue>());
  }

  public void addInstrumentRunValue(InstrumentRunValue value) {
    if(value != null) {
      getInstrumentRunValues().add(value);
      value.setInstrumentParameter(this);
    }
  }

}
