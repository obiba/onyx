package org.obiba.onyx.jade.core.domain.instrument;

import javax.measure.unit.Unit;

import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataType;

public final class UnitParameterValueConverter extends InstrumentParameterValueConverter {

  public void convert(InstrumentRunValue targetInstrumentRunValue, InstrumentRunValue sourceInstrumentRunValue) {

    Unit sourceUnit = Unit.valueOf(sourceInstrumentRunValue.getInstrumentParameter().getMeasurementUnit());
    Unit targetUnit = Unit.valueOf(targetInstrumentRunValue.getInstrumentParameter().getMeasurementUnit());

    Double newValue = sourceUnit.getConverterTo(targetUnit).convert(Double.valueOf(sourceInstrumentRunValue.getValue().toString()));

    switch(targetInstrumentRunValue.getInstrumentParameter().getDataType()) {
    case DECIMAL:
      targetInstrumentRunValue.setData(new Data(DataType.DECIMAL, newValue));
      break;

    case INTEGER:
      if(targetUnit.toString().equalsIgnoreCase("year")) newValue = Math.floor(newValue);
      targetInstrumentRunValue.setData(new Data(DataType.INTEGER, Math.round(newValue)));
      break;
    }

  }

}
