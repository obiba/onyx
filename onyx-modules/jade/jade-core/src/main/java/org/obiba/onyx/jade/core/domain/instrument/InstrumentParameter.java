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
import org.obiba.onyx.jade.core.domain.instrument.validation.AbstractIntegrityCheck;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;
import org.obiba.onyx.util.data.DataType;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.support.DefaultMessageSourceResolvable;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "instrument_parameter_type", discriminatorType = DiscriminatorType.STRING, length = 100)
@DiscriminatorValue("InstrumentParameter")
public abstract class InstrumentParameter extends AbstractEntity {

  @Column(length = 200)
  @Index(name = "code_idx")
  private String code;

  @Column(length = 200)
  @Index(name = "vendor_name_idx")
  private String vendorName;

  @Column(length = 20)
  private String measurementUnit;

  @Enumerated(EnumType.STRING)
  private DataType dataType;

  @Enumerated(EnumType.STRING)
  private InstrumentParameterCaptureMethod captureMethod;

  private String displayFormat;

  // insertable and updatable are set to false so that the bi-directional relation
  // is managed by the parent instead of this class
  @ManyToOne
  @JoinColumn(name = "instrument_type_id", insertable = false, updatable = false)
  private InstrumentType instrumentType;

  @OneToMany(mappedBy = "instrumentParameter")
  private List<InstrumentRunValue> instrumentRunValues;

  @OneToMany(mappedBy = "targetParameter", cascade = CascadeType.ALL)
  private List<AbstractIntegrityCheck> integrityChecks;

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getVendorName() {
    return vendorName;
  }

  public void setVendorName(String vendorName) {
    this.vendorName = vendorName;
  }

  public MessageSourceResolvable getLabel() {
    return new DefaultMessageSourceResolvable(new String[] { code }, code);
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

  public InstrumentType getInstrumentType() {
    return instrumentType;
  }

  public void setInstrumentType(InstrumentType instrumentType) {
    this.instrumentType = instrumentType;
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

  public List<AbstractIntegrityCheck> getIntegrityChecks() {
    return integrityChecks != null ? integrityChecks : (integrityChecks = new ArrayList<AbstractIntegrityCheck>());
  }

  public void addIntegrityChecks(AbstractIntegrityCheck integrityCheck) {
    if(integrityCheck != null) {
      getIntegrityChecks().add(integrityCheck);
      integrityCheck.setTargetParameter(this);
    }
  }

  public String getDisplayFormat() {
    return displayFormat;
  }

  public void setDisplayFormat(String displayFormat) {
    this.displayFormat = displayFormat;
  }
}
