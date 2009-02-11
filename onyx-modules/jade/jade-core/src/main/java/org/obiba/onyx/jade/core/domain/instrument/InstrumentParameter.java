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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.obiba.onyx.jade.core.domain.instrument.validation.IntegrityCheck;
import org.obiba.onyx.util.data.DataType;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.support.DefaultMessageSourceResolvable;

public abstract class InstrumentParameter implements Serializable {

  private String code;

  private String vendorName;

  private String measurementUnit;

  private DataType dataType;

  private InstrumentParameterCaptureMethod captureMethod;

  private String displayFormat;

  private List<IntegrityCheck> integrityChecks;

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

  public List<IntegrityCheck> getIntegrityChecks() {
    return integrityChecks != null ? integrityChecks : (integrityChecks = new ArrayList<IntegrityCheck>());
  }

  public void addIntegrityChecks(IntegrityCheck integrityCheck) {
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
