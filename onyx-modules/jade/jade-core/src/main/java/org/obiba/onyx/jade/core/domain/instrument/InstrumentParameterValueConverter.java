package org.obiba.onyx.jade.core.domain.instrument;

import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;

public abstract class InstrumentParameterValueConverter {

  public abstract void convert(InstrumentRunValue targetInstrumentRunValue, InstrumentRunValue sourceInstrumentRunValue);
  
}
