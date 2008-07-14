package org.obiba.onyx.jade.core.service;

import java.util.List;

import org.obiba.onyx.jade.core.domain.instrument.Instrument;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;

public interface InstrumentService {
  
  /**
   * Create an instrument type.
   * @param name
   * @param description
   * @return
   */
  public InstrumentType createInstrumentType(String name, String description);
  
  /**
   * Get the instrument type by its name.
   * @param name
   * @return null if not found
   */
  public InstrumentType getInstrumentType(String name);
  
  /**
   * Get the instruments for the given instrument type name.
   * @param typeName
   * @return 
   */
  public List<Instrument> getInstruments(String typeName);
  
  /**
   * Get the instruments for given instrument type.
   * @param instrumentType
   * @return
   */
  public List<Instrument> getInstruments(InstrumentType instrumentType);
  
  /**
   * Get the active instruments for given instrument type.
   * @param instrumentType
   * @return
   */
  public List<Instrument> getActiveInstruments(InstrumentType instrumentType);
  
  /**
   * Add an instrument to an instrument type.
   * @param instrumentType
   * @param instrument
   */
  public void addInstrument(InstrumentType instrumentType, Instrument instrument);
  
}
