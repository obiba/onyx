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

import java.util.Calendar;
import java.util.Date;

import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;
import org.obiba.onyx.util.data.DataBuilder;
import org.obiba.onyx.util.data.DataType;

/**
 * Data converter from date (DataType.DATE) to duration (DataType.INTEGER)
 * @author acarey
 */

public class DateParameterValueConverter implements InstrumentParameterValueConverter {

  /**
   * Convert a date to a duration in milliseconds
   * @param targetInstrumentRunValue
   * @param sourceInstrumentRunValue
   */
  public void convert(InstrumentRunValue targetInstrumentRunValue, InstrumentRunValue sourceInstrumentRunValue) {

    InstrumentParameter sourceInsrumentParameter = sourceInstrumentRunValue.getInstrumentParameter();

    if(!sourceInsrumentParameter.getDataType().equals(DataType.DATE) && !targetInstrumentRunValue.getDataType().equals(DataType.INTEGER)) return;

    Calendar todayCal = Calendar.getInstance();
    Calendar birthCal = Calendar.getInstance();
    birthCal.setTime((Date) sourceInstrumentRunValue.getValue());
    targetInstrumentRunValue.setData(DataBuilder.buildInteger(todayCal.getTimeInMillis() - birthCal.getTimeInMillis()));
  }

}
