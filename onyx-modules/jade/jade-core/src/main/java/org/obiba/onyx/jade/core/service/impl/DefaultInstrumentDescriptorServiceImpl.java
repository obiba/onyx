package org.obiba.onyx.jade.core.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.obiba.onyx.jade.core.domain.instrument.InstrumentDescriptor;
import org.obiba.onyx.jade.core.service.InstrumentDescriptorService;

public class DefaultInstrumentDescriptorServiceImpl implements InstrumentDescriptorService {

  private Map<String, InstrumentDescriptor> descriptorFile;;

  public DefaultInstrumentDescriptorServiceImpl() {
    descriptorFile = new HashMap<String, InstrumentDescriptor>();
  }

  public String getCodeBase(String instrumentBarCode) {
    return descriptorFile.get(instrumentBarCode).getCodeBase();
  }

  public void addCodeBase(String instrumentBarCode, String codeBase) {
    InstrumentDescriptor descriptor = new InstrumentDescriptor();
    descriptor.setCodeBase(codeBase);
    descriptorFile.put(instrumentBarCode, descriptor);
  }

}
