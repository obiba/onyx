package org.obiba.onyx.jade.core.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.obiba.core.service.impl.PersistenceManagerAwareService;
import org.obiba.onyx.jade.core.domain.instrument.Instrument;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentInputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentOutputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameterCaptureMethod;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentStatus;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.onyx.jade.core.service.InstrumentService;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class DefaultInstrumentServiceImpl extends PersistenceManagerAwareService implements InstrumentService {

  public InstrumentType createInstrumentType(String name, String description) {
    InstrumentType type = new InstrumentType(name, description);
    return getPersistenceManager().save(type);
  }

  public InstrumentType getInstrumentType(String name) {
    InstrumentType template = new InstrumentType(name, null);
    return getPersistenceManager().matchOne(template);
  }

  public void addInstrumentTypeDependency(InstrumentType type, InstrumentType dependency) {
    type.addDependentType(dependency);
    getPersistenceManager().save(type);
  }

  public List<Instrument> getInstruments(String typeName) {
    return getInstruments(getInstrumentType(typeName));
  }

  public List<Instrument> getInstruments(InstrumentType instrumentType) {
    Instrument template = new Instrument();
    template.setInstrumentType(instrumentType);

    return getPersistenceManager().match(template);
  }

  public List<Instrument> getActiveInstruments(InstrumentType instrumentType) {
    Instrument template = new Instrument();
    template.setInstrumentType(instrumentType);
    template.setStatus(InstrumentStatus.ACTIVE);

    return getPersistenceManager().match(template);
  }

  public void addInstrument(InstrumentType instrumentType, Instrument instrument) {
    if(instrumentType != null && instrument != null) {
      instrumentType.addInstrument(instrument);
      getPersistenceManager().save(instrumentType);
    }
  }

  public boolean isInteractiveInstrument(Instrument instrument) {
    if(instrument == null) return false;

    InstrumentOutputParameter template = new InstrumentOutputParameter();
    template.setInstrument(instrument);
    template.setCaptureMethod(InstrumentParameterCaptureMethod.AUTOMATIC);

    return getPersistenceManager().count(template) > 0;
  }

  public int countInstrumentInputParameter(Instrument instrument, boolean readOnlySource) {
    return getInstrumentInputParameter(instrument, readOnlySource).size();
  }

  public List<InstrumentInputParameter> getInstrumentInputParameter(Instrument instrument, boolean readOnlySource) {
    List<InstrumentInputParameter> list = new ArrayList<InstrumentInputParameter>();
    InstrumentInputParameter template = new InstrumentInputParameter();
    template.setInstrument(instrument);
    
    for (InstrumentInputParameter param : getPersistenceManager().match(template)) {
      if (param.getInputSource() != null && param.getInputSource().isReadOnly() == readOnlySource) {
        list.add(param);
      }
    }
    
    return list;
  }

}
