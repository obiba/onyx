package org.obiba.onyx.jade.core.service;

import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;

public interface InstrumentParameterValueConverter {

  public void convert(InstrumentRunValue targetInstrumentRunValue, InstrumentRunValue sourceInstrumentRunValue);
  
}
