package org.obiba.onyx.jade.core.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.obiba.onyx.jade.core.domain.instrument.InstrumentDescriptor;
import org.obiba.onyx.jade.core.service.InstrumentDescriptorService;

public class DefaultInstrumentDescriptorServiceImpl implements InstrumentDescriptorService {

  private Map<String, InstrumentDescriptor> descriptorMap;;

  public DefaultInstrumentDescriptorServiceImpl() {
    descriptorMap = new HashMap<String, InstrumentDescriptor>();
  }

  public String getCodeBase(String instrumentBarCode) {
    InstrumentDescriptor descriptor = descriptorMap.get(instrumentBarCode);
    if(descriptor == null) return null;
    else
      return descriptor.getCodeBase();
  }

  public void setCodeBase(String instrumentBarCode, String codeBase) {
    InstrumentDescriptor descriptor = descriptorMap.get(instrumentBarCode);
    if (descriptor == null) {
      descriptor = new InstrumentDescriptor(instrumentBarCode);
      descriptorMap.put(instrumentBarCode, descriptor);  
    }
    descriptor.setCodeBase(codeBase);
  }

}
