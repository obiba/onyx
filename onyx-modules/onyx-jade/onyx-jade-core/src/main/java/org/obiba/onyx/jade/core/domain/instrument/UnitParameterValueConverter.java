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

import javax.measure.unit.Unit;

import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;
import org.obiba.onyx.jade.core.service.ActiveInstrumentRunService;
import org.obiba.onyx.jade.core.service.InstrumentService;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unit converter
 * @author acarey
 */

public class UnitParameterValueConverter implements InstrumentParameterValueConverter {

  private static final Logger log = LoggerFactory.getLogger(UnitParameterValueConverter.class);

  /**
   * Convert the value from a source unit to a target unit Note: if the value is an age, the method adjusts the value to
   * return the right age
   * @param activeInstrumentRunService
   * @param targetInstrumentRunValue
   * @param sourceInstrumentRunValue
   */
  @SuppressWarnings("unchecked")
  public void convert(ActiveInstrumentRunService activeInstrumentRunService, InstrumentRunValue targetInstrumentRunValue, InstrumentRunValue sourceInstrumentRunValue) {

    InstrumentParameter sourceParameter = activeInstrumentRunService.getInstrumentType().getInstrumentParameter(sourceInstrumentRunValue.getInstrumentParameter());
    InstrumentParameter targetParameter = activeInstrumentRunService.getInstrumentType().getInstrumentParameter(targetInstrumentRunValue.getInstrumentParameter());

    log.debug("Converting parameters from source {} to target {}", sourceParameter, targetParameter);

    Unit sourceUnit = Unit.valueOf(sourceParameter.getMeasurementUnit());
    Unit targetUnit = Unit.valueOf(targetParameter.getMeasurementUnit());

    log.debug("Converting units from source {} to target {}", sourceUnit.toString(), targetUnit.toString());

    double sourceValue;
    // Extract the source value and convert it to a double
    try {
      sourceValue = Double.parseDouble(sourceInstrumentRunValue.getData(sourceParameter.getDataType()).getValueAsString());
    } catch(NumberFormatException e) {
      Data sourceData = sourceInstrumentRunValue.getData(sourceParameter.getDataType());
      log.error("Error converting between measurement units. Original value {} of type {} cannot be converted to a double, which is required to convert between measurement units.", sourceData.getValueAsString(), sourceData.getType());
      throw e;
    }

    double newValue = sourceUnit.getConverterTo(targetUnit).convert(sourceValue);

    switch(activeInstrumentRunService.getInstrumentType().getInstrumentParameter(targetInstrumentRunValue.getInstrumentParameter()).getDataType()) {
    case DECIMAL:
      targetInstrumentRunValue.setData(DataBuilder.buildDecimal(newValue));
      break;

    case INTEGER:
      if(targetUnit.toString().equalsIgnoreCase("year")) newValue = Math.floor(newValue);
      targetInstrumentRunValue.setData(DataBuilder.buildInteger(Math.round(newValue)));
      break;
    }

  }

  public void convert(InstrumentService instrumentService, InstrumentType instrumentType, InstrumentRunValue targetInstrumentRunValue, InstrumentRunValue sourceInstrumentRunValue) {

    InstrumentParameter sourceParameter = instrumentType.getInstrumentParameter(sourceInstrumentRunValue.getInstrumentParameter());
    InstrumentParameter targetParameter = instrumentType.getInstrumentParameter(targetInstrumentRunValue.getInstrumentParameter());

    log.debug("Converting parameters from source {} to target {}", sourceParameter, targetParameter);

    Unit sourceUnit = Unit.valueOf(sourceParameter.getMeasurementUnit());
    Unit targetUnit = Unit.valueOf(targetParameter.getMeasurementUnit());

    log.debug("Converting units from source {} to target {}", sourceUnit.toString(), targetUnit.toString());

    double sourceValue;
    // Extract the source value and convert it to a double
    try {
      sourceValue = Double.parseDouble(sourceInstrumentRunValue.getData(sourceParameter.getDataType()).getValueAsString());
    } catch(NumberFormatException e) {
      Data sourceData = sourceInstrumentRunValue.getData(sourceParameter.getDataType());
      log.error("Error converting between measurement units. Original value {} of type {} cannot be converted to a double, which is required to convert between measurement units.", sourceData.getValueAsString(), sourceData.getType());
      throw e;
    }

    double newValue = sourceUnit.getConverterTo(targetUnit).convert(sourceValue);

    switch(targetParameter.getDataType()) {
    case DECIMAL:
      targetInstrumentRunValue.setData(DataBuilder.buildDecimal(newValue));
      break;

    case INTEGER:
      if(targetUnit.toString().equalsIgnoreCase("year")) newValue = Math.floor(newValue);
      targetInstrumentRunValue.setData(DataBuilder.buildInteger(Math.round(newValue)));
      break;
    }

  }

  /**
   * Convert the value from a source DataType to a target DataType (DECIMAL vs INTEGER)
   * @param targetInstrumentRunValue
   * @param sourceData
   */
  @SuppressWarnings("unchecked")
  public void convert(InstrumentParameter targetInstrumentParameter, InstrumentRunValue targetInstrumentRunValue, Data sourceData) {

    log.debug("Converting parameters from source {} to target {}", sourceData.getType(), targetInstrumentParameter.getDataType());

    double newValue = Double.parseDouble(sourceData.getValueAsString());

    switch(targetInstrumentParameter.getDataType()) {
    case DECIMAL:
      targetInstrumentRunValue.setData(DataBuilder.buildDecimal(newValue));
      break;

    case INTEGER:
      targetInstrumentRunValue.setData(DataBuilder.buildInteger(Math.round(newValue)));
      break;
    }

  }
}
