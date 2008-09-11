package org.obiba.onyx.jade.core.domain.instrument;

import javax.measure.unit.Unit;

import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;
import org.obiba.onyx.util.data.DataType;

/**
 * Unit converter
 * @author acarey
 */

public final class UnitParameterValueConverter implements InstrumentParameterValueConverter {

  /**
   * Convert the value from a source unit to a target unit
   * Note: if the value is an age, the method adjusts the value to return the right age
   * @param targetInstrumentRunValue
   * @param sourceInstrumentRunValue
   */
  @SuppressWarnings("unchecked")
  public void convert(InstrumentRunValue targetInstrumentRunValue, InstrumentRunValue sourceInstrumentRunValue) {

    Unit sourceUnit = Unit.valueOf(sourceInstrumentRunValue.getInstrumentParameter().getMeasurementUnit());
    Unit targetUnit = Unit.valueOf(targetInstrumentRunValue.getInstrumentParameter().getMeasurementUnit());

    Double newValue = sourceUnit.getConverterTo(targetUnit).convert(Double.valueOf(sourceInstrumentRunValue.getValue().toString()));

    switch(targetInstrumentRunValue.getInstrumentParameter().getDataType()) {
    case DECIMAL:
      targetInstrumentRunValue.setData(DataBuilder.buildDecimal(newValue));
      break;

    case INTEGER:
      if(targetUnit.toString().equalsIgnoreCase("year")) newValue = Math.floor(newValue);
      targetInstrumentRunValue.setData(DataBuilder.buildInteger(Math.round(newValue)));
      break;
    }

  }

}
