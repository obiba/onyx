package org.obiba.onyx.jade.core.domain.instrument;

import java.util.Calendar;
import java.util.Date;

import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataType;

public class DateParameterValueConverter extends InstrumentParameterValueConverter{
  
  public void convert(InstrumentRunValue targetInstrumentRunValue, InstrumentRunValue sourceInstrumentRunValue){
    
    InstrumentParameter sourceInsrumentParameter = sourceInstrumentRunValue.getInstrumentParameter();
    
    if (!sourceInsrumentParameter.getDataType().equals(DataType.DATE) && !targetInstrumentRunValue.getDataType().equals(DataType.INTEGER))
      return;
    
    Calendar todayCal = Calendar.getInstance();
    Calendar birthCal = Calendar.getInstance();
    birthCal.setTime((Date) sourceInstrumentRunValue.getValue());
    targetInstrumentRunValue.setData(new Data (targetInstrumentRunValue.getDataType(), todayCal.getTimeInMillis() - birthCal.getTimeInMillis()));
  }

}
