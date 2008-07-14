package org.obiba.onyx.jade.core.domain.instrument;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Index;
import org.obiba.core.domain.AbstractEntity;

@Entity
public class Instrument extends AbstractEntity {

  private static final long serialVersionUID = 14533453L;

  @Column(length = 200)
  @Index(name = "name_index")
  private String name;

  @Column(length = 200)
  private String vendor;

  @Column(length = 200)
  private String model;

  @Column(length = 200)
  private String serialNumber;

  @Column(length = 200)
  private String barcode;

  @Enumerated(EnumType.STRING)
  private InstrumentStatus status;

  @ManyToOne
  @JoinColumn(name = "instrument_type_id")
  private InstrumentType instrumentType;

  @OneToMany(mappedBy = "instrument")
  private List<InstrumentParameter> instrumentParameters;

  @OneToMany(mappedBy = "instrument")
  private List<InstrumentRun> instrumentRuns;

  // TODO image

  public Instrument() {
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getVendor() {
    return vendor;
  }

  public void setVendor(String vendor) {
    this.vendor = vendor;
  }

  public String getModel() {
    return model;
  }

  public void setModel(String model) {
    this.model = model;
  }

  public String getSerialNumber() {
    return serialNumber;
  }

  public void setSerialNumber(String serialNumber) {
    this.serialNumber = serialNumber;
  }

  public String getBarcode() {
    return barcode;
  }

  public void setBarcode(String barcode) {
    this.barcode = barcode;
  }

  public InstrumentStatus getStatus() {
    return status;
  }

  public void setStatus(InstrumentStatus status) {
    this.status = status;
  }

  public InstrumentType getInstrumentType() {
    return instrumentType;
  }

  public void setInstrumentType(InstrumentType instrumentType) {
    this.instrumentType = instrumentType;
  }

  public List<InstrumentParameter> getInstrumentParameters() {
    return instrumentParameters != null ? instrumentParameters : (instrumentParameters = new ArrayList<InstrumentParameter>());
  }

  public void addInstrumentParameter(InstrumentParameter instrumentParameter) {
    if(instrumentParameter != null) {
      getInstrumentParameters().add(instrumentParameter);
      instrumentParameter.setInstrument(this);
    }
  }

  public List<InstrumentRun> getInstrumentRuns() {
    return instrumentRuns != null ? instrumentRuns : (instrumentRuns = new ArrayList<InstrumentRun>());
  }

  public void addInstrumentRun(InstrumentRun instrumentRun) {
    if(instrumentRun != null) {
      getInstrumentRuns().add(instrumentRun);
      instrumentRun.setInstrument(this);
    }
  }

}
