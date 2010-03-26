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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Index;
import org.obiba.core.domain.AbstractEntity;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "barcode" }) })
public class Instrument extends AbstractEntity {

  private static final long serialVersionUID = 14533453L;

  @OneToMany(cascade = { CascadeType.ALL }, mappedBy = "instrument")
  private List<InstrumentMeasurementType> types;

  @Column(length = 200)
  @Index(name = "name_index")
  private String name;

  @Column(length = 200)
  private String vendor;

  @Column(length = 200)
  private String model;

  @Column(length = 200)
  private String serialNumber;

  @Column(length = 200, nullable = false)
  private String barcode;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private InstrumentStatus status;

  @Column(length = 200)
  private String workstation;

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

  public InstrumentUsage getUsage() {
    if(getStatus().equals(InstrumentStatus.ACTIVE)) {
      return (getWorkstation() != null ? InstrumentUsage.RESERVED : InstrumentUsage.SHARED);
    } else {
      return InstrumentUsage.OUT_OF_SERVICE;
    }
  }

  public String getWorkstation() {
    return workstation;
  }

  public void setWorkstation(String workstation) {
    this.workstation = workstation;
  }

  public List<InstrumentMeasurementType> getInstrumentMeasurementTypes() {
    return types != null ? types : (types = new ArrayList<InstrumentMeasurementType>());
  }

  public void addType(String type) {
    if(type != null) {
      getInstrumentMeasurementTypes().add(new InstrumentMeasurementType(this, type));
    }
  }

  public List<String> getTypes() {
    List<String> typeStr = new ArrayList<String>();

    for(InstrumentMeasurementType instrumentMeasurementType : getInstrumentMeasurementTypes()) {
      typeStr.add(instrumentMeasurementType.getType());
    }

    return typeStr;
  }
}