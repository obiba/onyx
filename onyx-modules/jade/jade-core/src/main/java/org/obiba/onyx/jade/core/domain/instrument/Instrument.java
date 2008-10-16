/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
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
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Index;
import org.obiba.core.domain.AbstractEntity;
import org.obiba.onyx.jade.core.domain.run.InstrumentRun;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "barcode" }) })
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

  @OneToMany(mappedBy = "instrument")
  private List<ContraIndication> contraIndications;

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

  public List<ContraIndication> getContraIndications() {
    return contraIndications != null ? contraIndications : (contraIndications = new ArrayList<ContraIndication>());
  }

  public void addContraIndication(ContraIndication contraIndication) {
    if(contraIndication != null) {
      getContraIndications().add(contraIndication);
      contraIndication.setInstrument(this);
    }
  }
}
