package org.obiba.onyx.jade.core.service;

import java.util.List;

import org.obiba.onyx.jade.core.domain.instrument.Instrument;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;

public interface InstrumentService {
  
  public InstrumentType createInstrumentType(String name, String description);
  
  public List<Instrument> getInstruments(InstrumentType instrumentType);
  
  public void addInstrument(InstrumentType instrumentType, Instrument instrument);
  
}
