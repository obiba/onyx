package org.obiba.onyx.jade.core.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.obiba.onyx.jade.core.service.InstrumentDescriptorService;

public class DefaultInstrumentDescriptorServiceImpl implements InstrumentDescriptorService {
  
  private Map<String,String> codeBaseFile = new HashMap<String,String>();

  public String getCodeBase( String instrumentBarCode ) {
    return codeBaseFile.get( instrumentBarCode );
  }
  
  public void addCodeBase( String instrumentBarCode, String codeBase  ) {
    codeBaseFile.put( instrumentBarCode, codeBase );
  }
  
}
