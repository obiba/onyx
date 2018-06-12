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

import org.obiba.onyx.core.data.IDataSource;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.jade.core.domain.instrument.validation.IntegrityCheck;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataType;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.support.DefaultMessageSourceResolvable;

public abstract class InstrumentParameter implements Serializable {

  private static final long serialVersionUID = 1L;

  private String code;

  private String vendorName;

  private String measurementUnit;

  private DataType dataType;

  private InstrumentParameterCaptureMethod captureMethod;

  private boolean manualCaptureAllowed;

  private String displayFormat;

  /** A IDataSource instance expected to return a DataType.BOOLEAN which dictates whether this parameter is required. */
  private IDataSource condition;

  private List<IntegrityCheck> integrityChecks;

  /** A IDataSource instance that provides the value for this parameter for a given Participant */
  private IDataSource dataSource;

  private List<Data> allowedValues;

  private String mimeType;

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
    }
  }

  public String getDisplayFormat() {
    return displayFormat;
  }

  public void setDisplayFormat(String displayFormat) {
    this.displayFormat = displayFormat;
  }

  public void setDataSource(IDataSource dataSource) {
    this.dataSource = dataSource;
  }

  public IDataSource getDataSource() {
    return dataSource;
  }

  public IDataSource getCondition() {
    return condition;
  }

  public boolean isRequired(Participant participant) {
    if(condition == null) {
      return true;
    }
    Boolean required = (Boolean) condition.getData(participant).getValue();
    if(required != null) {
      return required;
    }
    return true;
  }

  /**
   * Returns a list of the values allowed for this parameter.
   * 
   * @return list of values allowed for this parameter (empty list if none)
   */
  public List<Data> getAllowedValues() {
    return allowedValues != null ? allowedValues : (allowedValues = new ArrayList<Data>());
  }

  public void addAllowedValue(Data data) {
    if(data != null) {
      getAllowedValues().add(data);
    }
  }

  public void addAllowedValues(Data... dataList) {
    if(dataList != null) {
      for(Data data : dataList) {
        addAllowedValue(data);
      }
    }
  }

  public boolean isManualCaptureAllowed() {
    return manualCaptureAllowed;
  }

  public void setManualCaptureAllowed(boolean manualCaptureAllowed) {
    this.manualCaptureAllowed = manualCaptureAllowed;
  }

  public String getMimeType() {
    return mimeType;
  }

  public void setMimeType(String mimeType) {
    this.mimeType = mimeType;
  }

  @Override
  public String toString() {
    return vendorName;
  }

}