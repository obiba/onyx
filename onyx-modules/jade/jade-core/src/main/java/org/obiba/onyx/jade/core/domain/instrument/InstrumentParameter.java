package org.obiba.onyx.jade.core.domain.instrument;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Index;
import org.obiba.core.domain.AbstractEntity;

@Entity
//@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
//@DiscriminatorColumn(
//    name="instrument_parameter_type",
//    discriminatorType=DiscriminatorType.STRING
//)
//@DiscriminatorValue("InstrumentParameter")
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

  @ManyToOne
  @JoinColumn(name = "instrument_id")
  private Instrument instrument;

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

  public Instrument getInstrument() {
    return instrument;
  }

  public void setInstrument(Instrument instrument) {
    this.instrument = instrument;
  }

}
