/*******************************************************************************
 * Copyright 2012(c) OBiBa. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.core.domain.instrument.validation;

import java.util.List;

import org.obiba.onyx.jade.core.domain.instrument.InstrumentOutputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameter;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;
import org.obiba.onyx.jade.core.service.ActiveInstrumentRunService;
import org.obiba.onyx.jade.core.service.InstrumentRunService;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataType;

/**
 * Checks repeated parameter such that the new value does not deviate more than a fixed threshold from the average of the current values.
 */
public class DeviationCheck extends AbstractIntegrityCheck implements IntegrityCheck {

  private static final long serialVersionUID = 1L;

  private Double maximum;

  public DeviationCheck() {
    super();
  }

  public void setValue(Double maximum) {
    this.maximum = maximum;
  }

  @Override
  public boolean checkParameterValue(InstrumentParameter checkedParameter, Data paramData, InstrumentRunService runService, ActiveInstrumentRunService activeRunService) {
    // We expect this to be a repeated measurement.
    if(checkedParameter instanceof InstrumentOutputParameter) {
      InstrumentOutputParameter outputParameter = (InstrumentOutputParameter) checkedParameter;
      if(outputParameter.getRepeatable() != null && outputParameter.getRepeatable()) {
        return checkStdDev(outputParameter, paramData, runService, activeRunService);
      }
    }
    return true;
  }

  protected Object[] getDescriptionArgs(InstrumentParameter checkedParameter, ActiveInstrumentRunService activeRunService) {
    String unit = checkedParameter.getMeasurementUnit() != null ? checkedParameter.getMeasurementUnit() : "";
    return new Object[] { checkedParameter.getLabel(), maximum, unit };
  }

  @Override
  public String toString() {
    return new StringBuffer(super.toString()).append("[STDDEV[<").append(maximum).append("]]").toString();
  }

  private boolean checkStdDev(InstrumentOutputParameter param, Data paramData, InstrumentRunService runService, ActiveInstrumentRunService activeRunService) {
    List<InstrumentRunValue> values = activeRunService.getInstrumentRunValues(param.getCode());
    // The first value is always valid
    if(values.size() == 0) return true;

    // Compute the average of the current values
    double avg = average(param.getDataType(), values);

    Number number = paramData.getValue();
    if(number == null) {
      return true;
    }

    // Make sure the new value is within "maximum" of the average
    double diff = Math.abs(number.doubleValue() - avg);
    return Double.compare(maximum, diff) >= 0;
  }

  private double average(DataType type, List<InstrumentRunValue> values) {
    double sum = 0;
    for(InstrumentRunValue value : values) {
      Number number = value.getValue(type);
      if(number != null) {
        sum += number.doubleValue();
      }
    }
    return sum / values.size();
  }
}
