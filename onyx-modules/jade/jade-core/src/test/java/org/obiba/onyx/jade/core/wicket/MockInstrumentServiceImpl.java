package org.obiba.onyx.jade.core.wicket;

import java.util.List;

import org.obiba.onyx.jade.core.domain.instrument.Instrument;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.onyx.jade.core.service.InstrumentService;

public class MockInstrumentServiceImpl implements InstrumentService {

  public void addInstrument(InstrumentType instrumentType, Instrument instrument) {
    // TODO Auto-generated method stub

  }

  public void addInstrumentTypeDependency(InstrumentType type, InstrumentType dependency) {
    // TODO Auto-generated method stub

  }

  public InstrumentType createInstrumentType(String name, String description) {
    // TODO Auto-generated method stub
    return null;
  }

  public List<Instrument> getActiveInstruments(InstrumentType instrumentType) {
    // TODO Auto-generated method stub
    return null;
  }

  public InstrumentType getInstrumentType(String name) {
    // TODO Auto-generated method stub
    return null;
  }

  public List<Instrument> getInstruments(String typeName) {
    // TODO Auto-generated method stub
    return null;
  }

  public List<Instrument> getInstruments(InstrumentType instrumentType) {
    // TODO Auto-generated method stub
    return null;
  }

  public boolean isInteractiveInstrument(Instrument instrument) {
    return true;
  }

}
